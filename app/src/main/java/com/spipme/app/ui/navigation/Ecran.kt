package com.spipme.app.ui.navigation

sealed class Ecran(val route: String) {
    data object Connexion : Ecran("connexion")
    data object ConnexionHorsLigne : Ecran("connexion_hors_ligne")
    data object Inscription : Ecran("inscription")
    data object Accueil : Ecran("accueil")
    data object Registre : Ecran("registre")
    data object CreationEntite : Ecran("registre/creation")
    data object Ressources : Ecran("ressources")
    data object CreationRessource : Ecran("ressources/creation")
    data object Tresorerie : Ecran("tresorerie")
    data object CreationTransaction : Ecran("tresorerie/creation")
    data object Taches : Ecran("taches")
    data object CreationTache : Ecran("taches/creation")
    data object Suggestions : Ecran("suggestions")
    data object Alertes : Ecran("alertes")
    data object Imports : Ecran("imports")
    // Écrans suivants ajoutés module par module (Audit...).
}
