package com.spipme.app.data.remote.dto.alerts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlerteDto(
    val id: Int,
    val type: String,
    val niveau: String,
    val titre: String,
    val description: String = "",
    val statut: String,
    val ressource: Int? = null,
    @SerialName("ressource_nom") val ressourceNom: String? = null,
    val entite: Int? = null,
    @SerialName("entite_nom") val entiteNom: String? = null,
    val tache: Int? = null,
    @SerialName("tache_titre") val tacheTitre: String? = null,
    val facture: Int? = null,
    @SerialName("facture_numero") val factureNumero: String? = null,
    val secteur: Int,
    @SerialName("secteur_nom") val secteurNom: String? = null,
    @SerialName("date_declenchement") val dateDeclenchement: String,
    @SerialName("date_resolution") val dateResolution: String? = null,
)

@Serializable
data class ResumeAlertesDto(
    val critiques: Int,
    val elevees: Int,
    val moderees: Int,
    val resolues: Int,
)

@Serializable
data class GenererAlertesRequestDto(val secteur: Int? = null)
