package com.spipme.app.data.remote.api

import retrofit2.http.Header
import com.spipme.app.data.remote.dto.registry.PageDto
import com.spipme.app.data.remote.dto.tasks.CreerTacheRequestDto
import com.spipme.app.data.remote.dto.tasks.HistoriqueStatutDto
import com.spipme.app.data.remote.dto.tasks.ModifierStatutTacheRequestDto
import com.spipme.app.data.remote.dto.tasks.ResumeTachesDto
import com.spipme.app.data.remote.dto.tasks.TacheDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TacheApi {
    @GET("tasks/")
    suspend fun lister(
        @Query("secteur") secteurId: Int? = null,
        @Query("statut") statut: String? = null,
        @Query("retard") retard: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PageDto<TacheDto>>

    @GET("tasks/summary/")
    suspend fun resume(@Query("secteur") secteurId: Int? = null): Response<ResumeTachesDto>

    @GET("tasks/{id}/history/")
    suspend fun historique(@Path("id") id: Int): Response<List<HistoriqueStatutDto>>

    @POST("tasks/")
    suspend fun creer(@Header("Idempotency-Key") cle: String? = null, @Body body: CreerTacheRequestDto): Response<TacheDto>

    @PATCH("tasks/{id}/")
    suspend fun modifierStatut(@Path("id") id: Int, @Body body: ModifierStatutTacheRequestDto): Response<TacheDto>
}
