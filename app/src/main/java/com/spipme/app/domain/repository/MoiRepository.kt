package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Utilisateur

/**
 * Profil de l'utilisateur connectÃ©. PATCH volontairement restreint Ã 
 * nom_complet/telephone â€” le serveur (MoiUpdateSerializer) refuse tout le
 * reste, ce contrat est reflÃ©tÃ© ici pour ne pas laisser croire Ã  l'UI
 * qu'elle peut modifier le rÃ´le, le secteur ou le statut actif.
 */
interface MoiRepository {
    suspend fun obtenirProfil(): Resultat<Utilisateur>
    suspend fun mettreAJourProfil(nomComplet: String?, telephone: String?): Resultat<Utilisateur>
    suspend fun changerMotDePasse(ancienMotDePasse: String, nouveauMotDePasse: String): Resultat<Unit>
}

