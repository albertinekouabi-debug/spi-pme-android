package com.spipme.app.ui.compliance

import com.spipme.app.core.notifications.NotificationBadgeViewModel
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.spipme.app.domain.model.DeclarationConformite
import com.spipme.app.ui.components.SpiPmeTextField
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeTheme

/**
 * Conformité réglementaire. Les deux seules transitions possibles sont
 * "déclarer" (avec référence obligatoire) et "exempter" (avec note
 * justificative obligatoire) — le serveur n'expose aucun champ modifiable,
 * ce qui garantit la traçabilité de chaque décision.
 */
@Composable
fun ComplianceScreen(
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    viewModel: ComplianceViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    val nombreNotifs by (hiltViewModel<NotificationBadgeViewModel>()).compte.collectAsStateWithLifecycle()
    val etatSnackbar = remember { SnackbarHostState() }

    var declarationADeclarer by remember { mutableStateOf<DeclarationConformite?>(null) }
    var declarationAExempter by remember { mutableStateOf<DeclarationConformite?>(null) }

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
                secteurActifNom = etat.secteurActifNom,
                nombreNotificationsNonLues = nombreNotifs,
                surClicSecteur = surClicSecteur,
                surClicNotifications = surClicNotifications,
                surClicProfil = surClicProfil,
            )
        },
        snackbarHost = { SnackbarHost(etatSnackbar) },
    ) { paddingInterne ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterne)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Conformité", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Opérations nécessitant une déclaration réglementaire",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                FiltresStatut(etat.filtreStatut, viewModel::surSelectionStatut)
            }

            when {
                etat.chargementInitial -> item { Chargement() }
                etat.messageErreur != null && etat.declarations.isEmpty() -> item {
                    Erreur(etat.messageErreur!!, viewModel::rafraichir)
                }
                etat.estVide -> item { Vide() }
                else -> {
                    items(etat.declarations, key = { it.id }) { declaration ->
                        LigneDeclaration(
                            declaration = declaration,
                            actionEnCours = etat.actionEnCoursSurId == declaration.id,
                            surClicDeclarer = { declarationADeclarer = declaration },
                            surClicExempter = { declarationAExempter = declaration },
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
                                ) { Text("Charger plus") }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    declarationADeclarer?.let { declaration ->
        DialogueSaisie(
            titre = "Déclarer l'opération",
            libelleChamp = "Référence de déclaration",
            texteConfirmer = "Déclarer",
            surAnnuler = { declarationADeclarer = null },
            surConfirmer = { valeur ->
                viewModel.declarer(declaration.id, valeur)
                declarationADeclarer = null
            },
        )
    }

    declarationAExempter?.let { declaration ->
        DialogueSaisie(
            titre = "Exempter l'opération",
            libelleChamp = "Note justificative",
            texteConfirmer = "Exempter",
            surAnnuler = { declarationAExempter = null },
            surConfirmer = { valeur ->
                viewModel.exempter(declaration.id, valeur)
                declarationAExempter = null
            },
        )
    }
}

@Composable
private fun DialogueSaisie(
    titre: String,
    libelleChamp: String,
    texteConfirmer: String,
    surAnnuler: () -> Unit,
    surConfirmer: (String) -> Unit,
) {
    var valeur by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = surAnnuler,
        title = { Text(titre) },
        text = {
            SpiPmeTextField(
                valeur = valeur,
                surChangement = { valeur = it },
                libelle = libelleChamp,
            )
        },
        confirmButton = {
            TextButton(onClick = { surConfirmer(valeur) }, enabled = valeur.isNotBlank()) {
                Text(texteConfirmer)
            }
        },
        dismissButton = { TextButton(onClick = surAnnuler) { Text("Annuler") } },
    )
}

@Composable
private fun FiltresStatut(filtre: String?, surSelection: (String?) -> Unit) {
    val options = listOf(
        null to "Toutes",
        "a_declarer" to "À déclarer",
        "declaree" to "Déclarées",
        "exemptee" to "Exemptées",
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (valeur, libelle) ->
            FilterChip(
                selected = filtre == valeur,
                onClick = { surSelection(valeur) },
                label = { Text(libelle, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@Composable
private fun LigneDeclaration(
    declaration: DeclarationConformite,
    actionEnCours: Boolean,
    surClicDeclarer: () -> Unit,
    surClicExempter: () -> Unit,
) {
    val couleurStatut = when (declaration.statut) {
        "declaree" -> SpiPmeTheme.extendedColors.succes
        "exemptee" -> SpiPmeTheme.extendedColors.information
        else -> SpiPmeTheme.extendedColors.avertissement
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    declaration.transactionReference ?: "Transaction #${declaration.id}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    declaration.statut.replace('_', ' '),
                    style = MaterialTheme.typography.labelMedium,
                    color = couleurStatut,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                declaration.motif.replace('_', ' '),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            declaration.transactionMontant?.let {
                Text("Montant : $it", style = MaterialTheme.typography.bodySmall)
            }
            declaration.seuilApplique?.let {
                Text(
                    "Seuil appliqué : $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            declaration.referenceDeclaration?.let {
                Text("Référence : $it", style = MaterialTheme.typography.labelSmall)
            }
            declaration.declarantNom?.let {
                Text(
                    "Traité par $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Les actions ne sont proposées que sur les dossiers encore à traiter.
            if (declaration.statut == "a_declarer") {
                Spacer(Modifier.height(8.dp))
                if (actionEnCours) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = surClicDeclarer) { Text("Déclarer") }
                        OutlinedButton(onClick = surClicExempter) { Text("Exempter") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chargement() {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Vide() {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Filled.GppGood,
            contentDescription = null,
            tint = SpiPmeTheme.extendedColors.succes,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Aucune opération à déclarer pour ce filtre.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Erreur(message: String, surReessayer: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = surReessayer) { Text("Réessayer") }
    }
}

