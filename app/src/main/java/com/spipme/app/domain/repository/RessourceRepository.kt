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
