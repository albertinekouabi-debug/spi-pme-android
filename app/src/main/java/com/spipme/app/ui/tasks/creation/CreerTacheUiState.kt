package com.spipme.app.ui.tasks.creation

data class CreerTacheUiState(
    val titre: String = "",
    val description: String = "",
    val categorie: String = "",
    val priorite: String = "moyenne",
    val echeance: String = "",
    val enCoursDEnvoi: Boolean = false,
    val messageErreur: String? = null,
    val creationReussie: Boolean = false,
)

val PRIORITES = listOf("basse", "moyenne", "haute")
