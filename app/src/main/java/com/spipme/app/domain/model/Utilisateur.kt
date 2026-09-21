package com.spipme.app.domain.model

data class Utilisateur(
    val id: Int,
    val nomUtilisateur: String,
    val email: String,
    val nomComplet: String?,
    val telephone: String?,
    val roleId: Int,
    val roleNom: String?,
    val secteurPrincipalId: Int?,
    val secteurPrincipalNom: String?,
    val secteurs: List<Int>,
    val actif: Boolean,
)
