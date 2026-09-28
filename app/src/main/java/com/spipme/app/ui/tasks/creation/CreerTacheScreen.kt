package com.spipme.app.ui.tasks.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.ui.components.SpiPmeBoutonPrincipal
import com.spipme.app.ui.components.SpiPmeTextField
import com.spipme.app.ui.theme.SpiPmeAppTheme

@Composable
fun CreerTacheScreen(
    surRetour: () -> Unit,
    surCreationReussie: () -> Unit,
    viewModel: CreerTacheViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(etat.creationReussie) {
        if (etat.creationReussie) surCreationReussie()
    }

    CreerTacheContenu(
        etat = etat,
        surRetour = surRetour,
        surChangementTitre = viewModel::surChangementTitre,
        surChangementDescription = viewModel::surChangementDescription,
        surChangementCategorie = viewModel::surChangementCategorie,
        surChangementPriorite = viewModel::surChangementPriorite,
        surChangementEcheance = viewModel::surChangementEcheance,
        surClicEnregistrer = viewModel::enregistrer,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CreerTacheContenu(
    etat: CreerTacheUiState,
    surRetour: () -> Unit,
    surChangementTitre: (String) -> Unit,
    surChangementDescription: (String) -> Unit,
    surChangementCategorie: (String) -> Unit,
    surChangementPriorite: (String) -> Unit,
    surChangementEcheance: (String) -> Unit,
    surClicEnregistrer: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle tÃ¢che") },
                navigationIcon = {
                    IconButton(onClick = surRetour) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text("Titre *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            SpiPmeTextField(valeur = etat.titre, surChangement = surChangementTitre, libelle = "Ex. Commander du stock")

            Spacer(Modifier.height(16.dp))
            Text("CatÃ©gorie", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            SpiPmeTextField(valeur = etat.categorie, surChangement = surChangementCategorie, libelle = "Ex. Approvisionnement")

            Spacer(Modifier.height(16.dp))
            Text("PrioritÃ©", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRIORITES.forEach { priorite ->
                    FilterChip(
                        selected = etat.priorite == priorite,
                        onClick = { surChangementPriorite(priorite) },
                        label = { Text(priorite.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Ã‰chÃ©ance", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            SpiPmeTextField(
                valeur = etat.echeance,
                surChangement = surChangementEcheance,
                libelle = "AAAA-MM-JJ",
                messageErreur = etat.messageErreur,
            )

            Spacer(Modifier.height(16.dp))
            Text("Description", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            SpiPmeTextField(valeur = etat.description, surChangement = surChangementDescription, libelle = "DÃ©tails de la tÃ¢che")

            Spacer(Modifier.height(24.dp))
            SpiPmeBoutonPrincipal(texte = "Enregistrer", surClic = surClicEnregistrer, enCours = etat.enCoursDEnvoi)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CreerTacheScreenApercu() {
    SpiPmeAppTheme {
        CreerTacheContenu(
            etat = CreerTacheUiState(),
            surRetour = {}, surChangementTitre = {}, surChangementDescription = {},
            surChangementCategorie = {}, surChangementPriorite = {}, surChangementEcheance = {},
            surClicEnregistrer = {},
        )
    }
}

