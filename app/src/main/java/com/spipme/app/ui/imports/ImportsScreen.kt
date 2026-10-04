package com.spipme.app.ui.imports

import com.spipme.app.core.notifications.NotificationBadgeViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.core.files.lireFichierDepuisUri
import com.spipme.app.domain.model.Anomalie
import com.spipme.app.domain.model.ApercuImport
import com.spipme.app.domain.model.ImportFichier
import com.spipme.app.ui.components.SpiPmeBoutonPrincipal
import com.spipme.app.ui.components.SpiPmeTopBar
import com.spipme.app.ui.theme.SpiPmeAppTheme
import com.spipme.app.ui.theme.SpiPmeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImportsScreen(
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    viewModel: ImportsViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    val nombreNotifs by (hiltViewModel<NotificationBadgeViewModel>()).compte.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val porteeCoroutine = rememberCoroutineScope()

    val selecteurFichier = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            // Le callback d'OpenDocument s'exécute sur le THREAD PRINCIPAL. La lecture
            // (contentResolver.query + openInputStream().readBytes()) est une I/O
            // bloquante, et un Uri SAF peut pointer vers un fournisseur DISTANT
            // (Google Drive, OneDrive...) — la lire ici gelait l'UI et pouvait
            // déclencher un ANR sur un fichier volumineux. Déporté sur Dispatchers.IO.
            porteeCoroutine.launch {
                val fichier = withContext(Dispatchers.IO) { lireFichierDepuisUri(context, uri) }
                if (fichier != null) {
                    viewModel.fichierSelectionne(fichier)
                }
            }
        }
    }

    ImportsContenu(
        nombreNotifs = nombreNotifs,
        etat = etat,
        surClicNotifications = surClicNotifications,
        surClicProfil = surClicProfil,
        surClicSecteur = surClicSecteur,
        surClicSelectionnerFichier = {
            selecteurFichier.launch(
                arrayOf(
                    "text/csv",
                    "text/comma-separated-values",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-excel",
                    "*/*", // certains gestionnaires de fichiers Android ne déclarent pas de type MIME correct pour le CSV
                )
            )
        },
        surClicApercu = viewModel::lancerApercu,
        surClicConfirmerImport = viewModel::confirmerImport,
        surClicRecommencer = viewModel::recommencer,
    )
}

