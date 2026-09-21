package com.spipme.app.core.network

import com.spipme.app.core.security.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor OkHttp : synchrone par contrat (pas de suspend). Depuis le
 * passage de TokenManager à EncryptedSharedPreferences (lecture native
 * synchrone, sans coroutine), l'appel se fait directement — plus besoin de
 * `runBlocking` pour ponter une lecture suspendue, ce qui évite la charge
 * inutile de créer un scope de coroutine pour une lecture mémoire rapide.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.accessTokenActuel()
        val requete = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(requete)
    }
}
