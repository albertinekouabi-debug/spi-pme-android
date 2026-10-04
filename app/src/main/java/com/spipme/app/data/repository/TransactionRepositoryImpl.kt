package com.spipme.app.data.repository

import kotlinx.coroutines.flow.map
import com.spipme.app.core.sync.android.SynchronisationRepository
import com.spipme.app.core.sync.android.EcritureHorsLigne
import com.spipme.app.core.sync.PREFIXE_ID_LOCAL
import com.spipme.app.core.sync.OperationsMetier
import com.spipme.app.data.remote.dto.registry.PageDto
import com.spipme.app.data.local.CacheLecture
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
    private val synchronisation: SynchronisationRepository,
    private val ecriture: EcritureHorsLigne,
    private val cacheLecture: CacheLecture,
    private val transactionApi: TransactionApi,
    private val json: Json,
) : TransactionRepository {

    override suspend fun lister(secteurId: Int, type: String?): Resultat<List<Transaction>> {
        val cle = "transactions:$secteurId:${type ?: "-"}"
        val resultat = cacheLecture.lire(cle, PageDto.serializer(TransactionDto.serializer())) {
            executerAppelApi(json) { transactionApi.lister(secteurId = secteurId, type = type) }
        }
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

    override suspend fun contrePasser(transactionId: Int, motif: String): Resultat<Unit> {
        if (motif.isBlank()) return Resultat.Echec("Le motif de la contre-écriture est obligatoire.")
        synchronisation.enfiler(
            OperationsMetier.contrePasserTransaction(transactionId.toString(), motif.trim(), System.currentTimeMillis()) {
                java.util.UUID.randomUUID().toString()
            }
        )
        return Resultat.Succes(Unit)
    }

    override fun idsAvecOperationEnAttente(): kotlinx.coroutines.flow.Flow<Set<Int>> =
        synchronisation.entitesEnAttente("transaction").map { ids -> ids.mapNotNull { it.toIntOrNull() }.toSet() }

    override suspend fun creer(
        secteurId: Int, type: String, montant: BigDecimal?, modePaiement: String?,
        description: String?, dateTransactionIso: String,
    ): Resultat<Transaction> {
        val dto = CreerTransactionRequestDto(
            type = type, montant = montant?.toPlainString(), modePaiement = modePaiement,
            description = description, secteur = secteurId, dateTransaction = dateTransactionIso,
        )
        val payload = OperationsMetier.jsonObjet(
            "type" to type, "montant" to montant?.toPlainString(), "mode_paiement" to modePaiement,
            "description" to description, "secteur" to secteurId, "date_transaction" to dateTransactionIso,
        )
        val resultat = ecriture.tenterOuEnfiler(
            cle = ecriture.nouvelleCle(),
            appel = { cle -> executerAppelApi(json) { transactionApi.creer(cle, dto) } },
            operation = { cle ->
                OperationsMetier.creer(
                    type = "CREER_TRANSACTION", entityType = "transaction", cheminCollection = "transactions/",
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
    statut = statut,
    contreEcritureDe = contreEcritureDe,
    version = version,
)
