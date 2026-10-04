package com.spipme.app.ui.auth.motdepasseoublie

enum class EtapeMotDePasseOublie { DEMANDE, CONFIRMATION }

data class MotDePasseOublieUiState(
    val etape: EtapeMotDePasseOublie = EtapeMotDePasseOublie.DEMANDE,
    val email: String = "",
    val token: String = "",
    val nouveauMotDePasse: String = "",
    val enCours: Boolean = false,
    val reinitialisationReussie: Boolean = false,
    val messageErreur: String? = null,
)
