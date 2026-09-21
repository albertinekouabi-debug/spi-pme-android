package com.spipme.app.domain.model

data class Entite(
    val id: Int,
    val type: String,
    val nom: String,
    val telephone: String,
    val email: String,
    val ville: String,
    val pays: String,
    val numeroRccm: String,
    val numeroFiscal: String,
    val statut: String,
    val secteurId: Int,
    val secteurNom: String?,
) {
    val estActif: Boolean get() = statut == "actif"
}

data class RepartitionParType(val type: String, val total: Int)

data class ResumeRegistre(
    val total: Int,
    val actives: Int,
    val parType: List<RepartitionParType>,
)
