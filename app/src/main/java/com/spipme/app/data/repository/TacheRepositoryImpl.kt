package com.spipme.app.data.repository

import com.spipme.app.core.sync.android.EcritureHorsLigne
import com.spipme.app.core.sync.PREFIXE_ID_LOCAL
import com.spipme.app.core.sync.OperationsMetier
import com.spipme.app.data.remote.dto.registry.PageDto
import com.spipme.app.data.local.CacheLecture
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
    private val ecriture: EcritureHorsLigne,
    private val cacheLecture: CacheLecture,
    private val tacheApi: TacheApi,
    private val json: Json,
) : TacheRepository {

    override suspend fun lister(secteurId: Int, statut: String?, retard: Boolean?): Resultat<List<Tache>> {
        val cle = "taches:$secteurId:${statut ?: "-"}:${retard ?: "-"}"
        val resultat = cacheLecture.lire(cle, PageDto.serializer(TacheDto.serializer())) {
            executerAppelApi(json) {
                tacheApi.lister(secteurId = secteurId, statut = statut, retard = retard?.let { if (it) "true" else null })
            }
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
        val dto = CreerTacheRequestDto(
            titre = titre, description = description, categorie = categorie,
            priorite = priorite, echeance = echeanceIso, secteur = secteurId,
        )
        val payload = OperationsMetier.jsonObjet(
            "titre" to titre, "description" to description, "categorie" to categorie,
            "priorite" to priorite, "echeance" to echeanceIso, "secteur" to secteurId,
        )
        val resultat = ecriture.tenterOuEnfiler(
            cle = ecriture.nouvelleCle(),
            appel = { cle -> executerAppelApi(json) { tacheApi.creer(cle, dto) } },
            operation = { cle ->
                OperationsMetier.creer(
                    type = "CREER_TACHE", entityType = "tache", cheminCollection = "tasks/",
                    payloadJson = payload, refLocale = "$PREFIXE_ID_LOCAL$cle",
                    maintenantMs = System.currentTimeMillis(),
                ) { cle }
            },
        )
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
