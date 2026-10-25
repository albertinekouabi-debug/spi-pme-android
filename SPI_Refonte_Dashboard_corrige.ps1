$ErrorActionPreference = "Stop"
$projectRoot = "C:\Android\spi_pme_android"
Set-Location $projectRoot

$dashboardPath = Join-Path $projectRoot "app\src\main\java\com\spipme\app\ui\dashboard\DashboardScreen.kt"
$viewModelPath = Join-Path $projectRoot "app\src\main\java\com\spipme\app\ui\dashboard\DashboardViewModel.kt"
$navPath = Join-Path $projectRoot "app\src\main\java\com\spipme\app\ui\navigation\SpiPmeNavGraph.kt"
$resourcesScreenPath = Join-Path $projectRoot "app\src\main\java\com\spipme\app\ui\resources\ResourcesScreen.kt"
$resourcesStatePath = Join-Path $projectRoot "app\src\main\java\com\spipme\app\ui\resources\ResourcesUiState.kt"
$resourcesViewModelPath = Join-Path $projectRoot "app\src\main\java\com\spipme\app\ui\resources\ResourcesViewModel.kt"

foreach ($path in @($dashboardPath, $viewModelPath, $navPath, $resourcesScreenPath, $resourcesStatePath, $resourcesViewModelPath)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Fichier obligatoire introuvable : $path. Aucun fichier n'a été modifié."
    }
}

$dashboardSource = @'
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
'@

$viewModelSource = @'
package com.spipme.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Alerte
import com.spipme.app.domain.model.PointSolde
import com.spipme.app.domain.model.ResumeAlertes
import com.spipme.app.domain.model.ResumeRegistre
import com.spipme.app.domain.model.ResumeRessources
import com.spipme.app.domain.model.ResumeSuggestions
import com.spipme.app.domain.model.ResumeTaches
import com.spipme.app.domain.model.ResumeTresorerie
import com.spipme.app.domain.model.Suggestion
import com.spipme.app.domain.model.Transaction
import com.spipme.app.domain.repository.AlerteRepository
import com.spipme.app.domain.repository.EntiteRepository
import com.spipme.app.domain.repository.RessourceRepository
import com.spipme.app.domain.repository.SuggestionRepository
import com.spipme.app.domain.repository.TacheRepository
import com.spipme.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.math.BigDecimal
import javax.inject.Inject

data class ActiviteDashboard(
    val id: String,
    val categorie: String,
    val titre: String,
    val detail: String,
    val dateIso: String,
)

data class DashboardUiState(
    val chargement: Boolean = true,
    val secteurActifNom: String = "",
    val resumeTresorerie: ResumeTresorerie? = null,
    val resumeRegistre: ResumeRegistre? = null,
    val resumeRessources: ResumeRessources? = null,
    val resumeTaches: ResumeTaches? = null,
    val resumeAlertes: ResumeAlertes? = null,
    val resumeSuggestions: ResumeSuggestions? = null,
    val evolutionTresorerie: List<PointSolde> = emptyList(),
    val activitesRecentes: List<ActiviteDashboard> = emptyList(),
    val suggestionsRecentes: List<Suggestion> = emptyList(),
    val messageErreur: String? = null,
) {
    val echecComplet: Boolean get() = !chargement && listOf(
        resumeTresorerie,
        resumeRegistre,
        resumeRessources,
        resumeTaches,
        resumeAlertes,
        resumeSuggestions,
    ).all { it == null } && activitesRecentes.isEmpty() && suggestionsRecentes.isEmpty() && evolutionTresorerie.size < 2
}

