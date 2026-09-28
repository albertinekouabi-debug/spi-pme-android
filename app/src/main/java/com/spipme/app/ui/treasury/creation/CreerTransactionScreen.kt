package com.spipme.app.ui.treasury.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.ui.components.SpiPmeBoutonPrincipal
import com.spipme.app.ui.components.SpiPmeTextField
import com.spipme.app.ui.theme.SpiPmeAppTheme

@Composable
fun CreerTransactionScreen(
    surRetour: () -> Unit,
    surCreationReussie: () -> Unit,
    viewModel: CreerTransactionViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(etat.creationReussie) {
        if (etat.creationReussie) surCreationReussie()
    }

    CreerTransactionContenu(
        etat = etat,
        surRetour = surRetour,
        surChangementType = viewModel::surChangementType,
        surChangementMontant = viewModel::surChangementMontant,
        surChangementModePaiement = viewModel::surChangementModePaiement,
        surChangementDescription = viewModel::surChangementDescription,
        surClicEnregistrer = viewModel::enregistrer,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CreerTransactionContenu(
    etat: CreerTransactionUiState,
    surRetour: () -> Unit,
    surChangementType: (String) -> Unit,
    surChangementMontant: (String) -> Unit,
    surChangementModePaiement: (String) -> Unit,
    surChangementDescription: (String) -> Unit,
    surClicEnregistrer: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle transaction") },
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
            Text("Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("entree" to "EntrÃ©e", "sortie" to "Sortie").forEach { (valeur, libelle) ->
                    FilterChip(
                        selected = etat.type == valeur,
                        onClick = { surChangementType(valeur) },
                        label = { Text(libelle) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Montant (FCFA) *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            SpiPmeTextField(
                valeur = etat.montant,
                surChangement = surChangementMontant,
                libelle = "0",
                typeClavier = KeyboardType.Decimal,
                messageErreur = etat.messageErreur,
            )

            Spacer(Modifier.height(16.dp))
            Text("Mode de paiement", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MODES_PAIEMENT.forEach { mode ->
                    FilterChip(
                        selected = etat.modePaiement == mode,
                        onClick = { surChangementModePaiement(mode) },
                        label = { Text(mode.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Description", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            SpiPmeTextField(valeur = etat.description, surChangement = surChangementDescription, libelle = "Ex. Vente de marchandises")

            Spacer(Modifier.height(24.dp))
            SpiPmeBoutonPrincipal(texte = "Enregistrer", surClic = surClicEnregistrer, enCours = etat.enCoursDEnvoi)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CreerTransactionScreenApercu() {
    SpiPmeAppTheme {
        CreerTransactionContenu(
            etat = CreerTransactionUiState(),
            surRetour = {}, surChangementType = {}, surChangementMontant = {},
            surChangementModePaiement = {}, surChangementDescription = {}, surClicEnregistrer = {},
        )
    }
}

