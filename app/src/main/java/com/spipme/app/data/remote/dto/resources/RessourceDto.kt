package com.spipme.app.data.remote.dto.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RessourceDto(
    val id: Int,
    val type: String,
    val nom: String,
    val unite: String = "",
    @SerialName("valeur_unitaire") val valeurUnitaire: String? = null,
    @SerialName("niveau_actuel") val niveauActuel: String,
    @SerialName("seuil_critique") val seuilCritique: String? = null,
    @SerialName("seuil_alerte") val seuilAlerte: String? = null,
    val statut: String,
    val emplacement: String = "",
    val entite: Int? = null,
    @SerialName("entite_nom") val entiteNom: String? = null,
    val secteur: Int,
    @SerialName("secteur_nom") val secteurNom: String? = null,
    @SerialName("date_maj") val dateMaj: String? = null,
    val version: Int = 1,   // contrôle de concurrence (If-Match)
)

@Serializable
data class ResumeRessourcesDto(
    val total: Int,
    val critiques: Int,
    @SerialName("a_surveiller") val aSurveiller: Int,
    val stables: Int,
)

@Serializable
data class CreerRessourceRequestDto(
    val type: String,
    val nom: String,
    val unite: String? = null,
    @SerialName("valeur_unitaire") val valeurUnitaire: String? = null,
    @SerialName("niveau_actuel") val niveauActuel: String,
    @SerialName("seuil_critique") val seuilCritique: String? = null,
    @SerialName("seuil_alerte") val seuilAlerte: String? = null,
    val emplacement: String? = null,
    val secteur: Int,
)

@Serializable
data class PointEvolutionDto(
    val date: String,
    val niveau: String,
)
