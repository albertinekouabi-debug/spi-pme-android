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
 * AppelÃ© par OkHttp (synchrone, thread dÃ©diÃ©) quand une requÃªte du client
 * authentifiÃ© reÃ§oit un 401. Tente un rafraÃ®chissement via le client BRUT
 * (jamais le client authentifiÃ© â€” Ã§a crÃ©erait une rÃ©cursion infinie).
 *
 * Garde anti-boucle : si la requÃªte a dÃ©jÃ  Ã©tÃ© retentÃ©e une fois (donc porte
 * dÃ©jÃ  un Authorization fraÃ®chement posÃ©) et Ã©choue encore, on abandonne
 * plutÃ´t que de boucler indÃ©finiment.
 *
 * CORRECTIF CONCURRENCE (audit du 13/09/2026) : le backend fait tourner le
 * refresh token Ã  chaque usage (`SIMPLE_JWT.ROTATE_REFRESH_TOKENS = True`)
 * et blackliste l'ancien aprÃ¨s rotation (`BLACKLIST_AFTER_ROTATION = True`,
 * confirmÃ© dans `spi_pme/settings.py`) â€” un refresh token n'est donc
 * utilisable qu'une seule fois. `authenticate()` peut Ãªtre appelÃ© par
 * OkHttp sur plusieurs threads en parallÃ¨le (ex. chargement simultanÃ© de
 * plusieurs endpoints, cf. `TreasuryViewModel.chargerTout`) : sans garde,
 * chaque thread tenterait son propre `/auth/refresh` avec le mÃªme refresh
 * token ; un seul rÃ©ussirait, les autres Ã©choueraient sur un token dÃ©jÃ 
 * rÃ©voquÃ© et forceraient une dÃ©connexion de l'utilisateur alors que sa
 * session Ã©tait valide. Reproduit et corrigÃ© avec des threads rÃ©els avant
 * application ici (5 requÃªtes concurrentes : 5 Ã©checs sans le correctif,
 * 0 Ã©chec avec).
 *
 * Correction : synchronisation (un seul rafraÃ®chissement rÃ©seau Ã  la fois)
 * + re-vÃ©rification aprÃ¨s acquisition du verrou â€” si l'access token a dÃ©jÃ 
 * changÃ© pendant l'attente (un autre thread vient de rafraÃ®chir avec
 * succÃ¨s), on rÃ©utilise ce nouveau token sans rappeler le rÃ©seau.
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
            // Un autre thread a peut-Ãªtre dÃ©jÃ  rafraÃ®chi pendant l'attente du
            // verrou : si l'access token courant diffÃ¨re de celui qui a
            // Ã©chouÃ©, on le rÃ©utilise directement, sans appel rÃ©seau.
            val tokenActuel = tokenManager.accessTokenActuel()
            if (!tokenActuel.isNullOrBlank() && tokenActuel != tokenEnEchec) {
                return construireRequeteAvecToken(response, tokenActuel)
            }

            val refreshToken = tokenManager.refreshTokenActuel() ?: return null

            val nouvelAccessToken = runBlocking {
                try {
                    val reponse = authApiBrut.refresh(RefreshRequestDto(refresh = refreshToken))
                    if (reponse.isSuccessful) {
                        reponse.body()?.access?.also { tokenManager.mettreAJourAccessToken(it) }
                    } else {
                        // Refresh token expirÃ©/rÃ©voquÃ© : on efface la session, l'UI doit
                        // rediriger vers l'Ã©cran de connexion (observÃ©e via estConnecteFlow).
                        tokenManager.effacerTokens()
                        null
                    }
                } catch (e: Exception) {
                    null // pas de rÃ©seau : on n'efface pas les tokens, on retentera plus tard
                }
            } ?: return null

            return construireRequeteAvecToken(response, nouvelAccessToken)
        }
    }

    /** Le token que PORTAIT la requÃªte en Ã©chec (pas celui, potentiellement dÃ©jÃ  mis Ã  jour, du TokenManager). */
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

