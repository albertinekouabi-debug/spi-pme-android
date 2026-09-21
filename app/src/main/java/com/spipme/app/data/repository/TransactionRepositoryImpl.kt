package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.TransactionApi
import com.spipme.app.data.remote.dto.treasury.CreerTransactionRequestDto
import com.spipme.app.data.remote.dto.treasury.TransactionDto
import com.spipme.app.domain.model.PointSolde
import com.spipme.app.domain.model.ResumeTresorerie
import com.spipme.app.domain.model.Transaction
import com.spipme.app.domain.repository.TransactionRepository
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionApi: TransactionApi,
    private val json: Json,
) : TransactionRepository {

    override suspend fun lister(secteurId: Int, type: String?): Resultat<List<Transaction>> {
        val resultat = executerAppelApi(json) { transactionApi.lister(secteurId = secteurId, type = type) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.results.map { it.versDomaine() })
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun obtenirResume(secteurId: Int): Resultat<ResumeTresorerie> {
        val resultat = executerAppelApi(json) { transactionApi.resume(secteurId = secteurId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(
                ResumeTresorerie(
                    entreesMois = BigDecimal(resultat.donnees.entreesMois),
                    sortiesMois = BigDecimal(resultat.donnees.sortiesMois),
                    soldeNetMois = BigDecimal(resultat.donnees.soldeNetMois),
                    soldeDisponible = BigDecimal(resultat.donnees.soldeDisponible),
                )
            )
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun obtenirEvolution(secteurId: Int, jours: Int): Resultat<List<PointSolde>> {
        val resultat = executerAppelApi(json) { transactionApi.evolution(secteurId = secteurId, jours = jours) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.map { PointSolde(it.date, BigDecimal(it.solde)) })
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun creer(
        secteurId: Int, type: String, montant: BigDecimal?, modePaiement: String?,
        description: String?, dateTransactionIso: String,
    ): Resultat<Transaction> {
        val resultat = executerAppelApi(json) {
            transactionApi.creer(
                CreerTransactionRequestDto(
                    type = type, montant = montant?.toPlainString(), modePaiement = modePaiement,
                    description = description, secteur = secteurId, dateTransaction = dateTransactionIso,
                )
            )
        }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }
}

private fun TransactionDto.versDomaine() = Transaction(
    id = id,
    type = type,
    reference = reference,
    description = description,
    montant = montant?.let { BigDecimal(it) },
    quantite = quantite?.let { BigDecimal(it) },
    devise = devise,
    modePaiement = modePaiement,
    entiteId = entite,
    entiteNom = entiteNom,
    ressourceId = ressource,
    ressourceNom = ressourceNom,
    secteurId = secteur,
    dateTransaction = dateTransaction,
)
