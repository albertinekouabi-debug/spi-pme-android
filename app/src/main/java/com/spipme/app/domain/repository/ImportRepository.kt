package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.ApercuImport
import com.spipme.app.domain.model.FichierSelectionne
import com.spipme.app.domain.model.ImportFichier

interface ImportRepository {
    suspend fun obtenirHistorique(secteurId: Int): Resultat<List<ImportFichier>>

    suspend fun previsualiser(secteurId: Int, fichier: FichierSelectionne): Resultat<ApercuImport>

    suspend fun importer(secteurId: Int, fichier: FichierSelectionne): Resultat<ImportFichier>
}
