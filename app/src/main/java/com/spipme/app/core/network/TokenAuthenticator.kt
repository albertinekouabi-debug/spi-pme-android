package com.spipme.app.core.network

import com.spipme.app.core.security.TokenManager
import com.spipme.app.data.remote.api.AuthApi
import com.spipme.app.data.remote.dto.auth.RefreshRequestDto
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * Appelé par OkHttp (synchrone, thread dédié) quand une requête du client
 * authentifié reçoit un 401. Tente un rafraîchissement via le client BRUT
 * (jamais le client authentifié — ça créerait une récursion infinie).
 *
 * Garde anti-boucle : si la requête a déjà été retentée une fois (donc porte
 * déjà un Authorization fraîchement posé) et échoue encore, on abandonne
 * plutôt que de boucler indéfiniment.
 *
 * CORRECTIF CONCURRENCE (audit du 13/09/2026) : le backend fait tourner le
 * refresh token à chaque usage (`SIMPLE_JWT.ROTATE_REFRESH_TOKENS = True`)
 * et blackliste l'ancien après rotation (`BLACKLIST_AFTER_ROTATION = True`,
 * confirmé dans `spi_pme/settings.py`) — un refresh token n'est donc
 * utilisable qu'une seule fois. `authenticate()` peut être appelé par
 * OkHttp sur plusieurs threads en parallèle (ex. chargement simultané de
 * plusieurs endpoints, cf. `TreasuryViewModel.chargerTout`) : sans garde,
 * chaque thread tenterait son propre `/auth/refresh` avec le même refresh
 * token ; un seul réussirait, les autres échoueraient sur un token déjà
 * révoqué et forceraient une déconnexion de l'utilisateur alors que sa
 * session était valide. Reproduit et corrigé avec des threads réels avant
 * application ici (5 requêtes concurrentes : 5 échecs sans le correctif,
 * 0 échec avec).
 *
 * Correction : synchronisation (un seul rafraîchissement réseau à la fois)
 * + re-vérification après acquisition du verrou — si l'access token a déjà
 * changé pendant l'attente (un autre thread vient de rafraîchir avec
 * succès), on réutilise ce nouveau token sans rappeler le réseau.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    @ClientBrut private val authApiBrut: AuthApi,
) : Authenticator {

    private val verrouRafraichissement = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (dejaRetente(response)) return null

        val tokenEnEchec = jetonDeLaRequeteEnEchec(response)

        synchronized(verrouRafraichissement) {
            // Un autre thread a peut-être déjà rafraîchi pendant l'attente du
            // verrou : si l'access token courant diffère de celui qui a
            // échoué, on le réutilise directement, sans appel réseau.
            val tokenActuel = tokenManager.accessTokenActuel()
            if (!tokenActuel.isNullOrBlank() && tokenActuel != tokenEnEchec) {
                return construireRequeteAvecToken(response, tokenActuel)
            }

            val refreshToken = tokenManager.refreshTokenActuel() ?: return null

            val nouvelAccessToken = runBlocking {
                try {
                    val reponse = authApiBrut.refresh(RefreshRequestDto(refresh = refreshToken))
                    if (reponse.isSuccessful) {
                        val corps = reponse.body()
                        val nouvelAccess = corps?.access?.takeIf { it.isNotBlank() }
                        val nouveauRefresh = corps?.refresh?.takeIf { it.isNotBlank() }
                    
                        if (nouvelAccess != null && nouveauRefresh != null) {
                            // Enregistrer les deux tokens : l'ancien refresh a ete invalide.
                            tokenManager.enregistrerTokens(nouvelAccess, nouveauRefresh)
                            nouvelAccess
                        } else {
                            // Reponse incomplete : ne pas continuer avec une session incoherente.
                            tokenManager.effacerTokens()
                            null
                        }
                    } else {
                        // Un rejet du token indique une session a renouveler par reconnexion.
                        // Une erreur temporaire (429 ou 5xx) ne doit pas effacer la session.
                        if (reponse.code() == 400 || reponse.code() == 401) {
                            tokenManager.effacerTokens()
                        }
                        null
                    }
                } catch (e: Exception) {
                    null // pas de réseau : on n'efface pas les tokens, on retentera plus tard
                }
            } ?: return null

            return construireRequeteAvecToken(response, nouvelAccessToken)
        }
    }

    /** Le token que PORTAIT la requête en échec (pas celui, potentiellement déjà mis à jour, du TokenManager). */
    private fun jetonDeLaRequeteEnEchec(response: Response): String? =
        response.request.header("Authorization")?.removePrefix("Bearer ")

    private fun construireRequeteAvecToken(response: Response, token: String): Request =
        response.request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

    private fun dejaRetente(response: Response): Boolean {
        var compte = 1
        var reponseCourante: Response? = response.priorResponse
        while (reponseCourante != null) {
            compte++
            reponseCourante = reponseCourante.priorResponse
        }
        return compte >= 2
    }
}

