package com.spipme.app.ui.invoices

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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.domain.model.Facture
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeTheme

@Composable
fun InvoicesScreen(
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    viewModel: InvoicesViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    val nombreNotifs by (hiltViewModel<NotificationBadgeViewModel>()).compte.collectAsStateWithLifecycle()

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
    ) { paddingInterne ->
        etat.factureAAnnuler?.let { facture ->
            DialogueAnnulation(
                facture = facture,
                messageErreur = etat.messageErreur,
                surConfirmer = viewModel::confirmerAnnulation,
                surAbandonner = viewModel::abandonnerAnnulation,
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterne)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Factures", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                FiltresStatut(etat.filtreStatut, viewModel::surSelectionStatut)
            }

            etat.messageInfo?.let { info ->
                item {
                    Text(info, style = MaterialTheme.typography.bodySmall, color = SpiPmeTheme.extendedColors.succes)
                }
            }

            when {
                etat.chargementInitial -> item { Chargement() }
                etat.messageErreur != null -> item {
                    Erreur(etat.messageErreur!!, viewModel::rafraichir)
                }
                etat.estVide -> item { Vide() }
                else -> {
                    items(etat.factures, key = { it.id }) { facture ->
                        LigneFacture(
                            facture = facture,
                            annulable = viewModel.peutEtreAnnulee(facture),
                            enAttente = facture.id in etat.idsEnAttente,
                            surAnnuler = { viewModel.demanderAnnulation(facture) },
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
                                ) { Text("Charger plus de factures") }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun FiltresStatut(filtre: String?, surSelection: (String?) -> Unit) {
    val options = listOf(
        null to "Toutes",
        "emise" to "Émises",
        "payee" to "Payées",
        "impayee" to "Impayées",
        "relancee" to "Relancées",
        "annulee" to "Annulées",
        "avoir" to "Avoirs",
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

/** Motif obligatoire : l'annulation est tracée (qui, quand, pourquoi) et ne supprime jamais la facture. */
@Composable
private fun DialogueAnnulation(
    facture: Facture,
    messageErreur: String?,
    surConfirmer: (String) -> Unit,
    surAbandonner: () -> Unit,
) {
    var motif by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = surAbandonner,
        title = { Text("Annuler la facture ${facture.numero} ?") },
        text = {
            Column {
                Text(
                    "Un avoir du montant total sera émis ; la facture reste consultable dans l'historique. " +
                        "Cette action est tracée et ne peut pas être supprimée.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = motif,
                    onValueChange = { motif = it },
                    label = { Text("Motif (obligatoire)") },
                    isError = messageErreur != null,
                    supportingText = messageErreur?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { TextButton(onClick = { surConfirmer(motif) }) { Text("Annuler la facture") } },
        dismissButton = { TextButton(onClick = surAbandonner) { Text("Retour") } },
    )
}

@Composable
private fun LigneFacture(facture: Facture, annulable: Boolean, enAttente: Boolean, surAnnuler: () -> Unit) {
    val couleurStatut = when (facture.statut) {
        "payee" -> SpiPmeTheme.extendedColors.succes
        "impayee" -> MaterialTheme.colorScheme.error
        "relancee" -> SpiPmeTheme.extendedColors.avertissement
        "emise" -> SpiPmeTheme.extendedColors.information
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(facture.numero, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    facture.statut.replace('_', ' '),
                    style = MaterialTheme.typography.labelMedium,
                    color = couleurStatut,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            facture.entiteNom?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "HT ${facture.montant}  ·  TVA ${facture.montantTva}  ·  TTC ${facture.montantTtc}",
                style = MaterialTheme.typography.bodySmall,
            )
            facture.dateEcheance?.let {
                Text(
                    "Échéance : $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (enAttente) {
                Text(
                    "Annulation en attente de synchronisation",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpiPmeTheme.extendedColors.avertissement,
                    fontWeight = FontWeight.SemiBold,
                )
            } else if (annulable) {
                TextButton(onClick = surAnnuler) { Text("Annuler la facture") }
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
            Icons.Filled.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Aucune facture pour ce filtre.",
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

