$ErrorActionPreference = 'Stop'

$racine = 'C:\Android\spi_pme_android'
$fichierPilotage = Join-Path $racine 'app\src\main\java\com\spipme\app\ui\pilotage\PilotageScreen.kt'
$fichierNavigation = Join-Path $racine 'app\src\main\java\com\spipme\app\ui\navigation\SpiPmeNavGraph.kt'

foreach ($fichier in @($fichierPilotage, $fichierNavigation)) {
    if (-not (Test-Path -LiteralPath $fichier -PathType Leaf)) {
        throw "Fichier requis introuvable : $fichier. Aucun fichier n'a été modifié."
    }
}

function Remplacer-Une-Fois {
    param(
        [Parameter(Mandatory = $true)][string]$Texte,
        [Parameter(Mandatory = $true)][string]$Motif,
        [Parameter(Mandatory = $true)][string]$Remplacement,
        [Parameter(Mandatory = $true)][string]$Description
    )
    $regex = [regex]::new($Motif)
    $correspondances = $regex.Matches($Texte)
    if ($correspondances.Count -ne 1) {
        throw "Validation préalable échouée pour '$Description' : $($correspondances.Count) correspondance(s), 1 attendue. Aucun fichier n'a encore été écrit."
    }
    $m = $correspondances[0]
    return $Texte.Substring(0, $m.Index) + $Remplacement + $Texte.Substring($m.Index + $m.Length)
}

$encodageUtf8 = [System.Text.UTF8Encoding]::new($false)
$sourcePilotage = [System.IO.File]::ReadAllText($fichierPilotage, [System.Text.Encoding]::UTF8)
$sourceNavigation = [System.IO.File]::ReadAllText($fichierNavigation, [System.Text.Encoding]::UTF8)

# 1. Corriger l'écran Pilotage : le fil de conversation défile, la zone de saisie reste fixe au-dessus du clavier.
if ($sourcePilotage -notmatch 'import androidx\.compose\.foundation\.layout\.imePadding') {
    $sourcePilotage = Remplacer-Une-Fois `
        -Texte $sourcePilotage `
        -Motif '(?m)^import androidx\.compose\.foundation\.layout\.fillMaxSize\r?$' `
        -Remplacement "import androidx.compose.foundation.layout.fillMaxSize`r`nimport androidx.compose.foundation.layout.imePadding" `
        -Description 'import imePadding dans PilotageScreen.kt'
}

