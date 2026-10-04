package com.spipme.app.data.local

import com.spipme.app.core.util.Resultat
import com.spipme.app.data.local.dao.CacheDao
import com.spipme.app.data.local.entity.CacheReponseEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lecture avec repli hors ligne, partagée par tous les dépôts (un seul endroit pour la règle) :
 *  - succès → la réponse est mémorisée (un échec d'écriture du cache ne fait JAMAIS échouer la lecture) ;
 *  - échec causé par le RÉSEAU → dernière réponse mémorisée, si elle existe et se relit ;
 *  - tout autre échec (403, 400, 5xx...) est renvoyé tel quel : de vieilles données ne doivent pas masquer
 *    un refus d'accès ou une erreur serveur.
 * `cle` doit identifier l'endpoint ET ses filtres (secteur, statut, page...).
 */
@Singleton
class CacheLecture @Inject constructor(
    private val dao: CacheDao,
    private val json: Json,
) {
    suspend fun <T> lire(cle: String, serializer: KSerializer<T>, appel: suspend () -> Resultat<T>): Resultat<T> =
        when (val r = lireAvecOrigine(cle, serializer, appel)) {
            is Resultat.Succes -> Resultat.Succes(r.donnees.first)
            is Resultat.Echec -> r
        }

    /**
     * Comme [lire], mais indique l'ORIGINE : `second` vaut null pour une donnée fraîche du serveur, ou la date
     * (ms) de mémorisation pour une donnée servie par le cache. L'écran peut ainsi afficher « Données du … ».
     */
    suspend fun <T> lireAvecOrigine(
        cle: String, serializer: KSerializer<T>, appel: suspend () -> Resultat<T>,
    ): Resultat<Pair<T, Long?>> {
        return when (val resultat = appel()) {
            is Resultat.Succes -> {
                runCatching {
                    dao.enregistrer(CacheReponseEntity(cle, json.encodeToString(serializer, resultat.donnees), System.currentTimeMillis()))
                }
                Resultat.Succes(resultat.donnees to null)
            }
            is Resultat.Echec -> {
                if (!resultat.reseau) return resultat
                val entree = dao.lire(cle)
                val valeur = entree?.let { runCatching { json.decodeFromString(serializer, it.json) }.getOrNull() }
                if (entree != null && valeur != null) Resultat.Succes(valeur to entree.majLe) else resultat
            }
        }
    }
}
