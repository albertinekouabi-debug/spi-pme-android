package com.spipme.app.ui.resources

import com.spipme.app.domain.model.ResumeRessources
import com.spipme.app.domain.model.Ressource

enum class OngletRessources(val libelle: String, val statutFiltre: String?) {
    TOUTES("Toutes", null),
    CRITIQUES("Critiques", "critique"),
    A_SURVEILLER("À surveiller", "a_surveiller"),
    STABLES("Stables", "stable"),
}

data class ResourcesUiState(
    val enChargement: Boolean = true,
    val ressources: List<Ressource> = emptyList(),
    val resume: ResumeRessources? = null,
    val ongletActif: OngletRessources = OngletRessources.TOUTES,
    val recherche: String = "",
    val messageErreur: String? = null,
    val secteurActifNom: String = "",
)
