$ErrorActionPreference = 'Stop'

$AndroidRoot = 'C:\Android\spi_pme_android'
$BackendRoot = 'C:\Android\spi_pme_backend'

if (-not (Test-Path -LiteralPath $AndroidRoot)) { throw "Projet Android introuvable : $AndroidRoot" }
if (-not (Test-Path -LiteralPath $BackendRoot)) { throw "Projet Django introuvable : $BackendRoot" }

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
    param(
        [Parameter(Mandatory=$true)]$Record,
        [Parameter(Mandatory=$true)][string]$NewText,
        [Parameter(Mandatory=$true)][string]$Suffix
    )
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
    param(
        [Parameter(Mandatory=$true)][string]$Source,
        [Parameter(Mandatory=$true)][string]$ImportLine
    )
    $name = $ImportLine.Substring(7)
    $pattern = '(?m)^import[ \t]+' + [regex]::Escape($name) + '[ \t]*$'
    if ([regex]::IsMatch($Source, $pattern)) { return $Source }
    $package = [regex]::Match($Source, '(?m)^package[^\n]*\n')
    if (-not $package.Success) { throw "Déclaration package introuvable pour l'import $ImportLine" }
    return $Source.Insert($package.Index + $package.Length, "$ImportLine`n")
}

# ------------------------------------------------------------------
# 1. Contrat du résumé des alertes : exposer resolues depuis Django
#    en réutilisant le même décompte que traitees, sans requête doublée.
# ------------------------------------------------------------------
$viewsPath = Join-Path $BackendRoot 'apps\alerts\views.py'
$viewsRecord = Read-SourceRecord $viewsPath
$viewsSource = $viewsRecord.Text
$summaryStart = $viewsSource.IndexOf('class ResumeAlertesView')
if ($summaryStart -lt 0) { throw 'Classe ResumeAlertesView introuvable dans apps\alerts\views.py' }
$nextClass = $viewsSource.IndexOf("`nclass ", $summaryStart + 1)
if ($nextClass -lt 0) { $nextClass = $viewsSource.Length }
$summaryBlock = $viewsSource.Substring($summaryStart, $nextClass - $summaryStart)

if ($summaryBlock -notmatch '(?m)^\s*"resolues"\s*:') {
    $responseMatch = [regex]::Match($summaryBlock, '(?m)^([ \t]*)return Response\(\{[ \t]*$')
    if (-not $responseMatch.Success) { throw 'Début du dictionnaire Response introuvable dans ResumeAlertesView' }
    $indent = $responseMatch.Groups[1].Value
    $countLine = $indent + 'traitees = _alertes_accessibles(request.user).filter(statut="traitee").count()'
    $summaryBlock = $summaryBlock.Substring(0, $responseMatch.Index) + $countLine + "`n" + $summaryBlock.Substring($responseMatch.Index)

    $treatedMatch = [regex]::Match($summaryBlock, '(?m)^([ \t]*)"traitees"[ \t]*:[ \t]*_alertes_accessibles\(request\.user\)\.filter\(statut="traitee"\)\.count\(\),[ \t]*$')
    if (-not $treatedMatch.Success) { throw 'Champ traitees introuvable ou différent du code attendu. Backend non modifié.' }
    $indentField = $treatedMatch.Groups[1].Value
    $replacement = $indentField + '"traitees": traitees,' + "`n" + $indentField + '"resolues": traitees,'
    $summaryBlock = $summaryBlock.Substring(0, $treatedMatch.Index) + $replacement + $summaryBlock.Substring($treatedMatch.Index + $treatedMatch.Length)

    if ($summaryBlock -notmatch '(?m)^\s*"resolues": traitees,\s*$') { throw 'Validation du champ resolues échouée. Backend non modifié.' }
    $viewsSource = $viewsSource.Substring(0, $summaryStart) + $summaryBlock + $viewsSource.Substring($nextClass)
    Write-SourceRecord $viewsRecord $viewsSource 'resume-alertes'
} else {
    Write-Host 'Le backend contient déjà le champ resolues.' -ForegroundColor DarkGray
}

# ------------------------------------------------------------------
# 2. Test de contrat API : le résumé doit exposer resolues.
# ------------------------------------------------------------------
$testPath = Join-Path $BackendRoot 'apps\alerts\tests\test_alerts.py'
if (Test-Path -LiteralPath $testPath) {
    $testRecord = Read-SourceRecord $testPath
    $testSource = $testRecord.Text
    $assertNeedle = '        assert response.data["elevees"] == 1'
    if ($testSource.Contains($assertNeedle) -and -not $testSource.Contains('assert response.data["resolues"] == 0')) {
        $testSource = $testSource.Replace($assertNeedle, $assertNeedle + "`n        assert response.data[""resolues""] == 0")
        Write-SourceRecord $testRecord $testSource 'contrat-resume-alertes'
    } else {
        Write-Host 'Test de contrat déjà présent ou point d’insertion absent : aucun changement au test.' -ForegroundColor DarkGray
    }
} else {
    Write-Host 'Fichier test_alerts.py introuvable : test de contrat non ajouté.' -ForegroundColor Yellow
}

