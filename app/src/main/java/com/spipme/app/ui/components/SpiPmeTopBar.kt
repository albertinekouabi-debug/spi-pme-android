package com.spipme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * En-tête présent sur les 16 maquettes : sélecteur "Secteur actif",
 * notifications (badge = nombre non lues), avatar utilisateur. Un seul
 * composant réutilisé partout plutôt que réimplémenté écran par écran.
 */
@Composable
fun SpiPmeTopBar(
    secteurActifNom: String,
    nombreNotificationsNonLues: Int,
    surClicSecteur: () -> Unit,
    surClicNotifications: () -> Unit,
    surClicProfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SelecteurSecteur(secteurActifNom, surClicSecteur, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        BoutonNotifications(nombreNotificationsNonLues, surClicNotifications)
        Spacer(Modifier.width(12.dp))
        AvatarUtilisateur(surClicProfil)
    }
}

@Composable
private fun RowScope.SelecteurSecteur(secteurActifNom: String, surClic: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clickable(onClick = surClic)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        androidx.compose.foundation.layout.Column {
            Text("Secteur actif", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(secteurActifNom, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.weight(1f, fill = false))
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Changer de secteur")
    }
}

@Composable
private fun BoutonNotifications(nombreNonLues: Int, surClic: () -> Unit) {
    BadgedBox(
        badge = {
            if (nombreNonLues > 0) {
                Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text(if (nombreNonLues > 99) "99+" else nombreNonLues.toString())
                }
            }
        }
    ) {
        IconButton(onClick = surClic) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
        }
    }
}

@Composable
private fun AvatarUtilisateur(surClic: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = surClic)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Person, contentDescription = "Profil", tint = MaterialTheme.colorScheme.primary)
    }
}
