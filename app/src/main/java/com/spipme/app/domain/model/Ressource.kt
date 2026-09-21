package com.spipme.app.domain.model

import java.math.BigDecimal

data class Ressource(
    val id: Int,
    val type: String,
    val nom: String,
    val unite: String,
    val valeurUnitaire: BigDecimal?,
    val niveauActuel: BigDecimal,
    val seuilCritique: BigDecimal?,
    val seuilAlerte: BigDecimal?,
    val statut: String,
    val emplacement: String,
    val secteurId: Int,
    val secteurNom: String?,
) {
    /** Progression pour la barre visuelle (0f..1f), bornée à l'intervalle même si niveau > seuil_alerte. */
    val progression: Float
        get() {
            val seuil = seuilAlerte ?: return 1f // pas de seuil configuré -> pas de barre pertinente, plein par défaut
            if (seuil <= BigDecimal.ZERO) return 1f
            val ratio = niveauActuel.divide(seuil, 4, java.math.RoundingMode.HALF_UP).toFloat()
            return ratio.coerceIn(0f, 1f)
        }

    val seuilsConfigures: Boolean get() = seuilCritique != null && seuilAlerte != null
}

data class ResumeRessources(
    val total: Int,
    val critiques: Int,
    val aSurveiller: Int,
    val stables: Int,
)

data class PointEvolution(val date: String, val niveau: BigDecimal)
