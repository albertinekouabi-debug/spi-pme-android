package com.spipme.app.ui.dashboard

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.core.notifications.NotificationBadgeViewModel
import com.spipme.app.domain.model.PointSolde
import com.spipme.app.domain.model.Suggestion
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeAppTheme
import com.spipme.app.ui.theme.SpiPmeTheme

private data class IndicateurDashboard(
    val cle: String,
    val titre: String,
    val valeur: String,
    val detail: String,
    val icone: ImageVector,
    val couleur: Color,
    val action: () -> Unit,
)

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
    surClicImports: () -> Unit,
    surClicAudit: () -> Unit,
    surClicFactures: () -> Unit,
    surClicConformite: () -> Unit,
    surClicAdministration: () -> Unit,
    surClicPilotage: () -> Unit,
    surClicNouvelleTransaction: () -> Unit,
    surClicNouvelleRessource: () -> Unit,
    surClicNouvelleTache: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    val badgeViewModel: NotificationBadgeViewModel = hiltViewModel()
    val nombreNotifs by badgeViewModel.compte.collectAsStateWithLifecycle()

    val indicateurs = listOfNotNull(
        etat.resumeRessources?.let { resume ->
            IndicateurDashboard(
                cle = "ressources",
                titre = "Ressources critiques",
                valeur = resume.critiques.toString(),
                detail = if (resume.critiques == 1) "À surveiller" else "À traiter",
                icone = Icons.Filled.Inventory,
                couleur = if (resume.critiques > 0) MaterialTheme.colorScheme.error else SpiPmeTheme.extendedColors.succes,
                action = surClicRessources,
            )
        },
        etat.resumeTaches?.let { resume ->
            IndicateurDashboard(
                cle = "taches",
                titre = "Tâches en cours",
                valeur = resume.enCours.toString(),
                detail = if (resume.enRetard > 0) "${resume.enRetard} en retard" else "Aucun retard signalé",
                icone = Icons.Filled.Assignment,
                couleur = if (resume.enRetard > 0) MaterialTheme.colorScheme.error else SpiPmeTheme.extendedColors.avertissement,
                action = surClicTaches,
            )
        },
        etat.resumeAlertes?.let { resume ->
            val actives = resume.critiques + resume.elevees + resume.moderees
            IndicateurDashboard(
                cle = "alertes",
                titre = "Alertes actives",
                valeur = actives.toString(),
                detail = "${resume.critiques} critique(s)",
                icone = Icons.Filled.NotificationsActive,
                couleur = if (resume.critiques > 0) MaterialTheme.colorScheme.error else SpiPmeTheme.extendedColors.information,
                action = surClicAlertes,
            )
        },
        etat.resumeSuggestions?.let { resume ->
            IndicateurDashboard(
                cle = "ia",
                titre = "Suggestions IA",
                valeur = resume.enAttente.toString(),
                detail = "En attente de décision",
                icone = Icons.Filled.AutoAwesome,
                couleur = SpiPmeTheme.extendedColors.ia,
                action = surClicSuggestions,
            )
        },
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                modifier = Modifier.fillMaxSize().padding(paddingInterne),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Préparation de votre tableau de bord…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            etat.echecComplet -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingInterne).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = etat.messageErreur ?: "Les données du tableau de bord sont momentanément indisponibles.",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = viewModel::rafraichir) { Text("Réessayer") }
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingInterne),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "bienvenue") {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text(
                            text = "Vue d’ensemble",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Voici un aperçu de la situation de votre entreprise aujourd’hui.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                item(key = "bandeau-info") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape,
                                ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Les indicateurs affichés proviennent des données du secteur actif.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                item(key = "titre-indicateurs") {
                    EnteteSection("Indicateurs clés")
                }

                if (indicateurs.isEmpty()) {
                    item(key = "indicateurs-indisponibles") {
                        TexteEtatVide("Les indicateurs ne sont pas disponibles pour le moment.")
                    }
                } else {
                    item(key = "indicateurs") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(indicateurs, key = { it.cle }) { indicateur ->
                                CarteIndicateur(indicateur)
                            }
                        }
                    }
                }

                item(key = "titre-actions") {
                    EnteteSection("Actions rapides")
                }
                item(key = "actions-ligne-1") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        CarteActionRapide(
                            modifier = Modifier.weight(1f),
                            titre = "Nouvelle transaction",
                            icone = Icons.Filled.Add,
                            couleur = MaterialTheme.colorScheme.primary,
                            surClic = surClicNouvelleTransaction,
                        )
                        CarteActionRapide(
                            modifier = Modifier.weight(1f),
                            titre = "Nouvelle ressource",
                            icone = Icons.Filled.Inventory,
                            couleur = SpiPmeTheme.extendedColors.succes,
                            surClic = surClicNouvelleRessource,
                        )
                    }
                }
                item(key = "actions-ligne-2") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        CarteActionRapide(
                            modifier = Modifier.weight(1f),
                            titre = "Nouvelle tâche",
                            icone = Icons.Filled.Assignment,
                            couleur = SpiPmeTheme.extendedColors.ia,
                            surClic = surClicNouvelleTache,
                        )
                        CarteActionRapide(
                            modifier = Modifier.weight(1f),
                            titre = "Importer un fichier",
                            icone = Icons.Filled.CloudUpload,
                            couleur = SpiPmeTheme.extendedColors.information,
                            surClic = surClicImports,
                        )
                    }
                }

                item(key = "titre-graphique") {
                    EnteteSection("Évolution de la trésorerie", "7 derniers jours")
                }
                item(key = "graphique-tresorerie") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Solde par date disponible",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            GraphiqueTresorerie(etat.evolutionTresorerie)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Le graphique utilise l’historique renvoyé par l’API. Aucun point fictif n’est ajouté.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                item(key = "titre-activite") {
                    EnteteSection("Activité récente", "Voir le journal") { surClicAudit() }
                }
                if (etat.activitesRecentes.isEmpty()) {
                    item(key = "activite-vide") {
                        TexteEtatVide("Aucune activité récente n’est disponible pour ce secteur.")
                    }
                } else {
                    item(key = "activite-recentes") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                                etat.activitesRecentes.take(5).forEachIndexed { index, activite ->
                                    LigneActivite(activite)
                                    if (index < etat.activitesRecentes.take(5).lastIndex) {
                                        androidx.compose.material3.HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "titre-suggestions") {
                    EnteteSection("Suggestions IA à examiner", "Voir tout") { surClicSuggestions() }
                }
                if (etat.suggestionsRecentes.isEmpty()) {
                    item(key = "suggestions-vides") {
                        TexteEtatVide("Aucune suggestion en attente n’est disponible.")
                    }
                } else {
                    items(etat.suggestionsRecentes, key = { "suggestion-${it.id}" }) { suggestion ->
                        CarteSuggestion(suggestion = suggestion, surClic = surClicSuggestions)
                    }
                }

                item(key = "tous-modules") {
                    EnteteSection("Autres modules", "Ouvrir le journal") { surClicAudit() }
                }
                item(key = "module-registre") {
                    CarteModuleSimple("Registre", "Clients, fournisseurs et entités", surClicRegistre)
                }
                item(key = "module-tresorerie") {
                    CarteModuleSimple("Trésorerie", "Transactions et situation financière", surClicTresorerie)
                }
                item(key = "module-factures") {
                    CarteModuleSimple("Factures et conformité", "Suivre les factures et les contrôles", {
                        surClicFactures()
                    }, actionSecondaire = surClicConformite)
                }
                item(key = "module-pilotage") {
                    CarteModuleSimple("Pilotage intelligent", "Analyses et décisions assistées", surClicPilotage)
                }
                item(key = "module-administration") {
                    CarteModuleSimple("Administration", "Utilisateurs et rôles", surClicAdministration)
                }
            }
        }
    }
}

