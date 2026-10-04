package com.spipme.app.ui.resources

import com.spipme.app.ui.sync.ElementEnAttente
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.LinearProgressIndicator
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
import com.spipme.app.domain.model.ResumeRessources
import com.spipme.app.domain.model.Ressource
import com.spipme.app.ui.components.SpiPmeTextField
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeAppTheme
import com.spipme.app.ui.theme.SpiPmeTheme
import java.math.BigDecimal

@Composable
fun ResourcesScreen(
    ressourceVientDetreCreee: Boolean,
    surRessourceCreeeConsommee: () -> Unit,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surClicNouvelleRessource: () -> Unit,
    viewModel: ResourcesViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    val nombreNotifs by (hiltViewModel<NotificationBadgeViewModel>()).compte.collectAsStateWithLifecycle()

    LaunchedEffect(ressourceVientDetreCreee) {
        if (ressourceVientDetreCreee) {
            viewModel.rafraichir()
            surRessourceCreeeConsommee()
        }
    }

    ResourcesContenu(
        nombreNotifs = nombreNotifs,
        etat = etat,
        surClicNotifications = surClicNotifications,
        surClicProfil = surClicProfil,
        surClicSecteur = surClicSecteur,
        surChangementOnglet = viewModel::surChangementOnglet,
        surChangementRecherche = viewModel::surChangementRecherche,
        surClicNouvelleRessource = surClicNouvelleRessource,
        surModifier = viewModel::demanderModification,
    )

    etat.ressourceAModifier?.let { ressource ->
        DialogueModificationRessource(
            ressource = ressource,
            erreur = etat.erreurModification,
            surConfirmer = viewModel::confirmerModification,
            surAbandonner = viewModel::abandonnerModification,
        )
    }
}

