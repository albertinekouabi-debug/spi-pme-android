package com.spipme.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.ui.components.SpiPmeBoutonPrincipal
import com.spipme.app.ui.components.SpiPmeTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    surRetour: () -> Unit,
    surDeconnexion: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    val etatSnackbar = remember { SnackbarHostState() }

    LaunchedEffect(etat.deconnecte) {
        if (etat.deconnecte) surDeconnexion()
    }
    LaunchedEffect(etat.messageSucces, etat.messageErreur) {
        val message = etat.messageSucces ?: etat.messageErreur
        if (message != null) {
            etatSnackbar.showSnackbar(message)
            viewModel.messageConsomme()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil") }) },
        snackbarHost = { SnackbarHost(etatSnackbar) },
    ) { paddingInterne ->
        if (etat.chargement) {
            Box(Modifier.fillMaxSize().padding(paddingInterne), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterne)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Informations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (!etat.enEdition) {
                            TextButton(onClick = viewModel::activerEdition) { Text("Modifier") }
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    etat.utilisateur?.let { utilisateur ->
                        ChampLectureSeule("Nom d'utilisateur", utilisateur.nomUtilisateur)
                        ChampLectureSeule("Email", utilisateur.email)
                        ChampLectureSeule("RÃ´le", utilisateur.roleNom ?: "â€”")
                        ChampLectureSeule("Secteur principal", utilisateur.secteurPrincipalNom ?: "â€”")
                    }

                    Spacer(Modifier.height(8.dp))
                    if (etat.enEdition) {
                        SpiPmeTextField(
                            valeur = etat.nomComplet,
                            surChangement = viewModel::surChangementNomComplet,
                            libelle = "Nom complet",
                        )
                        Spacer(Modifier.height(8.dp))
                        SpiPmeTextField(
                            valeur = etat.telephone,
                            surChangement = viewModel::surChangementTelephone,
                            libelle = "TÃ©lÃ©phone",
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SpiPmeBoutonPrincipal(
                                texte = "Enregistrer",
                                surClic = viewModel::enregistrer,
                                enCours = etat.enregistrementEnCours,
                            )
                            OutlinedButton(onClick = viewModel::annulerEdition) { Text("Annuler") }
                        }
                    } else {
                        ChampLectureSeule("Nom complet", etat.nomComplet.ifBlank { "â€”" })
                        ChampLectureSeule("TÃ©lÃ©phone", etat.telephone.ifBlank { "â€”" })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Changer le mot de passe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    SpiPmeTextField(
                        valeur = etat.ancienMotDePasse,
                        surChangement = viewModel::surChangementAncienMotDePasse,
                        libelle = "Mot de passe actuel",
                        motDePasse = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    SpiPmeTextField(
                        valeur = etat.nouveauMotDePasse,
                        surChangement = viewModel::surChangementNouveauMotDePasse,
                        libelle = "Nouveau mot de passe",
                        motDePasse = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    SpiPmeTextField(
                        valeur = etat.confirmationNouveauMotDePasse,
                        surChangement = viewModel::surChangementConfirmationNouveauMotDePasse,
                        libelle = "Confirmer le nouveau mot de passe",
                        motDePasse = true,
                    )
                    Spacer(Modifier.height(12.dp))
                    SpiPmeBoutonPrincipal(
                        texte = "Changer le mot de passe",
                        surClic = viewModel::changerMotDePasse,
                        enCours = etat.changementMotDePasseEnCours,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = viewModel::seDeconnecter, modifier = Modifier.fillMaxWidth()) {
                Text("Se dÃ©connecter")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChampLectureSeule(libelle: String, valeur: String) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(libelle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valeur, style = MaterialTheme.typography.bodyMedium)
    }
}

