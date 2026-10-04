package com.spipme.app.core.sync

/**
 * Fabrique des opérations synchronisables autorisées hors ligne.
 *
 * Règle : une correction financière n'est jamais un PATCH local. Annulation, avoir et contre-écriture
 * sont des écritures append-only que le serveur valide (état, plafonds, période clôturée, droits) au
 * moment de la synchronisation ; leur éventuel refus remonte comme échec définitif visible.
 */
object OperationsMetier {
    fun annulerFacture(factureId: String, motif: String, maintenantMs: Long, nouvelleCle: () -> String) =
        OperationEnAttente(
            idempotencyKey = nouvelleCle(), type = "ANNULER_FACTURE", entityType = "facture", entityId = factureId,
            methode = "POST", chemin = "invoices/$factureId/annuler/",
            payloadJson = json("motif" to motif), creeLe = maintenantMs,
        )

    fun emettreAvoir(factureId: String, motif: String, montant: String?, maintenantMs: Long, nouvelleCle: () -> String) =
        OperationEnAttente(
            idempotencyKey = nouvelleCle(), type = "EMETTRE_AVOIR", entityType = "facture", entityId = factureId,
            methode = "POST", chemin = "invoices/$factureId/avoir/",
            payloadJson = json(*listOfNotNull("motif" to motif, montant?.let { "montant" to it }).toTypedArray()),
            creeLe = maintenantMs,
        )

    fun contrePasserTransaction(transactionId: String, motif: String, maintenantMs: Long, nouvelleCle: () -> String) =
        OperationEnAttente(
            idempotencyKey = nouvelleCle(), type = "CONTRE_PASSER_TRANSACTION", entityType = "transaction",
            entityId = transactionId, methode = "POST", chemin = "transactions/$transactionId/contre-passer/",
            payloadJson = json("motif" to motif), creeLe = maintenantMs,
        )

    /** Création : l'appelant fournit un JSON déjà validé localement ; `refLocale` = `@local:<uuid>`. */
    fun creer(
        type: String, entityType: String, cheminCollection: String, payloadJson: String, refLocale: String,
        maintenantMs: Long, nouvelleCle: () -> String,
    ) = OperationEnAttente(
        idempotencyKey = nouvelleCle(), type = type, entityType = entityType, entityId = refLocale,
        methode = "POST", chemin = cheminCollection, payloadJson = payloadJson, refLocaleCreee = refLocale,
        creeLe = maintenantMs,
    )

    /** Modification : toujours accompagnée de la version lue (If-Match) pour détecter un conflit. */
    fun modifier(
        type: String, entityType: String, entityId: String, chemin: String, payloadJson: String,
        versionBase: Int, maintenantMs: Long, nouvelleCle: () -> String,
    ) = OperationEnAttente(
        idempotencyKey = nouvelleCle(), type = type, entityType = entityType, entityId = entityId,
        methode = "PATCH", chemin = chemin, payloadJson = payloadJson, versionBase = versionBase, creeLe = maintenantMs,
    )

    /**
     * Objet JSON typé : String → texte échappé, Int/Long/Boolean → valeur brute, null → champ omis.
     * (L'API attend par exemple `secteur` en entier ; les décimaux restent des chaînes, comme le serveur les renvoie.)
     */
    fun jsonObjet(vararg champs: Pair<String, Any?>): String =
        champs.filter { it.second != null }.joinToString(prefix = "{", postfix = "}", separator = ",") { (cle, valeur) ->
            "\"${echapper(cle)}\":" + when (valeur) {
                is Int, is Long, is Boolean -> valeur.toString()
                else -> "\"${echapper(valeur.toString())}\""
            }
        }

    fun json(vararg champs: Pair<String, String>): String =
        champs.joinToString(prefix = "{", postfix = "}", separator = ",") { (cle, valeur) ->
            "\"${echapper(cle)}\":\"${echapper(valeur)}\""
        }

    /** Échappement JSON complet (guillemets, antislash, contrôles) : un motif saisi ne casse jamais le corps. */
    fun echapper(texte: String): String = buildString {
        for (c in texte) when {
            c == '"' -> append("\\\"")
            c == '\\' -> append("\\\\")
            c == '\n' -> append("\\n")
            c == '\r' -> append("\\r")
            c == '\t' -> append("\\t")
            c < ' ' -> append("\\u%04x".format(c.code))
            else -> append(c)
        }
    }
}
