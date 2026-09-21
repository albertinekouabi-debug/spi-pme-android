package com.spipme.app.domain.model

data class Alerte(
    val id: Int,
    val type: String,
    val niveau: String,
    val titre: String,
    val description: String,
    val statut: String,
    val ressourceNom: String?,
    val entiteNom: String?,
    val tacheTitre: String?,
    val factureNumero: String?,
    val secteurId: Int,
    val dateDeclenchement: String,
    val dateResolution: String?,
) {
    val estActive: Boolean get() = statut == "active"
}

data class ResumeAlertes(
    val critiques: Int,
    val elevees: Int,
    val moderees: Int,
    val resolues: Int,
)
