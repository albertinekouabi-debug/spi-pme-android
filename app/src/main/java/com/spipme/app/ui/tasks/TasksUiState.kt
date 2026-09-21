package com.spipme.app.ui.tasks

import com.spipme.app.domain.model.ResumeTaches
import com.spipme.app.domain.model.Tache

enum class OngletTaches(val libelle: String) {
    TOUTES("Toutes"),
    EN_COURS("En cours"),
    TERMINEES("Terminées"),
    EN_RETARD("En retard"),
}

data class TasksUiState(
    val enChargement: Boolean = true,
    val taches: List<Tache> = emptyList(),
    val resume: ResumeTaches? = null,
    val ongletActif: OngletTaches = OngletTaches.TOUTES,
    val messageErreur: String? = null,
    val secteurActifNom: String = "",
)
