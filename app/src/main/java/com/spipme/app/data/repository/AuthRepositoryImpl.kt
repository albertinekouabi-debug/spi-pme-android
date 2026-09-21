package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.security.HachageMotDePasse
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.security.TokenManager
import com.spipme.app.data.local.dao.UtilisateurLocalDao
import com.spipme.app.data.local.entity.UtilisateurLocalEntity
import com.spipme.app.data.remote.api.AuthApi
import com.spipme.app.data.remote.dto.UtilisateurDto
import com.spipme.app.data.remote.dto.auth.InscriptionRequestDto
import com.spipme.app.data.remote.dto.auth.LoginRequestDto
import com.spipme.app.data.remote.dto.auth.LogoutRequestDto
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Utilisateur
import com.spipme.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager,
    private val utilisateurLocalDao: UtilisateurLocalDao,
    private val json: Json,
) : AuthRepository {

    override val estConnecteFlow: Flow<Boolean> = tokenManager.estConnecteFlow

    override suspend fun connexion(identifiant: String, motDePasse: String): Resultat<Utilisateur> {
        val resultat = executerAppelApi(json) { authApi.login(LoginRequestDto(identifiant, motDePasse)) }
        return when (resultat) {
            is Resultat.Succes -> {
                val corps = resultat.donnees
                tokenManager.enregistrerTokens(access = corps.access, refresh = corps.refresh)
                mettreEnCachePourHorsLigne(corps.utilisateur, motDePasse)
                val utilisateurDomaine = corps.utilisateur.versDomaine()
                sessionManager.enregistrerSession(utilisateurDomaine)
                Resultat.Succes(utilisateurDomaine)
            }
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun connexionHorsLigne(identifiant: String, motDePasse: String): Resultat<Utilisateur> {
        val entite = utilisateurLocalDao.trouverParIdentifiant(identifiant)
            ?: return Resultat.Echec("Aucun compte synchronisé sur cet appareil pour cet identifiant. Une première connexion en ligne est nécessaire.")

        val motDePasseValide = HachageMotDePasse.verifier(motDePasse.toCharArray(), entite.hashMotDePasse)
        if (!motDePasseValide) {
            return Resultat.Echec("Identifiant ou mot de passe incorrect.")
        }

        val utilisateurDomaine = Utilisateur(
            id = entite.id,
            nomUtilisateur = entite.nomUtilisateur,
            email = entite.email,
            nomComplet = entite.nomComplet,
            telephone = null,
            roleId = 0,
            roleNom = entite.roleNom,
            secteurPrincipalId = entite.secteurPrincipalId,
            secteurPrincipalNom = entite.secteurPrincipalNom,
            secteurs = emptyList(),
            actif = true,
        )
        sessionManager.enregistrerSession(utilisateurDomaine)
        return Resultat.Succes(utilisateurDomaine)
        // Note : le mode hors ligne complet (file de synchronisation des actions
        // effectuées sans réseau) est du ressort du futur module Offline — ici,
        // seule l'authentification hors ligne est couverte, périmètre du module
        // Identité & Accès.
    }

    override suspend fun inscription(
        nomUtilisateur: String,
        email: String,
        motDePasse: String,
        codeInvitation: String,
        nomComplet: String?,
        telephone: String?,
    ): Resultat<Unit> {
        val resultat = executerAppelApi(json) {
            authApi.inscription(
                InscriptionRequestDto(
                    nomUtilisateur = nomUtilisateur,
                    email = email,
                    password = motDePasse,
                    nomComplet = nomComplet,
                    telephone = telephone,
                    codeInvitation = codeInvitation,
                )
            )
        }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(Unit)
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun deconnexion() {
        val refresh = tokenManager.refreshTokenActuel()
        if (refresh != null) {
            // Best-effort : la révocation côté serveur ne doit pas bloquer la
            // déconnexion locale si le réseau est indisponible.
            executerAppelApi(json) { authApi.logout(LogoutRequestDto(refresh)) }
        }
        tokenManager.effacerTokens()
        sessionManager.effacerSession()
    }

    private suspend fun mettreEnCachePourHorsLigne(utilisateur: UtilisateurDto, motDePasse: String) {
        utilisateurLocalDao.upsert(
            UtilisateurLocalEntity(
                id = utilisateur.id,
                nomUtilisateur = utilisateur.nomUtilisateur,
                email = utilisateur.email,
                nomComplet = utilisateur.nomComplet,
                hashMotDePasse = HachageMotDePasse.hacher(motDePasse.toCharArray()),
                roleNom = utilisateur.roleNom,
                secteurPrincipalId = utilisateur.secteurPrincipal,
                secteurPrincipalNom = utilisateur.secteurPrincipalNom,
                derniereSynchronisation = System.currentTimeMillis(),
            )
        )
    }
}

private fun UtilisateurDto.versDomaine() = Utilisateur(
    id = id,
    nomUtilisateur = nomUtilisateur,
    email = email,
    nomComplet = nomComplet,
    telephone = telephone,
    roleId = role,
    roleNom = roleNom,
    secteurPrincipalId = secteurPrincipal,
    secteurPrincipalNom = secteurPrincipalNom,
    secteurs = secteurs,
    actif = actif,
)
