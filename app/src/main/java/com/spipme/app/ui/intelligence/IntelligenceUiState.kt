package com.spipme.app.ui.intelligence

import com.spipme.app.domain.model.ResumeSuggestions
import com.spipme.app.domain.model.Suggestion

enum class OngletSuggestions(val libelle: String, val statutFiltre: String?) {
    TOUTES("Toutes", null),
    EN_ATTENTE("En attente", "en_attente"),
    VALIDEES("Validées", "validee"),
    REJETEES("Rejetées", "rejetee"),
}

data class IntelligenceUiState(
    val enChargement: Boolean = true,
    val enCoursDeGeneration: Boolean = false,
    val suggestions: List<Suggestion> = emptyList(),
    val resume: ResumeSuggestions? = null,
    val ongletActif: OngletSuggestions = OngletSuggestions.TOUTES,
    val messageErreur: String? = null,
    val secteurActifNom: String = "",
    /** Suggestion pour laquelle la boîte de dialogue de rejet (motif obligatoire) est ouverte. */
    val suggestionARejeterId: Int? = null,
)
