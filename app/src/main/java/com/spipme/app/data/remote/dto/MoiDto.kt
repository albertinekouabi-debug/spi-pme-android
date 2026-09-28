package com.spipme.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** PATCH /me â€” restreint cÃ´tÃ© serveur Ã  ces deux champs (MoiUpdateSerializer). */
@Serializable
data class MettreAJourProfilRequestDto(
    @SerialName("nom_complet") val nomComplet: String? = null,
    val telephone: String? = null,
)

@Serializable
data class ChangerMotDePasseRequestDto(
    @SerialName("ancien_mot_de_passe") val ancienMotDePasse: String,
    @SerialName("nouveau_mot_de_passe") val nouveauMotDePasse: String,
)