@Composable
private fun ImportsContenu(
    etat: ImportsUiState,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    surClicSelectionnerFichier: () -> Unit,
    surClicApercu: () -> Unit,
    surClicConfirmerImport: () -> Unit,
    surClicRecommencer: () -> Unit,
    nombreNotifs: Int = 0,
) {
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
                    Text("Import de données", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Importez et intégrez vos données en toute sécurité",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                item {
                    CarteFluxImport(
                        etat = etat,
                        surClicSelectionnerFichier = surClicSelectionnerFichier,
                        surClicApercu = surClicApercu,
                        surClicConfirmerImport = surClicConfirmerImport,
                        surClicRecommencer = surClicRecommencer,
                    )
                }

                etat.messageErreur?.let { message ->
                    item {
                        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                item {
                    Text("Historique des imports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                if (etat.enChargementHistorique) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (etat.historique.isEmpty()) {
                    item {
                        Text(
                            "Aucun import effectué pour ce secteur.",
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(etat.historique, key = { it.id }) { import -> CarteHistoriqueImport(import) }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun CarteFluxImport(
    etat: ImportsUiState,
    surClicSelectionnerFichier: () -> Unit,
    surClicApercu: () -> Unit,
    surClicConfirmerImport: () -> Unit,
    surClicRecommencer: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (etat.etape) {
                EtapeImport.TERMINE -> etat.resultatImport?.let { ResultatImportTermine(it, surClicRecommencer) }
                else -> {
                    if (etat.fichierSelectionne == null) {
                        Text("1. Sélectionner un fichier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Formats supportés : .csv, .xlsx, .xls",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = surClicSelectionnerFichier, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.UploadFile, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Choisir un fichier")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(etat.fichierSelectionne.nom, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))

                        when (etat.etape) {
                            EtapeImport.SELECTION -> SpiPmeBoutonPrincipal(texte = "Valider et prévisualiser", surClic = surClicApercu)
                            EtapeImport.APERCU_EN_COURS -> SpiPmeBoutonPrincipal(texte = "Analyse...", surClic = {}, enCours = true)
                            EtapeImport.APERCU_PRET -> etat.apercu?.let { apercu ->
                                Column {
                                    ApercuResultats(apercu)
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = surClicRecommencer, modifier = Modifier.weight(1f)) {
                                            Text("Annuler")
                                        }
                                        Button(onClick = surClicConfirmerImport, modifier = Modifier.weight(1f)) {
                                            Text("Importer")
                                        }
                                    }
                                }
                            }
                            EtapeImport.IMPORT_EN_COURS -> SpiPmeBoutonPrincipal(texte = "Import en cours...", surClic = {}, enCours = true)
                            EtapeImport.TERMINE -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApercuResultats(apercu: ApercuImport) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        StatistiqueApercu("Total", apercu.lignesTotales, MaterialTheme.colorScheme.onSurface)
        StatistiqueApercu("Valides", apercu.lignesValides, SpiPmeTheme.extendedColors.succes)
        StatistiqueApercu("Erreurs", apercu.lignesRejetees, MaterialTheme.colorScheme.error)
    }

    if (apercu.anomalies.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text("Anomalies détectées", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        apercu.anomalies.take(10).forEach { anomalie -> LigneAnomalie(anomalie) }
        if (apercu.anomalies.size > 10) {
            Text(
                "+ ${apercu.anomalies.size - 10} autre(s) anomalie(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatistiqueApercu(libelle: String, valeur: Int, couleur: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valeur.toString(), style = MaterialTheme.typography.headlineSmall, color = couleur, fontWeight = FontWeight.Bold)
        Text(libelle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LigneAnomalie(anomalie: Anomalie) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Filled.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.height(16.dp).width(16.dp).padding(top = 2.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "Ligne ${anomalie.ligne} — ${anomalie.colonne} : ${anomalie.motif}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultatImportTermine(resultat: ImportFichier, surClicRecommencer: () -> Unit) {
    val succes = resultat.statut != "echec"
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (succes) Icons.Filled.CheckCircle else Icons.Filled.Error,
            contentDescription = null,
            tint = if (succes) SpiPmeTheme.extendedColors.succes else MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (succes) "Import terminé" else "Échec de l'import",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
    Spacer(Modifier.height(8.dp))
    Text(
        "${resultat.lignesImportees} ligne(s) importée(s), ${resultat.lignesRejetees} en erreur.",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(Modifier.height(12.dp))
    OutlinedButton(onClick = surClicRecommencer, modifier = Modifier.fillMaxWidth()) {
        Text("Nouvel import")
    }
}

@Composable
private fun CarteHistoriqueImport(import: ImportFichier) {
    val (couleur, libelle) = when (import.statut) {
        "termine" -> SpiPmeTheme.extendedColors.succes to "Import réussi"
        "termine_avec_anomalies" -> SpiPmeTheme.extendedColors.avertissement to "Terminé avec anomalies"
        "echec" -> MaterialTheme.colorScheme.error to "Échec de l'import"
        else -> SpiPmeTheme.extendedColors.information to "En cours"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(import.nomFichier, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${import.lignesTotales} lignes · ${import.lignesImportees} importées",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                import.auteurNom?.let {
                    Text("Importé par $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(
                modifier = Modifier
                    .background(couleur.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(libelle, style = MaterialTheme.typography.bodySmall, color = couleur)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ImportsScreenApercu() {
    SpiPmeAppTheme {
        ImportsContenu(
            etat = ImportsUiState(
                enChargementHistorique = false,
                secteurActifNom = "Commerce",
                historique = listOf(
                    ImportFichier(1, "stock_juin_2026.xlsx", "xlsx", "termine", 1245, 1245, 0, emptyList(), "Marie K.", 1, "2026-07-28"),
                ),
            ),
            surClicNotifications = {}, surClicProfil = {}, surClicSecteur = {},
            surClicSelectionnerFichier = {}, surClicApercu = {}, surClicConfirmerImport = {}, surClicRecommencer = {},
        )
    }
}

