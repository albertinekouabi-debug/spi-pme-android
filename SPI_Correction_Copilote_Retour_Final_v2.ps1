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
        throw "Validation préalable échouée pour '$Description' : $($correspondances.Count) correspondance(s), 1 attendue. Aucun fichier n'a été écrit."
    }
    $m = $correspondances[0]
    return $Texte.Substring(0, $m.Index) + $Remplacement + $Texte.Substring($m.Index + $m.Length)
}

$encodageUtf8 = [System.Text.UTF8Encoding]::new($false)
$sourcePilotage = [System.IO.File]::ReadAllText($fichierPilotage, [System.Text.Encoding]::UTF8)
$sourceNavigation = [System.IO.File]::ReadAllText($fichierNavigation, [System.Text.Encoding]::UTF8)

if ($sourcePilotage -notmatch 'import androidx\.compose\.foundation\.layout\.imePadding') {
    $sourcePilotage = Remplacer-Une-Fois `
        -Texte $sourcePilotage `
        -Motif '(?m)^import androidx\.compose\.foundation\.layout\.fillMaxSize\r?$' `
        -Remplacement "import androidx.compose.foundation.layout.fillMaxSize`r`nimport androidx.compose.foundation.layout.imePadding" `
        -Description 'import imePadding dans PilotageScreen.kt'
}

