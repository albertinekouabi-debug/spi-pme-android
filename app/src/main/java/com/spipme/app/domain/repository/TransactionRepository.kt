package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.PointSolde
import com.spipme.app.domain.model.ResumeTresorerie
import com.spipme.app.domain.model.Transaction
import java.math.BigDecimal

interface TransactionRepository {
    suspend fun lister(secteurId: Int, type: String? = null): Resultat<List<Transaction>>

    suspend fun obtenirResume(secteurId: Int): Resultat<ResumeTresorerie>

    suspend fun obtenirEvolution(secteurId: Int, jours: Int = 7): Resultat<List<PointSolde>>

    /**
     * Correction d'une transaction VALIDÉE : contre-écriture (jamais une modification ni une suppression).
     * Enfilée puis synchronisée avec clé d'idempotence ; le serveur revalide droits, état et période close.
     */
    suspend fun contrePasser(transactionId: Int, motif: String): Resultat<Unit>

    fun idsAvecOperationEnAttente(): kotlinx.coroutines.flow.Flow<Set<Int>>

    suspend fun creer(
        secteurId: Int,
        type: String,
        montant: BigDecimal?,
        modePaiement: String?,
        description: String?,
        dateTransactionIso: String,
    ): Resultat<Transaction>
}
