package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.local.CacheLecture
import com.spipme.app.data.remote.api.AnalyticsApi
import com.spipme.app.data.remote.dto.analytics.CopiloteRequestDto
import com.spipme.app.data.remote.dto.analytics.InsightDto
import com.spipme.app.data.remote.dto.analytics.InsightsReponseDto
import com.spipme.app.data.remote.dto.analytics.KpisDto
import com.spipme.app.data.remote.dto.analytics.MesureDto
import com.spipme.app.domain.model.FaitInsight
import com.spipme.app.domain.model.Insight
import com.spipme.app.domain.model.Kpi
import com.spipme.app.domain.model.Pilotage
import com.spipme.app.domain.model.ReponseCopilote
import com.spipme.app.domain.repository.AnalyticsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val api: AnalyticsApi,
    private val json: Json,
    private val cacheLecture: CacheLecture,
) : AnalyticsRepository {

    override suspend fun pilotage(secteurId: Int): Resultat<Pilotage> = coroutineScope {
        val kpis = async {
            cacheLecture.lireAvecOrigine("pilotage-kpis:$secteurId", KpisDto.serializer()) {
                executerAppelApi(json) { api.kpis(secteurId) }
            }
        }
        val insights = async {
            cacheLecture.lireAvecOrigine("pilotage-insights:$secteurId", InsightsReponseDto.serializer()) {
                executerAppelApi(json) { api.insights(secteurId) }
            }
        }
        val k = kpis.await()
        val i = insights.await()
        when {
            k is Resultat.Echec -> k
            i is Resultat.Echec -> i
            else -> {
                val (kpisDto, origineK) = (k as Resultat.Succes).donnees
                val (insightsDto, origineI) = (i as Resultat.Succes).donnees
                // Si l'une des deux vient du cache, on affiche la date la plus ancienne (la moins fraîche).
                val depuis = listOfNotNull(origineK, origineI).minOrNull()
                Resultat.Succes(Pilotage(versKpis(kpisDto), insightsDto.insights.map { it.versDomaine() }, depuis))
            }
        }
    }

    override suspend fun interroger(question: String, secteurId: Int): Resultat<ReponseCopilote> {
        val reponse = executerAppelApi(json) { api.copilote(CopiloteRequestDto(question.trim(), secteurId)) }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(
                ReponseCopilote(reponse.donnees.reponse, reponse.donnees.statut, reponse.donnees.questionsSupportees)
            )
            is Resultat.Echec -> reponse
        }
    }

    private fun versKpis(k: KpisDto): List<Kpi> = listOf(
        mesure("Entrées (30 j)", k.entrees30j, k.variationEntreesPct),
        mesure("Sorties (30 j)", k.sorties30j, k.variationSortiesPct),
        mesure("Flux net (30 j)", k.fluxNet30j, null),
        mesure("Solde cumulé enregistré", k.soldeCumule, null),
        mesure("Créances ouvertes", k.creancesOuvertes, null),
        Kpi(
            "Créances en retard", k.creancesEnRetard.valeur?.let { "$it ${k.creancesEnRetard.unite.orEmpty()}".trim() },
            k.tauxCreancesEnRetardPct?.let { "$it % des créances ouvertes" },
        ),
        Kpi("Ressources critiques", k.ressourcesCritiques.nb.toString(), "${k.ressourcesASurveiller.nb} à surveiller"),
    )

    /** Variation affichée seulement si le serveur a pu la calculer (base de comparaison suffisante). */
    private fun mesure(libelle: String, m: MesureDto, variation: Double?) = Kpi(
        libelle = libelle,
        valeur = m.valeur?.let { "$it ${m.unite.orEmpty()}".trim() },
        detail = variation?.let { "${if (it > 0) "+" else ""}$it % vs 30 j précédents" },
    )

    private fun InsightDto.versDomaine() = Insight(
        code = code, niveau = niveau, titre = titre, analyse = analyse,
        faits = faits.map { FaitInsight(it.libelle, it.valeur?.let { v -> (v as? JsonPrimitive)?.contentOrNull ?: v.toString() }.orEmpty(), it.source) },
        projection = projection?.let { p ->
            val nature = p["nature"]?.jsonPrimitive?.contentOrNull
            val enonce = p["enonce"]?.jsonPrimitive?.contentOrNull
            listOfNotNull(nature, enonce).joinToString(" — ").ifBlank { null }
        },
        options = recommandation.orEmpty().map { it.option to it.justification },
        confiance = confiance, limites = limites,
    )
}
