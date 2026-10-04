package com.spipme.app.data.remote.dto.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Contrat de /analytics/* (backend apps/analytics). Les montants restent des chaînes (précision décimale). */
@Serializable
data class MesureDto(
    val valeur: String? = null,
    val unite: String? = null,
    @SerialName("nb_elements") val nbElements: Int = 0,
    val definition: String = "",
    val statut: String = "ok",
)

@Serializable
data class CompteDto(val nb: Int = 0)

@Serializable
data class KpisDto(
    @SerialName("entrees_30j") val entrees30j: MesureDto = MesureDto(),
    @SerialName("sorties_30j") val sorties30j: MesureDto = MesureDto(),
    @SerialName("flux_net_30j") val fluxNet30j: MesureDto = MesureDto(),
    @SerialName("variation_entrees_pct") val variationEntreesPct: Double? = null,
    @SerialName("variation_sorties_pct") val variationSortiesPct: Double? = null,
    @SerialName("solde_cumule") val soldeCumule: MesureDto = MesureDto(),
    @SerialName("creances_ouvertes") val creancesOuvertes: MesureDto = MesureDto(),
    @SerialName("creances_en_retard") val creancesEnRetard: MesureDto = MesureDto(),
    @SerialName("taux_creances_en_retard_pct") val tauxCreancesEnRetardPct: Double? = null,
    @SerialName("ressources_critiques") val ressourcesCritiques: CompteDto = CompteDto(),
    @SerialName("ressources_a_surveiller") val ressourcesASurveiller: CompteDto = CompteDto(),
)

@Serializable
data class FaitDto(val libelle: String = "", val valeur: JsonElement? = null, val source: String = "")

@Serializable
data class OptionDto(val option: String = "", val justification: String = "")

@Serializable
data class InsightDto(
    val code: String = "",
    val categorie: String = "",
    val niveau: String = "info",
    val titre: String = "",
    val faits: List<FaitDto> = emptyList(),
    val analyse: String = "",
    val projection: JsonObject? = null,
    val recommandation: List<OptionDto>? = null,
    val confiance: String = "",
    val limites: String = "",
)

@Serializable
data class InsightsReponseDto(
    val insights: List<InsightDto> = emptyList(),
    @SerialName("version_moteur") val versionMoteur: String = "",
)

@Serializable
data class CopiloteRequestDto(val question: String, val secteur: Int? = null)

@Serializable
data class CopiloteReponseDto(
    val intention: String? = null,
    val statut: String = "",
    val reponse: String = "",
    val classification: String = "DISTANTE",
    @SerialName("questions_supportees") val questionsSupportees: List<String> = emptyList(),
)
