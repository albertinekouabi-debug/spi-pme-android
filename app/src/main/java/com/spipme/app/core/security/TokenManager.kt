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
 * Source unique de vérité pour les tokens JWT.
 *
 * SÉCURITÉ : contrairement au reste des préférences de l'app (profil,
 * secteur actif — non sensibles), les tokens sont chiffrés au repos via
 * EncryptedSharedPreferences (AES-256-GCM, clé maîtresse dans l'Android
 * Keystore matériel quand disponible). Un token d'accès de courte durée
 * n'est pas critique, mais le refresh token vit 14 jours (§8.3 backend) —
 * le laisser en clair sur disque serait une vraie faiblesse en cas
 * d'appareil root ou d'extraction de sauvegarde non exclue.
 *
 * Le TokenAuthenticator (rafraîchissement automatique) et l'AuthInterceptor
 * (en-tête Authorization) passent tous les deux par ici — jamais de token
 * gardé en variable statique éphémère qui désynchroniserait les deux.
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
    // classique) — un MutableStateFlow interne, mis à jour à chaque écriture par ce
    // singleton (seul point d'écriture de toute l'app), sert de source réactive sans
    // avoir à ré-déchiffrer à chaque lecture d'observateur.
    private val _accessTokenFlow = MutableStateFlow<String?>(null)
    val accessTokenFlow: StateFlow<String?> = _accessTokenFlow.asStateFlow()
    val estConnecteFlow: kotlinx.coroutines.flow.Flow<Boolean> get() = accessTokenFlow.map { it != null }

    // CORRECTIF (root cause d'un blocage au démarrage observé sur appareil réel) :
    // ce init{} s'exécute au moment de la CONSTRUCTION du singleton par Hilt —
    // c'est-à-dire ici, la toute première fois que le graphe de dépendances de
    // AppViewModel est résolu, dans setContent{} de MainActivity, donc SUR LE
    // THREAD PRINCIPAL. Lire `prefs` ici forçait l'évaluation synchrone du
    // `by lazy` (MasterKey Android Keystore + EncryptedSharedPreferences.create),
    // une opération de chiffrement + I/O disque pouvant prendre plusieurs
    // centaines de ms (voire davantage au tout premier lancement, génération de
    // la clé) — bloquant la première frame Compose (démarrage lent, pression GC,
    // timeouts SurfaceSync). Déporté sur Dispatchers.IO : accessTokenFlow reste
    // à sa valeur initiale (null) le temps de la lecture, ce qui est déjà le
    // comportement attendu par AppViewModel/MainActivity (écran de chargement
    // affiché tant que ecranDepart == null, cf. AppViewModel.kt).
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

