package com.spipme.app.ui.alerts

import com.spipme.app.domain.model.Alerte
import com.spipme.app.domain.model.ResumeAlertes

enum class OngletAlertes(val libelle: String, val niveauFiltre: String?, val statutFiltre: String?) {
    TOUTES("Toutes", null, "active"),
    CRITIQUES("Critiques", "critique", "active"),
    ELEVEES("Élevées", "elevee", "active"),
    MODEREES("Modérées", "moderee", "active"),
    RESOLUES("Résolues", null, "traitee"),
}

data class AlertsUiState(
    val enChargement: Boolean = true,
    val enCoursDeGeneration: Boolean = false,
    val alertes: List<Alerte> = emptyList(),
    val resume: ResumeAlertes? = null,
    val ongletActif: OngletAlertes = OngletAlertes.TOUTES,
    val messageErreur: String? = null,
    val messageInfo: String? = null,
    val secteurActifNom: String = "",
)
