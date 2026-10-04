package com.spipme.app.ui.auth.motdepasseoublie

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.ui.components.SpiPmeBoutonPrincipal
import com.spipme.app.ui.components.SpiPmeTextField

/**
 * Écran "Mot de passe oublié". Rejoint depuis LoginScreen (lien câblé,
 * cf. audit — le bouton ne faisait rien auparavant).
 */
@Composable
fun MotDePasseOublieScreen(
    surRetour: () -> Unit,
    surReinitialisationReussie: () -> Unit,
    viewModel: MotDePasseOublieViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(etat.reinitialisationReussie) {
        if (etat.reinitialisationReussie) surReinitialisationReussie()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mot de passe oublié") },
                navigationIcon = {
                    IconButton(onClick = surRetour) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { paddingInterne ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingInterne)
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    when (etat.etape) {
                        EtapeMotDePasseOublie.DEMANDE -> EtapeDemande(etat, viewModel)
                        EtapeMotDePasseOublie.CONFIRMATION -> EtapeConfirmation(etat, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun EtapeDemande(etat: MotDePasseOublieUiState, viewModel: MotDePasseOublieViewModel) {
    Text(
        "Réinitialiser votre mot de passe",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
    Text(
        "Indiquez votre email : si un compte existe, vous recevrez un code de réinitialisation.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(24.dp))

    Text("Email", style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(6.dp))
    SpiPmeTextField(
        valeur = etat.email,
        surChangement = viewModel::surChangementEmail,
        libelle = "Entrez votre email",
        iconeDebut = { Icon(Icons.Filled.Email, contentDescription = null) },
        messageErreur = etat.messageErreur,
    )

    Spacer(Modifier.height(24.dp))
    SpiPmeBoutonPrincipal(
        texte = "Envoyer le lien de réinitialisation",
        surClic = viewModel::demanderReinitialisation,
        enCours = etat.enCours,
        active = !etat.enCours,
    )
}

@Composable
private fun EtapeConfirmation(etat: MotDePasseOublieUiState, viewModel: MotDePasseOublieViewModel) {
    Text(
        "Vérifiez votre boîte mail",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
    Text(
        "Si un compte existe pour cette adresse, un email vient de vous être envoyé avec un code valable 1 heure.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(24.dp))

    Text("Code reçu par email", style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(6.dp))
    SpiPmeTextField(
        valeur = etat.token,
        surChangement = viewModel::surChangementToken,
        libelle = "Collez le code ici",
        iconeDebut = { Icon(Icons.Filled.Pin, contentDescription = null) },
    )

    Spacer(Modifier.height(16.dp))
    Text("Nouveau mot de passe", style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(6.dp))
    SpiPmeTextField(
        valeur = etat.nouveauMotDePasse,
        surChangement = viewModel::surChangementNouveauMotDePasse,
        libelle = "8 caractères minimum",
        iconeDebut = { Icon(Icons.Filled.Lock, contentDescription = null) },
        motDePasse = true,
        messageErreur = etat.messageErreur,
    )

    Spacer(Modifier.height(24.dp))
    SpiPmeBoutonPrincipal(
        texte = "Réinitialiser le mot de passe",
        surClic = viewModel::confirmerReinitialisation,
        enCours = etat.enCours,
        active = !etat.enCours,
    )

    Spacer(Modifier.height(8.dp))
    TextButton(onClick = viewModel::revenirALaDemande) {
        Text("Je n'ai pas reçu d'email")
    }
}
