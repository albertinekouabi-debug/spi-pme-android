package com.spipme.app.ui.auth.offline

data class OfflineLoginUiState(
    val identifiant: String = "",
    val motDePasse: String = "",
    val seSouvenirDeMoi: Boolean = true,
    val enCoursDeConnexion: Boolean = false,
    val messageErreur: String? = null,
    val connexionReussie: Boolean = false,
)
