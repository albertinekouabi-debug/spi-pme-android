package com.spipme.app.ui.auth.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.ui.components.SpiPmeBoutonPrincipal
import com.spipme.app.ui.components.SpiPmeTextField
import com.spipme.app.ui.theme.SpiPmeAppTheme
import com.spipme.app.ui.theme.SpiPmeTheme

@Composable
fun OfflineLoginScreen(
    surRetour: () -> Unit,
    surConnexionReussie: () -> Unit,
    viewModel: OfflineLoginViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(etat.connexionReussie) {
        if (etat.connexionReussie) surConnexionReussie()
    }

    OfflineLoginContenu(
        etat = etat,
        surRetour = surRetour,
        surChangementIdentifiant = viewModel::surChangementIdentifiant,
        surChangementMotDePasse = viewModel::surChangementMotDePasse,
        surChangementSeSouvenirDeMoi = viewModel::surChangementSeSouvenirDeMoi,
        surClicConnexion = viewModel::seConnecterHorsLigne,
    )
}

@Composable
private fun OfflineLoginContenu(
    etat: OfflineLoginUiState,
    surRetour: () -> Unit,
    surChangementIdentifiant: (String) -> Unit,
    surChangementMotDePasse: (String) -> Unit,
    surChangementSeSouvenirDeMoi: (Boolean) -> Unit,
    surClicConnexion: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = surRetour) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
            }
            Spacer(Modifier.weight(1f))
            Text("Connexion hors ligne", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp)) // équilibre visuel avec le bouton retour
        }

        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .height(88.dp)
                    .width(88.dp)
                    .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(40.dp).width(40.dp),
                )
            }
        }

        Text(
            "Vous êtes hors ligne",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            "Connectez-vous pour accéder à vos données enregistrées localement.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Connexion sécurisée", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Vos données restent protégées sur cet appareil. Elles seront synchronisées dès que la connexion Internet sera rétablie.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Compte local", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Text("Nom d'utilisateur", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        SpiPmeTextField(
            valeur = etat.identifiant,
            surChangement = surChangementIdentifiant,
            libelle = "Entrez votre nom d'utilisateur",
            iconeDebut = { Icon(Icons.Filled.Person, contentDescription = null) },
        )

        Spacer(Modifier.height(16.dp))
        Text("Mot de passe", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        SpiPmeTextField(
            valeur = etat.motDePasse,
            surChangement = surChangementMotDePasse,
            libelle = "Entrez votre mot de passe",
            iconeDebut = { Icon(Icons.Filled.Lock, contentDescription = null) },
            motDePasse = true,
            messageErreur = etat.messageErreur,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = etat.seSouvenirDeMoi, onCheckedChange = surChangementSeSouvenirDeMoi)
            Text("Se souvenir de moi", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { /* hors périmètre : le mot de passe oublié nécessite une connexion réseau */ }) {
                Text("Mot de passe oublié ?")
            }
        }

        Spacer(Modifier.height(8.dp))
        SpiPmeBoutonPrincipal(
            texte = "Se connecter hors ligne",
            surClic = surClicConnexion,
            enCours = etat.enCoursDeConnexion,
        )

        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = surRetour,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text("Revenir à la connexion en ligne")
        }

        Spacer(Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = SpiPmeTheme.extendedColors.information.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = SpiPmeTheme.extendedColors.information)
                    Spacer(Modifier.width(8.dp))
                    Text("Mode hors ligne actif", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                listOf(
                    "Vous pouvez consulter et saisir vos données",
                    "Certaines fonctionnalités peuvent être limitées",
                    "La synchronisation se fera automatiquement",
                ).forEach { ligne ->
                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = SpiPmeTheme.extendedColors.information,
                            modifier = Modifier.height(16.dp).width(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(ligne, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun OfflineLoginScreenApercu() {
    SpiPmeAppTheme {
        OfflineLoginContenu(
            etat = OfflineLoginUiState(),
            surRetour = {},
            surChangementIdentifiant = {},
            surChangementMotDePasse = {},
            surChangementSeSouvenirDeMoi = {},
            surClicConnexion = {},
        )
    }
}
