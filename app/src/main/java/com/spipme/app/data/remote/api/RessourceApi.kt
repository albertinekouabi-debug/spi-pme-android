package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.registry.PageDto
import com.spipme.app.data.remote.dto.resources.CreerRessourceRequestDto
import com.spipme.app.data.remote.dto.resources.PointEvolutionDto
import com.spipme.app.data.remote.dto.resources.ResumeRessourcesDto
import com.spipme.app.data.remote.dto.resources.RessourceDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RessourceApi {
    @GET("resources/")
    suspend fun lister(
        @Query("secteur") secteurId: Int? = null,
        @Query("statut") statut: String? = null,
        @Query("type") type: String? = null,
        @Query("q") recherche: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PageDto<RessourceDto>>

    @GET("resources/summary/")
    suspend fun resume(@Query("secteur") secteurId: Int? = null): Response<ResumeRessourcesDto>

    @GET("resources/{id}/evolution/")
    suspend fun evolution(@Path("id") id: Int, @Query("jours") jours: Int = 7): Response<List<PointEvolutionDto>>

    @POST("resources/")
    suspend fun creer(@Body body: CreerRessourceRequestDto): Response<RessourceDto>
}
