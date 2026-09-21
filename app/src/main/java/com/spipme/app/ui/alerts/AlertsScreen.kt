package com.spipme.app.ui.alerts

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.domain.model.Alerte
import com.spipme.app.domain.model.ResumeAlertes
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeAppTheme
import com.spipme.app.ui.theme.SpiPmeTheme

@Composable
fun AlertsScreen(
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    AlertsContenu(
        etat = etat,
        surClicNotifications = surClicNotifications,
        surClicProfil = surClicProfil,
        surClicSecteur = surClicSecteur,
        surChangementOnglet = viewModel::surChangementOnglet,
        surClicGenerer = viewModel::genererAlertes,
        surClicResoudre = viewModel::resoudre,
        surClicIgnorer = viewModel::ignorer,
    )
}

@Composable
private fun AlertsContenu(
    etat: AlertsUiState,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surChangementOnglet: (OngletAlertes) -> Unit,
    surClicGenerer: () -> Unit,
    surClicResoudre: (Int) -> Unit,
    surClicIgnorer: (Int) -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = surClicGenerer,
                icon = { Icon(Icons.Filled.NotificationsActive, null) },
                text = { Text(if (etat.enCoursDeGeneration) "Analyse..." else "Analyser") },
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
                    Text("Alertes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Surveillez les points critiques de votre activité",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                etat.resume?.let { resume -> item { CartesResume(resume) } }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(OngletAlertes.values().toList()) { onglet ->
                            FilterChip(
                                selected = etat.ongletActif == onglet,
                                onClick = { surChangementOnglet(onglet) },
                                label = { Text(onglet.libelle) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }
                }

                etat.messageInfo?.let { message ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SpiPmeTheme.extendedColors.information.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(message, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (etat.enChargement) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (etat.alertes.isEmpty()) {
                    item {
                        Text(
                            "Aucune alerte dans cette catégorie.",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(etat.alertes, key = { it.id }) { alerte ->
                        CarteAlerte(
                            alerte = alerte,
                            surClicResoudre = { surClicResoudre(alerte.id) },
                            surClicIgnorer = { surClicIgnorer(alerte.id) },
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
}

@Composable
private fun CartesResume(resume: ResumeAlertes) {
    val donnees = listOf(
        Triple("Critique", resume.critiques, MaterialTheme.colorScheme.error),
        Triple("Élevée", resume.elevees, SpiPmeTheme.extendedColors.avertissement),
        Triple("Modérée", resume.moderees, SpiPmeTheme.extendedColors.information),
        Triple("Résolue", resume.resolues, SpiPmeTheme.extendedColors.succes),
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
    }
}

@Composable
private fun CarteAlerte(alerte: Alerte, surClicResoudre: () -> Unit, surClicIgnorer: () -> Unit) {
    val couleur = when (alerte.niveau) {
        "critique" -> MaterialTheme.colorScheme.error
        "elevee" -> SpiPmeTheme.extendedColors.avertissement
        else -> SpiPmeTheme.extendedColors.information
    }
    val icone = when (alerte.niveau) {
        "critique" -> Icons.Filled.Warning
        "elevee" -> Icons.Filled.Warning
        else -> Icons.Filled.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .background(couleur.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
            ) {
                Icon(
                    if (alerte.estActive) icone else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (alerte.estActive) couleur else SpiPmeTheme.extendedColors.succes,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alerte.titre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (alerte.description.isNotBlank()) {
                    Text(alerte.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val contexte = listOfNotNull(alerte.ressourceNom, alerte.entiteNom, alerte.tacheTitre, alerte.factureNumero).firstOrNull()
                contexte?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (alerte.estActive) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = surClicIgnorer) { Text("Ignorer") }
                        OutlinedButton(onClick = surClicResoudre) { Text("Traiter") }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun AlertsScreenApercu() {
    SpiPmeAppTheme {
        AlertsContenu(
            etat = AlertsUiState(
                enChargement = false,
                secteurActifNom = "Commerce",
                resume = ResumeAlertes(critiques = 5, elevees = 8, moderees = 12, resolues = 24),
                alertes = listOf(
                    Alerte(1, "stock_critique", "critique", "Stock critique : Lait en poudre", "Stock actuel : 2 unités restantes.", "active", "Lait en poudre", null, null, null, 1, "2026-07-28", null),
                ),
            ),
            surClicNotifications = {}, surClicProfil = {}, surClicSecteur = {},
            surChangementOnglet = {}, surClicGenerer = {}, surClicResoudre = {}, surClicIgnorer = {},
        )
    }
}
