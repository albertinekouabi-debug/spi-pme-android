$ErrorActionPreference = 'Stop'

$AndroidRoot = 'C:\Android\spi_pme_android'
if (-not (Test-Path -LiteralPath $AndroidRoot)) { throw "Projet Android introuvable : $AndroidRoot" }

function Read-SourceRecord {
    param([Parameter(Mandatory=$true)][string]$Path)
    $resolved = (Resolve-Path -LiteralPath $Path).Path
    $bytes = [System.IO.File]::ReadAllBytes($resolved)
    $hasBom = $bytes.Length -ge 3 -and $bytes[0] -eq 239 -and $bytes[1] -eq 187 -and $bytes[2] -eq 191
    $raw = [System.IO.File]::ReadAllText($resolved)
    $newline = if ($raw.Contains("`r`n")) { "`r`n" } else { "`n" }
    $normalized = $raw.Replace("`r`n", "`n").Replace("`r", "`n")
    return [pscustomobject]@{ Path=$resolved; Raw=$raw; Text=$normalized; Newline=$newline; Bom=$hasBom }
}

function Write-SourceRecord {
    param($Record, [string]$NewText, [string]$Suffix)
    if ($NewText -ceq $Record.Text) {
        Write-Host "Aucun changement requis : $($Record.Path)" -ForegroundColor DarkGray
        return
    }
    $backup = "$($Record.Path).bak-$Suffix-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    Copy-Item -LiteralPath $Record.Path -Destination $backup
    $output = $NewText.Replace("`n", $Record.Newline)
    [System.IO.File]::WriteAllText($Record.Path, $output, [System.Text.UTF8Encoding]::new($Record.Bom))
    Write-Host "Modifié : $($Record.Path)" -ForegroundColor Green
    Write-Host "Sauvegarde : $backup"
}

function Ensure-Import {
    param([string]$Source, [string]$ImportLine)
    $name = $ImportLine.Substring(7)
    $pattern = '(?m)^import[ \t]+' + [regex]::Escape($name) + '[ \t]*$'
    if ([regex]::IsMatch($Source, $pattern)) { return $Source }
    $package = [regex]::Match($Source, '(?m)^package[^\n]*\n')
    if (-not $package.Success) { throw "Déclaration package introuvable pour $ImportLine" }
    return $Source.Insert($package.Index + $package.Length, "$ImportLine`n")
}

# ------------------------------------------------------------------
# 1. Copilote : IME + LazyColumn dimensionnée dans l'espace restant.
#    imePadding seul n'est pas suffisant si la LazyColumn réclame toute
#    la hauteur disponible au lieu de partager l'espace après la top bar.
# ------------------------------------------------------------------
$intelligencePath = Join-Path $AndroidRoot 'app\src\main\java\com\spipme\app\ui\intelligence\IntelligenceScreen.kt'
$intelRecord = Read-SourceRecord $intelligencePath
$intel = $intelRecord.Text

$intel = Ensure-Import $intel 'import androidx.compose.foundation.layout.imePadding'

$columnPattern = '(?m)^(\s*)Column\(modifier = Modifier\.fillMaxSize\(\)\.padding\(padding\)(?:\.imePadding\(\))?\) \{[ \t]*$'
$columnCount = [regex]::Matches($intel, $columnPattern).Count
if ($columnCount -eq 1) {
    $intel = [regex]::Replace($intel, $columnPattern, '${1}Column(modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {', 1)
} elseif ($columnCount -ne 0 -or $intel -notmatch '(?m)Column\(modifier = Modifier\.fillMaxSize\(\)\.padding\(padding\)\.imePadding\(\)\) \{') {
    throw "Structure du conteneur principal inattendue dans IntelligenceScreen.kt; fichier non écrit."
}

