package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.AlerteApi
import com.spipme.app.data.remote.dto.alerts.AlerteDto
import com.spipme.app.data.remote.dto.alerts.GenererAlertesRequestDto
import com.spipme.app.domain.model.Alerte
import com.spipme.app.domain.model.ResumeAlertes
import com.spipme.app.domain.repository.AlerteRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlerteRepositoryImpl @Inject constructor(
    private val alerteApi: AlerteApi,
    private val json: Json,
) : AlerteRepository {

    override suspend fun lister(secteurId: Int, statut: String?, niveau: String?): Resultat<List<Alerte>> {
        val resultat = executerAppelApi(json) {
            alerteApi.lister(secteurId = secteurId, statut = statut, niveau = niveau)
        }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.results.map { it.versDomaine() })
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun obtenirResume(secteurId: Int): Resultat<ResumeAlertes> {
        val resultat = executerAppelApi(json) { alerteApi.resume(secteurId = secteurId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(
                ResumeAlertes(
                    critiques = resultat.donnees.critiques,
                    elevees = resultat.donnees.elevees,
                    moderees = resultat.donnees.moderees,
                    resolues = resultat.donnees.resolues,
                )
            )
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun genererPourSecteur(secteurId: Int): Resultat<Pair<List<Alerte>, List<Alerte>>> {
        val resultat = executerAppelApi(json) { alerteApi.generer(GenererAlertesRequestDto(secteur = secteurId)) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(
                resultat.donnees.creees.map { it.versDomaine() } to resultat.donnees.resoluesAutomatiquement.map { it.versDomaine() }
            )
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun resoudre(alerteId: Int): Resultat<Alerte> {
        val resultat = executerAppelApi(json) { alerteApi.resoudre(alerteId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun ignorer(alerteId: Int): Resultat<Alerte> {
        val resultat = executerAppelApi(json) { alerteApi.ignorer(alerteId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }
}

private fun AlerteDto.versDomaine() = Alerte(
    id = id,
    type = type,
    niveau = niveau,
    titre = titre,
    description = description,
    statut = statut,
    ressourceNom = ressourceNom,
    entiteNom = entiteNom,
    tacheTitre = tacheTitre,
    factureNumero = factureNumero,
    secteurId = secteur,
    dateDeclenchement = dateDeclenchement,
    dateResolution = dateResolution,
)
