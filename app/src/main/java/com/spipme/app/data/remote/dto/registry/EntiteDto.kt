package com.spipme.app.data.remote.dto.registry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntiteDto(
    val id: Int,
    val type: String,
    val nom: String,
    val telephone: String = "",
    val email: String = "",
    val adresse: String = "",
    val ville: String = "",
    val pays: String = "",
    @SerialName("numero_rccm") val numeroRccm: String = "",
    @SerialName("numero_fiscal") val numeroFiscal: String = "",
    @SerialName("champs_dynamiques") val champsDynamiques: Map<String, String> = emptyMap(),
    val statut: String = "actif",
    val secteur: Int,
    @SerialName("secteur_nom") val secteurNom: String? = null,
    @SerialName("date_creation") val dateCreation: String? = null,
)

/** Réponse paginée standard DRF (PageNumberPagination). */
@Serializable
data class PageDto<T>(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<T>,
)

@Serializable
data class RepartitionParTypeDto(
    val type: String,
    val total: Int,
)

@Serializable
data class ResumeRegistreDto(
    val total: Int,
    val actives: Int,
    @SerialName("par_type") val parType: List<RepartitionParTypeDto>,
)

@Serializable
data class CreerEntiteRequestDto(
    val type: String,
    val nom: String,
    val telephone: String? = null,
    val email: String? = null,
    val adresse: String? = null,
    val ville: String? = null,
    val pays: String? = null,
    @SerialName("numero_rccm") val numeroRccm: String? = null,
    @SerialName("numero_fiscal") val numeroFiscal: String? = null,
    val secteur: Int,
)
