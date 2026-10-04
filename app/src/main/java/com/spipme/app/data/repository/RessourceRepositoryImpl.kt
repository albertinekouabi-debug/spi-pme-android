package com.spipme.app.data.repository

import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.spipme.app.core.sync.android.EcritureHorsLigne
import com.spipme.app.core.sync.PREFIXE_ID_LOCAL
import com.spipme.app.core.sync.OperationsMetier
import com.spipme.app.data.remote.dto.registry.PageDto
import com.spipme.app.data.local.CacheLecture
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
    private val ecriture: EcritureHorsLigne,
    private val cacheLecture: CacheLecture,
    private val ressourceApi: RessourceApi,
    private val json: Json,
) : RessourceRepository {

    override suspend fun lister(secteurId: Int, statut: String?, recherche: String?): Resultat<List<Ressource>> {
        val cle = "ressources:$secteurId:${statut ?: "-"}:${recherche ?: "-"}"
        val resultat = cacheLecture.lire(cle, PageDto.serializer(RessourceDto.serializer())) {
            executerAppelApi(json) {
                ressourceApi.lister(secteurId = secteurId, statut = statut, recherche = recherche)
            }
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

    override suspend fun modifier(
        ressource: Ressource, nom: String, emplacement: String?, seuilCritique: BigDecimal?, seuilAlerte: BigDecimal?,
    ): Resultat<Ressource> {
        // Un seul payload pour l'appel direct ET pour la file hors ligne.
        val payload = OperationsMetier.jsonObjet(
            "nom" to nom, "emplacement" to emplacement,
            "seuil_critique" to seuilCritique?.toPlainString(), "seuil_alerte" to seuilAlerte?.toPlainString(),
        )
        val resultat = ecriture.tenterOuEnfiler(
            cle = ecriture.nouvelleCle(),
            appel = { _ ->
                executerAppelApi(json) {
                    ressourceApi.modifier(
                        id = ressource.id, ifMatch = "\"${ressource.version}\"",
                        corps = payload.toRequestBody("application/json".toMediaType()),
                    )
                }
            },
            operation = { cle ->
                OperationsMetier.modifier(
                    type = "MODIFIER_RESSOURCE", entityType = "ressource", entityId = ressource.id.toString(),
                    chemin = "resources/${ressource.id}/", payloadJson = payload, versionBase = ressource.version,
                    maintenantMs = System.currentTimeMillis(),
                ) { cle }
            },
        )
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun creer(
        secteurId: Int, type: String, nom: String, unite: String?, valeurUnitaire: BigDecimal?,
        niveauActuel: BigDecimal, seuilCritique: BigDecimal?, seuilAlerte: BigDecimal?, emplacement: String?,
    ): Resultat<Ressource> {
        val dto = CreerRessourceRequestDto(
            type = type, nom = nom, unite = unite, valeurUnitaire = valeurUnitaire?.toPlainString(),
            niveauActuel = niveauActuel.toPlainString(), seuilCritique = seuilCritique?.toPlainString(),
            seuilAlerte = seuilAlerte?.toPlainString(), emplacement = emplacement, secteur = secteurId,
        )
        val payload = OperationsMetier.jsonObjet(
            "type" to type, "nom" to nom, "unite" to unite, "valeur_unitaire" to valeurUnitaire?.toPlainString(),
            "niveau_actuel" to niveauActuel.toPlainString(), "seuil_critique" to seuilCritique?.toPlainString(),
            "seuil_alerte" to seuilAlerte?.toPlainString(), "emplacement" to emplacement, "secteur" to secteurId,
        )
        val resultat = ecriture.tenterOuEnfiler(
            cle = ecriture.nouvelleCle(),
            appel = { cle -> executerAppelApi(json) { ressourceApi.creer(cle, dto) } },
            operation = { cle ->
                OperationsMetier.creer(
                    type = "CREER_RESSOURCE", entityType = "ressource", cheminCollection = "resources/",
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
    version = version,
)
