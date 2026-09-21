package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Utilisateur
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val estConnecteFlow: Flow<Boolean>

    suspend fun connexion(identifiant: String, motDePasse: String): Resultat<Utilisateur>

    /** Connexion hors ligne (maquette connexion_hors_ligne.png) — nécessite une connexion en ligne préalable réussie sur cet appareil. */
    suspend fun connexionHorsLigne(identifiant: String, motDePasse: String): Resultat<Utilisateur>

    /**
     * Auto-inscription publique. Le compte créé est INACTIF côté serveur
     * (vérification email requise) — ne retourne donc aucun token, aucune
     * session n'est ouverte. Résultat purement informatif pour l'écran
     * ("vérifiez votre boîte mail").
     */
    suspend fun inscription(
        nomUtilisateur: String,
        email: String,
        motDePasse: String,
        codeInvitation: String,
        nomComplet: String?,
        telephone: String?,
    ): Resultat<Unit>

    suspend fun deconnexion()
}
