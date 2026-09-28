package com.spipme.app.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Source unique de vÃ©ritÃ© pour les tokens JWT.
 *
 * SÃ‰CURITÃ‰ : contrairement au reste des prÃ©fÃ©rences de l'app (profil,
 * secteur actif â€” non sensibles), les tokens sont chiffrÃ©s au repos via
 * EncryptedSharedPreferences (AES-256-GCM, clÃ© maÃ®tresse dans l'Android
 * Keystore matÃ©riel quand disponible). Un token d'accÃ¨s de courte durÃ©e
 * n'est pas critique, mais le refresh token vit 14 jours (Â§8.3 backend) â€”
 * le laisser en clair sur disque serait une vraie faiblesse en cas
 * d'appareil root ou d'extraction de sauvegarde non exclue.
 *
 * Le TokenAuthenticator (rafraÃ®chissement automatique) et l'AuthInterceptor
 * (en-tÃªte Authorization) passent tous les deux par ici â€” jamais de token
 * gardÃ© en variable statique Ã©phÃ©mÃ¨re qui dÃ©synchroniserait les deux.
 */
@Singleton
class TokenManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: Context,
) {
    private object Cles {
        const val ACCESS = "access_token"
        const val REFRESH = "refresh_token"
    }

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "spi_pme_tokens_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    // EncryptedSharedPreferences n'expose pas de Flow nativement (API SharedPreferences
    // classique) â€” un MutableStateFlow interne, mis Ã  jour Ã  chaque Ã©criture par ce
    // singleton (seul point d'Ã©criture de toute l'app), sert de source rÃ©active sans
    // avoir Ã  rÃ©-dÃ©chiffrer Ã  chaque lecture d'observateur.
    private val _accessTokenFlow = MutableStateFlow<String?>(null)
    val accessTokenFlow: StateFlow<String?> = _accessTokenFlow.asStateFlow()
    val estConnecteFlow: kotlinx.coroutines.flow.Flow<Boolean> get() = accessTokenFlow.map { it != null }

    // CORRECTIF (root cause d'un blocage au dÃ©marrage observÃ© sur appareil rÃ©el) :
    // ce init{} s'exÃ©cute au moment de la CONSTRUCTION du singleton par Hilt â€”
    // c'est-Ã -dire ici, la toute premiÃ¨re fois que le graphe de dÃ©pendances de
    // AppViewModel est rÃ©solu, dans setContent{} de MainActivity, donc SUR LE
    // THREAD PRINCIPAL. Lire `prefs` ici forÃ§ait l'Ã©valuation synchrone du
    // `by lazy` (MasterKey Android Keystore + EncryptedSharedPreferences.create),
    // une opÃ©ration de chiffrement + I/O disque pouvant prendre plusieurs
    // centaines de ms (voire davantage au tout premier lancement, gÃ©nÃ©ration de
    // la clÃ©) â€” bloquant la premiÃ¨re frame Compose (dÃ©marrage lent, pression GC,
    // timeouts SurfaceSync). DÃ©portÃ© sur Dispatchers.IO : accessTokenFlow reste
    // Ã  sa valeur initiale (null) le temps de la lecture, ce qui est dÃ©jÃ  le
    // comportement attendu par AppViewModel/MainActivity (Ã©cran de chargement
    // affichÃ© tant que ecranDepart == null, cf. AppViewModel.kt).
    init {
        CoroutineScope(Dispatchers.IO).launch {
            _accessTokenFlow.value = prefs.getString(Cles.ACCESS, null)
        }
    }

    fun accessTokenActuel(): String? = prefs.getString(Cles.ACCESS, null)

    fun refreshTokenActuel(): String? = prefs.getString(Cles.REFRESH, null)

    fun enregistrerTokens(access: String, refresh: String) {
        prefs.edit().putString(Cles.ACCESS, access).putString(Cles.REFRESH, refresh).apply()
        _accessTokenFlow.value = access
    }

    fun mettreAJourAccessToken(access: String) {
        prefs.edit().putString(Cles.ACCESS, access).apply()
        _accessTokenFlow.value = access
    }

    fun effacerTokens() {
        prefs.edit().clear().apply()
        _accessTokenFlow.value = null
    }
}

