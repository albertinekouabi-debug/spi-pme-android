package com.spipme.app.ui.navigation
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState

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
    val entreeCourante by navController.currentBackStackEntryAsState()
    val routeActuelle = entreeCourante?.destination?.route
    var plusOuvert by rememberSaveable { mutableStateOf(false) }

    val routesAvecNavigation = setOf(
        Ecran.Accueil.route,
        Ecran.Dashboard.route,
        Ecran.Registre.route,
        Ecran.Ressources.route,
        Ecran.Tresorerie.route,
        Ecran.Taches.route,
        Ecran.Suggestions.route,
        Ecran.Alertes.route,
        Ecran.Imports.route,
        Ecran.Audit.route,
        Ecran.Factures.route,
        Ecran.Conformite.route,
        Ecran.Profil.route,
        Ecran.Administration.route,
        Ecran.Pilotage.route,
    )
    /**
     * Evite l'empilement des modules principaux.
     * Les ecrans secondaires gardent leur navigation habituelle.
     */
    fun naviguerVers(route: String) {
        plusOuvert = false

        if (route !in routesAvecNavigation) {
            navController.navigate(route) {
                launchSingleTop = true
            }
            return
        }

        if (navController.currentDestination?.route == route) return

        if (route == Ecran.Dashboard.route) {
            if (navController.popBackStack(Ecran.Dashboard.route, false)) return

            // En mode hors ligne, Accueil reste la racine de navigation.
            if (navController.popBackStack(Ecran.Accueil.route, false)) return

            navController.navigate(route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            return
        }

        if (route == Ecran.Accueil.route) {
            if (navController.popBackStack(Ecran.Accueil.route, false)) return

            if (navController.popBackStack(Ecran.Dashboard.route, false)) {
                navController.navigate(route) {
                    launchSingleTop = true
                }
                return
            }

            navController.navigate(route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            return
        }

        val racineDashboard = navController.popBackStack(
            Ecran.Dashboard.route,
            false
        )

        val racineAccueil = if (!racineDashboard) {
            navController.popBackStack(Ecran.Accueil.route, false)
        } else {
            false
        }

        if (racineDashboard || racineAccueil) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        } else {
            navController.navigate(route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    /**
     * Retour fiable au tableau de bord, ou a Accueil en mode hors ligne.
     */
    fun allerAuTableauDeBord() {
        plusOuvert = false

        if (navController.popBackStack(Ecran.Dashboard.route, false)) return
        if (navController.popBackStack(Ecran.Accueil.route, false)) return

        navController.navigate(Ecran.Dashboard.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (routeActuelle in routesAvecNavigation) {
                val accueilSelectionne = routeActuelle == Ecran.Accueil.route || routeActuelle == Ecran.Dashboard.route
                val plusSelectionne = plusOuvert || (
                    routeActuelle != Ecran.Accueil.route &&
                    routeActuelle != Ecran.Dashboard.route &&
                    routeActuelle != Ecran.Alertes.route &&
                    routeActuelle != Ecran.Ressources.route &&
                    routeActuelle != Ecran.Taches.route
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    NavigationBarItem(
                        selected = accueilSelectionne && !plusOuvert,
                        onClick = {
                            plusOuvert = false
                            val destination = if (routeActuelle == Ecran.Accueil.route) Ecran.Accueil.route else Ecran.Dashboard.route
                            if (routeActuelle != destination) naviguerVers(destination)
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("Accueil") },
                    )
                    NavigationBarItem(
                        selected = routeActuelle == Ecran.Alertes.route && !plusOuvert,
                        onClick = {
                            plusOuvert = false
                            if (routeActuelle != Ecran.Alertes.route) naviguerVers(Ecran.Alertes.route)
                        },
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                        label = { Text("Alertes") },
                    )
                    NavigationBarItem(
                        selected = routeActuelle == Ecran.Ressources.route && !plusOuvert,
                        onClick = {
                            plusOuvert = false
                            if (routeActuelle != Ecran.Ressources.route) naviguerVers(Ecran.Ressources.route)
                        },
                        icon = { Icon(Icons.Filled.Inventory, contentDescription = null) },
                        label = { Text("Stocks", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                    NavigationBarItem(
                        selected = routeActuelle == Ecran.Taches.route && !plusOuvert,
                        onClick = {
                            plusOuvert = false
                            if (routeActuelle != Ecran.Taches.route) naviguerVers(Ecran.Taches.route)
                        },
                        icon = { Icon(Icons.Filled.Assignment, contentDescription = null) },
                        label = { Text("Tâches") },
                    )
                    NavigationBarItem(
                        selected = plusSelectionne,
                        onClick = { plusOuvert = true },
                        icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = null) },
                        label = { Text("Plus") },
                    )
                }
            }
        },
    ) { paddingRacine ->
        NavHost(
            navController = navController,
            startDestination = ecranDepart,
            modifier = Modifier.fillMaxSize().padding(paddingRacine),
        ) {
        composable(Ecran.Connexion.route) {
            LoginScreen(
                surConnexionReussie = {
                    navController.navigate(Ecran.Dashboard.route) {
                        popUpTo(Ecran.Connexion.route) { inclusive = true }
                    }
                },
                surClicConnexionHorsLigne = { naviguerVers(Ecran.ConnexionHorsLigne.route) },
                surClicInscription = { naviguerVers(Ecran.Inscription.route) },
                surClicMotDePasseOublie = { naviguerVers(Ecran.MotDePasseOublie.route) },
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
                surClicRegistre = { naviguerVers(Ecran.Registre.route) },
                surClicRessources = { naviguerVers(Ecran.Ressources.route) },
                surClicTresorerie = { naviguerVers(Ecran.Tresorerie.route) },
                surClicTaches = { naviguerVers(Ecran.Taches.route) },
                surClicSuggestions = { naviguerVers(Ecran.Suggestions.route) },
                surClicAlertes = { naviguerVers(Ecran.Alertes.route) },
                surClicImports = { naviguerVers(Ecran.Imports.route) },
                surClicAudit = { naviguerVers(Ecran.Audit.route) },
                surClicFactures = { naviguerVers(Ecran.Factures.route) },
                surClicConformite = { naviguerVers(Ecran.Conformite.route) },
                surClicAdministration = { naviguerVers(Ecran.Administration.route) },
                surClicDashboard = { naviguerVers(Ecran.Dashboard.route) },
                surClicPilotage = { naviguerVers(Ecran.Pilotage.route) },
            )
        }

        composable(Ecran.Pilotage.route) {
            PilotageScreen(
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* le sélecteur de secteur s'ouvre depuis la barre supérieure */ },
            )
        }

        composable(Ecran.Registre.route) { backStackEntry ->
            val entiteCreeeFlow = backStackEntry.savedStateHandle.getStateFlow("entite_creee", false)
            val entiteCreee by entiteCreeeFlow.collectAsStateWithLifecycle()

            RegistryScreen(
                entiteVientDetreCreee = entiteCreee,
                surEntiteCreeeConsommee = { backStackEntry.savedStateHandle["entite_creee"] = false },
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
                surClicNouvelleEntite = { naviguerVers(Ecran.CreationEntite.route) },
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
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
                surClicNouvelleRessource = { naviguerVers(Ecran.CreationRessource.route) },
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
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
                surClicNouvelleTransaction = { naviguerVers(Ecran.CreationTransaction.route) },
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
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
                surClicNouvelleTache = { naviguerVers(Ecran.CreationTache.route) },
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
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Alertes.route) {
            AlertsScreen(
                surClicNotifications = {},  // déjà sur l'écran Alertes : rien à faire
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Imports.route) {
            ImportsScreen(
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Audit.route) {
            AuditScreen(
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Factures.route) {
            InvoicesScreen(
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* sélecteur de secteur — module à venir */ },
            )
        }

        composable(Ecran.Conformite.route) {
            ComplianceScreen(
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
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
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* auto-contenu dans SpiPmeTopBar */ },
            )
        }

        composable(Ecran.Dashboard.route) {
            DashboardScreen(
                surClicNotifications = { naviguerVers(Ecran.Alertes.route) },
                surClicProfil = { naviguerVers(Ecran.Profil.route) },
                surClicSecteur = { /* auto-contenu dans SpiPmeTopBar */ },
                surClicTresorerie = { naviguerVers(Ecran.Tresorerie.route) },
                surClicRegistre = { naviguerVers(Ecran.Registre.route) },
                surClicRessources = { naviguerVers(Ecran.Ressources.route) },
                surClicTaches = { naviguerVers(Ecran.Taches.route) },
                surClicAlertes = { naviguerVers(Ecran.Alertes.route) },
                surClicSuggestions = { naviguerVers(Ecran.Suggestions.route) },
                surClicImports = { naviguerVers(Ecran.Imports.route) },
                surClicAudit = { naviguerVers(Ecran.Audit.route) },
                surClicFactures = { naviguerVers(Ecran.Factures.route) },
                surClicConformite = { naviguerVers(Ecran.Conformite.route) },
                surClicAdministration = { naviguerVers(Ecran.Administration.route) },
                surClicPilotage = { naviguerVers(Ecran.Pilotage.route) },
                surClicNouvelleTransaction = { naviguerVers(Ecran.CreationTransaction.route) },
                surClicNouvelleRessource = { naviguerVers(Ecran.CreationRessource.route) },
                surClicNouvelleTache = { naviguerVers(Ecran.CreationTache.route) },
            )
        }
        }
    }

    BackHandler(
        enabled = plusOuvert || (
            routeActuelle != null &&
            routeActuelle !in setOf(
                Ecran.Connexion.route,
                Ecran.ConnexionHorsLigne.route,
                Ecran.Inscription.route,
                Ecran.MotDePasseOublie.route,
                Ecran.CreationEntite.route,
                Ecran.CreationRessource.route,
                Ecran.CreationTransaction.route,
                Ecran.CreationTache.route,
            ) && routeActuelle != Ecran.Dashboard.route &&
            routeActuelle != Ecran.Accueil.route
        ),
    ) {
        if (plusOuvert) {
            plusOuvert = false
        } else { allerAuTableauDeBord() }
    }
    if (plusOuvert) {
        val optionsPlus = listOf(
            "Registre" to Ecran.Registre.route,
            "Trésorerie" to Ecran.Tresorerie.route,
            "Suggestions IA" to Ecran.Suggestions.route,
            "Imports" to Ecran.Imports.route,
            "Journal d’audit" to Ecran.Audit.route,
            "Factures" to Ecran.Factures.route,
            "Conformité" to Ecran.Conformite.route,
            "Administration" to Ecran.Administration.route,
            "Pilotage intelligent" to Ecran.Pilotage.route,
            "Profil" to Ecran.Profil.route,
        )
        AlertDialog(
            onDismissRequest = { plusOuvert = false },
            title = { Text("Autres modules") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                ) {
                    optionsPlus.forEach { (libelle, destination) ->
                        TextButton(
                            onClick = {
                                plusOuvert = false
                                naviguerVers(destination)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(libelle, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { plusOuvert = false }) { Text("Fermer") }
            },
        )
    }
}
