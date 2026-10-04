package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Utilisateur

/**
 * Profil de l'utilisateur connecté. PATCH volontairement restreint à
 * nom_complet/telephone — le serveur (MoiUpdateSerializer) refuse tout le
 * reste, ce contrat est reflété ici pour ne pas laisser croire à l'UI
 * qu'elle peut modifier le rôle, le secteur ou le statut actif.
 */
interface MoiRepository {
    suspend fun obtenirProfil(): Resultat<Utilisateur>
    suspend fun mettreAJourProfil(nomComplet: String?, telephone: String?): Resultat<Utilisateur>
    suspend fun changerMotDePasse(ancienMotDePasse: String, nouveauMotDePasse: String): Resultat<Unit>
}

