package com.spipme.app.ui.navigation

sealed class Ecran(val route: String) {
    data object Connexion : Ecran("connexion")
    data object ConnexionHorsLigne : Ecran("connexion_hors_ligne")
    data object Inscription : Ecran("inscription")
    data object MotDePasseOublie : Ecran("mot_de_passe_oublie")
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
    data object Audit : Ecran("audit")
    data object Factures : Ecran("factures")
    data object Conformite : Ecran("conformite")
    data object Profil : Ecran("profil")
    data object Administration : Ecran("administration")
    data object Dashboard : Ecran("dashboard")
    data object Pilotage : Ecran("pilotage")
}

