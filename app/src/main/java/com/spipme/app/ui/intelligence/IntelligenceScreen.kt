package com.spipme.app.ui.intelligence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.domain.model.ResumeSuggestions
import com.spipme.app.domain.model.Suggestion
import com.spipme.app.ui.components.SpiPmeTextField
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeAppTheme
import com.spipme.app.ui.theme.SpiPmeTheme
import java.math.BigDecimal

@Composable
fun IntelligenceScreen(
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    viewModel: IntelligenceViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    IntelligenceContenu(
        etat = etat,
        surClicNotifications = surClicNotifications,
        surClicProfil = surClicProfil,
        surClicSecteur = surClicSecteur,
        surChangementOnglet = viewModel::surChangementOnglet,
        surClicGenerer = viewModel::genererSuggestions,
        surClicValider = viewModel::valider,
        surClicOuvrirRejet = viewModel::ouvrirDialogueRejet,
        surFermerDialogueRejet = viewModel::fermerDialogueRejet,
        surConfirmerRejet = viewModel::confirmerRejet,
    )
}

@Composable
private fun IntelligenceContenu(
    etat: IntelligenceUiState,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surChangementOnglet: (OngletSuggestions) -> Unit,
    surClicGenerer: () -> Unit,
    surClicValider: (Int) -> Unit,
    surClicOuvrirRejet: (Int) -> Unit,
    surFermerDialogueRejet: () -> Unit,
    surConfirmerRejet: (String) -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = surClicGenerer,
                icon = { Icon(Icons.Filled.AutoAwesome, null) },
                text = { Text(if (etat.enCoursDeGeneration) "Génération..." else "Nouvelle analyse") },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SpiPmeTopBar(
                secteurActifNom = etat.secteurActifNom,
                nombreNotificationsNonLues = 0,
                surClicSecteur = surClicSecteur,
                surClicNotifications = surClicNotifications,
                surClicProfil = surClicProfil,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SpiPmeTheme.extendedColors.ia)
                        Spacer(Modifier.width(8.dp))
                        Text("Suggestions IA", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Des recommandations intelligentes pour améliorer vos performances",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                etat.resume?.let { resume -> item { CartesResume(resume) } }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(OngletSuggestions.values().toList()) { onglet ->
                            FilterChip(
                                selected = etat.ongletActif == onglet,
                                onClick = { surChangementOnglet(onglet) },
                                label = { Text(onglet.libelle) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpiPmeTheme.extendedColors.ia,
                                    selectedLabelColor = MaterialTheme.colorScheme.surface,
                                ),
                            )
                        }
                    }
                }

                if (etat.enChargement) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (etat.suggestions.isEmpty()) {
                    item {
                        Text(
                            "Aucune suggestion pour le moment. Lancez une nouvelle analyse.",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(etat.suggestions, key = { it.id }) { suggestion ->
                        CarteSuggestion(
                            suggestion = suggestion,
                            surClicValider = { surClicValider(suggestion.id) },
                            surClicRejeter = { surClicOuvrirRejet(suggestion.id) },
                        )
                    }
                }

                etat.messageErreur?.let { message ->
                    item {
                        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    if (etat.suggestionARejeterId != null) {
        DialogueRejet(surConfirmer = surConfirmerRejet, surAnnuler = surFermerDialogueRejet)
    }
}

@Composable
private fun CartesResume(resume: ResumeSuggestions) {
    val donnees = listOf(
        Triple("Total (mois)", resume.total, MaterialTheme.colorScheme.onSurface),
        Triple("Validées", resume.validees, SpiPmeTheme.extendedColors.succes),
        Triple("En attente", resume.enAttente, SpiPmeTheme.extendedColors.avertissement),
        Triple("Rejetées", resume.rejetees, MaterialTheme.colorScheme.error),
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(donnees) { (libelle, valeur, couleur) ->
            Card(
                modifier = Modifier.width(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(valeur.toString(), style = MaterialTheme.typography.headlineMedium, color = couleur, fontWeight = FontWeight.Bold)
                    Text(libelle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        resume.tauxAcceptation?.let { taux ->
            item {
                Card(
                    modifier = Modifier.width(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpiPmeTheme.extendedColors.ia.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${taux}%", style = MaterialTheme.typography.headlineMedium, color = SpiPmeTheme.extendedColors.ia, fontWeight = FontWeight.Bold)
                        Text("Taux d'acceptation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CarteSuggestion(suggestion: Suggestion, surClicValider: () -> Unit, surClicRejeter: () -> Unit) {
    val couleurStatut = when (suggestion.statut) {
        "validee" -> SpiPmeTheme.extendedColors.succes
        "rejetee" -> MaterialTheme.colorScheme.error
        else -> SpiPmeTheme.extendedColors.avertissement
    }
    val libelleStatut = when (suggestion.statut) {
        "validee" -> "Validée"
        "rejetee" -> "Rejetée"
        "ignoree" -> "Ignorée"
        else -> "En attente"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(suggestion.titre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (suggestion.description.isNotBlank()) {
                        Text(suggestion.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(couleurStatut.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(libelleStatut, style = MaterialTheme.typography.bodySmall, color = couleurStatut)
                }
            }

            if (suggestion.facteurs.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestion.facteurs.take(4)) { (cle, valeur) ->
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text("$cle : $valeur", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                suggestion.impactEstime?.let { impact ->
                    Column {
                        Text("Impact estimé", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${formaterMontant(impact)} FCFA", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                suggestion.confiance?.let { confiance ->
                    Column {
                        Text("Confiance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${confiance.toPlainString()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (suggestion.estEnAttente) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = surClicRejeter, modifier = Modifier.weight(1f)) {
                        Text("Rejeter")
                    }
                    Button(onClick = surClicValider, modifier = Modifier.weight(1f)) {
                        Text("Valider")
                    }
                }
            } else if (suggestion.statut == "rejetee" && suggestion.motifDecision.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Motif : ${suggestion.motifDecision}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DialogueRejet(surConfirmer: (String) -> Unit, surAnnuler: () -> Unit) {
    var motif by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = surAnnuler,
        title = { Text("Rejeter la suggestion") },
        text = {
            Column {
                Text(
                    "Le motif de rejet est obligatoire — il sert à l'amélioration future des algorithmes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                SpiPmeTextField(valeur = motif, surChangement = { motif = it }, libelle = "Motif du rejet")
            }
        },
        confirmButton = {
            TextButton(onClick = { surConfirmer(motif) }) { Text("Confirmer le rejet") }
        },
        dismissButton = {
            TextButton(onClick = surAnnuler) { Text("Annuler") }
        },
    )
}

private fun formaterMontant(valeur: BigDecimal): String {
    val entier = valeur.abs().toBigInteger().toString()
    return entier.reversed().chunked(3).joinToString(" ").reversed()
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun IntelligenceScreenApercu() {
    SpiPmeAppTheme {
        IntelligenceContenu(
            etat = IntelligenceUiState(
                enChargement = false,
                secteurActifNom = "Commerce",
                resume = ResumeSuggestions(total = 18, validees = 11, enAttente = 5, rejetees = 2, tauxAcceptation = 61.0),
                suggestions = listOf(
                    Suggestion(
                        1, "seuil", "Recommandation", "Augmenter le stock de Riz étuvé 25kg",
                        "La demande a augmenté de 23% ces 7 derniers jours.",
                        listOf("Niveau actuel" to "18.0", "Seuil critique" to "10.0"),
                        BigDecimal("1250000"), BigDecimal("87"), "en_attente", "", "Riz étuvé 25kg", 1, null, "2026-07-28", null,
                    ),
                ),
            ),
            surClicNotifications = {}, surClicProfil = {}, surClicSecteur = {},
            surChangementOnglet = {}, surClicGenerer = {}, surClicValider = {},
            surClicOuvrirRejet = {}, surFermerDialogueRejet = {}, surConfirmerRejet = {},
        )
    }
}
