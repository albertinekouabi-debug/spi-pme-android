package com.spipme.app.ui.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.ui.components.SpiPmeBoutonPrincipal
import com.spipme.app.ui.components.SpiPmeTextField

@Composable
fun RegisterScreen(
    surRetourConnexion: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    RegisterContenu(
        etat = etat,
        surRetourConnexion = surRetourConnexion,
        surChangementNomUtilisateur = viewModel::surChangementNomUtilisateur,
        surChangementEmail = viewModel::surChangementEmail,
        surChangementMotDePasse = viewModel::surChangementMotDePasse,
        surChangementConfirmationMotDePasse = viewModel::surChangementConfirmationMotDePasse,
        surChangementNomComplet = viewModel::surChangementNomComplet,
        surChangementCodeInvitation = viewModel::surChangementCodeInvitation,
        surClicInscription = viewModel::sInscrire,
    )
}

@Composable
private fun RegisterContenu(
    etat: RegisterUiState,
    surRetourConnexion: () -> Unit,
    surChangementNomUtilisateur: (String) -> Unit,
    surChangementEmail: (String) -> Unit,
    surChangementMotDePasse: (String) -> Unit,
    surChangementConfirmationMotDePasse: (String) -> Unit,
    surChangementNomComplet: (String) -> Unit,
    surChangementCodeInvitation: (String) -> Unit,
    surClicInscription: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (etat.inscriptionReussie) {
                    Spacer(Modifier.height(48.dp))
                    EtatConfirmationEmail(email = etat.email, surRetourConnexion = surRetourConnexion)
                } else {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "CrÃ©er un compte",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Rejoignez l'espace professionnel de votre entreprise",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))

                    SpiPmeTextField(
                        valeur = etat.nomComplet,
                        surChangement = surChangementNomComplet,
                        libelle = "Nom complet",
                        iconeDebut = { Icon(Icons.Filled.Person, contentDescription = null) },
                    )
                    Spacer(Modifier.height(12.dp))
                    SpiPmeTextField(
                        valeur = etat.nomUtilisateur,
                        surChangement = surChangementNomUtilisateur,
                        libelle = "Nom d'utilisateur",
                        iconeDebut = { Icon(Icons.Filled.Person, contentDescription = null) },
                    )
                    Spacer(Modifier.height(12.dp))
                    SpiPmeTextField(
                        valeur = etat.email,
                        surChangement = surChangementEmail,
                        libelle = "Email professionnel",
                        iconeDebut = { Icon(Icons.Filled.Email, contentDescription = null) },
                    )
                    Spacer(Modifier.height(12.dp))
                    SpiPmeTextField(
                        valeur = etat.motDePasse,
                        surChangement = surChangementMotDePasse,
                        libelle = "Mot de passe",
                        motDePasse = true,
                        iconeDebut = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    )
                    Spacer(Modifier.height(12.dp))
                    SpiPmeTextField(
                        valeur = etat.confirmationMotDePasse,
                        surChangement = surChangementConfirmationMotDePasse,
                        libelle = "Confirmer le mot de passe",
                        motDePasse = true,
                        iconeDebut = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    )
                    Spacer(Modifier.height(12.dp))
                    SpiPmeTextField(
                        valeur = etat.codeInvitation,
                        surChangement = surChangementCodeInvitation,
                        libelle = "Code d'invitation de votre entreprise",
                        iconeDebut = { Icon(Icons.Filled.VpnKey, contentDescription = null) },
                        messageErreur = null,
                    )

                    etat.messageErreur?.let { erreur ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            erreur,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    SpiPmeBoutonPrincipal(
                        texte = "S'inscrire",
                        surClic = surClicInscription,
                        enCours = etat.enCoursDInscription,
                        afficherFlecheDroite = true,
                    )
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = surRetourConnexion) {
                        Text("DÃ©jÃ  un compte ? Se connecter")
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Le compte est crÃ©Ã© INACTIF cÃ´tÃ© serveur (vÃ©rification email requise avant
 * activation, cf. dÃ©cision produit du 16/09/2026) â€” cet Ã©cran ne doit donc
 * jamais laisser croire que l'utilisateur est dÃ©jÃ  connectÃ©.
 */
@Composable
private fun EtatConfirmationEmail(email: String, surRetourConnexion: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "VÃ©rifiez votre boÃ®te mail",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Un email de confirmation a Ã©tÃ© envoyÃ© Ã  $email. " +
                "Cliquez sur le lien qu'il contient pour activer votre compte, " +
                "puis revenez ici vous connecter.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = surRetourConnexion) {
            Text("Retour Ã  la connexion")
        }
    }
}

