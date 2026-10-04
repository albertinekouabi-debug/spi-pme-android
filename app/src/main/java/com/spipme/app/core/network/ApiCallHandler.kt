package com.spipme.app.core.network

import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.dto.auth.ErreurApiDto
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

/**
 * Exécute un appel Retrofit et normalise le résultat :
 *  - succès HTTP + corps non nul -> Resultat.Succes
 *  - succès HTTP + corps nul (ex. 204/205) -> erreur explicite (ne doit
 *    jamais arriver côté appelant sans body attendu, signalé plutôt que
 *    silencieusement transformé en `null` propagé plus loin)
 *  - échec HTTP -> parse le corps d'erreur avec le format homogène du
 *    backend (apps.core.exceptions), ou message générique si non parsable
 *  - pas de réseau / timeout -> message explicite, pas une exception qui
 *    remonte jusqu'à l'UI
 */
suspend fun <T> executerAppelApi(json: Json, appel: suspend () -> Response<T>): Resultat<T> {
    return try {
        val reponse = appel()
        if (reponse.isSuccessful) {
            val corps = reponse.body()
            if (corps != null) {
                Resultat.Succes(corps)
            } else {
                Resultat.Echec("Réponse vide inattendue du serveur.")
            }
        } else {
            val texteErreur = reponse.errorBody()?.string()
            val erreur = texteErreur?.let {
                try {
                    json.decodeFromString(ErreurApiDto.serializer(), it)
                } catch (e: Exception) {
                    null
                }
            }
            Resultat.Echec(
                message = erreur?.message ?: "Une erreur est survenue (code ${reponse.code()}).",
                champsInvalides = erreur?.champsInvalides,
            )
        }
    } catch (e: IOException) {
        Resultat.Echec("Impossible de contacter le serveur. Vérifiez votre connexion.", reseau = true)
    } catch (e: Exception) {
        Resultat.Echec("Une erreur inattendue est survenue : ${e.message ?: e::class.simpleName}")
    }
}
