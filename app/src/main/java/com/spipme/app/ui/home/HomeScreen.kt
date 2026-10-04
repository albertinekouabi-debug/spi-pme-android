package com.spipme.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Placeholder volontaire : le vrai tableau de bord (maquette accueil.png —
 * cartes de synthèse, actions rapides, activité récente, suggestions IA) est
 * un module à part entière, pas construit dans cette passe consacrée à
 * Identité & Accès. Cet écran confirme seulement que la session fonctionne
 * de bout en bout (connexion -> navigation -> déconnexion) et sert de menu
 * de navigation temporaire vers chaque module déjà construit.
 */
@Composable
fun HomeScreen(
    surDeconnexion: () -> Unit,
    surClicRegistre: () -> Unit,
    surClicRessources: () -> Unit,
    surClicTresorerie: () -> Unit,
    surClicTaches: () -> Unit,
    surClicSuggestions: () -> Unit,
    surClicAlertes: () -> Unit,
    surClicImports: () -> Unit,
    surClicAudit: () -> Unit,
    surClicFactures: () -> Unit,
    surClicConformite: () -> Unit,
    surClicAdministration: () -> Unit,
    surClicDashboard: () -> Unit,
    surClicPilotage: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Connexion réussie", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Le tableau de bord complet (écran Accueil) sera construit comme module suivant.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            Button(onClick = surClicRegistre, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Aller au Registre")
            }
            Button(onClick = surClicRessources, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Aller aux Ressources")
            }
            Button(onClick = surClicTresorerie, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Aller à la Trésorerie")
            }
            Button(onClick = surClicTaches, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Aller aux Tâches")
            }
            Button(onClick = surClicSuggestions, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Aller aux Suggestions IA")
            }
            Button(onClick = surClicAlertes, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Aller aux Alertes")
            }
            Button(onClick = surClicImports, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Aller aux Imports")
            }
            Button(onClick = surClicAudit, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Journal d'audit")
            }
            Button(onClick = surClicFactures, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Factures")
            }
            Button(onClick = surClicConformite, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Conformité")
            }
            Button(onClick = surClicDashboard, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Tableau de bord")
            }
            Button(onClick = surClicPilotage, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Pilotage intelligent")
            }
            Button(onClick = surClicAdministration, modifier = Modifier.padding(bottom = 12.dp)) {
                Text("Administration")
            }
            Button(onClick = { viewModel.seDeconnecter(); surDeconnexion() }) {
                Text("Se déconnecter")
            }
        }
    }
}