# ------------------------------------------------------------------
# 3. Tolérance Android : un backend ancien/incomplet ne doit pas faire
#    planter le décodage du résumé si resolues est absent.
# ------------------------------------------------------------------
$dtoPath = Join-Path $AndroidRoot 'app\src\main\java\com\spipme\app\data\remote\dto\alerts\AlerteDto.kt'
$dtoRecord = Read-SourceRecord $dtoPath
$dtoSource = $dtoRecord.Text
$dtoPattern = '(?m)^(\s*val resolues: Int),[ \t]*$'
$dtoCount = [regex]::Matches($dtoSource, $dtoPattern).Count
if ($dtoCount -eq 1) {
    $dtoSource = [regex]::Replace($dtoSource, $dtoPattern, '$1 = 0,')
    if ($dtoSource -notmatch '(?m)^\s*val resolues: Int = 0,\s*$') { throw 'Validation DTO resolues échouée.' }
    Write-SourceRecord $dtoRecord $dtoSource 'dto-resolues-optionnel'
} elseif ($dtoSource -match '(?m)^\s*val resolues: Int = 0,\s*$') {
    Write-Host 'DTO : valeur de repli de resolues déjà présente.' -ForegroundColor DarkGray
} else {
    throw "Champ resolues attendu une fois dans $dtoPath, trouvé $dtoCount. DTO non modifié."
}

# ------------------------------------------------------------------
# 4. Clavier du copilote : réduire l’espace Compose avec l’IME visible.
# ------------------------------------------------------------------
$intelligencePath = Join-Path $AndroidRoot 'app\src\main\java\com\spipme\app\ui\intelligence\IntelligenceScreen.kt'
$intelligenceRecord = Read-SourceRecord $intelligencePath
$intelligenceSource = $intelligenceRecord.Text
$intelligenceSource = Ensure-Import $intelligenceSource 'import androidx.compose.foundation.layout.imePadding'
$imePattern = '(?m)^(\s*)Column\(modifier = Modifier\.fillMaxSize\(\)\.padding\(padding\)\) \{[ \t]*$'
$imeCount = [regex]::Matches($intelligenceSource, $imePattern).Count
if ($imeCount -eq 1) {
    $intelligenceSource = [regex]::Replace($intelligenceSource, $imePattern, '${1}Column(modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {')
} elseif ($intelligenceSource -notmatch '(?m)Column\(modifier = Modifier\.fillMaxSize\(\)\.padding\(padding\)\.imePadding\(\)\) \{') {
    throw "Conteneur principal du copilote différent du code attendu. Fichier non modifié : $intelligencePath"
}
if ($intelligenceSource -notmatch 'imePadding\(\)') { throw 'Validation IME échouée.' }
Write-SourceRecord $intelligenceRecord $intelligenceSource 'clavier-copilote'

# ------------------------------------------------------------------
# 5. Bouton Retour Android sur les modules principaux : revenir au
#    tableau de bord plutôt que de traverser toute la pile d’écrans.
# ------------------------------------------------------------------
$navPath = Join-Path $AndroidRoot 'app\src\main\java\com\spipme\app\ui\navigation\SpiPmeNavGraph.kt'
$navRecord = Read-SourceRecord $navPath
$navSource = $navRecord.Text
$navSource = Ensure-Import $navSource 'import androidx.activity.compose.BackHandler'

if ($navSource -notmatch 'BackHandler\([\s\S]{0,220}Ecran\.Dashboard\.route') {
    $routesMatch = [regex]::Match($navSource, '(?s)val routesAvecNavigation = setOf\(.*?\n    \)')
    if (-not $routesMatch.Success) { throw 'Définition routesAvecNavigation introuvable. Navigation non modifiée.' }
    $backBlock = @'

    BackHandler(
        enabled = !plusOuvert &&
            routeActuelle in routesAvecNavigation &&
            routeActuelle != Ecran.Dashboard.route &&
            routeActuelle != Ecran.Accueil.route,
    ) {
        plusOuvert = false
        navController.navigate(Ecran.Dashboard.route) {
            popUpTo(Ecran.Dashboard.route) { inclusive = false }
            launchSingleTop = true
        }
    }
'@
    $insertAt = $routesMatch.Index + $routesMatch.Length
    $navSource = $navSource.Substring(0, $insertAt) + $backBlock + $navSource.Substring($insertAt)
}
if ($navSource -notmatch 'BackHandler\(' -or $navSource -notmatch 'popUpTo\(Ecran\.Dashboard\.route\) \{ inclusive = false \}') {
    throw 'Validation de la gestion du bouton Retour échouée.'
}
Write-SourceRecord $navRecord $navSource 'retour-tableau-de-bord'

Write-Host "`nCorrections préparées. Lance maintenant les tests backend et Android." -ForegroundColor Green

Write-Host "`nBACKEND : tests ciblés" -ForegroundColor Cyan
Push-Location $BackendRoot
try {
    & '.\.venv\Scripts\python.exe' -m pytest 'apps\alerts\tests\test_alerts.py' -q
    if ($LASTEXITCODE -ne 0) { throw 'Échec des tests backend des alertes. Consulte leur sortie avant de poursuivre.' }
} finally { Pop-Location }

Write-Host "`nANDROID : compilation et tests unitaires" -ForegroundColor Cyan
Push-Location $AndroidRoot
try {
    & '.\gradlew.bat' assembleDebug testDebugUnitTest --stacktrace
    if ($LASTEXITCODE -ne 0) { throw 'Échec du build Android. Corrige la première erreur avant installation.' }
} finally { Pop-Location }

Write-Host "`nTous les contrôles automatisés de ce lot sont réussis. L’installation sur téléphone et le contrôle visuel restent à faire." -ForegroundColor Green
