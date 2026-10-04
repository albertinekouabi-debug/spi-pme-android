package com.spipme.app.data.remote.api

import retrofit2.http.Header
import com.spipme.app.data.remote.dto.registry.CreerEntiteRequestDto
import com.spipme.app.data.remote.dto.registry.EntiteDto
import com.spipme.app.data.remote.dto.registry.PageDto
import com.spipme.app.data.remote.dto.registry.ResumeRegistreDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EntiteApi {
    @GET("entities/")
    suspend fun lister(
        @Query("secteur") secteurId: Int? = null,
        @Query("type") type: String? = null,
        @Query("statut") statut: String? = null,
        @Query("q") recherche: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PageDto<EntiteDto>>

    @GET("entities/summary/")
    suspend fun resume(@Query("secteur") secteurId: Int? = null): Response<ResumeRegistreDto>

    @GET("entities/{id}/")
    suspend fun obtenir(@Path("id") id: Int): Response<EntiteDto>

    @POST("entities/")
    suspend fun creer(@Header("Idempotency-Key") cle: String? = null, @Body body: CreerEntiteRequestDto): Response<EntiteDto>
}
