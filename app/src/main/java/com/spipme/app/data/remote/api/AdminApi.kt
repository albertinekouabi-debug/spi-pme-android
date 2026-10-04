package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.UtilisateurDto
import com.spipme.app.data.remote.dto.accounts.CreerUtilisateurRequestDto
import com.spipme.app.data.remote.dto.accounts.RoleDto
import com.spipme.app.data.remote.dto.registry.PageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Administration — RÉSERVÉ aux Administrateurs côté serveur (EstAdministrateur). */
interface AdminApi {
    @GET("users/")
    suspend fun listerUtilisateurs(@Query("page") page: Int? = null): Response<PageDto<UtilisateurDto>>

    @POST("users/")
    suspend fun creerUtilisateur(@Body body: CreerUtilisateurRequestDto): Response<UtilisateurDto>

    /** Désactivation (soft delete) — jamais de suppression physique, préserve l'intégrité référentielle. */
    @DELETE("users/{id}/")
    suspend fun desactiverUtilisateur(@Path("id") id: Int): Response<Unit>

    @GET("roles/")
    suspend fun listerRoles(): Response<PageDto<RoleDto>>
}

