package com.spipme.app.ui.pilotage

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.core.notifications.NotificationBadgeViewModel
import com.spipme.app.domain.model.Insight
import com.spipme.app.domain.model.Kpi
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeTheme
import java.text.DateFormat
import java.util.Date

/**
 * Pilotage intelligent. Tout provient du serveur (KPI et analyses DISTANTS, calculs déterministes) ; hors
 * ligne, la dernière analyse mémorisée est affichée avec sa date, et le copilote indique qu'il exige le réseau.
 * Faits, projections et recommandations sont présentés séparément : une projection n'est jamais un fait.
 */
@Composable
fun PilotageScreen(
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    viewModel: PilotageViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    val nombreNotifs by (hiltViewModel<NotificationBadgeViewModel>()).compte.collectAsStateWithLifecycle()

    Scaffold { padding ->
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
                    Text("Pilotage", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Indicateurs calculés sur vos données validées, analyses et copilote",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                when {
                    etat.enChargement -> item {
                        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    }
                    etat.pilotage != null -> {
                        val pilotage = etat.pilotage!!
                        pilotage.horsLigneDepuis?.let { quand ->
                            item {
                                Text(
                                    "Hors ligne — dernière analyse du " +
                                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(quand)),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SpiPmeTheme.extendedColors.avertissement,
                                )
                            }
                        }
                        item { LigneKpis(pilotage.kpis) }
                        item { Text("Analyses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                        items(pilotage.insights, key = { it.code + it.titre }) { CarteInsight(it) }
                    }
                    else -> item {
                        Column {
                            Text(
                                etat.messageErreur ?: "Analyse indisponible.",
                                color = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                "Les analyses sont calculées par le serveur : elles ne sont pas disponibles hors ligne tant qu'aucune n'a été mémorisée.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = viewModel::rafraichir) { Text("Réessayer") }
                        }
                    }
                }

                item { Text("Copilote", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(etat.suggestions) { suggestion ->
                            AssistChip(onClick = { viewModel.poser(suggestion) }, label = { Text(suggestion) })
                        }
                    }
                }
                items(etat.messages) { message -> BulleCopilote(message) }
                item {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = etat.question,
                            onValueChange = viewModel::surChangementQuestion,
                            label = { Text("Posez une question") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        IconButton(onClick = { viewModel.poser() }, enabled = etat.question.isNotBlank()) {
                            Icon(Icons.Filled.Send, contentDescription = "Envoyer la question")
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun LigneKpis(kpis: List<Kpi>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(kpis) { kpi ->
            Card(
                modifier = Modifier.width(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(kpi.libelle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // Pas de donnée → « Information insuffisante », jamais un 0 trompeur.
                    Text(
                        kpi.valeur ?: "Information insuffisante",
                        style = if (kpi.valeur == null) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    kpi.detail?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@Composable
private fun CarteInsight(insight: Insight) {
    val couleur: Color = when (insight.niveau) {
        "critique" -> MaterialTheme.colorScheme.error
        "attention" -> SpiPmeTheme.extendedColors.avertissement
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(insight.niveau.uppercase(), style = MaterialTheme.typography.labelSmall, color = couleur, fontWeight = FontWeight.Bold)
            Text(insight.titre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(insight.analyse, style = MaterialTheme.typography.bodyMedium)

            if (insight.faits.isNotEmpty()) {
                Text("Faits", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                insight.faits.forEach { fait ->
                    Text("• ${fait.libelle} : ${fait.valeur}", style = MaterialTheme.typography.bodySmall)
                    if (fait.source.isNotBlank()) {
                        Text("   source : ${fait.source}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            insight.projection?.let {
                Text("Projection (non acquise)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            if (insight.options.isNotEmpty()) {
                Text("Options", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                insight.options.forEach { (option, justification) ->
                    Text("• $option", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    if (justification.isNotBlank()) {
                        Text("   $justification", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text(
                "Confiance : ${insight.confiance} · Limites : ${insight.limites}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BulleCopilote(message: MessageCopilote) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(message.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        when {
            message.enCours -> CircularProgressIndicator(modifier = Modifier.height(20.dp).width(20.dp), strokeWidth = 2.dp)
            else -> Text(
                message.reponse.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.erreur) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
