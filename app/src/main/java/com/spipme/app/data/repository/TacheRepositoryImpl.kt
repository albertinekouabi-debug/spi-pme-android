package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.TacheApi
import com.spipme.app.data.remote.dto.tasks.CreerTacheRequestDto
import com.spipme.app.data.remote.dto.tasks.HistoriqueStatutDto
import com.spipme.app.data.remote.dto.tasks.ModifierStatutTacheRequestDto
import com.spipme.app.data.remote.dto.tasks.TacheDto
import com.spipme.app.domain.model.HistoriqueStatut
import com.spipme.app.domain.model.ResumeTaches
import com.spipme.app.domain.model.Tache
import com.spipme.app.domain.repository.TacheRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TacheRepositoryImpl @Inject constructor(
    private val tacheApi: TacheApi,
    private val json: Json,
) : TacheRepository {

    override suspend fun lister(secteurId: Int, statut: String?, retard: Boolean?): Resultat<List<Tache>> {
        val resultat = executerAppelApi(json) {
            tacheApi.lister(secteurId = secteurId, statut = statut, retard = retard?.let { if (it) "true" else null })
        }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.results.map { it.versDomaine() })
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun obtenirResume(secteurId: Int): Resultat<ResumeTaches> {
        val resultat = executerAppelApi(json) { tacheApi.resume(secteurId = secteurId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(
                ResumeTaches(
                    total = resultat.donnees.total,
                    terminees = resultat.donnees.terminees,
                    enCours = resultat.donnees.enCours,
                    enRetard = resultat.donnees.enRetard,
                )
            )
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun obtenirHistorique(tacheId: Int): Resultat<List<HistoriqueStatut>> {
        val resultat = executerAppelApi(json) { tacheApi.historique(tacheId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.map { it.versDomaine() })
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun creer(
        secteurId: Int, titre: String, description: String?, categorie: String?, priorite: String, echeanceIso: String?,
    ): Resultat<Tache> {
        val resultat = executerAppelApi(json) {
            tacheApi.creer(
                CreerTacheRequestDto(
                    titre = titre, description = description, categorie = categorie,
                    priorite = priorite, echeance = echeanceIso, secteur = secteurId,
                )
            )
        }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun changerStatut(tacheId: Int, nouveauStatut: String): Resultat<Tache> {
        val resultat = executerAppelApi(json) {
            tacheApi.modifierStatut(tacheId, ModifierStatutTacheRequestDto(statut = nouveauStatut))
        }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }
}

private fun TacheDto.versDomaine() = Tache(
    id = id,
    titre = titre,
    description = description,
    categorie = categorie,
    priorite = priorite,
    statut = statut,
    lieu = lieu,
    assigneeId = assignee,
    assigneeNom = assigneeNom,
    echeance = echeance,
    secteurId = secteur,
    enRetard = enRetard,
)

private fun HistoriqueStatutDto.versDomaine() = HistoriqueStatut(
    id = id,
    ancienStatut = ancienStatut,
    nouveauStatut = nouveauStatut,
    auteurNom = auteurNom,
    dateChangement = dateChangement,
)
