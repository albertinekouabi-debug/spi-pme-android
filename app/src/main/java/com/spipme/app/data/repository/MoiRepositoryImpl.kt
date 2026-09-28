package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.MoiApi
import com.spipme.app.data.remote.dto.ChangerMotDePasseRequestDto
import com.spipme.app.data.remote.dto.MettreAJourProfilRequestDto
import com.spipme.app.data.remote.dto.UtilisateurDto
import com.spipme.app.domain.model.Utilisateur
import com.spipme.app.domain.repository.MoiRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoiRepositoryImpl @Inject constructor(
    private val moiApi: MoiApi,
    private val json: Json,
) : MoiRepository {

    override suspend fun obtenirProfil(): Resultat<Utilisateur> {
        val reponse = executerAppelApi(json) { moiApi.obtenirProfil() }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(reponse.donnees.versDomaine())
            is Resultat.Echec -> reponse
        }
    }

    override suspend fun mettreAJourProfil(nomComplet: String?, telephone: String?): Resultat<Utilisateur> {
        val reponse = executerAppelApi(json) {
            moiApi.mettreAJourProfil(MettreAJourProfilRequestDto(nomComplet = nomComplet, telephone = telephone))
        }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(reponse.donnees.versDomaine())
            is Resultat.Echec -> reponse
        }
    }

    override suspend fun changerMotDePasse(ancienMotDePasse: String, nouveauMotDePasse: String): Resultat<Unit> {
        val reponse = executerAppelApi(json) {
            moiApi.changerMotDePasse(
                ChangerMotDePasseRequestDto(ancienMotDePasse = ancienMotDePasse, nouveauMotDePasse = nouveauMotDePasse)
            )
        }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(Unit)
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
)

