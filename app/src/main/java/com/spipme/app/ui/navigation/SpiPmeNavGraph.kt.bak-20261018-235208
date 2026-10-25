package com.spipme.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spipme.app.ui.auth.login.LoginScreen
import com.spipme.app.ui.pilotage.PilotageScreen
import com.spipme.app.ui.auth.motdepasseoublie.MotDePasseOublieScreen
import com.spipme.app.ui.auth.offline.OfflineLoginScreen
import com.spipme.app.ui.audit.AuditScreen
import com.spipme.app.ui.compliance.ComplianceScreen
import com.spipme.app.ui.admin.AdminScreen
import com.spipme.app.ui.dashboard.DashboardScreen
import com.spipme.app.ui.profile.ProfileScreen
import com.spipme.app.ui.invoices.InvoicesScreen
import com.spipme.app.ui.auth.register.RegisterScreen
import com.spipme.app.ui.home.HomeScreen
import com.spipme.app.ui.registry.RegistryScreen
import com.spipme.app.ui.registry.creation.CreerEntiteScreen
import com.spipme.app.ui.resources.ResourcesScreen
import com.spipme.app.ui.resources.creation.CreerRessourceScreen
import com.spipme.app.ui.treasury.TreasuryScreen
import com.spipme.app.ui.treasury.creation.CreerTransactionScreen
import com.spipme.app.ui.tasks.TasksScreen
import com.spipme.app.ui.tasks.creation.CreerTacheScreen
import com.spipme.app.ui.intelligence.IntelligenceScreen
import com.spipme.app.ui.alerts.AlertsScreen
import com.spipme.app.ui.imports.ImportsScreen
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SpiPmeNavGraph(
    navController: NavHostController = rememberNavController(),
    ecranDepart: String = Ecran.Connexion.route,
) {
    NavHost(navController = navController, startDestination = ecranDepart) {
        composable(Ecran.Connexion.route) {
            LoginScreen(
                surConnexionReussie = {
                    navController.navigate(Ecran.Accueil.route) {
                        popUpTo(Ecran.Connexion.route) { inclusive = true }
                    }
                },
                surClicConnexionHorsLigne = { navController.navigate(Ecran.ConnexionHorsLigne.route) },
                surClicInscription = { navController.navigate(Ecran.Inscription.route) },
                surClicMotDePasseOublie = { navController.navigate(Ecran.MotDePasseOublie.route) },
            )
        }

        composable(Ecran.Inscription.route) {
            RegisterScreen(
                surRetourConnexion = { navController.popBackStack() },
            )
        }

        composable(Ecran.MotDePasseOublie.route) {
            MotDePasseOublieScreen(
                surRetour = { navController.popBackStack() },
                surReinitialisationReussie = {
                    navController.popBackStack(Ecran.Connexion.route, inclusive = false)
                },
            )
        }

        composable(Ecran.ConnexionHorsLigne.route) {
            OfflineLoginScreen(
                surRetour = { navController.popBackStack() },
                surConnexionReussie = {
                    navController.navigate(Ecran.Accueil.route) {
                        popUpTo(Ecran.Connexion.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Ecran.Accueil.route) {
            HomeScreen(
                surDeconnexion = {
                    navController.navigate(Ecran.Connexion.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                surClicRegistre = { navController.navigate(Ecran.Registre.route) },
                surClicRessources = { navController.navigate(Ecran.Ressources.route) },
                surClicTresorerie = { navController.navigate(Ecran.Tresorerie.route) },
                surClicTaches = { navController.navigate(Ecran.Taches.route) },
                surClicSuggestions = { navController.navigate(Ecran.Suggestions.route) },
                surClicAlertes = { navController.navigate(Ecran.Alertes.route) },
                surClicImports = { navController.navigate(Ecran.Imports.route) },
                surClicAudit = { navController.navigate(Ecran.Audit.route) },
                surClicFactures = { navController.navigate(Ecran.Factures.route) },
                surClicConformite = { navController.navigate(Ecran.Conformite.route) },
                surClicAdministration = { navController.navigate(Ecran.Administration.route) },
                surClicDashboard = { navController.navigate(Ecran.Dashboard.route) },
                surClicPilotage = { navController.navigate(Ecran.Pilotage.route) },
            )
        }

        composable(Ecran.Pilotage.route) {
            PilotageScreen(
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* le sélecteur de secteur s'ouvre depuis la barre supérieure */ },
            )
        }

        composable(Ecran.Registre.route) { backStackEntry ->
            val entiteCreeeFlow = backStackEntry.savedStateHandle.getStateFlow("entite_creee", false)
            val entiteCreee by entiteCreeeFlow.collectAsStateWithLifecycle()

            RegistryScreen(
                entiteVientDetreCreee = entiteCreee,
                surEntiteCreeeConsommee = { backStackEntry.savedStateHandle["entite_creee"] = false },
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
                surClicNouvelleEntite = { navController.navigate(Ecran.CreationEntite.route) },
            )
        }

        composable(Ecran.CreationEntite.route) {
            CreerEntiteScreen(
                surRetour = { navController.popBackStack() },
                surCreationReussie = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("entite_creee", true)
                    navController.popBackStack()
                },
            )
        }

        composable(Ecran.Ressources.route) { backStackEntry ->
            val ressourceCreeeFlow = backStackEntry.savedStateHandle.getStateFlow("ressource_creee", false)
            val ressourceCreee by ressourceCreeeFlow.collectAsStateWithLifecycle()

            ResourcesScreen(
                ressourceVientDetreCreee = ressourceCreee,
                surRessourceCreeeConsommee = { backStackEntry.savedStateHandle["ressource_creee"] = false },
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
                surClicNouvelleRessource = { navController.navigate(Ecran.CreationRessource.route) },
            )
        }

        composable(Ecran.CreationRessource.route) {
            CreerRessourceScreen(
                surRetour = { navController.popBackStack() },
                surCreationReussie = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("ressource_creee", true)
                    navController.popBackStack()
                },
            )
        }

        composable(Ecran.Tresorerie.route) { backStackEntry ->
            val transactionCreeeFlow = backStackEntry.savedStateHandle.getStateFlow("transaction_creee", false)
            val transactionCreee by transactionCreeeFlow.collectAsStateWithLifecycle()

            TreasuryScreen(
                transactionVientDetreCreee = transactionCreee,
                surTransactionCreeeConsommee = { backStackEntry.savedStateHandle["transaction_creee"] = false },
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
                surClicNouvelleTransaction = { navController.navigate(Ecran.CreationTransaction.route) },
            )
        }

        composable(Ecran.CreationTransaction.route) {
            CreerTransactionScreen(
                surRetour = { navController.popBackStack() },
                surCreationReussie = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("transaction_creee", true)
                    navController.popBackStack()
                },
            )
        }

        composable(Ecran.Taches.route) { backStackEntry ->
            val tacheCreeeFlow = backStackEntry.savedStateHandle.getStateFlow("tache_creee", false)
            val tacheCreee by tacheCreeeFlow.collectAsStateWithLifecycle()

            TasksScreen(
                tacheVientDetreCreee = tacheCreee,
                surTacheCreeeConsommee = { backStackEntry.savedStateHandle["tache_creee"] = false },
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
                surClicNouvelleTache = { navController.navigate(Ecran.CreationTache.route) },
            )
        }

        composable(Ecran.CreationTache.route) {
            CreerTacheScreen(
                surRetour = { navController.popBackStack() },
                surCreationReussie = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("tache_creee", true)
                    navController.popBackStack()
                },
            )
        }

        composable(Ecran.Suggestions.route) {
            IntelligenceScreen(
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Alertes.route) {
            AlertsScreen(
                surClicNotifications = {},  // déjà sur l'écran Alertes : rien à faire
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Imports.route) {
            ImportsScreen(
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Audit.route) {
            AuditScreen(
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Factures.route) {
            InvoicesScreen(
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Conformite.route) {
            ComplianceScreen(
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Profil.route) {
            ProfileScreen(
                surRetour = { navController.popBackStack() },
                surDeconnexion = {
                    navController.navigate(Ecran.Connexion.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Ecran.Administration.route) {
            AdminScreen(
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* auto-contenu dans SpiPmeTopBar */ },
            )
        }

        composable(Ecran.Dashboard.route) {
            DashboardScreen(
                surClicNotifications = { navController.navigate(Ecran.Alertes.route) },
                surClicProfil = { navController.navigate(Ecran.Profil.route) },
                surClicSecteur = { /* auto-contenu dans SpiPmeTopBar */ },
                surClicTresorerie = { navController.navigate(Ecran.Tresorerie.route) },
                surClicRegistre = { navController.navigate(Ecran.Registre.route) },
                surClicRessources = { navController.navigate(Ecran.Ressources.route) },
                surClicTaches = { navController.navigate(Ecran.Taches.route) },
                surClicAlertes = { navController.navigate(Ecran.Alertes.route) },
                surClicSuggestions = { navController.navigate(Ecran.Suggestions.route) },
            )
        }
    }
}

