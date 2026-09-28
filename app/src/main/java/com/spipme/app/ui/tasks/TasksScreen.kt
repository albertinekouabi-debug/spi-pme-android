package com.spipme.app.ui.tasks

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.domain.model.ResumeTaches
import com.spipme.app.domain.model.Tache
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeAppTheme
import com.spipme.app.ui.theme.SpiPmeTheme

@Composable
fun TasksScreen(
    tacheVientDetreCreee: Boolean,
    surTacheCreeeConsommee: () -> Unit,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surClicNouvelleTache: () -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(tacheVientDetreCreee) {
        if (tacheVientDetreCreee) {
            viewModel.rafraichir()
            surTacheCreeeConsommee()
        }
    }

    TasksContenu(
        etat = etat,
        surClicNotifications = surClicNotifications,
        surClicProfil = surClicProfil,
        surClicSecteur = surClicSecteur,
        surChangementOnglet = viewModel::surChangementOnglet,
        surClicNouvelleTache = surClicNouvelleTache,
        surClicMarquerTerminee = viewModel::marquerTerminee,
    )
}

@Composable
private fun TasksContenu(
    etat: TasksUiState,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surChangementOnglet: (OngletTaches) -> Unit,
    surClicNouvelleTache: () -> Unit,
    surClicMarquerTerminee: (Int) -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = surClicNouvelleTache,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nouvelle tÃ¢che") },
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
                    Text("TÃ¢ches", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Organisez, suivez et accomplissez vos tÃ¢ches",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                etat.resume?.let { resume -> item { CartesResume(resume) } }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(OngletTaches.values().toList()) { onglet ->
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

                if (etat.enChargement) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (etat.taches.isEmpty()) {
                    item {
                        Text(
                            "Aucune tÃ¢che trouvÃ©e.",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(etat.taches, key = { it.id }) { tache ->
                        CarteTache(tache, surClicMarquerTerminee = { surClicMarquerTerminee(tache.id) })
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
private fun CartesResume(resume: ResumeTaches) {
    val donnees = listOf(
        Triple("Toutes", resume.total, MaterialTheme.colorScheme.onSurface),
        Triple("TerminÃ©es", resume.terminees, SpiPmeTheme.extendedColors.succes),
        Triple("En cours", resume.enCours, SpiPmeTheme.extendedColors.avertissement),
        Triple("En retard", resume.enRetard, MaterialTheme.colorScheme.error),
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
private fun CarteTache(tache: Tache, surClicMarquerTerminee: () -> Unit) {
    val couleurPriorite = when (tache.priorite) {
        "haute" -> MaterialTheme.colorScheme.error
        "moyenne" -> SpiPmeTheme.extendedColors.avertissement
        else -> SpiPmeTheme.extendedColors.succes
    }
    val estTerminee = tache.statut == "terminee"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = surClicMarquerTerminee, enabled = !estTerminee) {
                Icon(
                    if (estTerminee) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (estTerminee) "TerminÃ©e" else "Marquer comme terminÃ©e",
                    tint = if (estTerminee) SpiPmeTheme.extendedColors.succes else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tache.titre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (estTerminee) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                )
                if (tache.categorie.isNotBlank()) {
                    Text(
                        "CatÃ©gorie : ${tache.categorie}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    tache.echeance?.let { echeance ->
                        Text(
                            echeance,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (tache.enRetard) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (tache.enRetard) FontWeight.SemiBold else FontWeight.Normal,
                        )
                        if (tache.enRetard) {
                            Spacer(Modifier.width(6.dp))
                            Text("En retard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .background(couleurPriorite.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    tache.priorite.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = couleurPriorite,
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun TasksScreenApercu() {
    SpiPmeAppTheme {
        TasksContenu(
            etat = TasksUiState(
                enChargement = false,
                secteurActifNom = "Commerce",
                resume = ResumeTaches(total = 24, terminees = 10, enCours = 7, enRetard = 7),
                taches = listOf(
                    Tache(1, "Commander du stock critique", "", "Approvisionnement", "haute", "a_faire", "", null, null, "28 juil. 2026", 1, true),
                ),
            ),
            surClicNotifications = {}, surClicProfil = {}, surClicSecteur = {},
            surChangementOnglet = {}, surClicNouvelleTache = {}, surClicMarquerTerminee = {},
        )
    }
}

