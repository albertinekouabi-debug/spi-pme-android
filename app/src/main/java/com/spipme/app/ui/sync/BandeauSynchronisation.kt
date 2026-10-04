package com.spipme.app.ui.sync

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.core.sync.LibelleOperation
import com.spipme.app.core.sync.OperationEnAttente
import com.spipme.app.core.sync.ResumeSynchronisation
import com.spipme.app.core.sync.StatutOperation

/**
 * Bandeau d'état de synchronisation, affiché sous l'en-tête de chaque écran (invisible quand tout est
 * à jour). Le libellé et la priorité viennent de EtatSynchronisation (testé). Un appui ouvre la
 * liste des opérations nécessitant une décision (conflits, échecs).
 */
@Composable
fun BandeauSynchronisation(viewModel: SyncStatusViewModel = hiltViewModel()) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    val aTraiter by viewModel.aTraiter.collectAsStateWithLifecycle()
    val dialogueOuvert by viewModel.dialogueOuvert.collectAsStateWithLifecycle()

    val message = etat.message
    if (message != null) {
        val (fond, contenu) = when (etat.resume) {
            ResumeSynchronisation.CONFLITS, ResumeSynchronisation.ECHECS ->
                MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
            ResumeSynchronisation.HORS_LIGNE ->
                MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }
        val necessiteDecision = etat.conflits + etat.echecs > 0
        Surface(
            color = fond,
            modifier = Modifier.fillMaxWidth().then(if (necessiteDecision) Modifier.clickable { viewModel.ouvrir() } else Modifier),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (etat.resume) {
                    ResumeSynchronisation.EN_COURS ->
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = contenu)
                    ResumeSynchronisation.HORS_LIGNE -> Icon(Icons.Filled.CloudOff, null, tint = contenu, modifier = Modifier.size(16.dp))
                    ResumeSynchronisation.CONFLITS -> Icon(Icons.Filled.Warning, null, tint = contenu, modifier = Modifier.size(16.dp))
                    ResumeSynchronisation.ECHECS -> Icon(Icons.Filled.ErrorOutline, null, tint = contenu, modifier = Modifier.size(16.dp))
                    else -> Icon(Icons.Filled.Sync, null, tint = contenu, modifier = Modifier.size(16.dp))
                }
                Text(message, style = MaterialTheme.typography.labelMedium, color = contenu)
            }
        }
    }

    if (dialogueOuvert) {
        DialogueOperationsATraiter(
            operations = aTraiter,
            surRelancer = viewModel::relancer,
            surAbandonner = viewModel::abandonner,
            surGarderMaVersion = viewModel::garderMaVersion,
            surFermer = viewModel::fermer,
        )
    }
}

@Composable
private fun DialogueOperationsATraiter(
    operations: List<OperationEnAttente>,
    surRelancer: (Long) -> Unit,
    surAbandonner: (Long) -> Unit,
    surGarderMaVersion: (Long) -> Unit,
    surFermer: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = surFermer,
        title = { Text("Opérations à traiter") },
        text = {
            if (operations.isEmpty()) {
                Text("Tout est synchronisé.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(operations, key = { it.id }) { op ->
                        Column {
                            Text(LibelleOperation.titre(op.type), style = MaterialTheme.typography.titleSmall)
                            LibelleOperation.detail(op.type, op.payloadJson).takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                op.messageErreur ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (op.statut == StatutOperation.CONFLIT && op.versionBase != null) {
                                    TextButton(onClick = { surGarderMaVersion(op.id) }) { Text("Garder ma version") }
                                }
                                if (op.statut == StatutOperation.ECHEC_DEFINITIF) {
                                    TextButton(onClick = { surRelancer(op.id) }) { Text("Réessayer") }
                                }
                                TextButton(onClick = { surAbandonner(op.id) }) {
                                    Text(if (op.statut == StatutOperation.CONFLIT) "Garder la version serveur" else "Abandonner")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = surFermer) { Text("Fermer") } },
    )
}
