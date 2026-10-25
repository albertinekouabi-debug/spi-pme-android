package com.spipme.app.ui.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.ui.components.SpiPmeBoutonPrincipal
import com.spipme.app.ui.components.SpiPmeTextField
import com.spipme.app.ui.theme.SpiPmeAppTheme

@Composable
fun LoginScreen(
    surConnexionReussie: () -> Unit,
    surClicConnexionHorsLigne: () -> Unit,
    surClicInscription: () -> Unit,
    surClicMotDePasseOublie: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(etat.connexionReussie) {
        if (etat.connexionReussie) surConnexionReussie()
    }

    LoginContenu(
        etat = etat,
        surChangementIdentifiant = viewModel::surChangementIdentifiant,
        surChangementMotDePasse = viewModel::surChangementMotDePasse,
        surClicConnexion = viewModel::seConnecter,
        surClicConnexionHorsLigne = surClicConnexionHorsLigne,
        surClicInscription = surClicInscription,
        surClicMotDePasseOublie = surClicMotDePasseOublie,
    )
}

@Composable
private fun LoginContenu(
    etat: LoginUiState,
    surChangementIdentifiant: (String) -> Unit,
    surChangementMotDePasse: (String) -> Unit,
    surClicConnexion: () -> Unit,
    surClicConnexionHorsLigne: () -> Unit,
    surClicInscription: () -> Unit,
    surClicMotDePasseOublie: () -> Unit,
) {
    Scaffold { paddingInterne ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingInterne)
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            EnTeteMarque()
            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Connexion",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "Accédez à votre espace professionnel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))

                    Text("Email ou nom d'utilisateur", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    SpiPmeTextField(
                        valeur = etat.identifiant,
                        surChangement = surChangementIdentifiant,
                        libelle = "Entrez votre email ou nom d'utilisateur",
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

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = surClicMotDePasseOublie) {
                            Text("Mot de passe oublié ?")
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    SpiPmeBoutonPrincipal(
                        texte = "Se connecter",
                        surClic = surClicConnexion,
                        enCours = etat.enCoursDeConnexion,
                        afficherFlecheDroite = true,
                    )

                    Spacer(Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            "ou",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = surClicConnexionHorsLigne,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.height(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Connexion hors ligne", fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        "Accéder en mode hors ligne",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = surClicInscription, modifier = Modifier.fillMaxWidth()) {
                        Text("Pas encore de compte ? S'inscrire")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Vos données sont sécurisées",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EnTeteMarque() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(72.dp)
                .width(72.dp)
                .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("SP", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(12.dp))
        Text("SPI-PME", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            "Système de Pilotage Intelligent des PME",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Gérez. Analysez. Anticipez. Développez.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenApercu() {
    SpiPmeAppTheme {
        LoginContenu(
            etat = LoginUiState(),
            surChangementIdentifiant = {},
            surChangementMotDePasse = {},
            surClicConnexion = {},
            surClicConnexionHorsLigne = {},
            surClicInscription = {},
            surClicMotDePasseOublie = {},
        )
    }
}

