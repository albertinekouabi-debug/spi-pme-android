package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Entite
import com.spipme.app.domain.model.ResumeRegistre

interface EntiteRepository {
    suspend fun lister(secteurId: Int, type: String? = null, recherche: String? = null): Resultat<List<Entite>>

    /**
     * Le backend n'expose pas d'endpoint /entities/summary/ dédié (à la
     * différence de resources/tasks/alerts/suggestions) — Entite.type est un
     * champ libre configurable par secteur (§10.2 du CDC backend), donc un
     * résumé générique par type n'a pas de sens universel. Ici, on lit
     * simplement le champ `count` de la pagination DRF pour les 3 types
     * visibles dans la maquette Commerce (client/fournisseur/partenaire) —
     * 4 requêtes légères plutôt qu'un vrai résumé serveur. À rationaliser
     * avec un endpoint dédié si ce module reste dans ce périmètre.
     */
    suspend fun obtenirResume(secteurId: Int): Resultat<ResumeRegistre>

    suspend fun creer(
        secteurId: Int,
        type: String,
        nom: String,
        telephone: String?,
        email: String?,
        ville: String?,
    ): Resultat<Entite>
}
