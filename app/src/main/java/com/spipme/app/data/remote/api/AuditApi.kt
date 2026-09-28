package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.audit.EvenementAuditDto
import com.spipme.app.data.remote.dto.audit.ResumeAuditDto
import com.spipme.app.data.remote.dto.registry.PageDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Journal d'audit â€” LECTURE SEULE cÃ´tÃ© serveur (ReadOnlyModelViewSet +
 * protections ORM et trigger PostgreSQL). Aucune mÃ©thode d'Ã©criture n'est
 * exposÃ©e ici volontairement : les logs ne doivent pas pouvoir Ãªtre altÃ©rÃ©s.
 * Filtres conformes au contrat rÃ©el du backend (apps/audit/views.py).
 */
interface AuditApi {
    @GET("audit-log/")
    suspend fun lister(
        @Query("module") module: String? = null,
        @Query("resultat") resultat: String? = null,
        @Query("auteur") auteurId: Int? = null,
        @Query("action") action: String? = null,
        @Query("q") recherche: String? = null,
        @Query("date_debut") dateDebut: String? = null,
        @Query("date_fin") dateFin: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PageDto<EvenementAuditDto>>

    @GET("audit-log/summary/")
    suspend fun resume(
        @Query("module") module: String? = null,
        @Query("resultat") resultat: String? = null,
        @Query("q") recherche: String? = null,
    ): Response<ResumeAuditDto>
}

