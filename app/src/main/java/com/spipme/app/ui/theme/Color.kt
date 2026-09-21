package com.spipme.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette officielle SPI-PME (Guide_Palette_Couleurs_SPI_PME.docx).
 * Aucune couleur "magique" ailleurs dans le code — toujours passer par ce
 * fichier ou par MaterialTheme.colorScheme pour rester cohérent.
 */

// --- Mode clair ---
val BleuPrincipal = Color(0xFF2563EB)      // marque, boutons principaux, liens
val BleuFonce = Color(0xFF1E40AF)          // états survolés, éléments actifs, navigation
val Cyan = Color(0xFF38BDF8)               // accents visuels, graphiques
val FondClair = Color(0xFFF8FAFC)          // arrière-plan principal
val SurfaceClair = Color(0xFFFFFFFF)       // cartes, fenêtres, formulaires
val Sidebar = Color(0xFF0F172A)            // menu latéral
val BordureClair = Color(0xFFE2E8F0)       // séparateurs, contours
val TexteClair = Color(0xFF0F172A)         // texte principal
val TexteSecondaireClair = Color(0xFF64748B) // descriptions, métadonnées

// --- Sémantique (identique clair/sombre) ---
val Succes = Color(0xFF22C55E)
val Avertissement = Color(0xFFF59E0B)
val Erreur = Color(0xFFEF4444)
val Information = Color(0xFF3B82F6)
val IA = Color(0xFF7C3AED)                 // suggestions et fonctionnalités IA

// --- Mode sombre ---
val FondSombre = Color(0xFF020617)
val SurfaceSombre = Color(0xFF0F172A)
val CarteSombre = Color(0xFF1E293B)
val TexteSombre = Color(0xFFF8FAFC)
val TexteSecondaireSombre = Color(0xFFCBD5E1)
