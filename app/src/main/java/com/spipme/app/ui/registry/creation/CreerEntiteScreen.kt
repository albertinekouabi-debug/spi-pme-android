package com.spipme.app.ui.registry.creation

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.Alignment
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
fun CreerEntiteScreen(
    surRetour: () -> Unit,
    surCreationReussie: () -> Unit,
    viewModel: CreerEntiteViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(etat.creationReussie) {
        if (etat.creationReussie) surCreationReussie()
    }

    CreerEntiteContenu(
        etat = etat,
        surRetour = surRetour,
        surChangementType = viewModel::surChangementType,
        surChangementTypePersonnalise = viewModel::surChangementTypePersonnalise,
        surChangementNom = viewModel::surChangementNom,
        surChangementTelephone = viewModel::surChangementTelephone,
        surChangementEmail = viewModel::surChangementEmail,
        surChangementVille = viewModel::surChangementVille,
        surClicEnregistrer = viewModel::enregistrer,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CreerEntiteContenu(
    etat: CreerEntiteUiState,
    surRetour: () -> Unit,
    surChangementType: (String) -> Unit,
    surChangementTypePersonnalise: (String) -> Unit,
    surChangementNom: (String) -> Unit,
    surChangementTelephone: (String) -> Unit,
    surChangementEmail: (String) -> Unit,
    surChangementVille: (String) -> Unit,
    surClicEnregistrer: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle entitÃ©") },
                navigationIcon = {
                    IconButton(onClick = surRetour) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
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
            Text("Type d'entitÃ©", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TYPES_SUGGERES.forEach { type ->
                    FilterChip(
                        selected = etat.type == type,
                        onClick = { surChangementType(type) },
                        label = { Text(type.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            if (etat.type == "autre") {
                Spacer(Modifier.height(12.dp))
                SpiPmeTextField(
                    valeur = etat.typePersonnalise,
                    surChangement = surChangementTypePersonnalise,
                    libelle = "PrÃ©ciser le type (ex. patient, locataire...)",
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Nom *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            SpiPmeTextField(valeur = etat.nom, surChangement = surChangementNom, libelle = "Nom de l'entitÃ©")

            Spacer(Modifier.height(16.dp))
            Text("TÃ©lÃ©phone", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            SpiPmeTextField(
                valeur = etat.telephone,
                surChangement = surChangementTelephone,
                libelle = "Ex. +242 06 123 45 67",
                iconeDebut = { Icon(Icons.Filled.Phone, contentDescription = null) },
                typeClavier = KeyboardType.Phone,
            )

            Spacer(Modifier.height(16.dp))
            Text("Email", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            SpiPmeTextField(
                valeur = etat.email,
                surChangement = surChangementEmail,
                libelle = "email@exemple.com",
                iconeDebut = { Icon(Icons.Filled.Email, contentDescription = null) },
                typeClavier = KeyboardType.Email,
            )

            Spacer(Modifier.height(16.dp))
            Text("Ville", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            SpiPmeTextField(
                valeur = etat.ville,
                surChangement = surChangementVille,
                libelle = "Ex. Brazzaville",
                iconeDebut = { Icon(Icons.Filled.LocationCity, contentDescription = null) },
            )

            etat.messageErreur?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))
            SpiPmeBoutonPrincipal(texte = "Enregistrer", surClic = surClicEnregistrer, enCours = etat.enCoursDEnvoi)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CreerEntiteScreenApercu() {
    SpiPmeAppTheme {
        CreerEntiteContenu(
            etat = CreerEntiteUiState(),
            surRetour = {},
            surChangementType = {},
            surChangementTypePersonnalise = {},
            surChangementNom = {},
            surChangementTelephone = {},
            surChangementEmail = {},
            surChangementVille = {},
            surClicEnregistrer = {},
        )
    }
}

