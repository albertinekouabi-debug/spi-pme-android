package com.spipme.app.core.sync.android

import com.spipme.app.core.sync.OperationEnAttente
import com.spipme.app.core.util.Resultat
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Écriture « d'abord en ligne, jamais perdue » :
 *  1. une clé d'idempotence est créée UNE fois ;
 *  2. l'appel direct l'utilise (retour immédiat des erreurs de validation si le réseau est là) ;
 *  3. si — et seulement si — c'est le RÉSEAU qui échoue, la même opération est enfilée avec LA MÊME clé.
 * Cas critique : la requête est arrivée au serveur mais la réponse s'est perdue (coupure) → l'appel est
 * rejoué par la file avec la même clé → le serveur renvoie la réponse d'origine, aucun doublon.
 * Un refus du serveur (400/403/409...) n'est JAMAIS enfilé : rejouer ne changerait rien.
 */
@Singleton
class EcritureHorsLigne @Inject constructor(
    private val synchronisation: SynchronisationRepository,
) {
    fun nouvelleCle(): String = UUID.randomUUID().toString()

    suspend fun <T> tenterOuEnfiler(
        cle: String,
        appel: suspend (cle: String) -> Resultat<T>,
        operation: (cle: String) -> OperationEnAttente,
    ): Resultat<T> {
        val resultat = appel(cle)
        if (resultat is Resultat.Echec && resultat.reseau) {
            synchronisation.enfiler(operation(cle))
            return Resultat.Echec(
                message = "Enregistré hors ligne : la saisie sera synchronisée dès le retour du réseau.",
                reseau = true, enFile = true,
            )
        }
        return resultat
    }
}
