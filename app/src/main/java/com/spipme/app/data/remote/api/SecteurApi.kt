package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.core.SecteurDto
import com.spipme.app.data.remote.dto.registry.PageDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * Secteurs accessibles Ã  l'utilisateur connectÃ© (pour le sÃ©lecteur "Secteur
 * actif"). Le serveur ne fait JAMAIS confiance Ã  un secteur envoyÃ© par le
 * client â€” cette liste, elle, est la source de vÃ©ritÃ© de ce qui est
 * rÃ©ellement autorisÃ© (apps.core.views.MesSecteursView).
 * RÃ©ponse PAGINÃ‰E (DEFAULT_PAGINATION_CLASS global) â€” 20/page, largement
 * suffisant ici (11 secteurs max au total selon le CDC).
 */
interface SecteurApi {
    @GET("secteurs")
    suspend fun mesSecteurs(): Response<PageDto<SecteurDto>>
}

