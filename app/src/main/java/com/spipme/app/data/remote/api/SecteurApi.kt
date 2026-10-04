package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.core.SecteurDto
import com.spipme.app.data.remote.dto.registry.PageDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * Secteurs accessibles à l'utilisateur connecté (pour le sélecteur "Secteur
 * actif"). Le serveur ne fait JAMAIS confiance à un secteur envoyé par le
 * client — cette liste, elle, est la source de vérité de ce qui est
 * réellement autorisé (apps.core.views.MesSecteursView).
 * Réponse PAGINÉE (DEFAULT_PAGINATION_CLASS global) — 20/page, largement
 * suffisant ici (11 secteurs max au total selon le CDC).
 */
interface SecteurApi {
    @GET("secteurs")
    suspend fun mesSecteurs(): Response<PageDto<SecteurDto>>
}

