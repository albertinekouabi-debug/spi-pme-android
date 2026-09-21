package com.spipme.app.domain.model

import java.math.BigDecimal

data class Transaction(
    val id: Int,
    val type: String,
    val reference: String,
    val description: String,
    val montant: BigDecimal?,
    val quantite: BigDecimal?,
    val devise: String,
    val modePaiement: String,
    val entiteId: Int?,
    val entiteNom: String?,
    val ressourceId: Int?,
    val ressourceNom: String?,
    val secteurId: Int,
    val dateTransaction: String,
)

data class ResumeTresorerie(
    val entreesMois: BigDecimal,
    val sortiesMois: BigDecimal,
    val soldeNetMois: BigDecimal,
    val soldeDisponible: BigDecimal,
)

data class PointSolde(val date: String, val solde: BigDecimal)