$nouvelleFonctionPilotageBase64 = @(
    'QENvbXBvc2FibGUKZnVuIFBpbG90YWdlU2NyZWVuKAogICAgc3VyQ2xpY05vdGlmaWNhdGlvbnM6ICgpIC0+IFVuaXQsCiAgICBz'
    'dXJDbGljUHJvZmlsOiAoKSAtPiBVbml0LAogICAgc3VyQ2xpY1NlY3RldXI6ICgpIC0+IFVuaXQsCiAgICB2aWV3TW9kZWw6IFBp'
    'bG90YWdlVmlld01vZGVsID0gaGlsdFZpZXdNb2RlbCgpLAopIHsKICAgIHZhbCBldGF0IGJ5IHZpZXdNb2RlbC51aVN0YXRlLmNv'
    'bGxlY3RBc1N0YXRlV2l0aExpZmVjeWNsZSgpCiAgICB2YWwgbm9tYnJlTm90aWZzIGJ5IChoaWx0Vmlld01vZGVsPE5vdGlmaWNh'
    'dGlvbkJhZGdlVmlld01vZGVsPigpKS5jb21wdGUuY29sbGVjdEFzU3RhdGVXaXRoTGlmZWN5Y2xlKCkKCiAgICBTY2FmZm9sZCB7'
    'IHBhZGRpbmcgLT4KICAgICAgICBDb2x1bW4oCiAgICAgICAgICAgIG1vZGlmaWVyID0gTW9kaWZpZXIKICAgICAgICAgICAgICAg'
    'IC5maWxsTWF4U2l6ZSgpCiAgICAgICAgICAgICAgICAucGFkZGluZyhwYWRkaW5nKQogICAgICAgICAgICAgICAgLmltZVBhZGRp'
    'bmcoKSwKICAgICAgICApIHsKICAgICAgICAgICAgU3BpUG1lVG9wQmFyKAogICAgICAgICAgICAgICAgc2VjdGV1ckFjdGlmTm9t'
    'ID0gZXRhdC5zZWN0ZXVyQWN0aWZOb20sCiAgICAgICAgICAgICAgICBub21icmVOb3RpZmljYXRpb25zTm9uTHVlcyA9IG5vbWJy'
    'ZU5vdGlmcywKICAgICAgICAgICAgICAgIHN1ckNsaWNTZWN0ZXVyID0gc3VyQ2xpY1NlY3RldXIsCiAgICAgICAgICAgICAgICBz'
    'dXJDbGljTm90aWZpY2F0aW9ucyA9IHN1ckNsaWNOb3RpZmljYXRpb25zLAogICAgICAgICAgICAgICAgc3VyQ2xpY1Byb2ZpbCA9'
    'IHN1ckNsaWNQcm9maWwsCiAgICAgICAgICAgICkKCiAgICAgICAgICAgIExhenlDb2x1bW4oCiAgICAgICAgICAgICAgICBtb2Rp'
    'ZmllciA9IE1vZGlmaWVyLndlaWdodCgxZikuZmlsbE1heFdpZHRoKCksCiAgICAgICAgICAgICAgICBjb250ZW50UGFkZGluZyA9'
    'IFBhZGRpbmdWYWx1ZXMoaG9yaXpvbnRhbCA9IDE2LmRwLCB2ZXJ0aWNhbCA9IDguZHApLAogICAgICAgICAgICAgICAgdmVydGlj'
    'YWxBcnJhbmdlbWVudCA9IEFycmFuZ2VtZW50LnNwYWNlZEJ5KDEyLmRwKSwKICAgICAgICAgICAgKSB7CiAgICAgICAgICAgICAg'
    'ICBpdGVtIHsKICAgICAgICAgICAgICAgICAgICBUZXh0KCJQaWxvdGFnZSIsIHN0eWxlID0gTWF0ZXJpYWxUaGVtZS50eXBvZ3Jh'
    'cGh5LmhlYWRsaW5lTWVkaXVtLCBmb250V2VpZ2h0ID0gRm9udFdlaWdodC5Cb2xkKQogICAgICAgICAgICAgICAgICAgIFRleHQo'
    'CiAgICAgICAgICAgICAgICAgICAgICAgICJJbmRpY2F0ZXVycyBjYWxjdWzDqXMgc3VyIHZvcyBkb25uw6llcyB2YWxpZMOpZXMs'
    'IGFuYWx5c2VzIGV0IGNvcGlsb3RlIiwKICAgICAgICAgICAgICAgICAgICAgICAgc3R5bGUgPSBNYXRlcmlhbFRoZW1lLnR5cG9n'
    'cmFwaHkuYm9keU1lZGl1bSwKICAgICAgICAgICAgICAgICAgICAgICAgY29sb3IgPSBNYXRlcmlhbFRoZW1lLmNvbG9yU2NoZW1l'
    'Lm9uU3VyZmFjZVZhcmlhbnQsCiAgICAgICAgICAgICAgICAgICAgKQogICAgICAgICAgICAgICAgfQoKICAgICAgICAgICAgICAg'
    'IHdoZW4gewogICAgICAgICAgICAgICAgICAgIGV0YXQuZW5DaGFyZ2VtZW50IC0+IGl0ZW0gewogICAgICAgICAgICAgICAgICAg'
    'ICAgICBDaXJjdWxhclByb2dyZXNzSW5kaWNhdG9yKG1vZGlmaWVyID0gTW9kaWZpZXIucGFkZGluZygzMi5kcCkpCiAgICAgICAg'
    'ICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgIGV0YXQucGlsb3RhZ2UgIT0gbnVsbCAtPiB7CiAgICAgICAgICAgICAg'
    'ICAgICAgICAgIHZhbCBwaWxvdGFnZSA9IGV0YXQucGlsb3RhZ2UhIQogICAgICAgICAgICAgICAgICAgICAgICBwaWxvdGFnZS5o'
    'b3JzTGlnbmVEZXB1aXM/LmxldCB7IHF1YW5kIC0+CiAgICAgICAgICAgICAgICAgICAgICAgICAgICBpdGVtIHsKICAgICAgICAg'
    'ICAgICAgICAgICAgICAgICAgICAgICBUZXh0KAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiSG9ycyBsaWdu'
    'ZSDigJQgZGVybmnDqHJlIGFuYWx5c2UgZHUgIiArCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBEYXRl'
    'Rm9ybWF0LmdldERhdGVUaW1lSW5zdGFuY2UoRGF0ZUZvcm1hdC5NRURJVU0sIERhdGVGb3JtYXQuU0hPUlQpLmZvcm1hdChEYXRl'
    'KHF1YW5kKSksCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHN0eWxlID0gTWF0ZXJpYWxUaGVtZS50eXBvZ3Jh'
    'cGh5LmxhYmVsTWVkaXVtLAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBjb2xvciA9IFNwaVBtZVRoZW1lLmV4'
    'dGVuZGVkQ29sb3JzLmF2ZXJ0aXNzZW1lbnQsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgKQogICAgICAgICAgICAg'
    'ICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgICAgIGl0ZW0geyBM'
    'aWduZUtwaXMocGlsb3RhZ2Uua3BpcykgfQogICAgICAgICAgICAgICAgICAgICAgICBpdGVtIHsgVGV4dCgiQW5hbHlzZXMiLCBz'
    'dHlsZSA9IE1hdGVyaWFsVGhlbWUudHlwb2dyYXBoeS50aXRsZUxhcmdlLCBmb250V2VpZ2h0ID0gRm9udFdlaWdodC5TZW1pQm9s'
    'ZCkgfQogICAgICAgICAgICAgICAgICAgICAgICBpdGVtcyhwaWxvdGFnZS5pbnNpZ2h0cywga2V5ID0geyBpdC5jb2RlICsgaXQu'
    'dGl0cmUgfSkgeyBDYXJ0ZUluc2lnaHQoaXQpIH0KICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgZWxz'
    'ZSAtPiBpdGVtIHsKICAgICAgICAgICAgICAgICAgICAgICAgQ29sdW1uIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgIFRl'
    'eHQoCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZXRhdC5tZXNzYWdlRXJyZXVyID86ICJBbmFseXNlIGluZGlzcG9u'
    'aWJsZS4iLAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGNvbG9yID0gTWF0ZXJpYWxUaGVtZS5jb2xvclNjaGVtZS5l'
    'cnJvciwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICkKICAgICAgICAgICAgICAgICAgICAgICAgICAgIFRleHQoCiAgICAg'
    'ICAgICAgICAgICAgICAgICAgICAgICAgICAgIkxlcyBhbmFseXNlcyBzb250IGNhbGN1bMOpZXMgcGFyIGxlIHNlcnZldXIgOiBl'
    'bGxlcyBuZSBzb250IHBhcyBkaXNwb25pYmxlcyBob3JzIGxpZ25lIHRhbnQgcXUnYXVjdW5lIG4nYSDDqXTDqSBtw6ltb3Jpc8Op'
    'ZS4iLAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHN0eWxlID0gTWF0ZXJpYWxUaGVtZS50eXBvZ3JhcGh5LmJvZHlT'
    'bWFsbCwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBjb2xvciA9IE1hdGVyaWFsVGhlbWUuY29sb3JTY2hlbWUub25T'
    'dXJmYWNlVmFyaWFudCwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICkKICAgICAgICAgICAgICAgICAgICAgICAgICAgIFRl'
    'eHRCdXR0b24ob25DbGljayA9IHZpZXdNb2RlbDo6cmFmcmFpY2hpcikgeyBUZXh0KCJSw6llc3NheWVyIikgfQogICAgICAgICAg'
    'ICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgfQoKICAgICAgICAgICAgICAgIGl0'
    'ZW0geyBUZXh0KCJDb3BpbG90ZSIsIHN0eWxlID0gTWF0ZXJpYWxUaGVtZS50eXBvZ3JhcGh5LnRpdGxlTGFyZ2UsIGZvbnRXZWln'
    'aHQgPSBGb250V2VpZ2h0LlNlbWlCb2xkKSB9CiAgICAgICAgICAgICAgICBpdGVtIHsKICAgICAgICAgICAgICAgICAgICBMYXp5'
    'Um93KGhvcml6b250YWxBcnJhbmdlbWVudCA9IEFycmFuZ2VtZW50LnNwYWNlZEJ5KDguZHApKSB7CiAgICAgICAgICAgICAgICAg'
    'ICAgICAgIGl0ZW1zKGV0YXQuc3VnZ2VzdGlvbnMpIHsgc3VnZ2VzdGlvbiAtPgogICAgICAgICAgICAgICAgICAgICAgICAgICAg'
    'QXNzaXN0Q2hpcChvbkNsaWNrID0geyB2aWV3TW9kZWwucG9zZXIoc3VnZ2VzdGlvbikgfSwgbGFiZWwgPSB7IFRleHQoc3VnZ2Vz'
    'dGlvbikgfSkKICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgIH0K'
    'ICAgICAgICAgICAgICAgIGl0ZW1zKGV0YXQubWVzc2FnZXMpIHsgbWVzc2FnZSAtPiBCdWxsZUNvcGlsb3RlKG1lc3NhZ2UpIH0K'
    'ICAgICAgICAgICAgICAgIGl0ZW0geyBTcGFjZXIoTW9kaWZpZXIuaGVpZ2h0KDguZHApKSB9CiAgICAgICAgICAgIH0KCiAgICAg'
    'ICAgICAgIFJvdygKICAgICAgICAgICAgICAgIG1vZGlmaWVyID0gTW9kaWZpZXIKICAgICAgICAgICAgICAgICAgICAuZmlsbE1h'
    'eFdpZHRoKCkKICAgICAgICAgICAgICAgICAgICAucGFkZGluZyhob3Jpem9udGFsID0gMTYuZHAsIHZlcnRpY2FsID0gOC5kcCks'
    'CiAgICAgICAgICAgICAgICB2ZXJ0aWNhbEFsaWdubWVudCA9IGFuZHJvaWR4LmNvbXBvc2UudWkuQWxpZ25tZW50LkNlbnRlclZl'
    'cnRpY2FsbHksCiAgICAgICAgICAgICkgewogICAgICAgICAgICAgICAgT3V0bGluZWRUZXh0RmllbGQoCiAgICAgICAgICAgICAg'
    'ICAgICAgdmFsdWUgPSBldGF0LnF1ZXN0aW9uLAogICAgICAgICAgICAgICAgICAgIG9uVmFsdWVDaGFuZ2UgPSB2aWV3TW9kZWw6'
    'OnN1ckNoYW5nZW1lbnRRdWVzdGlvbiwKICAgICAgICAgICAgICAgICAgICBsYWJlbCA9IHsgVGV4dCgiUG9zZXogdW5lIHF1ZXN0'
    'aW9uIikgfSwKICAgICAgICAgICAgICAgICAgICBtb2RpZmllciA9IE1vZGlmaWVyLndlaWdodCgxZiksCiAgICAgICAgICAgICAg'
    'ICAgICAgc2luZ2xlTGluZSA9IHRydWUsCiAgICAgICAgICAgICAgICApCiAgICAgICAgICAgICAgICBJY29uQnV0dG9uKAogICAg'
    'ICAgICAgICAgICAgICAgIG9uQ2xpY2sgPSB7IHZpZXdNb2RlbC5wb3NlcigpIH0sCiAgICAgICAgICAgICAgICAgICAgZW5hYmxl'
    'ZCA9IGV0YXQucXVlc3Rpb24uaXNOb3RCbGFuaygpLAogICAgICAgICAgICAgICAgKSB7CiAgICAgICAgICAgICAgICAgICAgSWNv'
    'bihJY29ucy5GaWxsZWQuU2VuZCwgY29udGVudERlc2NyaXB0aW9uID0gIkVudm95ZXIgbGEgcXVlc3Rpb24iKQogICAgICAgICAg'
    'ICAgICAgfQogICAgICAgICAgICB9CiAgICAgICAgfQogICAgfQp9'
) -join ''
$nouvelleFonctionPilotage = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($nouvelleFonctionPilotageBase64))