@Composable
private fun EnteteSection(
    titre: String,
    actionTexte: String? = null,
    action: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(titre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        if (actionTexte != null && action != null) {
            TextButton(onClick = action, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)) {
                Text(actionTexte, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun CarteIndicateur(indicateur: IndicateurDashboard) {
    Card(
        onClick = indicateur.action,
        modifier = Modifier.width(158.dp).heightIn(min = 158.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Box(
                modifier = Modifier.size(42.dp).background(indicateur.couleur.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(indicateur.icone, contentDescription = null, tint = indicateur.couleur)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = indicateur.valeur,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = indicateur.couleur,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = indicateur.titre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = indicateur.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CarteActionRapide(
    modifier: Modifier = Modifier,
    titre: String,
    icone: ImageVector,
    couleur: Color,
    surClic: () -> Unit,
) {
    Card(
        onClick = surClic,
        modifier = modifier.heightIn(min = 112.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(couleur, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icone, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = titre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GraphiqueTresorerie(pointsBruts: List<PointSolde>) {
    val points = pointsBruts.sortedBy { it.date }
    val couleurLigne = MaterialTheme.colorScheme.primary
    val couleurGrille = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)

    if (points.size < 2) {
        TexteEtatVide("L’historique ne contient pas encore assez de points pour afficher une courbe.")
        return
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        val valeurs = points.map { it.solde.toFloat() }
        val minimum = valeurs.minOrNull() ?: 0f
        val maximum = valeurs.maxOrNull() ?: 0f
        val amplitude = (maximum - minimum).takeIf { it > 0.0001f } ?: 1f
        val gauche = 8.dp.toPx()
        val droite = size.width - 8.dp.toPx()
        val haut = 8.dp.toPx()
        val bas = size.height - 8.dp.toPx()
        val hauteurUtile = bas - haut

        for (i in 0..3) {
            val y = haut + hauteurUtile * i / 3f
            drawLine(
                color = couleurGrille,
                start = Offset(gauche, y),
                end = Offset(droite, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        val chemin = Path()
        val positions = valeurs.mapIndexed { index, valeur ->
            val x = gauche + (droite - gauche) * index / (valeurs.size - 1).toFloat()
            val y = bas - ((valeur - minimum) / amplitude) * hauteurUtile
            Offset(x, y)
        }
        positions.forEachIndexed { index, position ->
            if (index == 0) chemin.moveTo(position.x, position.y) else chemin.lineTo(position.x, position.y)
        }
        drawPath(
            path = chemin,
            color = couleurLigne,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
        positions.forEach { position ->
            drawCircle(color = couleurLigne, radius = 4.dp.toPx(), center = position)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(dateCourte(points.first().date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(dateCourte(points[points.size / 2].date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(dateCourte(points.last().date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LigneActivite(activite: ActiviteDashboard) {
    val couleur = when (activite.categorie) {
        "alerte" -> MaterialTheme.colorScheme.error
        "suggestion" -> SpiPmeTheme.extendedColors.ia
        else -> SpiPmeTheme.extendedColors.succes
    }
    val icone = when (activite.categorie) {
        "alerte" -> Icons.Filled.NotificationsActive
        "suggestion" -> Icons.Filled.AutoAwesome
        else -> Icons.Filled.AccountBalanceWallet
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(couleur.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = couleur, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activite.titre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (activite.detail.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = activite.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(dateCourte(activite.dateIso), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CarteSuggestion(suggestion: Suggestion, surClic: () -> Unit) {
    Card(
        onClick = surClic,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(SpiPmeTheme.extendedColors.ia.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SpiPmeTheme.extendedColors.ia)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(suggestion.categorie.ifBlank { "Recommandation" }, style = MaterialTheme.typography.labelMedium, color = SpiPmeTheme.extendedColors.ia)
                    Text(
                        text = suggestion.titre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (suggestion.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = suggestion.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            suggestion.confiance?.let { confiance ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Indice de confiance : ${confiance.stripTrailingZeros().toPlainString()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpiPmeTheme.extendedColors.ia,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = surClic) { Text("Examiner la suggestion") }
            }
        }
    }
}

@Composable
private fun CarteModuleSimple(
    titre: String,
    description: String,
    surClic: () -> Unit,
    actionSecondaire: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = surClic) { Text("Ouvrir") }
            if (actionSecondaire != null) {
                TextButton(onClick = actionSecondaire) { Text("Conformité") }
            }
        }
    }
}

@Composable
private fun TexteEtatVide(texte: String) {
    Text(
        text = texte,
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun dateCourte(dateIso: String): String {
    val morceaux = dateIso.take(10).split("-")
    return if (morceaux.size == 3) "${morceaux[2]}/${morceaux[1]}" else dateIso.take(10)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun DashboardScreenApercu() {
    SpiPmeAppTheme {
        TexteEtatVide("Aperçu de la nouvelle présentation du tableau de bord.")
    }
}