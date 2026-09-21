package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.SuggestionApi
import com.spipme.app.data.remote.dto.intelligence.GenererSuggestionsRequestDto
import com.spipme.app.data.remote.dto.intelligence.RejeterSuggestionRequestDto
import com.spipme.app.data.remote.dto.intelligence.SuggestionDto
import com.spipme.app.domain.model.ResumeSuggestions
import com.spipme.app.domain.model.Suggestion
import com.spipme.app.domain.repository.SuggestionRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionRepositoryImpl @Inject constructor(
    private val suggestionApi: SuggestionApi,
    private val json: Json,
) : SuggestionRepository {

    override suspend fun lister(secteurId: Int, statut: String?): Resultat<List<Suggestion>> {
        val resultat = executerAppelApi(json) { suggestionApi.lister(secteurId = secteurId, statut = statut) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.results.map { it.versDomaine() })
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun obtenirResume(secteurId: Int): Resultat<ResumeSuggestions> {
        val resultat = executerAppelApi(json) { suggestionApi.resume(secteurId = secteurId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(
                ResumeSuggestions(
                    total = resultat.donnees.total,
                    validees = resultat.donnees.validees,
                    enAttente = resultat.donnees.enAttente,
                    rejetees = resultat.donnees.rejetees,
                    tauxAcceptation = resultat.donnees.tauxAcceptation,
                )
            )
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun genererPourSecteur(secteurId: Int): Resultat<List<Suggestion>> {
        val resultat = executerAppelApi(json) { suggestionApi.generer(GenererSuggestionsRequestDto(secteur = secteurId)) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.map { it.versDomaine() })
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun valider(suggestionId: Int): Resultat<Suggestion> {
        val resultat = executerAppelApi(json) { suggestionApi.valider(suggestionId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun rejeter(suggestionId: Int, motif: String): Resultat<Suggestion> {
        val resultat = executerAppelApi(json) { suggestionApi.rejeter(suggestionId, RejeterSuggestionRequestDto(motif = motif)) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }
}

private fun SuggestionDto.versDomaine() = Suggestion(
    id = id,
    typeAlgorithme = typeAlgorithme,
    categorie = categorie,
    titre = titre,
    description = description,
    facteurs = facteurs.versPaires(),
    impactEstime = impactEstime?.let { BigDecimal(it) },
    confiance = confiance?.let { BigDecimal(it) },
    statut = statut,
    motifDecision = motifDecision,
    ressourceLieeNom = ressourceLieeNom,
    secteurId = secteur,
    decideurNom = decideurNom,
    dateCreation = dateCreation,
    dateDecision = dateDecision,
)

/**
 * Les facteurs varient par algorithme (seuil vs régression linéaire, cf.
 * backend apps/intelligence/algorithms/) — pas de schéma fixe possible côté
 * client. Conversion générique clé/valeur pour affichage (chips "Facteurs :"
 * de la maquette), plutôt que des champs typés qui devraient être maintenus
 * en double à chaque nouvel algorithme ajouté côté backend.
 */
private fun JsonObject.versPaires(): List<Pair<String, String>> =
    entries.map { (cle, valeur) ->
        val texte = when (valeur) {
            is JsonNull -> "—"
            is JsonPrimitive -> valeur.content
            else -> valeur.toString()
        }
        cle.replace("_", " ").replaceFirstChar { it.uppercase() } to texte
    }