/** Agrège les résumés et historiques réellement fournis par les repositories du projet. */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val entiteRepository: EntiteRepository,
    private val ressourceRepository: RessourceRepository,
    private val tacheRepository: TacheRepository,
    private val alerteRepository: AlerteRepository,
    private val suggestionRepository: SuggestionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private var chargementJob: Job? = null

    init { charger() }

    fun rafraichir() = charger()

    private fun charger() {
        if (chargementJob?.isActive == true) return

        chargementJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    chargement = true,
                    resumeTresorerie = null,
                    resumeRegistre = null,
                    resumeRessources = null,
                    resumeTaches = null,
                    resumeAlertes = null,
                    resumeSuggestions = null,
                    evolutionTresorerie = emptyList(),
                    activitesRecentes = emptyList(),
                    suggestionsRecentes = emptyList(),
                    messageErreur = null,
                )
            }

            val secteurNom = withTimeoutOrNull(2_000L) {
                try {
                    sessionManager.secteurActifNomFlow.first().orEmpty()
                } catch (annulation: CancellationException) {
                    throw annulation
                } catch (_: Exception) {
                    ""
                }
            }.orEmpty()

            val secteurId = withTimeoutOrNull(5_000L) {
                sessionManager.secteurActifIdFlow.filterNotNull().first()
            }

            if (secteurId == null) {
                _uiState.update {
                    it.copy(
                        chargement = false,
                        secteurActifNom = secteurNom,
                        messageErreur = "Aucun secteur actif n’est disponible. Choisissez un secteur puis réessayez.",
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(secteurActifNom = secteurNom) }

            coroutineScope {
                launch {
                    val resultat = resultatSecurise { transactionRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeTresorerie = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { entiteRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeRegistre = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { ressourceRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeRessources = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { tacheRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeTaches = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { alerteRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeAlertes = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { suggestionRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeSuggestions = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { transactionRepository.obtenirEvolution(secteurId, jours = 7) }
                    if (resultat is Resultat.Succes) {
                        _uiState.update { it.copy(evolutionTresorerie = resultat.donnees.sortedBy(PointSolde::date)) }
                    }
                }
                launch {
                    val resultat = resultatSecurise { transactionRepository.lister(secteurId) }
                    if (resultat is Resultat.Succes) {
                        ajouterActivites(resultat.donnees.map(::activiteTransaction))
                    }
                }
                launch {
                    val resultat = resultatSecurise { alerteRepository.lister(secteurId, statut = null, niveau = null) }
                    if (resultat is Resultat.Succes) {
                        ajouterActivites(resultat.donnees.map(::activiteAlerte))
                    }
                }
                launch {
                    val resultat = resultatSecurise { suggestionRepository.lister(secteurId, statut = "en_attente") }
                    if (resultat is Resultat.Succes) {
                        val suggestions = resultat.donnees
                            .filter { it.estEnAttente }
                            .sortedByDescending { it.dateCreation }
                            .take(3)
                        _uiState.update { it.copy(suggestionsRecentes = suggestions) }
                        ajouterActivites(suggestions.map(::activiteSuggestion))
                    }
                }
            }

            val final = _uiState.value
            val resumesIndisponibles = listOf(
                final.resumeTresorerie,
                final.resumeRegistre,
                final.resumeRessources,
                final.resumeTaches,
                final.resumeAlertes,
                final.resumeSuggestions,
            ).all { it == null }
            val sourcesPartielles = listOf(
                final.resumeTresorerie,
                final.resumeRegistre,
                final.resumeRessources,
                final.resumeTaches,
                final.resumeAlertes,
                final.resumeSuggestions,
            ).any { it == null }

            _uiState.update {
                it.copy(
                    chargement = false,
                    messageErreur = when {
                        resumesIndisponibles && it.activitesRecentes.isEmpty() && it.suggestionsRecentes.isEmpty() && it.evolutionTresorerie.size < 2 ->
                            "Les données n’ont pas pu être chargées. Vérifiez la connexion puis réessayez."
                        sourcesPartielles ->
                            "Certaines données sont temporairement indisponibles. Les autres sections restent accessibles."
                        else -> null
                    },
                )
            }
        }
    }

    private fun ajouterActivites(nouvelles: List<ActiviteDashboard>) {
        if (nouvelles.isEmpty()) return
        _uiState.update { ancien ->
            ancien.copy(
                activitesRecentes = (ancien.activitesRecentes + nouvelles)
                    .distinctBy { it.id }
                    .sortedByDescending { it.dateIso }
                    .take(5),
            )
        }
    }

    private fun activiteTransaction(transaction: Transaction) = ActiviteDashboard(
        id = "transaction-${transaction.id}",
        categorie = "transaction",
        titre = transaction.reference.ifBlank { libelleTypeTransaction(transaction.type) },
        detail = listOfNotNull(
            transaction.description.takeIf { it.isNotBlank() },
            transaction.entiteNom?.takeIf { it.isNotBlank() },
            transaction.montant?.let { "${it.stripTrailingZeros().toPlainString()} ${transaction.devise}" },
        ).joinToString(" · ").ifBlank { libelleTypeTransaction(transaction.type) },
        dateIso = transaction.dateTransaction,
    )

    private fun activiteAlerte(alerte: Alerte) = ActiviteDashboard(
        id = "alerte-${alerte.id}",
        categorie = "alerte",
        titre = alerte.titre,
        detail = listOfNotNull(
            alerte.description.takeIf { it.isNotBlank() },
            alerte.ressourceNom,
            alerte.entiteNom,
        ).joinToString(" · "),
        dateIso = alerte.dateDeclenchement,
    )

    private fun activiteSuggestion(suggestion: Suggestion) = ActiviteDashboard(
        id = "suggestion-${suggestion.id}",
        categorie = "suggestion",
        titre = suggestion.titre,
        detail = suggestion.description.ifBlank { suggestion.categorie },
        dateIso = suggestion.dateCreation,
    )

    private fun libelleTypeTransaction(type: String): String = when (type.lowercase()) {
        "entree" -> "Entrée de fonds"
        "sortie" -> "Sortie de fonds"
        "mouvement_stock" -> "Mouvement de stock"
        else -> type.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }

    private suspend fun <T> resultatSecurise(action: suspend () -> Resultat<T>): Resultat<T>? =
        withTimeoutOrNull(15_000L) {
            try {
                action()
            } catch (annulation: CancellationException) {
                throw annulation
            } catch (_: Exception) {
                null
            }
        }
}
'@

# Préparer l’évolution des stocks à partir du endpoint déjà déclaré dans RessourceApi.
$resourcesSource = [System.IO.File]::ReadAllText($resourcesScreenPath).Replace("`r`n", "`n").Replace("`r", "`n")
$resourcesStateSource = [System.IO.File]::ReadAllText($resourcesStatePath).Replace("`r`n", "`n").Replace("`r", "`n")
$resourcesViewModelSource = [System.IO.File]::ReadAllText($resourcesViewModelPath).Replace("`r`n", "`n").Replace("`r", "`n")

$resourceScreenImports = @(
    "import androidx.compose.foundation.Canvas"
    "import androidx.compose.ui.geometry.Offset"
    "import androidx.compose.ui.graphics.Color"
    "import androidx.compose.ui.graphics.Path"
    "import androidx.compose.ui.graphics.StrokeCap"
    "import androidx.compose.ui.graphics.drawscope.Stroke"
    "import androidx.compose.material.icons.filled.ShowChart"
)
foreach ($imp in $resourceScreenImports) {
    if (-not $resourcesSource.Contains($imp)) {
        $resourcesSource = $resourcesSource.Replace(
            "package com.spipme.app.ui.resources",
            "package com.spipme.app.ui.resources`n`n$imp"
        )
    }
}

$resourceSummaryAnchor = '                etat.resume?.let { resume -> item { CartesResume(resume) } }'
if ([regex]::Matches($resourcesSource, [regex]::Escape($resourceSummaryAnchor)).Count -ne 1) {
    throw "Le point d'insertion du graphique dans ResourcesScreen.kt n'est pas unique. Aucun fichier modifié."
}
$resourceSummaryReplacement = @'
                etat.resume?.let { resume -> item { CartesResume(resume) } }

                item(key = "evolution-stocks") {
                    CarteEvolutionStocks(
                        series = etat.evolutionsStock,
                        enCours = etat.chargementEvolutionStock,
                    )
                }
'@
$resourcesSource = $resourcesSource.Replace($resourceSummaryAnchor, $resourceSummaryReplacement.TrimEnd("`r", "`n"))

$resourceChartFunction = @'
@Composable
private fun CarteEvolutionStocks(series: List<EvolutionStock>, enCours: Boolean) {
    val couleurs = listOf(
        MaterialTheme.colorScheme.primary,
        SpiPmeTheme.extendedColors.avertissement,
        SpiPmeTheme.extendedColors.information,
    )
    val seriesVisibles = series.filter { it.points.size >= 2 }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Évolution des niveaux de stock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Historique disponible sur 7 jours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            when {
                enCours -> Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Chargement de l’historique…", style = MaterialTheme.typography.bodySmall)
                }

                seriesVisibles.isEmpty() -> Text(
                    "L’API ne fournit pas encore assez de points historiques pour tracer une courbe. Les données ne sont pas simulées.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 10.dp),
                )

                else -> {
                    val grille = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                    Canvas(modifier = Modifier.fillMaxWidth().height(158.dp)) {
                        val valeurs = seriesVisibles.flatMap { it.points }.map { it.niveau.toFloat() }
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
                                color = grille,
                                start = Offset(gauche, y),
                                end = Offset(droite, y),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }

                        seriesVisibles.forEachIndexed { index, serie ->
                            val donnees = serie.points.map { it.niveau.toFloat() }
                            val positions = donnees.mapIndexed { pointIndex, valeur ->
                                val fractionX = if (donnees.size < 2) 0f else pointIndex / (donnees.size - 1).toFloat()
                                val x = gauche + (droite - gauche) * fractionX
                                val y = bas - ((valeur - minimum) / amplitude) * hauteurUtile
                                Offset(x, y)
                            }
                            val chemin = Path()
                            positions.forEachIndexed { pointIndex, position ->
                                if (pointIndex == 0) chemin.moveTo(position.x, position.y)
                                else chemin.lineTo(position.x, position.y)
                            }
                            val couleur = couleurs[index % couleurs.size]
                            drawPath(chemin, couleur, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round))
                            positions.forEach { position -> drawCircle(couleur, radius = 3.5.dp.toPx(), center = position) }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        seriesVisibles.forEachIndexed { index, serie ->
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier.size(9.dp).background(couleurs[index % couleurs.size], CircleShape),
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    serie.nom,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    val premiereSerie = seriesVisibles.first().points
                    if (premiereSerie.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(dateCourteGraphique(premiereSerie.first().date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(dateCourteGraphique(premiereSerie.last().date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private fun dateCourteGraphique(dateIso: String): String {
    val parties = dateIso.take(10).split("-")
    return if (parties.size == 3) "${parties[2]}/${parties[1]}" else dateIso.take(10)
}

'@
$resourceChartAnchor = "@Composable`nprivate fun CarteRessource("
if ([regex]::Matches($resourcesSource, [regex]::Escape($resourceChartAnchor)).Count -ne 1) {
    throw "Le point d'insertion du composant de graphique des stocks n'est pas unique. Aucun fichier modifié."
}
$resourcesSource = $resourcesSource.Replace($resourceChartAnchor, $resourceChartFunction + $resourceChartAnchor)

# Ajouter le modèle d'une série au state des ressources.
$resourcesStateImports = @(
    "import com.spipme.app.domain.model.PointEvolution"
)
foreach ($imp in $resourcesStateImports) {
    if (-not $resourcesStateSource.Contains($imp)) {
        $resourcesStateSource = $resourcesStateSource.Replace(
            "package com.spipme.app.ui.resources",
            "package com.spipme.app.ui.resources`n`n$imp"
        )
    }
}
$stateClassAnchor = "data class ResourcesUiState("
if ([regex]::Matches($resourcesStateSource, [regex]::Escape($stateClassAnchor)).Count -ne 1) {
    throw "Déclaration ResourcesUiState inattendue. Aucun fichier modifié."
}
$resourcesStateSource = $resourcesStateSource.Replace(
    $stateClassAnchor,
    "data class EvolutionStock(val ressourceId: Int, val nom: String, val points: List<PointEvolution>)`n`n$stateClassAnchor"
)
$stateResumeAnchor = '    val resume: ResumeRessources? = null,'
if ([regex]::Matches($resourcesStateSource, [regex]::Escape($stateResumeAnchor)).Count -ne 1) {
    throw "Champ resume inattendu dans ResourcesUiState.kt. Aucun fichier modifié."
}
$resourcesStateSource = $resourcesStateSource.Replace(
    $stateResumeAnchor,
    $stateResumeAnchor + "`n    val evolutionsStock: List<EvolutionStock> = emptyList(),`n    val chargementEvolutionStock: Boolean = false,"
)

# Charger jusqu'à trois séries réelles, annuler un chargement devenu obsolète et éviter les requêtes répétées sur les mêmes ressources.
$resourcesVmImports = @(
    "import kotlinx.coroutines.CancellationException"
    "import kotlinx.coroutines.Job"
    "import kotlinx.coroutines.async"
    "import kotlinx.coroutines.awaitAll"
    "import kotlinx.coroutines.coroutineScope"
    "import kotlinx.coroutines.withTimeoutOrNull"
)
foreach ($imp in $resourcesVmImports) {
    if (-not $resourcesViewModelSource.Contains($imp)) {
        $resourcesViewModelSource = $resourcesViewModelSource.Replace(
            "package com.spipme.app.ui.resources",
            "package com.spipme.app.ui.resources`n`n$imp"
        )
    }
}
$resourceFieldAnchor = '    private var secteurActifId: Int? = null'
if ([regex]::Matches($resourcesViewModelSource, [regex]::Escape($resourceFieldAnchor)).Count -ne 1) {
    throw "Point d'insertion des champs de chargement des évolutions introuvable. Aucun fichier modifié."
}
$resourcesViewModelSource = $resourcesViewModelSource.Replace(
    $resourceFieldAnchor,
    $resourceFieldAnchor + "`n    private var evolutionJob: Job? = null`n    private var idsEvolutionChargee: List<Int> = emptyList()"
)
$resourceSuccessAnchor = '            is Resultat.Succes -> _uiState.update { it.copy(ressources = resultat.donnees, messageErreur = null) }'
if ([regex]::Matches($resourcesViewModelSource, [regex]::Escape($resourceSuccessAnchor)).Count -ne 1) {
    throw "Bloc de succès du chargement des ressources inattendu. Aucun fichier modifié."
}
$resourceSuccessReplacement = @'
            is Resultat.Succes -> {
                _uiState.update { it.copy(ressources = resultat.donnees, messageErreur = null) }
                chargerEvolutionStocks(resultat.donnees)
            }
'@
$resourcesViewModelSource = $resourcesViewModelSource.Replace($resourceSuccessAnchor, $resourceSuccessReplacement.TrimEnd("`r", "`n"))

$resourceRefreshAnchor = '    fun rafraichir() {'
if ([regex]::Matches($resourcesViewModelSource, [regex]::Escape($resourceRefreshAnchor)).Count -ne 1) {
    throw "Fonction rafraichir de ResourcesViewModel.kt introuvable. Aucun fichier modifié."
}
$resourcesViewModelSource = $resourcesViewModelSource.Replace(
    $resourceRefreshAnchor,
    "    fun rafraichir() {`n        idsEvolutionChargee = emptyList()`n        evolutionJob?.cancel()"
)

$resourceEvolutionMethod = @'
    private fun chargerEvolutionStocks(ressources: List<Ressource>) {
        val selection = ressources.filter { it.id > 0 }.take(3)
        val ids = selection.map { it.id }
        if (ids == idsEvolutionChargee && (ids.isNotEmpty() || _uiState.value.evolutionsStock.isEmpty())) return

        idsEvolutionChargee = ids
        evolutionJob?.cancel()

        if (selection.isEmpty()) {
            _uiState.update { it.copy(evolutionsStock = emptyList(), chargementEvolutionStock = false) }
            return
        }

        _uiState.update { it.copy(chargementEvolutionStock = true) }
        evolutionJob = viewModelScope.launch {
            val series = coroutineScope {
                selection.map { ressource ->
                    async {
                        val resultat = withTimeoutOrNull(12_000L) {
                            try {
                                ressourceRepository.obtenirEvolution(ressource.id, jours = 7)
                            } catch (annulation: CancellationException) {
                                throw annulation
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (resultat is Resultat.Succes && resultat.donnees.isNotEmpty()) {
                            EvolutionStock(
                                ressourceId = ressource.id,
                                nom = ressource.nom,
                                points = resultat.donnees.sortedBy { it.date },
                            )
                        } else null
                    }
                }.awaitAll().filterNotNull()
            }
            _uiState.update {
                it.copy(evolutionsStock = series, chargementEvolutionStock = false)
            }
        }
    }

'@
$resourceToutAnchor = '    private suspend fun chargerTout() {'
if ([regex]::Matches($resourcesViewModelSource, [regex]::Escape($resourceToutAnchor)).Count -ne 1) {
    throw "Point d'insertion de chargerEvolutionStocks introuvable. Aucun fichier modifié."
}
$resourcesViewModelSource = $resourcesViewModelSource.Replace($resourceToutAnchor, $resourceEvolutionMethod + $resourceToutAnchor)

# Préparer la nouvelle navigation sans écrire immédiatement.
$navSource = [System.IO.File]::ReadAllText($navPath).Replace("`r`n", "`n").Replace("`r", "`n")
$navOriginal = $navSource

$dashboardCallAnchor = @'
                surClicPilotage = { navController.navigate(Ecran.Pilotage.route) },
            )
        }
    }
}
'@
$dashboardCallReplacement = @'
                surClicPilotage = { navController.navigate(Ecran.Pilotage.route) },
                surClicNouvelleTransaction = { navController.navigate(Ecran.CreationTransaction.route) },
                surClicNouvelleRessource = { navController.navigate(Ecran.CreationRessource.route) },
                surClicNouvelleTache = { navController.navigate(Ecran.CreationTache.route) },
            )
        }
    }
}
'@
if ([regex]::Matches($navSource, [regex]::Escape($dashboardCallAnchor)).Count -ne 1) {
    throw "Le bloc Dashboard de SpiPmeNavGraph.kt n'est pas unique. Aucun fichier modifié."
}
$navSource = $navSource.Replace($dashboardCallAnchor, $dashboardCallReplacement)

$navImports = @(
    "import androidx.compose.foundation.layout.Column"
    "import androidx.compose.foundation.layout.fillMaxSize"
    "import androidx.compose.foundation.layout.heightIn"
    "import androidx.compose.foundation.layout.padding"
    "import androidx.compose.foundation.rememberScrollState"
    "import androidx.compose.foundation.verticalScroll"
    "import androidx.compose.material.icons.Icons"
    "import androidx.compose.material.icons.filled.Assignment"
    "import androidx.compose.material.icons.filled.Home"
    "import androidx.compose.material.icons.filled.Inventory"
    "import androidx.compose.material.icons.filled.MoreHoriz"
    "import androidx.compose.material.icons.filled.Notifications"
    "import androidx.compose.material3.AlertDialog"
    "import androidx.compose.material3.Icon"
    "import androidx.compose.material3.MaterialTheme"
    "import androidx.compose.material3.NavigationBar"
    "import androidx.compose.material3.NavigationBarItem"
    "import androidx.compose.material3.Scaffold"
    "import androidx.compose.material3.Text"
    "import androidx.compose.material3.TextButton"
    "import androidx.compose.runtime.mutableStateOf"
    "import androidx.compose.runtime.rememberSaveable"
    "import androidx.compose.runtime.setValue"
    "import androidx.compose.ui.Modifier"
    "import androidx.compose.ui.unit.dp"
    "import androidx.navigation.compose.currentBackStackEntryAsState"
)
$packageLine = "package com.spipme.app.ui.navigation"
if (-not $navSource.Contains($packageLine)) {
    throw "Ligne package attendue introuvable dans SpiPmeNavGraph.kt. Aucun fichier modifié."
}
$inserts = @()
foreach ($imp in $navImports) {
    if (-not $navSource.Contains($imp)) { $inserts += $imp }
}
$inserts = @($inserts | Select-Object -Unique)
if ($inserts.Count -gt 0) {
    $navSource = $navSource.Replace($packageLine, $packageLine + "`r`n`r`n" + ($inserts -join "`r`n"))
}

$openAnchor = "    NavHost(navController = navController, startDestination = ecranDepart) {"
if ([regex]::Matches($navSource, [regex]::Escape($openAnchor)).Count -ne 1) {
    throw "L'ouverture du NavHost n'est pas unique. Aucun fichier modifié."
}
$openReplacement = @'
    val entreeCourante by navController.currentBackStackEntryAsState()
    val routeActuelle = entreeCourante?.destination?.route
    var plusOuvert by rememberSaveable { mutableStateOf(false) }

    val routesAvecNavigation = setOf(
        Ecran.Accueil.route,
        Ecran.Dashboard.route,
        Ecran.Registre.route,
        Ecran.Ressources.route,
        Ecran.Tresorerie.route,
        Ecran.Taches.route,
        Ecran.Suggestions.route,
        Ecran.Alertes.route,
        Ecran.Imports.route,
        Ecran.Audit.route,
        Ecran.Factures.route,
        Ecran.Conformite.route,
        Ecran.Profil.route,
        Ecran.Administration.route,
        Ecran.Pilotage.route,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (routeActuelle in routesAvecNavigation) {
                val accueilSelectionne = routeActuelle == Ecran.Accueil.route || routeActuelle == Ecran.Dashboard.route
                val plusSelectionne = plusOuvert || (
                    routeActuelle != Ecran.Accueil.route &&
                    routeActuelle != Ecran.Dashboard.route &&
                    routeActuelle != Ecran.Alertes.route &&
                    routeActuelle != Ecran.Ressources.route &&
                    routeActuelle != Ecran.Taches.route
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    NavigationBarItem(
                        selected = accueilSelectionne && !plusOuvert,
                        onClick = {
                            plusOuvert = false
                            val destination = if (routeActuelle == Ecran.Accueil.route) Ecran.Accueil.route else Ecran.Dashboard.route
                            if (routeActuelle != destination) navController.navigate(destination) { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("Accueil") },
                    )
                    NavigationBarItem(
                        selected = routeActuelle == Ecran.Alertes.route && !plusOuvert,
                        onClick = {
                            plusOuvert = false
                            if (routeActuelle != Ecran.Alertes.route) navController.navigate(Ecran.Alertes.route) { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                        label = { Text("Alertes") },
                    )
                    NavigationBarItem(
                        selected = routeActuelle == Ecran.Ressources.route && !plusOuvert,
                        onClick = {
                            plusOuvert = false
                            if (routeActuelle != Ecran.Ressources.route) navController.navigate(Ecran.Ressources.route) { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Filled.Inventory, contentDescription = null) },
                        label = { Text("Ressources") },
                    )
                    NavigationBarItem(
                        selected = routeActuelle == Ecran.Taches.route && !plusOuvert,
                        onClick = {
                            plusOuvert = false
                            if (routeActuelle != Ecran.Taches.route) navController.navigate(Ecran.Taches.route) { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Filled.Assignment, contentDescription = null) },
                        label = { Text("Tâches") },
                    )
                    NavigationBarItem(
                        selected = plusSelectionne,
                        onClick = { plusOuvert = true },
                        icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = null) },
                        label = { Text("Plus") },
                    )
                }
            }
        },
    ) { paddingRacine ->
        NavHost(
            navController = navController,
            startDestination = ecranDepart,
            modifier = Modifier.fillMaxSize().padding(paddingRacine),
        ) {
'@
$navSource = $navSource.Replace($openAnchor, $openReplacement.TrimEnd("`r", "`n"))

$endAnchor = "    }`r`n}`r`n"
$endIndex = $navSource.LastIndexOf($endAnchor, [System.StringComparison]::Ordinal)
if ($endIndex -lt 0) {
    $endAnchor = "    }`n}`n"
    $endIndex = $navSource.LastIndexOf($endAnchor, [System.StringComparison]::Ordinal)
}
if ($endIndex -lt 0) {
    throw "Fin du NavGraph inattendue. Aucun fichier modifié."
}
$dialogTail = @'
        }
    }

    if (plusOuvert) {
        val optionsPlus = listOf(
            "Registre" to Ecran.Registre.route,
            "Trésorerie" to Ecran.Tresorerie.route,
            "Suggestions IA" to Ecran.Suggestions.route,
            "Imports" to Ecran.Imports.route,
            "Journal d’audit" to Ecran.Audit.route,
            "Factures" to Ecran.Factures.route,
            "Conformité" to Ecran.Conformite.route,
            "Administration" to Ecran.Administration.route,
            "Pilotage intelligent" to Ecran.Pilotage.route,
            "Profil" to Ecran.Profil.route,
        )
        AlertDialog(
            onDismissRequest = { plusOuvert = false },
            title = { Text("Autres modules") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                ) {
                    optionsPlus.forEach { (libelle, destination) ->
                        TextButton(
                            onClick = {
                                plusOuvert = false
                                navController.navigate(destination) { launchSingleTop = true }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(libelle, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { plusOuvert = false }) { Text("Fermer") }
            },
        )
    }
}
'@
$navSource = $navSource.Substring(0, $endIndex) + $dialogTail.TrimEnd("`r", "`n") + [Environment]::NewLine

# Prévalidation séparée : chaque exigence est vérifiée dans le fichier qu'elle concerne.
# L'ancienne validation recherchait des symboles de ressources dans NavGraph et
# attendait "val paddingRacine ->" au lieu du vrai paramètre ") { paddingRacine ->".
foreach ($required in @(
    "surClicNouvelleTransaction = { navController.navigate(Ecran.CreationTransaction.route) }"
    "surClicNouvelleRessource = { navController.navigate(Ecran.CreationRessource.route) }"
    "surClicNouvelleTache = { navController.navigate(Ecran.CreationTache.route) }"
    "NavigationBarItem("
    "optionsPlus.forEach"
    "paddingRacine ->"
)) {
    if (-not $navSource.Contains($required)) {
        throw "Prévalidation de navigation échouée : $required. Aucun fichier modifié."
    }
}

foreach ($required in @(
    "chargerEvolutionStocks(resultat.donnees)"
    "private fun chargerEvolutionStocks(ressources: List<Ressource>)"
)) {
    if (-not $resourcesViewModelSource.Contains($required)) {
        throw "Prévalidation de ResourcesViewModel échouée : $required. Aucun fichier modifié."
    }
}

foreach ($required in @(
    "CarteEvolutionStocks("
    "private fun CarteEvolutionStocks(series: List<EvolutionStock>, enCours: Boolean)"
)) {
    if (-not $resourcesSource.Contains($required)) {
        throw "Prévalidation de ResourcesScreen échouée : $required. Aucun fichier modifié."
    }
}

foreach ($required in @(
    "evolutionsStock: List<EvolutionStock>"
    "chargementEvolutionStock: Boolean"
)) {
    if (-not $resourcesStateSource.Contains($required)) {
        throw "Prévalidation de ResourcesUiState échouée : $required. Aucun fichier modifié."
    }
}

foreach ($required in @(
    "GraphiqueTresorerie(etat.evolutionTresorerie)"
    "etat.activitesRecentes"
    "etat.suggestionsRecentes"
)) {
    if (-not $dashboardSource.Contains($required)) {
        throw "Prévalidation du tableau de bord échouée : $required. Aucun fichier modifié."
    }
}

# Toutes les sources candidates sont maintenant prêtes. Sauvegarder avant la première écriture.
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$targets = @(
    [pscustomobject]@{ Path = $dashboardPath; Content = $dashboardSource }
    [pscustomobject]@{ Path = $viewModelPath; Content = $viewModelSource }
    [pscustomobject]@{ Path = $resourcesScreenPath; Content = $resourcesSource }
    [pscustomobject]@{ Path = $resourcesStatePath; Content = $resourcesStateSource }
    [pscustomobject]@{ Path = $resourcesViewModelPath; Content = $resourcesViewModelSource }
    [pscustomobject]@{ Path = $navPath; Content = $navSource }
)
$backups = @{}
foreach ($target in $targets) {
    $backup = "$($target.Path).bak-dashboard-$timestamp"
    Copy-Item -LiteralPath $target.Path -Destination $backup
    $backups[$target.Path] = $backup
}

$written = New-Object System.Collections.Generic.List[string]
try {
    foreach ($target in $targets) {
        $written.Add($target.Path)
        [System.IO.File]::WriteAllText(
            $target.Path,
            $target.Content,
            [System.Text.UTF8Encoding]::new($false)
        )
    }
}
catch {
    foreach ($path in $written) {
        if ($backups.ContainsKey($path)) {
            Copy-Item -LiteralPath $backups[$path] -Destination $path -Force
        }
    }
    throw "Écriture interrompue : les fichiers touchés ont été restaurés. Détail : $($_.Exception.Message)"
}

Write-Host "REFONTE DU TABLEAU DE BORD APPLIQUÉE." -ForegroundColor Green
Write-Host "Fichiers modifiés :" -ForegroundColor Cyan
$targets | ForEach-Object { Write-Host ("  " + $_.Path) }
Write-Host "Sauvegardes :" -ForegroundColor Cyan
$targets | ForEach-Object { Write-Host ("  " + $backups[$_.Path]) }
Write-Host "`nLa barre du bas est ajoutée aux écrans principaux, avec un menu Plus." -ForegroundColor Green
Write-Host "Le tableau utilise les résumés, l'évolution de trésorerie, les transactions, les alertes et les suggestions venant des repositories existants." -ForegroundColor Green
Write-Host "L'écran Ressources reçoit aussi un graphique multi-séries basé sur l'historique réel de chaque ressource." -ForegroundColor Green
Write-Host "Aucune photo de profil fictive ni statistique inventée n'a été ajoutée." -ForegroundColor Yellow
Write-Host "Prochaine étape : exécuter assembleDebug et testDebugUnitTest avant toute installation." -ForegroundColor Yellow
