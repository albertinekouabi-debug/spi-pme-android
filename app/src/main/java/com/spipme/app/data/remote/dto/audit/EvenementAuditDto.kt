package com.spipme.app.data.remote.dto.audit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** ReflÃ¨te exactement JournalAuditSerializer cÃ´tÃ© backend (lecture seule). */
@Serializable
data class EvenementAuditDto(
    val id: Int,
    val action: String,
    val module: String,
    @SerialName("cible_type") val cibleType: String = "",
    @SerialName("cible_id") val cibleId: String = "",
    val resultat: String,
    @SerialName("adresse_ip") val adresseIp: String? = null,
    val auteur: Int? = null,
    @SerialName("auteur_nom") val auteurNom: String? = null,
    @SerialName("date_action") val dateAction: String,
)

/** ReflÃ¨te exactement l'agrÃ©gat de GET /audit-log/summary (total, reussies, avertissements, echecs). */
@Serializable
data class ResumeAuditDto(
    val total: Int,
    val reussies: Int,
    val avertissements: Int,
    val echecs: Int,
)

