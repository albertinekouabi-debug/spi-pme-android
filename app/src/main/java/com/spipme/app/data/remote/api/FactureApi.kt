package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.registry.PageDto
import com.spipme.app.data.remote.dto.treasury.DeclarationConformiteDto
import com.spipme.app.data.remote.dto.treasury.DeclarerRequestDto
import com.spipme.app.data.remote.dto.treasury.ExempterRequestDto
import com.spipme.app.data.remote.dto.treasury.FactureDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FactureApi {
    @GET("invoices/")
    suspend fun lister(
        @Query("secteur") secteurId: Int? = null,
        @Query("statut") statut: String? = null,
        @Query("entite") entiteId: Int? = null,
        @Query("page") page: Int? = null,
    ): Response<PageDto<FactureDto>>
}

/**
 * ConformitÃ© rÃ©glementaire. Les transitions d'Ã©tat passent EXCLUSIVEMENT par
 * /declare et /exempt (le serializer est en lecture seule sur tous ses champs) :
 * aucune modification gÃ©nÃ©rique n'est possible, et c'est volontaire.
 */
interface ConformiteApi {
    @GET("compliance-declarations/")
    suspend fun lister(
        @Query("secteur") secteurId: Int? = null,
        @Query("statut") statut: String? = null,
        @Query("motif") motif: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PageDto<DeclarationConformiteDto>>

    @POST("compliance-declarations/{id}/declare/")
    suspend fun declarer(
        @Path("id") id: Int,
        @Body body: DeclarerRequestDto,
    ): Response<DeclarationConformiteDto>

    @POST("compliance-declarations/{id}/exempt/")
    suspend fun exempter(
        @Path("id") id: Int,
        @Body body: ExempterRequestDto,
    ): Response<DeclarationConformiteDto>
}

