package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.ResumeSuggestions
import com.spipme.app.domain.model.Suggestion

interface SuggestionRepository {
    suspend fun lister(secteurId: Int, statut: String? = null): Resultat<List<Suggestion>>

    suspend fun obtenirResume(secteurId: Int): Resultat<ResumeSuggestions>

    suspend fun genererPourSecteur(secteurId: Int): Resultat<List<Suggestion>>

    suspend fun valider(suggestionId: Int): Resultat<Suggestion>

    suspend fun rejeter(suggestionId: Int, motif: String): Resultat<Suggestion>
}
