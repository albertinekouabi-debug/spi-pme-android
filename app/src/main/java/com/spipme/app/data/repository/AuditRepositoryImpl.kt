package com.spipme.app.data.repository

import com.spipme.app.core.network.executerAppelApi
import com.spipme.app.core.util.Resultat
import com.spipme.app.data.remote.api.AuditApi
import com.spipme.app.data.remote.dto.audit.EvenementAuditDto
import com.spipme.app.domain.model.EvenementAudit
import com.spipme.app.domain.model.ResumeAudit
import com.spipme.app.domain.repository.AuditRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditRepositoryImpl @Inject constructor(
    private val auditApi: AuditApi,
    private val json: Json,
) : AuditRepository {

    /** Retourne la page demandée + un booléen indiquant s'il reste des pages (pagination DRF). */
    override suspend fun lister(
        module: String?,
        resultat: String?,
        recherche: String?,
        page: Int?,
    ): Resultat<Pair<List<EvenementAudit>, Boolean>> {
        val reponse = executerAppelApi(json) {
            auditApi.lister(module = module, resultat = resultat, recherche = recherche, page = page)
        }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(
                reponse.donnees.results.map { it.versDomaine() } to (reponse.donnees.next != null)
            )
            is Resultat.Echec -> reponse
        }
    }

    override suspend fun obtenirResume(
        module: String?,
        resultat: String?,
        recherche: String?,
    ): Resultat<ResumeAudit> {
        val reponse = executerAppelApi(json) {
            auditApi.resume(module = module, resultat = resultat, recherche = recherche)
        }
        return when (reponse) {
            is Resultat.Succes -> Resultat.Succes(
                ResumeAudit(
                    total = reponse.donnees.total,
                    reussies = reponse.donnees.reussies,
                    avertissements = reponse.donnees.avertissements,
                    echecs = reponse.donnees.echecs,
                )
            )
            is Resultat.Echec -> reponse
        }
    }
}

private fun EvenementAuditDto.versDomaine() = EvenementAudit(
    id = id,
    action = action,
    module = module,
    cibleType = cibleType,
    cibleId = cibleId,
    resultat = resultat,
    adresseIp = adresseIp,
    auteurNom = auteurNom,
    dateAction = dateAction,
)

