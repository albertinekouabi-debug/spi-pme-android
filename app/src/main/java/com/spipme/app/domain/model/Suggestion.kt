package com.spipme.app.domain.model

import java.math.BigDecimal

data class Suggestion(
    val id: Int,
    val typeAlgorithme: String,
    val categorie: String,
    val titre: String,
    val description: String,
    val facteurs: List<Pair<String, String>>,
    val impactEstime: BigDecimal?,
    val confiance: BigDecimal?,
    val statut: String,
    val motifDecision: String,
    val ressourceLieeNom: String?,
    val secteurId: Int,
    val decideurNom: String?,
    val dateCreation: String,
    val dateDecision: String?,
) {
    val estEnAttente: Boolean get() = statut == "en_attente"
}

data class ResumeSuggestions(
    val total: Int,
    val validees: Int,
    val enAttente: Int,
    val rejetees: Int,
    val tauxAcceptation: Double?,
)
