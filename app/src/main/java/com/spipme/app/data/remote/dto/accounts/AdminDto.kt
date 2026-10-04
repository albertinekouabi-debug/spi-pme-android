package com.spipme.app.data.remote.dto.accounts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoleDto(
    val id: Int,
    val nom: String,
    val description: String? = null,
    @SerialName("date_creation") val dateCreation: String? = null,
)

/** Écriture (POST /users) : reflète exactement UtilisateurCreationSerializer. */
@Serializable
data class CreerUtilisateurRequestDto(
    @SerialName("nom_utilisateur") val nomUtilisateur: String,
    val email: String,
    val password: String,
    @SerialName("nom_complet") val nomComplet: String? = null,
    val telephone: String? = null,
    val role: Int,
    @SerialName("secteur_principal") val secteurPrincipal: Int,
    val secteurs: List<Int> = emptyList(),
    val actif: Boolean = true,
)

