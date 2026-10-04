package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.DeclarationConformite
import com.spipme.app.domain.model.Facture
import kotlinx.coroutines.flow.Flow

interface FactureRepository {
    /** Hors ligne (et seulement si le réseau est la cause de l'échec) : sert la dernière page lue en cache. */
    suspend fun lister(
        secteurId: Int,
        statut: String? = null,
        page: Int? = null,
    ): Resultat<Pair<List<Facture>, Boolean>>

    /**
     * Annulation = écriture append-only (avoir total côté serveur), jamais une suppression. Enfilée
     * localement puis synchronisée avec une clé d'idempotence : fonctionne hors ligne. Le serveur
     * revalide (droits, état, période clôturée) ; un refus remonte comme opération en échec.
     */
    suspend fun annuler(factureId: Int, motif: String): Resultat<Unit>

    /** Ids des factures ayant une écriture en attente de synchronisation. */
    fun idsAvecOperationEnAttente(): Flow<Set<Int>>
}

/**
 * Conformité : les transitions passent par des actions dédiées (declarer /
 * exempter), jamais par une mise à jour générique — le serveur n'expose
 * volontairement aucun champ modifiable.
 */
interface ConformiteRepository {
    suspend fun lister(
        secteurId: Int,
        statut: String? = null,
        page: Int? = null,
    ): Resultat<Pair<List<DeclarationConformite>, Boolean>>

    suspend fun declarer(id: Int, referenceDeclaration: String): Resultat<DeclarationConformite>

    suspend fun exempter(id: Int, note: String): Resultat<DeclarationConformite>
}

