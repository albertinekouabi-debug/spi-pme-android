package com.spipme.app.data.remote.dto.treasury

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reflète exactement FactureSerializer. Montants en String : Decimal sérialisé par DRF. */
@Serializable
data class FactureDto(
    val id: Int,
    val numero: String,
    val transaction: Int? = null,
    val entite: Int? = null,
    @SerialName("entite_nom") val entiteNom: String? = null,
    val montant: String,
    // La TVA est optionnelle côté serveur : sans taux, taux_tva ET montant_tva valent null
    // (contrat verrouillé par apps/treasury/tests/test_contrat_facture.py). Sans défaut
    // nullable, kotlinx.serialization rejetterait toute la liste de factures.
    @SerialName("taux_tva") val tauxTva: String? = null,
    @SerialName("montant_tva") val montantTva: String? = null,
    @SerialName("montant_ttc") val montantTtc: String,
    val statut: String,
    @SerialName("date_echeance") val dateEcheance: String? = null,
    @SerialName("date_derniere_relance") val dateDerniereRelance: String? = null,
    val secteur: Int? = null,
    @SerialName("date_creation") val dateCreation: String,
)

/** Reflète exactement DeclarationConformiteSerializer (read_only_fields = fields). */
@Serializable
data class DeclarationConformiteDto(
    val id: Int,
    val transaction: Int? = null,
    @SerialName("transaction_reference") val transactionReference: String? = null,
    @SerialName("transaction_montant") val transactionMontant: String? = null,
    val motif: String,
    @SerialName("seuil_applique") val seuilApplique: String? = null,
    val statut: String,
    @SerialName("reference_declaration") val referenceDeclaration: String? = null,
    val note: String? = null,
    val declarant: Int? = null,
    @SerialName("declarant_nom") val declarantNom: String? = null,
    @SerialName("date_detection") val dateDetection: String,
    @SerialName("date_declaration") val dateDeclaration: String? = null,
)

@Serializable
data class DeclarerRequestDto(
    @SerialName("reference_declaration") val referenceDeclaration: String,
)

@Serializable
data class ExempterRequestDto(
    val note: String,
)

