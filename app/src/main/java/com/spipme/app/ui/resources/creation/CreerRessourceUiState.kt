package com.spipme.app.ui.resources.creation

data class CreerRessourceUiState(
    val type: String = "",
    val nom: String = "",
    val unite: String = "",
    val niveauActuel: String = "",
    val seuilCritique: String = "",
    val seuilAlerte: String = "",
    val valeurUnitaire: String = "",
    val emplacement: String = "",
    val enCoursDEnvoi: Boolean = false,
    val messageErreur: String? = null,
    val creationReussie: Boolean = false,
)
