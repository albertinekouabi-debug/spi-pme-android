package com.spipme.app.core.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spipme.app.domain.model.Utilisateur
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "spi_pme_session")

/**
 * Profil de l'utilisateur connecté, mis en cache localement après connexion
 * (en ligne ou hors ligne) — nécessaire dès l'écran suivant (Registre,
 * Ressources...) qui ont besoin de connaître le secteur actif sans le
 * redemander à chaque fois. Distinct de TokenManager : les tokens sont un
 * détail d'authentification réseau, ce profil est une donnée d'application.
 */
@Singleton
class SessionManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    private object Cles {
        val UTILISATEUR_ID = intPreferencesKey("utilisateur_id")
        val NOM_UTILISATEUR = stringPreferencesKey("nom_utilisateur")
        val ROLE_NOM = stringPreferencesKey("role_nom")
        val SECTEUR_ACTIF_ID = intPreferencesKey("secteur_actif_id")
        val SECTEUR_ACTIF_NOM = stringPreferencesKey("secteur_actif_nom")
    }

    val secteurActifIdFlow: Flow<Int?> = context.sessionDataStore.data.map { it[Cles.SECTEUR_ACTIF_ID] }
    val secteurActifNomFlow: Flow<String?> = context.sessionDataStore.data.map { it[Cles.SECTEUR_ACTIF_NOM] }
    val roleNomFlow: Flow<String?> = context.sessionDataStore.data.map { it[Cles.ROLE_NOM] }
    val nomUtilisateurFlow: Flow<String?> = context.sessionDataStore.data.map { it[Cles.NOM_UTILISATEUR] }

    suspend fun enregistrerSession(utilisateur: Utilisateur) {
        context.sessionDataStore.edit { prefs ->
            prefs[Cles.UTILISATEUR_ID] = utilisateur.id
            prefs[Cles.NOM_UTILISATEUR] = utilisateur.nomUtilisateur
            utilisateur.roleNom?.let { prefs[Cles.ROLE_NOM] = it }
            utilisateur.secteurPrincipalId?.let { prefs[Cles.SECTEUR_ACTIF_ID] = it }
            utilisateur.secteurPrincipalNom?.let { prefs[Cles.SECTEUR_ACTIF_NOM] = it }
        }
    }

    /** Changement du secteur actif (sélecteur "Secteur actif" présent en en-tête de chaque écran des maquettes). */
    suspend fun changerSecteurActif(id: Int, nom: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[Cles.SECTEUR_ACTIF_ID] = id
            prefs[Cles.SECTEUR_ACTIF_NOM] = nom
        }
    }

    suspend fun effacerSession() {
        context.sessionDataStore.edit { it.clear() }
    }
}
