package com.spipme.app.ui.resources.creation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
fun CreerRessourceScreen(
    surRetour: () -> Unit,
    surCreationReussie: () -> Unit,
    viewModel: CreerRessourceViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(etat.creationReussie) {
        if (etat.creationReussie) surCreationReussie()
    }

    CreerRessourceContenu(
        etat = etat,
        surRetour = surRetour,
        surChangementType = viewModel::surChangementType,
        surChangementNom = viewModel::surChangementNom,
        surChangementUnite = viewModel::surChangementUnite,
        surChangementNiveauActuel = viewModel::surChangementNiveauActuel,
        surChangementSeuilCritique = viewModel::surChangementSeuilCritique,
        surChangementSeuilAlerte = viewModel::surChangementSeuilAlerte,
        surChangementValeurUnitaire = viewModel::surChangementValeurUnitaire,
        surChangementEmplacement = viewModel::surChangementEmplacement,
        surClicEnregistrer = viewModel::enregistrer,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CreerRessourceContenu(
    etat: CreerRessourceUiState,
    surRetour: () -> Unit,
    surChangementType: (String) -> Unit,
    surChangementNom: (String) -> Unit,
    surChangementUnite: (String) -> Unit,
    surChangementNiveauActuel: (String) -> Unit,
    surChangementSeuilCritique: (String) -> Unit,
    surChangementSeuilAlerte: (String) -> Unit,
    surChangementValeurUnitaire: (String) -> Unit,
    surChangementEmplacement: (String) -> Unit,
    surClicEnregistrer: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle ressource") },
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
            Champ("Catégorie *", etat.type, surChangementType, "Ex. Produits laitiers")
            Champ("Nom *", etat.nom, surChangementNom, "Nom de la ressource")

            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Champ("Quantité actuelle *", etat.niveauActuel, surChangementNiveauActuel, "0", KeyboardType.Decimal)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Champ("Unité", etat.unite, surChangementUnite, "Ex. unités, sacs")
                }
            }

            Text(
                "Seuils d'alerte (optionnels — configurables plus tard, à partir des données réelles de consommation)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Champ("Seuil critique", etat.seuilCritique, surChangementSeuilCritique, "0", KeyboardType.Decimal)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Champ("Seuil d'alerte", etat.seuilAlerte, surChangementSeuilAlerte, "0", KeyboardType.Decimal)
                }
            }

            Champ("Valeur unitaire (FCFA)", etat.valeurUnitaire, surChangementValeurUnitaire, "0", KeyboardType.Decimal)
            Champ("Emplacement", etat.emplacement, surChangementEmplacement, "Ex. Entrepôt principal")

            etat.messageErreur?.let { message ->
                Spacer(Modifier.height(4.dp))
                Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))
            SpiPmeBoutonPrincipal(texte = "Enregistrer", surClic = surClicEnregistrer, enCours = etat.enCoursDEnvoi)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Champ(
    libelle: String,
    valeur: String,
    surChangement: (String) -> Unit,
    indication: String,
    typeClavier: KeyboardType = KeyboardType.Text,
) {
    Text(libelle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    SpiPmeTextField(valeur = valeur, surChangement = surChangement, libelle = indication, typeClavier = typeClavier)
    Spacer(Modifier.height(16.dp))
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CreerRessourceScreenApercu() {
    SpiPmeAppTheme {
        CreerRessourceContenu(
            etat = CreerRessourceUiState(),
            surRetour = {}, surChangementType = {}, surChangementNom = {}, surChangementUnite = {},
            surChangementNiveauActuel = {}, surChangementSeuilCritique = {}, surChangementSeuilAlerte = {},
            surChangementValeurUnitaire = {}, surChangementEmplacement = {}, surClicEnregistrer = {},
        )
    }
}

