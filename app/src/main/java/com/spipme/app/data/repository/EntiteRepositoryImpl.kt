package com.spipme.app.data.repository

import com.spipme.app.core.sync.android.EcritureHorsLigne
import com.spipme.app.core.sync.PREFIXE_ID_LOCAL
import com.spipme.app.core.sync.OperationsMetier
import com.spipme.app.data.remote.dto.registry.PageDto
import com.spipme.app.data.local.CacheLecture
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
    private val ecriture: EcritureHorsLigne,
    private val cacheLecture: CacheLecture,
    private val entiteApi: EntiteApi,
    private val json: Json,
) : EntiteRepository {

    override suspend fun lister(secteurId: Int, type: String?, recherche: String?): Resultat<List<Entite>> {
        val cle = "entites:$secteurId:${type ?: "-"}:${recherche ?: "-"}"
        val resultat = cacheLecture.lire(cle, PageDto.serializer(EntiteDto.serializer())) {
            executerAppelApi(json) {
                entiteApi.lister(secteurId = secteurId, type = type, recherche = recherche)
            }
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
        val dto = CreerEntiteRequestDto(
            type = type, nom = nom, telephone = telephone, email = email, ville = ville, secteur = secteurId,
        )
        val payload = OperationsMetier.jsonObjet(
            "type" to type, "nom" to nom, "telephone" to telephone, "email" to email, "ville" to ville, "secteur" to secteurId,
        )
        val resultat = ecriture.tenterOuEnfiler(
            cle = ecriture.nouvelleCle(),
            appel = { cle -> executerAppelApi(json) { entiteApi.creer(cle, dto) } },
            operation = { cle ->
                OperationsMetier.creer(
                    type = "CREER_ENTITE", entityType = "entite", cheminCollection = "entities/",
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
