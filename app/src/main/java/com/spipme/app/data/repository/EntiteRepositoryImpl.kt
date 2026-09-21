package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.EntiteApi
import com.spipme.app.data.remote.dto.registry.CreerEntiteRequestDto
import com.spipme.app.data.remote.dto.registry.EntiteDto
import com.spipme.app.domain.model.Entite
import com.spipme.app.domain.model.RepartitionParType
import com.spipme.app.domain.model.ResumeRegistre
import com.spipme.app.domain.repository.EntiteRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntiteRepositoryImpl @Inject constructor(
    private val entiteApi: EntiteApi,
    private val json: Json,
) : EntiteRepository {

    override suspend fun lister(secteurId: Int, type: String?, recherche: String?): Resultat<List<Entite>> {
        val resultat = executerAppelApi(json) {
            entiteApi.lister(secteurId = secteurId, type = type, recherche = recherche)
        }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.results.map { it.versDomaine() })
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun obtenirResume(secteurId: Int): Resultat<ResumeRegistre> {
        val resultat = executerAppelApi(json) { entiteApi.resume(secteurId = secteurId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(
                ResumeRegistre(
                    total = resultat.donnees.total,
                    actives = resultat.donnees.actives,
                    parType = resultat.donnees.parType.map { RepartitionParType(it.type, it.total) },
                )
            )
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun creer(
        secteurId: Int, type: String, nom: String, telephone: String?, email: String?, ville: String?,
    ): Resultat<Entite> {
        val resultat = executerAppelApi(json) {
            entiteApi.creer(
                CreerEntiteRequestDto(type = type, nom = nom, telephone = telephone, email = email, ville = ville, secteur = secteurId)
            )
        }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }
}

private fun EntiteDto.versDomaine() = Entite(
    id = id,
    type = type,
    nom = nom,
    telephone = telephone,
    email = email,
    ville = ville,
    pays = pays,
    numeroRccm = numeroRccm,
    numeroFiscal = numeroFiscal,
    statut = statut,
    secteurId = secteur,
    secteurNom = secteurNom,
)
