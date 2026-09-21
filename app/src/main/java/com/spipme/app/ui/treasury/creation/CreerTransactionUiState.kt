package com.spipme.app.ui.treasury.creation

data class CreerTransactionUiState(
    val type: String = "entree",
    val montant: String = "",
    val modePaiement: String = "virement",
    val description: String = "",
    val enCoursDEnvoi: Boolean = false,
    val messageErreur: String? = null,
    val creationReussie: Boolean = false,
)

val MODES_PAIEMENT = listOf("especes", "virement", "mobile_money", "cheque", "carte")
