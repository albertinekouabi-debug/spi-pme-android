package com.spipme.app.data.remote.dto.imports

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AnomalieDto(
    val ligne: Int,
    val colonne: String,
    val motif: String,
)

@Serializable
data class ImportFichierDto(
    val id: Int,
    @SerialName("nom_fichier") val nomFichier: String,
    @SerialName("type_fichier") val typeFichier: String,
    @SerialName("taille_octets") val tailleOctets: Long? = null,
    val statut: String,
    @SerialName("lignes_totales") val lignesTotales: Int,
    @SerialName("lignes_importees") val lignesImportees: Int,
    @SerialName("lignes_rejetees") val lignesRejetees: Int,
    @SerialName("rapport_anomalies") val rapportAnomalies: List<AnomalieDto> = emptyList(),
    @SerialName("auteur_nom") val auteurNom: String? = null,
    val secteur: Int,
    @SerialName("secteur_nom") val secteurNom: String? = null,
    @SerialName("date_import") val dateImport: String,
    @SerialName("date_fin") val dateFin: String? = null,
)

@Serializable
data class ApercuImportDto(
    @SerialName("lignes_totales") val lignesTotales: Int,
    @SerialName("lignes_valides") val lignesValides: Int,
    @SerialName("lignes_rejetees") val lignesRejetees: Int,
    @SerialName("rapport_anomalies") val rapportAnomalies: List<AnomalieDto> = emptyList(),
    val apercu: List<JsonObject> = emptyList(),
)
