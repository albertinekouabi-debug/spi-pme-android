package com.spipme.app.core.files

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.spipme.app.domain.model.FichierSelectionne

/**
 * Lecture d'un fichier sélectionné via le Storage Access Framework
 * (`ActivityResultContracts.OpenDocument`). Volontairement une fonction
 * simple prenant un `Context` en paramètre plutôt qu'un objet injecté par
 * Hilt : elle n'est appelée que depuis la couche UI (où le Context est déjà
 * disponible via `LocalContext.current`), jamais depuis un ViewModel — évite
 * toute fuite de Context dans une classe à portée plus longue que l'écran.
 */
fun lireFichierDepuisUri(context: Context, uri: Uri): FichierSelectionne? {
    val nom = obtenirNomFichier(context, uri) ?: "fichier"
    val octets = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    return FichierSelectionne(nom = nom, octets = octets)
}

private fun obtenirNomFichier(context: Context, uri: Uri): String? {
    context.contentResolver.query(uri, null, null, null, null)?.use { curseur ->
        val index = curseur.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && curseur.moveToFirst()) {
            return curseur.getString(index)
        }
    }
    return uri.lastPathSegment
}
