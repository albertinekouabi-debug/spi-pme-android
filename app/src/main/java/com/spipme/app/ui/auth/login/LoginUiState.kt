package com.spipme.app.ui.auth.login

data class LoginUiState(
    val identifiant: String = "",
    val motDePasse: String = "",
    val enCoursDeConnexion: Boolean = false,
    val messageErreur: String? = null,
    val connexionReussie: Boolean = false,
)
