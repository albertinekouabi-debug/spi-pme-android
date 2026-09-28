package com.spipme.app.ui.audit

import com.spipme.app.domain.model.EvenementAudit
import com.spipme.app.domain.model.ResumeAudit

data class AuditUiState(
    val chargementInitial: Boolean = true,
    val chargementPageSuivante: Boolean = false,
    val secteurActifNom: String = "",
    val evenements: List<EvenementAudit> = emptyList(),
    val resume: ResumeAudit? = null,
    val pageCourante: Int = 1,
    val ilResteDesPages: Boolean = false,
    val filtreModule: String? = null,
    val filtreResultat: String? = null,
    val recherche: String = "",
    val messageErreur: String? = null,
) {
    val estVide: Boolean get() = !chargementInitial && evenements.isEmpty() && messageErreur == null
}

