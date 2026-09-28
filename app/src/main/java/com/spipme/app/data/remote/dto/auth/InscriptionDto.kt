package com.spipme.app.data.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InscriptionRequestDto(
    @SerialName("nom_utilisateur") val nomUtilisateur: String,
    val email: String,
    val password: String,
    @SerialName("nom_complet") val nomComplet: String? = null,
    val telephone: String? = null,
    @SerialName("code_invitation") val codeInvitation: String,
)

/** Ne contient jamais de token : le compte est crÃ©Ã© INACTIF, en attente de vÃ©rification email. */
@Serializable
data class InscriptionResponseDto(
    val id: Int,
    @SerialName("nom_utilisateur") val nomUtilisateur: String,
    val email: String,
    @SerialName("nom_complet") val nomComplet: String? = null,
    val telephone: String? = null,
)

