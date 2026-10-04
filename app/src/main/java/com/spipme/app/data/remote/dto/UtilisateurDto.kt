package com.spipme.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UtilisateurDto(
    val id: Int,
    @kotlinx.serialization.SerialName("nom_utilisateur") val nomUtilisateur: String,
    val email: String,
    @kotlinx.serialization.SerialName("nom_complet") val nomComplet: String? = null,
    val telephone: String? = null,
    val role: Int,
    @kotlinx.serialization.SerialName("role_nom") val roleNom: String? = null,
    @kotlinx.serialization.SerialName("secteur_principal") val secteurPrincipal: Int? = null,
    @kotlinx.serialization.SerialName("secteur_principal_nom") val secteurPrincipalNom: String? = null,
    val secteurs: List<Int> = emptyList(),
    val actif: Boolean = true,
    @kotlinx.serialization.SerialName("mfa_active") val mfaActive: Boolean = false,
    @kotlinx.serialization.SerialName("date_creation") val dateCreation: String? = null,
    @kotlinx.serialization.SerialName("derniere_connexion") val derniereConnexion: String? = null,
    // Fournies par /me et /auth/login ; vide ailleurs (listes d'utilisateurs). Indicatif : le serveur reste l'autorité.
    val permissions: List<String> = emptyList(),
)
