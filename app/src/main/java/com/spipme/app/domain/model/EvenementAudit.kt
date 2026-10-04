package com.spipme.app.domain.model

data class EvenementAudit(
    val id: Int,
    val action: String,
    val module: String,
    val cibleType: String,
    val cibleId: String,
    val resultat: String,
    val adresseIp: String?,
    val auteurNom: String?,
    val dateAction: String,
)

data class ResumeAudit(
    val total: Int,
    val reussies: Int,
    val avertissements: Int,
    val echecs: Int,
)

