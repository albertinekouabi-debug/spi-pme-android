package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.alerts.AlerteDto
import com.spipme.app.data.remote.dto.alerts.GenererAlertesRequestDto
import com.spipme.app.data.remote.dto.alerts.GenerationAlertesResponseDto
import com.spipme.app.data.remote.dto.alerts.ResumeAlertesDto
import com.spipme.app.data.remote.dto.registry.PageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AlerteApi {
    @GET("alerts/")
    suspend fun lister(
        @Query("secteur") secteurId: Int? = null,
        @Query("statut") statut: String? = null,
        @Query("niveau") niveau: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PageDto<AlerteDto>>

    @GET("alerts/summary/")
    suspend fun resume(@Query("secteur") secteurId: Int? = null): Response<ResumeAlertesDto>

    @POST("alerts/generate/")
    suspend fun generer(@Body body: GenererAlertesRequestDto): Response<GenerationAlertesResponseDto>

    @POST("alerts/{id}/resolve/")
    suspend fun resoudre(@Path("id") id: Int): Response<AlerteDto>

    @POST("alerts/{id}/ignore/")
    suspend fun ignorer(@Path("id") id: Int): Response<AlerteDto>
}
