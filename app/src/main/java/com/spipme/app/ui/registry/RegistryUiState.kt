package com.spipme.app.ui.registry

import com.spipme.app.domain.model.Entite
import com.spipme.app.domain.model.ResumeRegistre

enum class OngletRegistre(val libelle: String, val typeFiltre: String?) {
    TOUTES("Toutes", null),
    CLIENTS("Clients", "client"),
    FOURNISSEURS("Fournisseurs", "fournisseur"),
    PARTENAIRES("Partenaires", "partenaire"),
}

data class RegistryUiState(
    val enChargement: Boolean = true,
    val entites: List<Entite> = emptyList(),
    val resume: ResumeRegistre? = null,
    val ongletActif: OngletRegistre = OngletRegistre.TOUTES,
    val recherche: String = "",
    val messageErreur: String? = null,
    val secteurActifNom: String = "",
)