$sourcePilotage = Remplacer-Une-Fois `
    -Texte $sourcePilotage `
    -Motif '(?s)@Composable\s+fun PilotageScreen\(.*?\r?\n\}\s*(?=@Composable\s*\r?\nprivate fun LigneKpis)' `
    -Remplacement ($nouvelleFonctionPilotage + "`r`n`r`n") `
    -Description 'fonction PilotageScreen complète'

# Supprimer l'ancien gestionnaire placé avant Scaffold/NavHost.
$sourceNavigation = Remplacer-Une-Fois `
    -Texte $sourceNavigation `
    -Motif '(?s)\r?\n[ \t]*BackHandler\(\s*enabled = !plusOuvert &&\s*routeActuelle in routesAvecNavigation &&\s*routeActuelle != Ecran\.Dashboard\.route &&\s*routeActuelle != Ecran\.Accueil\.route,\s*\)\s*\{\s*plusOuvert = false\s*navController\.navigate\(Ecran\.Dashboard\.route\)\s*\{\s*popUpTo\(Ecran\.Dashboard\.route\)\s*\{\s*inclusive = false\s*\}\s*launchSingleTop = true\s*\}\s*\}\s*(?=Scaffold\()' `
    -Remplacement "`r`n" `
    -Description 'retrait de l’ancien BackHandler avant Scaffold'

