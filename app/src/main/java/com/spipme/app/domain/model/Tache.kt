package com.spipme.app.domain.model

data class Tache(
    val id: Int,
    val titre: String,
    val description: String,
    val categorie: String,
    val priorite: String,
    val statut: String,
    val lieu: String,
    val assigneeId: Int?,
    val assigneeNom: String?,
    val echeance: String?,
    val secteurId: Int,
    val enRetard: Boolean,
)

data class ResumeTaches(
    val total: Int,
    val terminees: Int,
    val enCours: Int,
    val enRetard: Int,
)

data class HistoriqueStatut(
    val id: Int,
    val ancienStatut: String?,
    val nouveauStatut: String,
    val auteurNom: String?,
    val dateChangement: String,
)
