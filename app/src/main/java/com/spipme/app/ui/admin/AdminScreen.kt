package com.spipme.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.domain.model.Role
import com.spipme.app.domain.model.Secteur
import com.spipme.app.domain.model.Utilisateur
import com.spipme.app.ui.components.SpiPmeBoutonPrincipal
import com.spipme.app.ui.components.SpiPmeTextField
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeTheme

/**
 * Administration des comptes. RÃ©servÃ© au rÃ´le Administrateur â€” contrÃ´lÃ©
 * CÃ”TÃ‰ SERVEUR (EstAdministrateur sur UtilisateurViewSet), pas seulement
 * masquÃ© ici : cet Ã©cran n'est qu'une commoditÃ©, jamais la barriÃ¨re de
 * sÃ©curitÃ©.
 */
@Composable
fun AdminScreen(
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    val etatSnackbar = remember { SnackbarHostState() }

    LaunchedEffect(etat.messageSucces, etat.messageErreur) {
        val message = etat.messageSucces ?: etat.messageErreur
        if (message != null) {
            etatSnackbar.showSnackbar(message)
            viewModel.messageConsomme()
        }
    }

    Scaffold(
        topBar = {
            SpiPmeTopBar(
                secteurActifNom = "",
                nombreNotificationsNonLues = 0,
                surClicSecteur = surClicSecteur,
                surClicNotifications = surClicNotifications,
                surClicProfil = surClicProfil,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::ouvrirFormulaireCreation) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        },
        snackbarHost = { SnackbarHost(etatSnackbar) },
    ) { paddingInterne ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingInterne).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Administration", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Gestion des comptes de votre entreprise",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
            }

            when {
                etat.chargementInitial -> item { Chargement() }
                etat.messageErreur != null && etat.utilisateurs.isEmpty() -> item {
                    Erreur(etat.messageErreur!!, viewModel::rafraichir)
                }
                etat.estVide -> item {
                    Text(
                        "Aucun compte pour le moment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    items(etat.utilisateurs, key = { it.id }) { utilisateur ->
                        LigneUtilisateur(
                            utilisateur = utilisateur,
                            actionEnCours = etat.actionEnCoursSurId == utilisateur.id,
                            surClicDesactiver = { viewModel.desactiverUtilisateur(utilisateur.id) },
                        )
                    }
                    if (etat.ilResteDesPages) {
                        item {
                            if (etat.chargementPageSuivante) {
                                Chargement()
                            } else {
                                TextButton(
                                    onClick = viewModel::chargerPageSuivante,
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text("Charger plus de comptes") }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (etat.afficherFormulaireCreation) {
        DialogueCreationUtilisateur(
            roles = etat.roles,
            secteurs = etat.secteurs,
            creationEnCours = etat.creationEnCours,
            surAnnuler = viewModel::fermerFormulaireCreation,
            surConfirmer = viewModel::creerUtilisateur,
        )
    }
}

@Composable
private fun LigneUtilisateur(
    utilisateur: Utilisateur,
    actionEnCours: Boolean,
    surClicDesactiver: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    utilisateur.nomComplet?.ifBlank { null } ?: utilisateur.nomUtilisateur,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(utilisateur.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    buildString {
                        append(utilisateur.roleNom ?: "â€”")
                        utilisateur.secteurPrincipalNom?.let { append(" Â· $it") }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!utilisateur.actif) {
                Text(
                    "DÃ©sactivÃ©",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (actionEnCours) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                TextButton(onClick = surClicDesactiver) { Text("DÃ©sactiver") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogueCreationUtilisateur(
    roles: List<Role>,
    secteurs: List<Secteur>,
    creationEnCours: Boolean,
    surAnnuler: () -> Unit,
    surConfirmer: (
        nomUtilisateur: String,
        email: String,
        motDePasse: String,
        roleId: Int,
        secteurPrincipalId: Int,
        nomComplet: String,
        telephone: String,
    ) -> Unit,
) {
    var nomUtilisateur by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }
    var nomComplet by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var roleSelectionne by remember { mutableStateOf<Role?>(null) }
    var secteurSelectionne by remember { mutableStateOf<Secteur?>(null) }
    var menuRoleOuvert by remember { mutableStateOf(false) }
    var menuSecteurOuvert by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = surAnnuler,
        title = { Text("CrÃ©er un compte") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                SpiPmeTextField(valeur = nomUtilisateur, surChangement = { nomUtilisateur = it }, libelle = "Nom d'utilisateur")
                Spacer(Modifier.height(8.dp))
                SpiPmeTextField(valeur = email, surChangement = { email = it }, libelle = "Email")
                Spacer(Modifier.height(8.dp))
                SpiPmeTextField(valeur = motDePasse, surChangement = { motDePasse = it }, libelle = "Mot de passe", motDePasse = true)
                Spacer(Modifier.height(8.dp))
                SpiPmeTextField(valeur = nomComplet, surChangement = { nomComplet = it }, libelle = "Nom complet")
                Spacer(Modifier.height(8.dp))
                SpiPmeTextField(valeur = telephone, surChangement = { telephone = it }, libelle = "TÃ©lÃ©phone")
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = menuRoleOuvert, onExpandedChange = { menuRoleOuvert = it }) {
                    SpiPmeTextField(
                        valeur = roleSelectionne?.nom.orEmpty(),
                        surChangement = {},
                        libelle = "RÃ´le",
                        modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = menuRoleOuvert, onDismissRequest = { menuRoleOuvert = false }) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.nom) },
                                onClick = { roleSelectionne = role; menuRoleOuvert = false },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = menuSecteurOuvert, onExpandedChange = { menuSecteurOuvert = it }) {
                    SpiPmeTextField(
                        valeur = secteurSelectionne?.nom.orEmpty(),
                        surChangement = {},
                        libelle = "Secteur principal",
                        modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = menuSecteurOuvert, onDismissRequest = { menuSecteurOuvert = false }) {
                        secteurs.forEach { secteur ->
                            DropdownMenuItem(
                                text = { Text(secteur.nom) },
                                onClick = { secteurSelectionne = secteur; menuSecteurOuvert = false },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (creationEnCours) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                TextButton(
                    onClick = {
                        val role = roleSelectionne ?: return@TextButton
                        val secteur = secteurSelectionne ?: return@TextButton
                        surConfirmer(nomUtilisateur, email, motDePasse, role.id, secteur.id, nomComplet, telephone)
                    },
                    enabled = roleSelectionne != null && secteurSelectionne != null,
                ) { Text("CrÃ©er") }
            }
        },
        dismissButton = { TextButton(onClick = surAnnuler) { Text("Annuler") } },
    )
}

@Composable
private fun Chargement() {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Erreur(message: String, surReessayer: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = surReessayer) { Text("RÃ©essayer") }
    }
}

