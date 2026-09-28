package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Role
import com.spipme.app.domain.model.Utilisateur

/** RÃ©servÃ© aux Administrateurs â€” contrÃ´lÃ© cÃ´tÃ© serveur (EstAdministrateur), pas seulement masquÃ© cÃ´tÃ© client. */
interface AdminRepository {
    suspend fun listerUtilisateurs(page: Int? = null): Resultat<Pair<List<Utilisateur>, Boolean>>

    suspend fun creerUtilisateur(
        nomUtilisateur: String,
        email: String,
        motDePasse: String,
        roleId: Int,
        secteurPrincipalId: Int,
        nomComplet: String?,
        telephone: String?,
    ): Resultat<Utilisateur>

    suspend fun desactiverUtilisateur(id: Int): Resultat<Unit>

    suspend fun listerRoles(): Resultat<List<Role>>
}

