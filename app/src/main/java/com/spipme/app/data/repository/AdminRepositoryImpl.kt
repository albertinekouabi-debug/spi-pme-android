package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.AdminApi
import com.spipme.app.data.remote.dto.UtilisateurDto
import com.spipme.app.data.remote.dto.accounts.CreerUtilisateurRequestDto
import com.spipme.app.data.remote.dto.accounts.RoleDto
import com.spipme.app.domain.model.Role
import com.spipme.app.domain.model.Utilisateur
import com.spipme.app.domain.repository.AdminRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val adminApi: AdminApi,
    private val json: Json,
) : AdminRepository {

    override suspend fun listerUtilisateurs(page: Int?): Resultat<Pair<List<Utilisateur>, Boolean>> {
        val reponse = executerAppelApi(json) { adminApi.listerUtilisateurs(page) }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(
                reponse.donnees.results.map { it.versDomaine() } to (reponse.donnees.next != null)
            )
            is Resultat.Echec -> reponse
        }
    }

    override suspend fun creerUtilisateur(
        nomUtilisateur: String,
        email: String,
        motDePasse: String,
        roleId: Int,
        secteurPrincipalId: Int,
        nomComplet: String?,
        telephone: String?,
    ): Resultat<Utilisateur> {
        val reponse = executerAppelApi(json) {
            adminApi.creerUtilisateur(
                CreerUtilisateurRequestDto(
                    nomUtilisateur = nomUtilisateur,
                    email = email,
                    password = motDePasse,
                    nomComplet = nomComplet,
                    telephone = telephone,
                    role = roleId,
                    secteurPrincipal = secteurPrincipalId,
                )
            )
        }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(reponse.donnees.versDomaine())
            is Resultat.Echec -> reponse
        }
    }

    override suspend fun desactiverUtilisateur(id: Int): Resultat<Unit> {
        return when (val reponse = executerAppelApi(json) { adminApi.desactiverUtilisateur(id) }) {
            is Resultat.Succes -> Resultat.Succes(Unit)
            is Resultat.Echec -> reponse
        }
    }

    override suspend fun listerRoles(): Resultat<List<Role>> {
        val reponse = executerAppelApi(json) { adminApi.listerRoles() }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(
                reponse.donnees.results.map { Role(id = it.id, nom = it.nom, description = it.description) }
            )
            is Resultat.Echec -> reponse
        }
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
    permissions = permissions,
)