$nouveauGestionnaireRetourBase64 = @(
    'ICAgIEJhY2tIYW5kbGVyKAogICAgICAgIGVuYWJsZWQgPSBwbHVzT3V2ZXJ0IHx8ICgKICAgICAgICAgICAgcm91dGVBY3R1ZWxs'
    'ZSAhPSBudWxsICYmCiAgICAgICAgICAgICAgICByb3V0ZUFjdHVlbGxlICFpbiBzZXRPZigKICAgICAgICAgICAgICAgICAgICBF'
    'Y3Jhbi5Db25uZXhpb24ucm91dGUsCiAgICAgICAgICAgICAgICAgICAgRWNyYW4uQ29ubmV4aW9uSG9yc0xpZ25lLnJvdXRlLAog'
    'ICAgICAgICAgICAgICAgICAgIEVjcmFuLkluc2NyaXB0aW9uLnJvdXRlLAogICAgICAgICAgICAgICAgICAgIEVjcmFuLk1vdERl'
    'UGFzc2VPdWJsaWUucm91dGUsCiAgICAgICAgICAgICAgICApICYmCiAgICAgICAgICAgICAgICByb3V0ZUFjdHVlbGxlICE9IEVj'
    'cmFuLkRhc2hib2FyZC5yb3V0ZSAmJgogICAgICAgICAgICAgICAgcm91dGVBY3R1ZWxsZSAhPSBFY3Jhbi5BY2N1ZWlsLnJvdXRl'
    'CiAgICAgICAgKSwKICAgICkgewogICAgICAgIGlmIChwbHVzT3V2ZXJ0KSB7CiAgICAgICAgICAgIHBsdXNPdXZlcnQgPSBmYWxz'
    'ZQogICAgICAgIH0gZWxzZSB7CiAgICAgICAgICAgIG5hdkNvbnRyb2xsZXIubmF2aWdhdGUoRWNyYW4uRGFzaGJvYXJkLnJvdXRl'
    'KSB7CiAgICAgICAgICAgICAgICBwb3BVcFRvKDApIHsgaW5jbHVzaXZlID0gdHJ1ZSB9CiAgICAgICAgICAgICAgICBsYXVuY2hT'
    'aW5nbGVUb3AgPSB0cnVlCiAgICAgICAgICAgIH0KICAgICAgICB9CiAgICB9Cg=='
) -join ''
$nouveauGestionnaireRetour = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($nouveauGestionnaireRetourBase64))

$sourceNavigation = Remplacer-Une-Fois `
    -Texte $sourceNavigation `
    -Motif '(?m)^    if \(plusOuvert\) \{' `
    -Remplacement ($nouveauGestionnaireRetour + "`r`n    if (plusOuvert) {") `
    -Description 'insertion du BackHandler après NavHost/Scaffold'

# Valider toutes les invariantes avant d'écrire.
if ([regex]::Matches($sourceNavigation, '(?m)^\s*BackHandler\(').Count -ne 1) {
    throw 'Validation échouée : le graphe doit contenir exactement un BackHandler.'
}
if ($sourceNavigation -notmatch 'popUpTo\(0\)\s*\{\s*inclusive = true\s*\}') {
    throw 'Validation échouée : le retour doit vider la pile de navigation.'
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
