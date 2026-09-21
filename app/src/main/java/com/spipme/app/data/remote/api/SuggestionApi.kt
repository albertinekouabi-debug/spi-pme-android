package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.intelligence.GenererSuggestionsRequestDto
import com.spipme.app.data.remote.dto.intelligence.RejeterSuggestionRequestDto
import com.spipme.app.data.remote.dto.intelligence.ResumeSuggestionsDto
import com.spipme.app.data.remote.dto.intelligence.SuggestionDto
import com.spipme.app.data.remote.dto.registry.PageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SuggestionApi {
    @GET("suggestions/")
    suspend fun lister(
        @Query("secteur") secteurId: Int? = null,
        @Query("statut") statut: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PageDto<SuggestionDto>>

    @GET("suggestions/summary/")
    suspend fun resume(@Query("secteur") secteurId: Int? = null): Response<ResumeSuggestionsDto>

    @POST("suggestions/generate/")
    suspend fun generer(@Body body: GenererSuggestionsRequestDto): Response<List<SuggestionDto>>

    @POST("suggestions/{id}/validate/")
    suspend fun valider(@Path("id") id: Int): Response<SuggestionDto>

    @POST("suggestions/{id}/reject/")
    suspend fun rejeter(@Path("id") id: Int, @Body body: RejeterSuggestionRequestDto): Response<SuggestionDto>
}
