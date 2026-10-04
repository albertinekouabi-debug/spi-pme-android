package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Pilotage
import com.spipme.app.domain.model.ReponseCopilote

interface AnalyticsRepository {
    /** KPI + insights ; hors ligne, la dernière analyse mémorisée (mention de sa date). Calcul DISTANT (serveur). */
    suspend fun pilotage(secteurId: Int): Resultat<Pilotage>

    /** Le copilote exige le serveur : jamais simulé hors ligne. */
    suspend fun interroger(question: String, secteurId: Int): Resultat<ReponseCopilote>
}