$nouvelleFonctionPilotage = @'
@Composable
fun PilotageScreen(
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    surClicSecteur: () -> Unit,
    viewModel: PilotageViewModel = hiltViewModel(),
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    val nombreNotifs by (hiltViewModel<NotificationBadgeViewModel>()).compte.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            SpiPmeTopBar(
                secteurActifNom = etat.secteurActifNom,
                nombreNotificationsNonLues = nombreNotifs,
                surClicSecteur = surClicSecteur,
                surClicNotifications = surClicNotifications,
                surClicProfil = surClicProfil,
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("Pilotage", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Indicateurs calculés sur vos données validées, analyses et copilote",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                when {
                    etat.enChargement -> item {
                        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    }
                    etat.pilotage != null -> {
                        val pilotage = etat.pilotage!!
                        pilotage.horsLigneDepuis?.let { quand ->
                            item {
                                Text(
                                    "Hors ligne — dernière analyse du " +
                                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(quand)),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SpiPmeTheme.extendedColors.avertissement,
                                )
                            }
                        }
                        item { LigneKpis(pilotage.kpis) }
                        item { Text("Analyses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                        items(pilotage.insights, key = { it.code + it.titre }) { CarteInsight(it) }
                    }
                    else -> item {
                        Column {
                            Text(
                                etat.messageErreur ?: "Analyse indisponible.",
                                color = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                "Les analyses sont calculées par le serveur : elles ne sont pas disponibles hors ligne tant qu'aucune n'a été mémorisée.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = viewModel::rafraichir) { Text("Réessayer") }
                        }
                    }
                }

                item { Text("Copilote", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(etat.suggestions) { suggestion ->
                            AssistChip(onClick = { viewModel.poser(suggestion) }, label = { Text(suggestion) })
                        }
                    }
                }
                items(etat.messages) { message -> BulleCopilote(message) }
                item { Spacer(Modifier.height(8.dp)) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = etat.question,
                    onValueChange = viewModel::surChangementQuestion,
                    label = { Text("Posez une question") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(
                    onClick = { viewModel.poser() },
                    enabled = etat.question.isNotBlank(),
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Envoyer la question")
                }
            }
        }
    }
}

'@

$sourcePilotage = Remplacer-Une-Fois `
    -Texte $sourcePilotage `
    -Motif '(?s)@Composable\s+fun PilotageScreen\(.*?\r?\n\}\s*(?=@Composable\s*\r?\nprivate fun LigneKpis)' `
    -Remplacement $nouvelleFonctionPilotage `
    -Description 'fonction PilotageScreen complète'

# Éviter un import devenu inutile : Spacer reste utilisé dans le fil pour ménager une marge avant la zone de saisie.
# 2. Retirer l'ancien BackHandler situé avant Scaffold/NavHost ; il perdait la priorité face au gestionnaire du NavHost.
$sourceNavigation = Remplacer-Une-Fois `
    -Texte $sourceNavigation `
    -Motif '(?s)\r?\n[ \t]*BackHandler\(\s*enabled = !plusOuvert &&\s*routeActuelle in routesAvecNavigation &&\s*routeActuelle != Ecran\.Dashboard\.route &&\s*routeActuelle != Ecran\.Accueil\.route,\s*\)\s*\{\s*plusOuvert = false\s*navController\.navigate\(Ecran\.Dashboard\.route\)\s*\{\s*popUpTo\(Ecran\.Dashboard\.route\)\s*\{\s*inclusive = false\s*\}\s*launchSingleTop = true\s*\}\s*\}\s*(?=Scaffold\()' `
    -Remplacement "`r`n" `
    -Description 'retrait de l’ancien BackHandler avant Scaffold'

$nouveauGestionnaireRetour = @'
    BackHandler(
        enabled = plusOuvert || (
            routeActuelle != null &&
                routeActuelle !in setOf(
                    Ecran.Connexion.route,
                    Ecran.ConnexionHorsLigne.route,
                    Ecran.Inscription.route,
                    Ecran.MotDePasseOublie.route,
                ) &&
                routeActuelle != Ecran.Dashboard.route &&
                routeActuelle != Ecran.Accueil.route
        ),
    ) {
        if (plusOuvert) {
            plusOuvert = false
        } else {
            navController.navigate(Ecran.Dashboard.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

'@
$sourceNavigation = Remplacer-Une-Fois `
    -Texte $sourceNavigation `
    -Motif '(?m)^    if \(plusOuvert\) \{' `
    -Remplacement ($nouveauGestionnaireRetour + '    if (plusOuvert) {') `
    -Description 'insertion du BackHandler après NavHost/Scaffold'

# Vérifier les invariants avant d'écrire quoi que ce soit.
if ([regex]::Matches($sourceNavigation, '(?m)^\s*BackHandler\(').Count -ne 1) {
    throw 'Validation échouée : le graphe doit contenir exactement un BackHandler.'
}
if ($sourceNavigation -notmatch 'popUpTo\(navController\.graph\.id\)\s*\{\s*inclusive = true\s*\}') {
    throw 'Validation échouée : la navigation doit vider la pile avant le retour au tableau de bord.'
}
if ($sourcePilotage -notmatch 'Modifier\.weight\(1f\)\.fillMaxWidth\(\)' -or $sourcePilotage -notmatch '\.imePadding\(\)' -or $sourcePilotage -notmatch 'OutlinedTextField\(') {
    throw 'Validation échouée : mise en page du copilote incomplète.'
}
if ($sourcePilotage -notmatch '(?s)LazyColumn\(.*?\n\s*\}\s*\r?\n\s*Row\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)') {
    throw 'Validation échouée : la zone de saisie ne semble pas placée hors de LazyColumn.'
}

$horodatage = Get-Date -Format 'yyyyMMdd-HHmmss'
$sauvegardePilotage = "${fichierPilotage}.bak-copilote-retour-$horodatage"
$sauvegardeNavigation = "${fichierNavigation}.bak-copilote-retour-$horodatage"
[System.IO.File]::Copy($fichierPilotage, $sauvegardePilotage, $false)
[System.IO.File]::Copy($fichierNavigation, $sauvegardeNavigation, $false)

try {
    [System.IO.File]::WriteAllText($fichierPilotage, $sourcePilotage, $encodageUtf8)
    [System.IO.File]::WriteAllText($fichierNavigation, $sourceNavigation, $encodageUtf8)
} catch {
    if (Test-Path -LiteralPath $sauvegardePilotage) { [System.IO.File]::Copy($sauvegardePilotage, $fichierPilotage, $true) }
    if (Test-Path -LiteralPath $sauvegardeNavigation) { [System.IO.File]::Copy($sauvegardeNavigation, $fichierNavigation, $true) }
    throw "Écriture interrompue : les fichiers ont été restaurés depuis leurs sauvegardes. Détail : $($_.Exception.Message)"
}

Write-Host "`nCorrections écrites et validations statiques réussies." -ForegroundColor Green
Write-Host "Pilotage : $fichierPilotage"
Write-Host "Sauvegarde : $sauvegardePilotage"
Write-Host "Navigation : $fichierNavigation"
Write-Host "Sauvegarde : $sauvegardeNavigation"
Write-Host "`nÉtape suivante : lancer assembleDebug et testDebugUnitTest pour vérifier la compilation et les tests." -ForegroundColor Cyan
