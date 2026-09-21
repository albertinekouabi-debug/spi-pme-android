package com.spipme.app.data.remote.dto.intelligence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SuggestionDto(
    val id: Int,
    @SerialName("type_algorithme") val typeAlgorithme: String,
    val categorie: String = "",
    val titre: String,
    val description: String = "",
    val facteurs: JsonObject = JsonObject(emptyMap()),
    @SerialName("impact_estime") val impactEstime: String? = null,
    val confiance: String? = null,
    val statut: String,
    @SerialName("motif_decision") val motifDecision: String = "",
    @SerialName("ressource_liee") val ressourceLiee: Int? = null,
    @SerialName("ressource_liee_nom") val ressourceLieeNom: String? = null,
    @SerialName("entite_liee") val entiteLiee: Int? = null,
    @SerialName("entite_liee_nom") val entiteLieeNom: String? = null,
    val secteur: Int,
    @SerialName("secteur_nom") val secteurNom: String? = null,
    val decideur: Int? = null,
    @SerialName("decideur_nom") val decideurNom: String? = null,
    @SerialName("date_creation") val dateCreation: String,
    @SerialName("date_decision") val dateDecision: String? = null,
)

@Serializable
data class ResumeSuggestionsDto(
    val total: Int,
    val validees: Int,
    @SerialName("en_attente") val enAttente: Int,
    val rejetees: Int,
    @SerialName("taux_acceptation") val tauxAcceptation: Double? = null,
)

@Serializable
data class GenererSuggestionsRequestDto(val secteur: Int? = null)

@Serializable
data class RejeterSuggestionRequestDto(val motif: String)
