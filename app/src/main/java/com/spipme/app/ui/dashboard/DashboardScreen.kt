package com.spipme.app.ui.dashboard

import com.spipme.app.core.notifications.NotificationBadgeViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeTheme

/**
 * Tableau de bord — assemble les `summary` déjà existants et testés côté
 * serveur (pas de duplication de logique métier). Chaque carte navigue
 * vers son module. Un échec sur un seul module n'empêche jamais
 * l'affichage des autres — voir DashboardViewModel.
 */
@Composable
fun DashboardScreen(
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surClicTresorerie: () -> Unit,
    surClicRegistre: () -> Unit,
    surClicRessources: () -> Unit,
    surClicTaches: () -> Unit,
    surClicAlertes: () -> Unit,
    surClicSuggestions: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
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
        when {
            etat.chargement -> Box(
                Modifier.fillMaxSize().padding(paddingInterne),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            etat.echecComplet -> Box(
                Modifier.fillMaxSize().padding(paddingInterne),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        etat.messageErreur ?: "Impossible de charger le tableau de bord.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = viewModel::rafraichir) { Text("Réessayer") }
                }
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(paddingInterne),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Text("Tableau de bord", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }

                etat.resumeTresorerie?.let { resume ->
                    item {
                        CarteDashboard(
                            titre = "Trésorerie",
                            valeurPrincipale = "${resume.soldeDisponible}",
                            sousTitre = "Solde disponible",
                            couleur = MaterialTheme.colorScheme.primary,
                            surClic = surClicTresorerie,
                        )
                    }
                }
                etat.resumeRegistre?.let { resume ->
                    item {
                        CarteDashboard(
                            titre = "Registre",
                            valeurPrincipale = "${resume.total}",
                            sousTitre = "${resume.actives} actives",
                            couleur = MaterialTheme.colorScheme.onSurface,
                            surClic = surClicRegistre,
                        )
                    }
                }
                etat.resumeRessources?.let { resume ->
                    item {
                        CarteDashboard(
                            titre = "Ressources",
                            valeurPrincipale = "${resume.total}",
                            sousTitre = "${resume.critiques} critiques",
                            couleur = if (resume.critiques > 0) MaterialTheme.colorScheme.error else SpiPmeTheme.extendedColors.succes,
                            surClic = surClicRessources,
                        )
                    }
                }
                etat.resumeTaches?.let { resume ->
                    item {
                        CarteDashboard(
                            titre = "Tâches",
                            valeurPrincipale = "${resume.total}",
                            sousTitre = "${resume.enRetard} en retard",
                            couleur = if (resume.enRetard > 0) MaterialTheme.colorScheme.error else SpiPmeTheme.extendedColors.succes,
                            surClic = surClicTaches,
                        )
                    }
                }
                etat.resumeAlertes?.let { resume ->
                    item {
                        CarteDashboard(
                            titre = "Alertes",
                            valeurPrincipale = "${resume.critiques}",
                            sousTitre = "Critiques actives",
                            couleur = if (resume.critiques > 0) MaterialTheme.colorScheme.error else SpiPmeTheme.extendedColors.succes,
                            surClic = surClicAlertes,
                        )
                    }
                }
                etat.resumeSuggestions?.let { resume ->
                    item {
                        CarteDashboard(
                            titre = "Suggestions IA",
                            valeurPrincipale = "${resume.enAttente}",
                            sousTitre = "En attente",
                            couleur = SpiPmeTheme.extendedColors.information,
                            surClic = surClicSuggestions,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarteDashboard(
    titre: String,
    valeurPrincipale: String,
    sousTitre: String,
    couleur: Color,
    surClic: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        onClick = surClic,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titre, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(valeurPrincipale, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = couleur)
            Text(sousTitre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

