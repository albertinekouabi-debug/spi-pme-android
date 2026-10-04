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

/** Ne contient jamais de token : le compte est créé INACTIF, en attente de vérification email. */
@Serializable
data class InscriptionResponseDto(
    val id: Int,
    @SerialName("nom_utilisateur") val nomUtilisateur: String,
    val email: String,
    @SerialName("nom_complet") val nomComplet: String? = null,
    val telephone: String? = null,
)

@Serializable
data class DemandeReinitialisationRequestDto(val email: String)

@Serializable
data class ConfirmationReinitialisationRequestDto(
    val token: String,
    @SerialName("nouveau_mot_de_passe") val nouveauMotDePasse: String,
)

/** Réponse générique {"detail": "..."} du serveur pour ces deux endpoints. */
@Serializable
data class MessageReponseDto(val detail: String)

