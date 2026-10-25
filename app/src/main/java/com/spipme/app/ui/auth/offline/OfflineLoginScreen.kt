package com.spipme.app.ui.auth.offline
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.toggleable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingInterne ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(paddingInterne).imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = surRetour) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                    Text(
                        text = "Connexion hors ligne",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(48.dp))
                }

                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }

                Text(
                    text = "Vous êtes hors ligne",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Connectez-vous pour accéder aux données enregistrées localement sur cet appareil.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Connexion sécurisée", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Les données locales restent sur cet appareil. La synchronisation reprendra lorsque le réseau sera disponible.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .toggleable(
                            value = etat.seSouvenirDeMoi,
                            role = Role.Checkbox,
                            onValueChange = surChangementSeSouvenirDeMoi,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = etat.seSouvenirDeMoi,
                        onCheckedChange = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Se souvenir de moi",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = "La récupération d’un mot de passe nécessite une connexion Internet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                )

                SpiPmeBoutonPrincipal(
                    texte = "Se connecter hors ligne",
                    surClic = surClicConnexion,
                    enCours = etat.enCoursDeConnexion,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = surRetour,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = "Revenir à la connexion en ligne",
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SpiPmeTheme.extendedColors.information.copy(alpha = 0.08f),
                    ),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, SpiPmeTheme.extendedColors.information.copy(alpha = 0.25f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = SpiPmeTheme.extendedColors.information,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Mode hors ligne", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "Consultez les données déjà enregistrées",
                            "Certaines fonctions nécessitent le réseau",
                            "Les opérations en attente seront synchronisées ultérieurement",
                        ).forEach { ligne ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = SpiPmeTheme.extendedColors.information,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = ligne,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
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