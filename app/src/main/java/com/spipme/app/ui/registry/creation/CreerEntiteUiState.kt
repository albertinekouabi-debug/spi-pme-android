package com.spipme.app.ui.registry.creation

data class CreerEntiteUiState(
    val type: String = "client",
    val typePersonnalise: String = "",
    val nom: String = "",
    val telephone: String = "",
    val email: String = "",
    val ville: String = "",
    val enCoursDEnvoi: Boolean = false,
    val messageErreur: String? = null,
    val creationReussie: Boolean = false,
) {
    val typeEffectif: String get() = if (type == "autre") typePersonnalise else type
}

val TYPES_SUGGERES = listOf("client", "fournisseur", "partenaire", "autre")
