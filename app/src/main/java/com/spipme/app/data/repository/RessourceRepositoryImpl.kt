package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.RessourceApi
import com.spipme.app.data.remote.dto.resources.CreerRessourceRequestDto
import com.spipme.app.data.remote.dto.resources.RessourceDto
import com.spipme.app.domain.model.PointEvolution
import com.spipme.app.domain.model.ResumeRessources
import com.spipme.app.domain.model.Ressource
import com.spipme.app.domain.repository.RessourceRepository
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RessourceRepositoryImpl @Inject constructor(
    private val ressourceApi: RessourceApi,
    private val json: Json,
) : RessourceRepository {

    override suspend fun lister(secteurId: Int, statut: String?, recherche: String?): Resultat<List<Ressource>> {
        val resultat = executerAppelApi(json) {
            ressourceApi.lister(secteurId = secteurId, statut = statut, recherche = recherche)
        }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.results.map { it.versDomaine() })
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun obtenirResume(secteurId: Int): Resultat<ResumeRessources> {
        val resultat = executerAppelApi(json) { ressourceApi.resume(secteurId = secteurId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(
                ResumeRessources(
                    total = resultat.donnees.total,
                    critiques = resultat.donnees.critiques,
                    aSurveiller = resultat.donnees.aSurveiller,
                    stables = resultat.donnees.stables,
                )
            )
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun obtenirEvolution(ressourceId: Int, jours: Int): Resultat<List<PointEvolution>> {
        val resultat = executerAppelApi(json) { ressourceApi.evolution(ressourceId, jours) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(
                resultat.donnees.map { PointEvolution(it.date, BigDecimal(it.niveau)) }
            )
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun creer(
        secteurId: Int, type: String, nom: String, unite: String?, valeurUnitaire: BigDecimal?,
        niveauActuel: BigDecimal, seuilCritique: BigDecimal?, seuilAlerte: BigDecimal?, emplacement: String?,
    ): Resultat<Ressource> {
        val resultat = executerAppelApi(json) {
            ressourceApi.creer(
                CreerRessourceRequestDto(
                    type = type, nom = nom, unite = unite,
                    valeurUnitaire = valeurUnitaire?.toPlainString(),
                    niveauActuel = niveauActuel.toPlainString(),
                    seuilCritique = seuilCritique?.toPlainString(),
                    seuilAlerte = seuilAlerte?.toPlainString(),
                    emplacement = emplacement, secteur = secteurId,
                )
            )
        }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }
}

private fun RessourceDto.versDomaine() = Ressource(
    id = id,
    type = type,
    nom = nom,
    unite = unite,
    valeurUnitaire = valeurUnitaire?.let { BigDecimal(it) },
    niveauActuel = BigDecimal(niveauActuel),
    seuilCritique = seuilCritique?.let { BigDecimal(it) },
    seuilAlerte = seuilAlerte?.let { BigDecimal(it) },
    statut = statut,
    emplacement = emplacement,
    secteurId = secteur,
    secteurNom = secteurNom,
)
