package com.spipme.app.ui.audit

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.domain.model.EvenementAudit
import com.spipme.app.domain.model.ResumeAudit
import com.spipme.app.ui.components.SpiPmeTextField
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeTheme

/**
 * Journal d'audit â€” LECTURE SEULE. Aucune action de modification ou de
 * suppression n'est proposÃ©e : le journal doit rester inaltÃ©rable (garanti
 * cÃ´tÃ© serveur par ReadOnlyModelViewSet + protections ORM + trigger).
 * AccÃ¨s restreint par la permission `audit.read` (rÃ´les Administrateur et
 * Auditeur) â€” contrÃ´le appliquÃ© cÃ´tÃ© serveur, pas seulement masquÃ© ici.
 */
@Composable
fun AuditScreen(
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    viewModel: AuditViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SpiPmeTopBar(
                secteurActifNom = etat.secteurActifNom,
                nombreNotificationsNonLues = 0,
                surClicSecteur = surClicSecteur,
                surClicNotifications = surClicNotifications,
                surClicProfil = surClicProfil,
            )
        },
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
                Text(
                    "Journal d'audit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Consultation en lecture seule des Ã©vÃ©nements du systÃ¨me",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                SpiPmeTextField(
                    valeur = etat.recherche,
                    surChangement = viewModel::surChangementRecherche,
                    libelle = "Rechercher une action, une cible...",
                    iconeDebut = { Icon(Icons.Filled.Search, contentDescription = null) },
                )
            }

            etat.resume?.let { resume ->
                item { CartesResumeAudit(resume) }
            }

            item {
                FiltresAudit(
                    filtreResultat = etat.filtreResultat,
                    surSelectionResultat = viewModel::surSelectionResultat,
                )
            }

            when {
                etat.chargementInitial -> item { EtatChargement() }
                etat.messageErreur != null -> item {
                    EtatErreur(message = etat.messageErreur!!, surReessayer = viewModel::rafraichir)
                }
                etat.estVide -> item { EtatVide() }
                else -> {
                    items(etat.evenements, key = { it.id }) { evenement ->
                        LigneEvenementAudit(evenement)
                    }
                    if (etat.ilResteDesPages) {
                        item {
                            if (etat.chargementPageSuivante) {
                                EtatChargement()
                            } else {
                                TextButton(
                                    onClick = viewModel::chargerPageSuivante,
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text("Charger plus d'Ã©vÃ©nements") }
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
private fun CartesResumeAudit(resume: ResumeAudit) {
    // Couleurs lues ici, dans un contexte @Composable valide (pas dans un
    // lambda LazyListScope), cf. correctif COMPILE-KOTLIN-002.
    val donnees = listOf(
        Triple("Total", resume.total, MaterialTheme.colorScheme.onSurface),
        Triple("RÃ©ussies", resume.reussies, SpiPmeTheme.extendedColors.succes),
        Triple("Avertissements", resume.avertissements, SpiPmeTheme.extendedColors.avertissement),
        Triple("Ã‰checs", resume.echecs, MaterialTheme.colorScheme.error),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        donnees.forEach { (libelle, valeur, couleur) ->
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        valeur.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = couleur,
                    )
                    Text(
                        libelle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltresAudit(filtreResultat: String?, surSelectionResultat: (String?) -> Unit) {
    val options = listOf(null to "Tous", "reussi" to "RÃ©ussis", "avertissement" to "Avertissements", "echec" to "Ã‰checs")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (valeur, libelle) ->
            FilterChip(
                selected = filtreResultat == valeur,
                onClick = { surSelectionResultat(valeur) },
                label = { Text(libelle, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@Composable
private fun LigneEvenementAudit(evenement: EvenementAudit) {
    val couleurResultat = when (evenement.resultat) {
        "reussi" -> SpiPmeTheme.extendedColors.succes
        "avertissement" -> SpiPmeTheme.extendedColors.avertissement
        else -> MaterialTheme.colorScheme.error
    }
    val iconeResultat = when (evenement.resultat) {
        "reussi" -> Icons.Filled.CheckCircle
        "avertissement" -> Icons.Filled.WarningAmber
        else -> Icons.Filled.ErrorOutline
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = iconeResultat,
                contentDescription = evenement.resultat,
                tint = couleurResultat,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    evenement.action,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    buildString {
                        append(evenement.module)
                        if (evenement.cibleType.isNotBlank()) {
                            append(" Â· ")
                            append(evenement.cibleType)
                            if (evenement.cibleId.isNotBlank()) append(" #${evenement.cibleId}")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    buildString {
                        append(evenement.dateAction)
                        evenement.auteurNom?.let { append(" Â· $it") }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EtatChargement() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EtatVide() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.HistoryToggleOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Aucun Ã©vÃ©nement d'audit ne correspond Ã  ces critÃ¨res.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EtatErreur(message: String, surReessayer: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
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
        TextButton(onClick = surReessayer) { Text("RÃ©essayer") }
    }
}

