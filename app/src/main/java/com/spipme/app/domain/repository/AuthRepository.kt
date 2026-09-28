package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Utilisateur
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val estConnecteFlow: Flow<Boolean>

    suspend fun connexion(identifiant: String, motDePasse: String): Resultat<Utilisateur>

    /** Connexion hors ligne (maquette connexion_hors_ligne.png) â€” nÃ©cessite une connexion en ligne prÃ©alable rÃ©ussie sur cet appareil. */
    suspend fun connexionHorsLigne(identifiant: String, motDePasse: String): Resultat<Utilisateur>

    /**
     * Auto-inscription publique. Le compte crÃ©Ã© est INACTIF cÃ´tÃ© serveur
     * (vÃ©rification email requise) â€” ne retourne donc aucun token, aucune
     * session n'est ouverte. RÃ©sultat purement informatif pour l'Ã©cran
     * ("vÃ©rifiez votre boÃ®te mail").
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

