package com.spipme.app.ui.auth.register

data class RegisterUiState(
    val nomUtilisateur: String = "",
    val email: String = "",
    val motDePasse: String = "",
    val confirmationMotDePasse: String = "",
    val nomComplet: String = "",
    val codeInvitation: String = "",
    val enCoursDInscription: Boolean = false,
    val messageErreur: String? = null,
    val inscriptionReussie: Boolean = false,
)

