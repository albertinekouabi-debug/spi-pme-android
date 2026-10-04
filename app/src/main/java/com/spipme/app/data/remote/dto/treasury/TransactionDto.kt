package com.spipme.app.data.remote.dto.treasury

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: Int,
    val type: String,
    val reference: String = "",
    val description: String = "",
    val montant: String? = null,
    val quantite: String? = null,
    val devise: String = "XOF",
    @SerialName("mode_paiement") val modePaiement: String = "",
    val entite: Int? = null,
    @SerialName("entite_nom") val entiteNom: String? = null,
    val ressource: Int? = null,
    @SerialName("ressource_nom") val ressourceNom: String? = null,
    val secteur: Int,
    @SerialName("secteur_nom") val secteurNom: String? = null,
    @SerialName("date_transaction") val dateTransaction: String,
    val statut: String = "validee",
    @SerialName("contre_ecriture_de") val contreEcritureDe: Int? = null,
    val version: Int = 1,
)

@Serializable
data class ResumeTresorerieDto(
    @SerialName("entrees_mois") val entreesMois: String,
    @SerialName("sorties_mois") val sortiesMois: String,
    @SerialName("solde_net_mois") val soldeNetMois: String,
    @SerialName("solde_disponible") val soldeDisponible: String,
)

@Serializable
data class PointSoldeDto(
    val date: String,
    val solde: String,
)

@Serializable
data class CreerTransactionRequestDto(
    val type: String,
    val reference: String? = null,
    val description: String? = null,
    val montant: String? = null,
    val quantite: String? = null,
    val devise: String = "XOF",
    @SerialName("mode_paiement") val modePaiement: String? = null,
    val entite: Int? = null,
    val ressource: Int? = null,
    val secteur: Int,
    @SerialName("date_transaction") val dateTransaction: String,
)