@Composable
private fun ResourcesContenu(
    etat: ResourcesUiState,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surChangementOnglet: (OngletRessources) -> Unit,
    surChangementRecherche: (String) -> Unit,
    surClicNouvelleRessource: () -> Unit,
    surModifier: (Ressource) -> Unit = {},
    nombreNotifs: Int = 0,
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = surClicNouvelleRessource,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nouvelle ressource") },
            )
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
                    Text("Ressources", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Suivez vos stocks et ressources en temps réel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                etat.resume?.let { resume -> item { CartesResume(resume) } }

                etat.messageInfo?.let { info ->
                    item { Text(info, style = MaterialTheme.typography.bodySmall, color = SpiPmeTheme.extendedColors.succes) }
                }
                items(etat.creationsEnAttente, key = { "attente-${it.id}" }) { element -> CarteEnAttente(element) }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(OngletRessources.values().toList()) { onglet ->
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
                        libelle = "Rechercher une ressource...",
                        iconeDebut = { Icon(Icons.Filled.Search, contentDescription = null) },
                    )
                }

                if (etat.enChargement) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (etat.ressources.isEmpty()) {
                    item {
                        Text(
                            "Aucune ressource trouvée.",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(etat.ressources, key = { it.id }) { ressource ->
                        CarteRessource(
                            ressource = ressource,
                            modifiable = etat.peutModifier && ressource.id !in etat.idsModificationEnAttente,
                            modificationEnAttente = ressource.id in etat.idsModificationEnAttente,
                            surModifier = { surModifier(ressource) },
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
private fun CartesResume(resume: ResumeRessources) {
    val donnees = listOf(
        Triple("Critiques", resume.critiques, MaterialTheme.colorScheme.error),
        Triple("À surveiller", resume.aSurveiller, SpiPmeTheme.extendedColors.avertissement),
        Triple("Stables", resume.stables, SpiPmeTheme.extendedColors.succes),
        Triple("Total", resume.total, MaterialTheme.colorScheme.onSurface),
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(donnees) { (libelle, valeur, couleur) ->
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
    }
}

@Composable
private fun CarteRessource(
    ressource: Ressource,
    modifiable: Boolean = false,
    modificationEnAttente: Boolean = false,
    surModifier: () -> Unit = {},
) {
    val couleurStatut = when (ressource.statut) {
        "critique" -> MaterialTheme.colorScheme.error
        "a_surveiller" -> SpiPmeTheme.extendedColors.avertissement
        else -> SpiPmeTheme.extendedColors.succes
    }
    val libelleStatut = when (ressource.statut) {
        "critique" -> "Critique"
        "a_surveiller" -> "À surveiller"
        else -> "Stable"
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
                    Text(ressource.nom, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Catégorie : ${ressource.type}" + if (ressource.emplacement.isNotBlank()) " · ${ressource.emplacement}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .background(couleurStatut.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(libelleStatut, style = MaterialTheme.typography.bodySmall, color = couleurStatut)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formaterQuantite(ressource.niveauActuel)} ${ressource.unite}",
                    style = MaterialTheme.typography.titleMedium,
                    color = couleurStatut,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.weight(1f))
                if (ressource.seuilsConfigures) {
                    Text(
                        "Min : ${formaterQuantite(ressource.seuilAlerte!!)} ${ressource.unite}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { ressource.progression },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = couleurStatut,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            if (!ressource.seuilsConfigures) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Seuils non configurés",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (modificationEnAttente) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Modification en attente de synchronisation",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpiPmeTheme.extendedColors.avertissement,
                    fontWeight = FontWeight.SemiBold,
                )
            } else if (modifiable) {
                TextButton(onClick = surModifier) { Text("Modifier") }
            }
        }
    }
}

/** Saisie faite hors ligne : visible immédiatement, clairement marquée comme non synchronisée. */
@Composable
private fun CarteEnAttente(element: ElementEnAttente) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(element.detail.ifBlank { element.titre }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${element.titre} — en attente de synchronisation",
                style = MaterialTheme.typography.labelMedium,
                color = SpiPmeTheme.extendedColors.avertissement,
            )
        }
    }
}

/** Le NIVEAU de stock n'est pas modifiable ici : il ne change que par des mouvements tracés. */
@Composable
private fun DialogueModificationRessource(
    ressource: Ressource,
    erreur: String?,
    surConfirmer: (String, String, String, String) -> Unit,
    surAbandonner: () -> Unit,
) {
    var nom by rememberSaveable(ressource.id) { mutableStateOf(ressource.nom) }
    var emplacement by rememberSaveable(ressource.id) { mutableStateOf(ressource.emplacement) }
    var critique by rememberSaveable(ressource.id) { mutableStateOf(ressource.seuilCritique?.toPlainString().orEmpty()) }
    var alerte by rememberSaveable(ressource.id) { mutableStateOf(ressource.seuilAlerte?.toPlainString().orEmpty()) }
    AlertDialog(
        onDismissRequest = surAbandonner,
        title = { Text("Modifier la ressource") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nom, { nom = it }, label = { Text("Nom") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(emplacement, { emplacement = it }, label = { Text("Emplacement") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(critique, { critique = it }, label = { Text("Seuil critique") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(alerte, { alerte = it }, label = { Text("Seuil d'alerte") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                erreur?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = { TextButton(onClick = { surConfirmer(nom, emplacement, critique, alerte) }) { Text("Enregistrer") } },
        dismissButton = { TextButton(onClick = surAbandonner) { Text("Annuler") } },
    )
}

private fun formaterQuantite(valeur: BigDecimal): String =
    if (valeur.compareTo(BigDecimal.ZERO) == 0) "0" else valeur.stripTrailingZeros().toPlainString()

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ResourcesScreenApercu() {
    SpiPmeAppTheme {
        ResourcesContenu(
            etat = ResourcesUiState(
                enChargement = false,
                secteurActifNom = "Commerce",
                resume = ResumeRessources(total = 76, critiques = 12, aSurveiller = 18, stables = 46),
                ressources = listOf(
                    Ressource(
                        1, "produit", "Lait en poudre", "unités", null,
                        BigDecimal("2"), BigDecimal("10"), BigDecimal("25"), "critique", "Entrepôt principal", 1, "Commerce",
                    ),
                ),
            ),
            surClicNotifications = {},
            surClicProfil = {},
            surClicSecteur = {},
            surChangementOnglet = {},
            surChangementRecherche = {},
            surClicNouvelleRessource = {},
        )
    }
}

