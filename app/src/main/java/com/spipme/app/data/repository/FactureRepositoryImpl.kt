package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.ConformiteApi
import com.spipme.app.data.remote.api.FactureApi
import com.spipme.app.data.remote.dto.treasury.DeclarationConformiteDto
import com.spipme.app.data.remote.dto.treasury.DeclarerRequestDto
import com.spipme.app.data.remote.dto.treasury.ExempterRequestDto
import com.spipme.app.data.remote.dto.treasury.FactureDto
import com.spipme.app.domain.model.DeclarationConformite
import com.spipme.app.domain.model.Facture
import com.spipme.app.domain.repository.ConformiteRepository
import com.spipme.app.domain.repository.FactureRepository
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FactureRepositoryImpl @Inject constructor(
    private val factureApi: FactureApi,
    private val json: Json,
) : FactureRepository {

    override suspend fun lister(
        secteurId: Int,
        statut: String?,
        page: Int?,
    ): Resultat<Pair<List<Facture>, Boolean>> {
        val reponse = executerAppelApi(json) {
            factureApi.lister(secteurId = secteurId, statut = statut, page = page)
        }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(
                reponse.donnees.results.map { it.versDomaine() } to (reponse.donnees.next != null)
            )
            is Resultat.Echec -> reponse
        }
    }
}

@Singleton
class ConformiteRepositoryImpl @Inject constructor(
    private val conformiteApi: ConformiteApi,
    private val json: Json,
) : ConformiteRepository {

    override suspend fun lister(
        secteurId: Int,
        statut: String?,
        page: Int?,
    ): Resultat<Pair<List<DeclarationConformite>, Boolean>> {
        val reponse = executerAppelApi(json) {
            conformiteApi.lister(secteurId = secteurId, statut = statut, page = page)
        }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(
                reponse.donnees.results.map { it.versDomaine() } to (reponse.donnees.next != null)
            )
            is Resultat.Echec -> reponse
        }
    }

    override suspend fun declarer(id: Int, referenceDeclaration: String): Resultat<DeclarationConformite> {
        val reponse = executerAppelApi(json) {
            conformiteApi.declarer(id, DeclarerRequestDto(referenceDeclaration = referenceDeclaration))
        }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(reponse.donnees.versDomaine())
            is Resultat.Echec -> reponse
        }
    }

    override suspend fun exempter(id: Int, note: String): Resultat<DeclarationConformite> {
        val reponse = executerAppelApi(json) {
            conformiteApi.exempter(id, ExempterRequestDto(note = note))
        }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(reponse.donnees.versDomaine())
            is Resultat.Echec -> reponse
        }
    }
}

/** DRF sÃ©rialise les Decimal en chaÃ®ne : conversion dÃ©fensive, jamais de crash sur une valeur inattendue. */
private fun String?.versBigDecimal(): BigDecimal? = this?.toBigDecimalOrNull()

private fun FactureDto.versDomaine() = Facture(
    id = id,
    numero = numero,
    entiteNom = entiteNom,
    montant = montant.versBigDecimal() ?: BigDecimal.ZERO,
    tauxTva = tauxTva.versBigDecimal() ?: BigDecimal.ZERO,
    montantTva = montantTva.versBigDecimal() ?: BigDecimal.ZERO,
    montantTtc = montantTtc.versBigDecimal() ?: BigDecimal.ZERO,
    statut = statut,
    dateEcheance = dateEcheance,
    dateDerniereRelance = dateDerniereRelance,
    dateCreation = dateCreation,
)

private fun DeclarationConformiteDto.versDomaine() = DeclarationConformite(
    id = id,
    transactionReference = transactionReference,
    transactionMontant = transactionMontant.versBigDecimal(),
    motif = motif,
    seuilApplique = seuilApplique.versBigDecimal(),
    statut = statut,
    referenceDeclaration = referenceDeclaration,
    note = note,
    declarantNom = declarantNom,
    dateDetection = dateDetection,
    dateDeclaration = dateDeclaration,
)

