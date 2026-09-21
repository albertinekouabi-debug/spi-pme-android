package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.HistoriqueStatut
import com.spipme.app.domain.model.ResumeTaches
import com.spipme.app.domain.model.Tache

interface TacheRepository {
    suspend fun lister(secteurId: Int, statut: String? = null, retard: Boolean? = null): Resultat<List<Tache>>

    suspend fun obtenirResume(secteurId: Int): Resultat<ResumeTaches>

    suspend fun obtenirHistorique(tacheId: Int): Resultat<List<HistoriqueStatut>>

    suspend fun creer(
        secteurId: Int,
        titre: String,
        description: String?,
        categorie: String?,
        priorite: String,
        echeanceIso: String?,
    ): Resultat<Tache>

    suspend fun changerStatut(tacheId: Int, nouveauStatut: String): Resultat<Tache>
}
