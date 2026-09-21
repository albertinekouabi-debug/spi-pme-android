package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.ImportApi
import com.spipme.app.data.remote.dto.imports.AnomalieDto
import com.spipme.app.data.remote.dto.imports.ImportFichierDto
import com.spipme.app.domain.model.Anomalie
import com.spipme.app.domain.model.ApercuImport
import com.spipme.app.domain.model.FichierSelectionne
import com.spipme.app.domain.model.ImportFichier
import com.spipme.app.domain.repository.ImportRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportRepositoryImpl @Inject constructor(
    private val importApi: ImportApi,
    private val json: Json,
) : ImportRepository {

    override suspend fun obtenirHistorique(secteurId: Int): Resultat<List<ImportFichier>> {
        val resultat = executerAppelApi(json) { importApi.historique(secteurId = secteurId) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.results.map { it.versDomaine() })
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun previsualiser(secteurId: Int, fichier: FichierSelectionne): Resultat<ApercuImport> {
        val (partieFichier, corpsSecteur) = construireCorpsMultipart(secteurId, fichier)
        val resultat = executerAppelApi(json) { importApi.previsualiser(partieFichier, corpsSecteur) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(
                ApercuImport(
                    lignesTotales = resultat.donnees.lignesTotales,
                    lignesValides = resultat.donnees.lignesValides,
                    lignesRejetees = resultat.donnees.lignesRejetees,
                    anomalies = resultat.donnees.rapportAnomalies.map { it.versDomaine() },
                    apercu = resultat.donnees.apercu.map { it.versPaires() },
                )
            )
            is Resultat.Echec -> resultat
        }
    }

    override suspend fun importer(secteurId: Int, fichier: FichierSelectionne): Resultat<ImportFichier> {
        val (partieFichier, corpsSecteur) = construireCorpsMultipart(secteurId, fichier)
        val resultat = executerAppelApi(json) { importApi.importer(partieFichier, corpsSecteur) }
        return when (resultat) {
            is Resultat.Succes -> Resultat.Succes(resultat.donnees.versDomaine())
            is Resultat.Echec -> resultat
        }
    }

    private fun construireCorpsMultipart(secteurId: Int, fichier: FichierSelectionne): Pair<MultipartBody.Part, okhttp3.RequestBody> {
        val typeMime = deviverTypeMime(fichier.nom)
        val corpsFichier = fichier.octets.toRequestBody(typeMime.toMediaTypeOrNull())
        val partieFichier = MultipartBody.Part.createFormData("fichier", fichier.nom, corpsFichier)
        val corpsSecteur = secteurId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        return partieFichier to corpsSecteur
    }

    private fun deviverTypeMime(nomFichier: String): String = when (nomFichier.substringAfterLast('.', "").lowercase()) {
        "csv" -> "text/csv"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "xls" -> "application/vnd.ms-excel"
        else -> "application/octet-stream"
    }
}

private fun AnomalieDto.versDomaine() = Anomalie(ligne = ligne, colonne = colonne, motif = motif)

private fun ImportFichierDto.versDomaine() = ImportFichier(
    id = id,
    nomFichier = nomFichier,
    typeFichier = typeFichier,
    statut = statut,
    lignesTotales = lignesTotales,
    lignesImportees = lignesImportees,
    lignesRejetees = lignesRejetees,
    rapportAnomalies = rapportAnomalies.map { it.versDomaine() },
    auteurNom = auteurNom,
    secteurId = secteur,
    dateImport = dateImport,
)

/** Même logique générique que pour les facteurs de suggestion IA — les colonnes du fichier importé varient. */
private fun JsonObject.versPaires(): List<Pair<String, String>> =
    entries.map { (cle, valeur) ->
        val texte = when (valeur) {
            is JsonNull -> "—"
            is JsonPrimitive -> valeur.content
            else -> valeur.toString()
        }
        cle to texte
    }
