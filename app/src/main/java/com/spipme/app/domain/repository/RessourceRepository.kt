package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.PointEvolution
import com.spipme.app.domain.model.ResumeRessources
import com.spipme.app.domain.model.Ressource
import java.math.BigDecimal

interface RessourceRepository {
    suspend fun lister(secteurId: Int, statut: String? = null, recherche: String? = null): Resultat<List<Ressource>>

    suspend fun obtenirResume(secteurId: Int): Resultat<ResumeRessources>

    suspend fun obtenirEvolution(ressourceId: Int, jours: Int = 7): Resultat<List<PointEvolution>>

    /**
     * Modification (nom, emplacement, seuils) avec contrôle de version : refusée par le serveur (412) si la
     * ressource a changé depuis la lecture. Hors ligne, enfilée avec la version lue. Le NIVEAU de stock ne se
     * modifie jamais directement : il ne bouge que par mouvements de stock tracés.
     */
    suspend fun modifier(
        ressource: Ressource, nom: String, emplacement: String?, seuilCritique: BigDecimal?, seuilAlerte: BigDecimal?,
    ): Resultat<Ressource>

    suspend fun creer(
        secteurId: Int,
        type: String,
        nom: String,
        unite: String?,
        valeurUnitaire: BigDecimal?,
        niveauActuel: BigDecimal,
        seuilCritique: BigDecimal?,
        seuilAlerte: BigDecimal?,
        emplacement: String?,
    ): Resultat<Ressource>
}
