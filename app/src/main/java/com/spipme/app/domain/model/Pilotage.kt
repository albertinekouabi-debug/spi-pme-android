package com.spipme.app.domain.model

/** Indicateur chiffré (déjà formaté par le serveur). `valeur == null` = information insuffisante (jamais un faux zéro). */
data class Kpi(val libelle: String, val valeur: String?, val detail: String? = null)

data class FaitInsight(val libelle: String, val valeur: String, val source: String)

/** `niveau` : "critique" | "attention" | "info". Faits, projection et recommandation restent séparés. */
data class Insight(
    val code: String,
    val niveau: String,
    val titre: String,
    val analyse: String,
    val faits: List<FaitInsight>,
    val projection: String?,
    val options: List<Pair<String, String>>,
    val confiance: String,
    val limites: String,
)

/** `horsLigneDepuis` : date (ms) de la dernière mise à jour si les données viennent du cache, sinon null. */
data class Pilotage(val kpis: List<Kpi>, val insights: List<Insight>, val horsLigneDepuis: Long?)

data class ReponseCopilote(val texte: String, val statut: String, val questionsSupportees: List<String>)
