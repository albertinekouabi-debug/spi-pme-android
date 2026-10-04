package com.spipme.app.core.sync

import kotlinx.coroutines.CancellationException

/** Pourquoi la passe s'est arrêtée avant d'avoir vidé la file (null = file traitée jusqu'au bout). */
enum class RaisonArret { RESEAU, SESSION_EXPIREE }

data class BilanSynchronisation(
    val envoyees: Int = 0,
    val conflits: Int = 0,
    val echecsDefinitifs: Int = 0,
    val reportees: Int = 0,      // en attente d'un délai de nouvelle tentative
    val bloquees: Int = 0,       // dépendent d'une opération en échec/conflit/non résolue
    val arretPour: RaisonArret? = null,
) {
    val aCompleter: Boolean get() = arretPour != null || reportees > 0
}

/**
 * Vide la file d'opérations. Garanties :
 *  - ordre FIFO, et une opération en échec/conflit bloque les suivantes de la MÊME entité (jamais de
 *    correction rejouée avant l'opération qu'elle corrige) sans bloquer les entités indépendantes ;
 *  - la clé d'idempotence est celle de l'opération, identique à chaque tentative ;
 *  - jamais de boucle infinie : backoff exponentiel et plafond de tentatives (les coupures réseau et
 *    la session expirée n'y sont pas comptées : rester hors ligne des jours ne détruit aucune saisie) ;
 *  - reprise après arrêt brutal (`EN_COURS` orphelines) ;
 *  - une identité `@local:` est remplacée par l'id serveur dès que la création correspondante aboutit.
 */
class SyncEngine(
    private val file: FileOperations,
    private val passerelle: PasserelleDistante,
    private val ids: CorrespondancesIds,
    private val backoff: BackoffPolicy = BackoffPolicy(),
    private val maxTentatives: Int = 10,
    private val maintenantMs: () -> Long,
) {
    suspend fun synchroniser(): BilanSynchronisation {
        file.recupererOrphelines()

        var bilan = BilanSynchronisation()
        val cleBloquees = mutableSetOf<String>()

        for (operation in file.operationsAEnvoyer()) {
            val maintenant = maintenantMs()

            if (operation.cleEntite in cleBloquees) {
                bilan = bilan.copy(bloquees = bilan.bloquees + 1); continue
            }
            if (operation.prochaineTentativeLe > maintenant) {
                cleBloquees += operation.cleEntite  // l'ordre au sein de l'entité doit rester respecté
                bilan = bilan.copy(reportees = bilan.reportees + 1); continue
            }

            val resolue = resoudreReferences(operation)
            if (resolue == null) {  // dépend d'une création pas encore synchronisée
                cleBloquees += operation.cleEntite
                bilan = bilan.copy(bloquees = bilan.bloquees + 1); continue
            }

            file.mettreAJour(operation.copy(statut = StatutOperation.EN_COURS))
            val resultat = try {
                passerelle.envoyer(resolue)
            } catch (annulation: CancellationException) {
                file.mettreAJour(operation)  // rendue à la file : rien n'est perdu
                throw annulation
            } catch (inattendue: Exception) {
                ResultatEnvoi.ServeurTemporaire(0, "Erreur inattendue : ${inattendue::class.simpleName}")
            }

            when (resultat) {
                is ResultatEnvoi.Succes -> {
                    val ref = operation.refLocaleCreee
                    if (ref != null && resultat.idServeur != null) ids.enregistrer(ref, resultat.idServeur)
                    file.supprimer(operation.id)
                    bilan = bilan.copy(envoyees = bilan.envoyees + 1)
                }
                is ResultatEnvoi.Reseau -> {
                    file.mettreAJour(operation.copy(
                        statut = StatutOperation.EN_ATTENTE, derniereTentativeLe = maintenant,
                        prochaineTentativeLe = maintenant + backoff.delaiMs(1), messageErreur = resultat.message,
                    ))
                    return bilan.copy(arretPour = RaisonArret.RESEAU)  // le reste échouerait de même
                }
                is ResultatEnvoi.AuthExpiree -> {
                    file.mettreAJour(operation.copy(
                        statut = StatutOperation.EN_ATTENTE, messageErreur = "Session expirée : reconnexion nécessaire.",
                    ))
                    return bilan.copy(arretPour = RaisonArret.SESSION_EXPIREE)
                }
                is ResultatEnvoi.ServeurTemporaire -> {
                    val tentatives = operation.tentatives + 1
                    cleBloquees += operation.cleEntite
                    if (tentatives >= maxTentatives) {
                        file.mettreAJour(operation.copy(
                            statut = StatutOperation.ECHEC_DEFINITIF, tentatives = tentatives, derniereTentativeLe = maintenant,
                            messageErreur = "Abandon après $tentatives tentatives : ${resultat.message}",
                        ))
                        bilan = bilan.copy(echecsDefinitifs = bilan.echecsDefinitifs + 1)
                    } else {
                        file.mettreAJour(operation.copy(
                            statut = StatutOperation.EN_ATTENTE, tentatives = tentatives, derniereTentativeLe = maintenant,
                            prochaineTentativeLe = maintenant + backoff.delaiMs(tentatives), messageErreur = resultat.message,
                        ))
                        bilan = bilan.copy(reportees = bilan.reportees + 1)
                    }
                }
                is ResultatEnvoi.Definitive -> {
                    file.mettreAJour(operation.copy(
                        statut = StatutOperation.ECHEC_DEFINITIF, tentatives = operation.tentatives + 1,
                        derniereTentativeLe = maintenant, messageErreur = "${resultat.code} — ${resultat.message}",
                    ))
                    cleBloquees += operation.cleEntite
                    bilan = bilan.copy(echecsDefinitifs = bilan.echecsDefinitifs + 1)
                }
                is ResultatEnvoi.Conflit -> {
                    file.mettreAJour(operation.copy(
                        statut = StatutOperation.CONFLIT, derniereTentativeLe = maintenant,
                        messageErreur = "Modifié sur le serveur depuis votre dernière lecture.",
                        conflitJson = resultat.donneesServeurJson,
                    ))
                    cleBloquees += operation.cleEntite
                    bilan = bilan.copy(conflits = bilan.conflits + 1)
                }
            }
        }
        return bilan
    }

    /** Remplace les `@local:` par les ids serveur ; null si l'un d'eux n'est pas (encore) connu. */
    private suspend fun resoudreReferences(operation: OperationEnAttente): OperationEnAttente? {
        val references = (REFERENCE.findAll(operation.chemin) +
            REFERENCE.findAll(operation.payloadJson.orEmpty()) +
            REFERENCE.findAll(operation.entityId)).map { it.value }.toSet()
        if (references.isEmpty()) return operation

        val correspondances = mutableMapOf<String, String>()
        for (reference in references) {
            // la référence que CETTE opération crée n'a évidemment pas encore d'id serveur
            if (reference == operation.refLocaleCreee) continue
            correspondances[reference] = ids.resoudre(reference) ?: return null
        }
        fun remplacer(texte: String) = correspondances.entries.fold(texte) { t, (ref, id) -> t.replace(ref, id) }
        return operation.copy(
            chemin = remplacer(operation.chemin),
            payloadJson = operation.payloadJson?.let(::remplacer),
            entityId = remplacer(operation.entityId),
        )
    }

    private companion object {
        val REFERENCE = Regex(Regex.escape(PREFIXE_ID_LOCAL) + "[A-Za-z0-9-]+")
    }
}
