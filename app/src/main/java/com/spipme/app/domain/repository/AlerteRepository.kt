package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Alerte
import com.spipme.app.domain.model.ResumeAlertes

interface AlerteRepository {
    suspend fun lister(secteurId: Int, statut: String? = null, niveau: String? = null): Resultat<List<Alerte>>

    suspend fun obtenirResume(secteurId: Int): Resultat<ResumeAlertes>

    suspend fun genererPourSecteur(secteurId: Int): Resultat<Pair<List<Alerte>, List<Alerte>>>

    suspend fun resoudre(alerteId: Int): Resultat<Alerte>

    suspend fun ignorer(alerteId: Int): Resultat<Alerte>
}
