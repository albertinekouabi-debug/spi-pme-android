package com.spipme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.domain.model.Secteur
import com.spipme.app.ui.sync.BandeauSynchronisation

/**
 * En-tête présent sur les 16 maquettes : sélecteur "Secteur actif",
 * notifications (badge = nombre non lues), avatar utilisateur. Un seul
 * composant réutilisé partout plutôt que réimplémenté écran par écran.
 *
 * Le sélecteur de secteur est AUTO-CONTENU (SecteurSelectorViewModel) : le
 * clic ouvre directement le dialogue de choix, chargé depuis GET /secteurs
 * (jamais une liste inventée). Aucun écran appelant n'a besoin d'être
 * modifié — surClicSecteur reste appelé en plus, pour un usage futur
 * (analytics...), mais n'est plus le seul déclencheur du sélecteur.
 */
@Composable
fun SpiPmeTopBar(
    secteurActifNom: String,
    nombreNotificationsNonLues: Int,
    surClicSecteur: () -> Unit,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    modifier: Modifier = Modifier,
    selecteurViewModel: SecteurSelectorViewModel = hiltViewModel(),
) {
    val etatSelecteur by selecteurViewModel.uiState.collectAsStateWithLifecycle()

    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelecteurSecteur(
                secteurActifNom = secteurActifNom,
                surClic = {
                    surClicSecteur()
                    selecteurViewModel.ouvrir()
                },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            BoutonNotifications(nombreNotificationsNonLues, surClicNotifications)
            Spacer(Modifier.width(12.dp))
            AvatarUtilisateur(surClicProfil)
        }
        BandeauSynchronisation()
    }

    if (etatSelecteur.ouvert) {
        DialogueSelecteurSecteur(
            etat = etatSelecteur,
            secteurActifNom = secteurActifNom,
            surSelection = selecteurViewModel::selectionner,
            surFermer = selecteurViewModel::fermer,
        )
    }
}

@Composable
private fun DialogueSelecteurSecteur(
    etat: SecteurSelectorUiState,
    secteurActifNom: String,
    surSelection: (Secteur) -> Unit,
    surFermer: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = surFermer,
        title = { Text("Changer de secteur") },
        text = {
            when {
                etat.chargement -> Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                ) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) }

                etat.messageErreur != null -> Text(
                    etat.messageErreur,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )

                etat.secteurs.isEmpty() -> Text(
                    "Aucun secteur accessible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> LazyColumn {
                    items(etat.secteurs, key = { it.id }) { secteur ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { surSelection(secteur) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(secteur.nom, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            if (secteur.nom == secteurActifNom) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Secteur actif",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = surFermer) { Text("Fermer") } },
    )
}

@Composable
private fun RowScope.SelecteurSecteur(secteurActifNom: String, surClic: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clickable(onClick = surClic)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Column {
            Text("Secteur actif", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(secteurActifNom, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.weight(1f, fill = false))
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Changer de secteur")
    }
}

@Composable
private fun BoutonNotifications(nombreNonLues: Int, surClic: () -> Unit) {
    BadgedBox(
        badge = {
            if (nombreNonLues > 0) {
                Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text(if (nombreNonLues > 99) "99+" else nombreNonLues.toString())
                }
            }
        }
    ) {
        IconButton(onClick = surClic) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
        }
    }
}

@Composable
private fun AvatarUtilisateur(surClic: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = surClic)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Person, contentDescription = "Profil", tint = MaterialTheme.colorScheme.primary)
    }
}

