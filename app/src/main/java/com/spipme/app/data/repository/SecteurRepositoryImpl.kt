package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.SecteurApi
import com.spipme.app.domain.model.Secteur
import com.spipme.app.domain.repository.SecteurRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecteurRepositoryImpl @Inject constructor(
    private val secteurApi: SecteurApi,
    private val json: Json,
) : SecteurRepository {

    override suspend fun mesSecteurs(): Resultat<List<Secteur>> {
        val reponse = executerAppelApi(json) { secteurApi.mesSecteurs() }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(
                reponse.donnees.results.map { Secteur(id = it.id, code = it.code, nom = it.nom) }
            )
            is Resultat.Echec -> reponse
        }
    }
}

