package com.spipme.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = BleuPrincipal,
    onPrimary = SurfaceClair,
    primaryContainer = BleuFonce,
    onPrimaryContainer = SurfaceClair,
    secondary = Cyan,
    onSecondary = TexteClair,
    background = FondClair,
    onBackground = TexteClair,
    surface = SurfaceClair,
    onSurface = TexteClair,
    surfaceVariant = FondClair,
    onSurfaceVariant = TexteSecondaireClair,
    outline = BordureClair,
    error = Erreur,
    onError = SurfaceClair,
)

private val DarkColors = darkColorScheme(
    primary = BleuPrincipal,
    onPrimary = TexteSombre,
    primaryContainer = BleuFonce,
    onPrimaryContainer = TexteSombre,
    secondary = Cyan,
    onSecondary = TexteSombre,
    background = FondSombre,
    onBackground = TexteSombre,
    surface = SurfaceSombre,
    onSurface = TexteSombre,
    surfaceVariant = CarteSombre,
    onSurfaceVariant = TexteSecondaireSombre,
    outline = TexteSecondaireSombre,
    error = Erreur,
    onError = TexteSombre,
)

/**
 * Couleurs sémantiques (statuts, IA) non couvertes par le rôle Material3
 * standard — exposées séparément plutôt que détournées d'un rôle existant
 * (ex. "tertiary") qui prêterait à confusion.
 */
data class SpiPmeExtendedColors(
    val succes: androidx.compose.ui.graphics.Color,
    val avertissement: androidx.compose.ui.graphics.Color,
    val information: androidx.compose.ui.graphics.Color,
    val ia: androidx.compose.ui.graphics.Color,
    val sidebar: androidx.compose.ui.graphics.Color,
)

private val LightExtendedColors = SpiPmeExtendedColors(
    succes = Succes, avertissement = Avertissement, information = Information, ia = IA, sidebar = Sidebar,
)
private val DarkExtendedColors = SpiPmeExtendedColors(
    succes = Succes, avertissement = Avertissement, information = Information, ia = IA, sidebar = CarteSombre,
)

val LocalSpiPmeExtendedColors = staticCompositionLocalOf { LightExtendedColors }

object SpiPmeTheme {
    val extendedColors: SpiPmeExtendedColors
        @Composable get() = LocalSpiPmeExtendedColors.current
}

@Composable
fun SpiPmeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalSpiPmeExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SpiPmeTypography,
            content = content,
        )
    }
}
