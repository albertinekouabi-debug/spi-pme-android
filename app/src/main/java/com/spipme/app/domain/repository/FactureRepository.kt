package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.DeclarationConformite
import com.spipme.app.domain.model.Facture

interface FactureRepository {
    suspend fun lister(
        secteurId: Int,
        statut: String? = null,
        page: Int? = null,
    ): Resultat<Pair<List<Facture>, Boolean>>
}

/**
 * ConformitÃ© : les transitions passent par des actions dÃ©diÃ©es (declarer /
 * exempter), jamais par une mise Ã  jour gÃ©nÃ©rique â€” le serveur n'expose
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

