package com.spipme.app.ui.treasury

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.domain.model.PointSolde
import com.spipme.app.domain.model.ResumeTresorerie
import com.spipme.app.domain.model.Transaction
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeAppTheme
import com.spipme.app.ui.theme.SpiPmeTheme
import java.math.BigDecimal

@Composable
fun TreasuryScreen(
    transactionVientDetreCreee: Boolean,
    surTransactionCreeeConsommee: () -> Unit,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surClicNouvelleTransaction: () -> Unit,
    viewModel: TreasuryViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(transactionVientDetreCreee) {
        if (transactionVientDetreCreee) {
            viewModel.rafraichir()
            surTransactionCreeeConsommee()
        }
    }

    TreasuryContenu(
        etat = etat,
        surClicNotifications = surClicNotifications,
        surClicProfil = surClicProfil,
        surClicSecteur = surClicSecteur,
        surChangementOnglet = viewModel::surChangementOnglet,
        surClicNouvelleTransaction = surClicNouvelleTransaction,
    )
}

@Composable
private fun TreasuryContenu(
    etat: TreasuryUiState,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surChangementOnglet: (OngletTresorerie) -> Unit,
    surClicNouvelleTransaction: () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = surClicNouvelleTransaction,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nouvelle transaction") },
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
                    Text("Trésorerie", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Suivez vos flux de trésorerie en temps réel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                etat.resume?.let { resume -> item { CartesResume(resume) } }

                if (etat.evolution.isNotEmpty()) {
                    item { GrapheEvolution(etat.evolution) }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(OngletTresorerie.values().toList()) { onglet ->
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
                } else if (etat.transactions.isEmpty()) {
                    item {
                        Text(
                            "Aucune transaction trouvée.",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(etat.transactions, key = { it.id }) { transaction -> CarteTransaction(transaction) }
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
private fun CartesResume(resume: ResumeTresorerie) {
    val donnees = listOf(
        Triple("Entrées (mois)", resume.entreesMois, SpiPmeTheme.extendedColors.succes),
        Triple("Sorties (mois)", resume.sortiesMois, MaterialTheme.colorScheme.error),
        Triple("Solde net (mois)", resume.soldeNetMois, MaterialTheme.colorScheme.primary),
        Triple("Solde disponible", resume.soldeDisponible, MaterialTheme.colorScheme.onSurface),
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(donnees) { (libelle, valeur, couleur) ->
            Card(
                modifier = Modifier.width(160.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${formaterMontant(valeur)} FCFA",
                        style = MaterialTheme.typography.titleMedium,
                        color = couleur,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(libelle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun GrapheEvolution(points: List<PointSolde>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Évolution du solde", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            val couleurLigne = MaterialTheme.colorScheme.primary
            val valeurs = points.map { it.solde.toDouble() }
            val minValeur = valeurs.minOrNull() ?: 0.0
            val maxValeur = valeurs.maxOrNull() ?: 1.0
            val etendue = (maxValeur - minValeur).takeIf { it > 0.0 } ?: 1.0

            Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                if (valeurs.size < 2) return@Canvas
                val largeurPas = size.width / (valeurs.size - 1)
                val chemin = androidx.compose.ui.graphics.Path()
                valeurs.forEachIndexed { index, valeur ->
                    val x = index * largeurPas
                    val yNormalise = ((valeur - minValeur) / etendue).toFloat()
                    val y = size.height - (yNormalise * size.height)
                    if (index == 0) chemin.moveTo(x, y) else chemin.lineTo(x, y)
                }
                drawPath(chemin, color = couleurLigne, style = Stroke(width = 4f))
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(points.first().date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(points.last().date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CarteTransaction(transaction: Transaction) {
    val estEntree = transaction.type == "entree"
    val couleur = if (estEntree) SpiPmeTheme.extendedColors.succes else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(couleur.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
            ) {
                Icon(
                    if (estEntree) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = couleur,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.description.ifBlank { if (estEntree) "Entrée" else "Sortie" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (transaction.entiteNom != null) {
                    Text(
                        "Client : ${transaction.entiteNom}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (transaction.reference.isNotBlank()) {
                    Text(
                        transaction.reference,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            transaction.montant?.let { montant ->
                Text(
                    "${if (estEntree) "+" else "-"}${formaterMontant(montant)} ${transaction.devise}",
                    style = MaterialTheme.typography.titleMedium,
                    color = couleur,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun formaterMontant(valeur: BigDecimal): String {
    val entier = valeur.abs().toBigInteger().toString()
    val avecEspaces = entier.reversed().chunked(3).joinToString(" ").reversed()
    return if (valeur.signum() < 0) "-$avecEspaces" else avecEspaces
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun TreasuryScreenApercu() {
    SpiPmeAppTheme {
        TreasuryContenu(
            etat = TreasuryUiState(
                enChargement = false,
                secteurActifNom = "Commerce",
                resume = ResumeTresorerie(
                    BigDecimal("12450000"), BigDecimal("8230000"), BigDecimal("4220000"), BigDecimal("28750000"),
                ),
                evolution = listOf(
                    PointSolde("22 juil.", BigDecimal("10000000")),
                    PointSolde("28 juil.", BigDecimal("28750000")),
                ),
                transactions = listOf(
                    Transaction(1, "entree", "FAC-2026-0452", "Vente de marchandises", BigDecimal("2450000"), null, "XOF", "virement", null, "Chez Martine SARL", null, null, 1, "2026-07-28"),
                ),
            ),
            surClicNotifications = {},
            surClicProfil = {},
            surClicSecteur = {},
            surChangementOnglet = {},
            surClicNouvelleTransaction = {},
        )
    }
}
