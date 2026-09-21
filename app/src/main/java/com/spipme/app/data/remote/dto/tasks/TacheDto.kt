package com.spipme.app.data.remote.dto.tasks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TacheDto(
    val id: Int,
    val titre: String,
    val description: String = "",
    val categorie: String = "",
    val priorite: String,
    val statut: String,
    val lieu: String = "",
    val assignee: Int? = null,
    @SerialName("assignee_nom") val assigneeNom: String? = null,
    val createur: Int? = null,
    @SerialName("createur_nom") val createurNom: String? = null,
    val echeance: String? = null,
    val secteur: Int,
    @SerialName("secteur_nom") val secteurNom: String? = null,
    @SerialName("en_retard") val enRetard: Boolean = false,
    @SerialName("date_creation") val dateCreation: String? = null,
)

@Serializable
data class ResumeTachesDto(
    val total: Int,
    val terminees: Int,
    @SerialName("en_cours") val enCours: Int,
    @SerialName("en_retard") val enRetard: Int,
)

@Serializable
data class HistoriqueStatutDto(
    val id: Int,
    @SerialName("ancien_statut") val ancienStatut: String? = null,
    @SerialName("nouveau_statut") val nouveauStatut: String,
    val auteur: Int? = null,
    @SerialName("auteur_nom") val auteurNom: String? = null,
    @SerialName("date_changement") val dateChangement: String,
)

@Serializable
data class CreerTacheRequestDto(
    val titre: String,
    val description: String? = null,
    val categorie: String? = null,
    val priorite: String = "moyenne",
    val lieu: String? = null,
    val assignee: Int? = null,
    val echeance: String? = null,
    val secteur: Int,
)

@Serializable
data class ModifierStatutTacheRequestDto(
    val statut: String,
)
