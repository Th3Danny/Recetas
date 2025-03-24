package com.example.recetas.gustos.data.datasource

import com.example.recetas.gustos.data.model.GustoResponse
import com.example.recetas.gustos.data.model.PreferenceRequest
import retrofit2.Response
import retrofit2.http.*

interface GustosService {

    @GET("ingredients")
    suspend fun getAllGustos(): Response<List<GustoResponse>>

    @GET("preferences")
    suspend fun getUserGustos(
        @Query("userId") userId: Int
    ): Response<List<GustoResponse>>


    @POST("preferences/ingredient")
    suspend fun addGustoToUser(
        @Body requestBody: PreferenceRequest
    ): Response<Unit>


}