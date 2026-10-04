package com.spipme.app.ui.registry

import com.spipme.app.core.notifications.NotificationBadgeViewModel
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.spipme.app.domain.model.Entite
import com.spipme.app.domain.model.RepartitionParType
import com.spipme.app.domain.model.ResumeRegistre
import com.spipme.app.ui.components.SpiPmeTextField
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeAppTheme

@Composable
fun RegistryScreen(
    entiteVientDetreCreee: Boolean,
    surEntiteCreeeConsommee: () -> Unit,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surClicNouvelleEntite: () -> Unit,
    viewModel: RegistryViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    val nombreNotifs by (hiltViewModel<NotificationBadgeViewModel>()).compte.collectAsStateWithLifecycle()

    LaunchedEffect(entiteVientDetreCreee) {
        if (entiteVientDetreCreee) {
            viewModel.rafraichir()
            surEntiteCreeeConsommee()
        }
    }

    RegistryContenu(
        nombreNotifs = nombreNotifs,
        etat = etat,
        surClicNotifications = surClicNotifications,
        surClicProfil = surClicProfil,
        surClicSecteur = surClicSecteur,
        surChangementOnglet = viewModel::surChangementOnglet,
        surChangementRecherche = viewModel::surChangementRecherche,
        surClicNouvelleEntite = surClicNouvelleEntite,
    )
}

@Composable
private fun RegistryContenu(
    etat: RegistryUiState,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surChangementOnglet: (OngletRegistre) -> Unit,
    surChangementRecherche: (String) -> Unit,
    surClicNouvelleEntite: () -> Unit,
    nombreNotifs: Int = 0,
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = surClicNouvelleEntite, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nouvelle entité") })
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SpiPmeTopBar(
                secteurActifNom = etat.secteurActifNom,
                nombreNotificationsNonLues = nombreNotifs,
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
                    Text("Registre", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Gérez vos clients, fournisseurs et autres entités",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                etat.resume?.let { resume -> item { CartesResume(resume) } }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(OngletRegistre.values().toList()) { onglet ->
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

                item {
                    SpiPmeTextField(
                        valeur = etat.recherche,
                        surChangement = surChangementRecherche,
                        libelle = "Rechercher une entité...",
                        iconeDebut = { Icon(Icons.Filled.Search, contentDescription = null) },
                    )
                }

                if (etat.enChargement) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (etat.entites.isEmpty()) {
                    item {
                        Text(
                            "Aucune entité trouvée.",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(etat.entites, key = { it.id }) { entite -> CarteEntite(entite) }
                }

                etat.messageErreur?.let { message ->
                    item {
                        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                item { Spacer(Modifier.height(72.dp)) } // laisse de la place au-dessus du FAB
            }
        }
    }
}

@Composable
private fun CartesResume(resume: ResumeRegistre) {
    val couleurs = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        com.spipme.app.ui.theme.SpiPmeTheme.extendedColors.information,
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            CarteResumeItem("Total", resume.total, MaterialTheme.colorScheme.onSurface)
        }
        items(resume.parType) { ligne ->
            val index = resume.parType.indexOf(ligne)
            CarteResumeItem(
                libelle = ligne.type.replaceFirstChar { it.uppercase() },
                valeur = ligne.total,
                couleur = couleurs[index % couleurs.size],
            )
        }
    }
}

@Composable
private fun CarteResumeItem(libelle: String, valeur: Int, couleur: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.width(140.dp),
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

@Composable
private fun CarteEntite(entite: Entite) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    entite.nom.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entite.nom, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    BadgeType(entite.type)
                }
                if (entite.ville.isNotBlank() || entite.pays.isNotBlank()) {
                    Text(
                        listOf(entite.ville, entite.pays).filter { it.isNotBlank() }.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (entite.telephone.isNotBlank()) {
                    Text(entite.telephone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            BadgeStatut(entite.estActif)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BadgeType(type: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(type.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun BadgeStatut(actif: Boolean) {
    val couleur = if (actif) com.spipme.app.ui.theme.SpiPmeTheme.extendedColors.succes else MaterialTheme.colorScheme.error
    Box(
        modifier = Modifier
            .background(couleur.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(if (actif) "Actif" else "Inactif", style = MaterialTheme.typography.bodySmall, color = couleur)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun RegistryScreenApercu() {
    SpiPmeAppTheme {
        RegistryContenu(
            etat = RegistryUiState(
                enChargement = false,
                secteurActifNom = "Commerce",
                resume = ResumeRegistre(
                    total = 216, actives = 210,
                    parType = listOf(
                        RepartitionParType("client", 124),
                        RepartitionParType("fournisseur", 68),
                        RepartitionParType("partenaire", 24),
                    ),
                ),
                entites = listOf(
                    Entite(1, "client", "Chez Martine SARL", "+242 06 123 45 67", "", "Brazzaville", "Congo", "", "", "actif", 1, "Commerce"),
                ),
            ),
            surClicNotifications = {},
            surClicProfil = {},
            surClicSecteur = {},
            surChangementOnglet = {},
            surChangementRecherche = {},
            surClicNouvelleEntite = {},
        )
    }
}