# La LazyColumn ciblée est la première située après SpiPmeTopBar dans le contenu principal.
$topBarIndex = $intel.IndexOf('SpiPmeTopBar(')
if ($topBarIndex -lt 0) { throw 'SpiPmeTopBar introuvable dans IntelligenceScreen.kt.' }
$lazyIndex = $intel.IndexOf('LazyColumn(', $topBarIndex)
if ($lazyIndex -lt 0) { throw 'LazyColumn du copilote introuvable après SpiPmeTopBar.' }
$modifierIndex = $intel.IndexOf('modifier = Modifier.fillMaxSize()', $lazyIndex)
$openBrace = $intel.IndexOf('{', $lazyIndex)
if ($modifierIndex -lt 0 -or ($openBrace -ge 0 -and $modifierIndex -gt $openBrace)) {
    if ($intel.Substring($lazyIndex, [Math]::Min(700, $intel.Length - $lazyIndex)) -notmatch 'modifier = Modifier\.weight\(1f\)\.fillMaxWidth\(\)') {
        throw 'Modificateur de la LazyColumn du copilote différent du code attendu; fichier non écrit.'
    }
} else {
    $intel = $intel.Remove($modifierIndex, 'modifier = Modifier.fillMaxSize()'.Length).Insert($modifierIndex, 'modifier = Modifier.weight(1f).fillMaxWidth()')
}

if ($intel -notmatch '(?m)Column\(modifier = Modifier\.fillMaxSize\(\)\.padding\(padding\)\.imePadding\(\)\) \{' -or
    $intel -notmatch 'LazyColumn\([\s\S]{0,180}modifier = Modifier\.weight\(1f\)\.fillMaxWidth\(\)') {
    throw 'Validation de la gestion clavier/capacité de défilement échouée; fichier non écrit.'
}
Write-SourceRecord $intelRecord $intel 'clavier-copilote-v2'

# ------------------------------------------------------------------
# 2. Bouton Retour Android : fermer Plus si ouvert; sinon, sur toute
#    route métier (y compris formulaires), revenir directement au dashboard.
#    Réinitialisation de la pile pour ne pas revisiter les écrans précédents.
# ------------------------------------------------------------------
$navPath = Join-Path $AndroidRoot 'app\src\main\java\com\spipme\app\ui\navigation\SpiPmeNavGraph.kt'
$navRecord = Read-SourceRecord $navPath
$nav = $navRecord.Text
$nav = Ensure-Import $nav 'import androidx.activity.compose.BackHandler'

$backRegex = '(?s)\n[ \t]*BackHandler\([\s\S]*?\n[ \t]*\}\n(?=[ \t]*Scaffold\()'
$nav = [regex]::Replace($nav, $backRegex, "`n", 1)

$routesMatch = [regex]::Match($nav, '(?s)val routesAvecNavigation = setOf\(.*?\n[ \t]*\)')
if (-not $routesMatch.Success) { throw 'routesAvecNavigation introuvable; navigation non écrite.' }
$insertAt = $routesMatch.Index + $routesMatch.Length
$backBlock = @'

    val routesAuthentification = setOf(
        Ecran.Connexion.route,
        Ecran.ConnexionHorsLigne.route,
        Ecran.Inscription.route,
        Ecran.MotDePasseOublie.route,
    )

    BackHandler(
        enabled = plusOuvert || (routeActuelle != null && routeActuelle !in routesAuthentification && routeActuelle != Ecran.Dashboard.route),
    ) {
        if (plusOuvert) {
            plusOuvert = false
        } else {
            navController.navigate(Ecran.Dashboard.route) {
                popUpTo(navController.graph.id) { inclusive = false }
                launchSingleTop = true
            }
        }
    }
'@
$nav = $nav.Substring(0, $insertAt) + $backBlock + $nav.Substring($insertAt)

if ([regex]::Matches($nav, '(?m)^\s*BackHandler\(').Count -ne 1 -or
    $nav -notmatch 'popUpTo\(navController\.graph\.id\) \{ inclusive = false \}' -or
    $nav -notmatch 'if \(plusOuvert\)') {
    throw 'Validation du BackHandler échouée; navigation non écrite.'
}
Write-SourceRecord $navRecord $nav 'retour-dashboard-v2'

Write-Host "`nCorrections source appliquées. Le comportement doit être validé sur l'appareil après compilation." -ForegroundColor Green
Write-Host 'Ensuite, exécute : .\gradlew.bat assembleDebug testDebugUnitTest --stacktrace' -ForegroundColor Cyan
