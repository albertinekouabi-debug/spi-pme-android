package com.spipme.app.domain.model

import java.math.BigDecimal

data class Facture(
    val id: Int,
    val numero: String,
    val entiteNom: String?,
    val montant: BigDecimal,
    val tauxTva: BigDecimal,
    val montantTva: BigDecimal,
    val montantTtc: BigDecimal,
    val statut: String,
    val dateEcheance: String?,
    val dateDerniereRelance: String?,
    val dateCreation: String,
)

data class DeclarationConformite(
    val id: Int,
    val transactionReference: String?,
    val transactionMontant: BigDecimal?,
    val motif: String,
    val seuilApplique: BigDecimal?,
    val statut: String,
    val referenceDeclaration: String?,
    val note: String?,
    val declarantNom: String?,
    val dateDetection: String,
    val dateDeclaration: String?,
)

