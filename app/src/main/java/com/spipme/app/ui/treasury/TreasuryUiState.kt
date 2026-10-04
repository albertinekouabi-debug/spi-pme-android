package com.spipme.app.ui.treasury

import com.spipme.app.ui.sync.ElementEnAttente
import com.spipme.app.domain.model.PointSolde
import com.spipme.app.domain.model.ResumeTresorerie
import com.spipme.app.domain.model.Transaction

enum class OngletTresorerie(val libelle: String, val typeFiltre: String?) {
    TOUTES("Toutes", null),
    ENTREES("Entrées", "entree"),
    SORTIES("Sorties", "sortie"),
}

data class TreasuryUiState(
    val enChargement: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val resume: ResumeTresorerie? = null,
    val evolution: List<PointSolde> = emptyList(),
    val ongletActif: OngletTresorerie = OngletTresorerie.TOUTES,
    val messageErreur: String? = null,
    val secteurActifNom: String = "",
    val peutCorriger: Boolean = false,
    val idsEnAttente: Set<Int> = emptySet(),
    val creationsEnAttente: List<ElementEnAttente> = emptyList(),
    val transactionAContrePasser: Transaction? = null,
    val erreurContrePassation: String? = null,
    val messageInfo: String? = null,
)
