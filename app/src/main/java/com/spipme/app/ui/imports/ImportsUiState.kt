package com.spipme.app.ui.imports

import com.spipme.app.domain.model.ApercuImport
import com.spipme.app.domain.model.FichierSelectionne
import com.spipme.app.domain.model.ImportFichier

enum class EtapeImport {
    SELECTION, APERCU_EN_COURS, APERCU_PRET, IMPORT_EN_COURS, TERMINE
}

data class ImportsUiState(
    val enChargementHistorique: Boolean = true,
    val historique: List<ImportFichier> = emptyList(),
    val fichierSelectionne: FichierSelectionne? = null,
    val etape: EtapeImport = EtapeImport.SELECTION,
    val apercu: ApercuImport? = null,
    val resultatImport: ImportFichier? = null,
    val messageErreur: String? = null,
    val secteurActifNom: String = "",
)
