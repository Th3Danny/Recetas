package com.example.recetas.gustos.data.datasource

import com.example.recetas.gustos.data.model.GustoResponse
import retrofit2.Response
import retrofit2.http.*

interface GustosService {

    @GET("gustos")
    suspend fun getAllGustos(): Response<List<GustoResponse>>

    @GET("users/{userId}/gustos")
    suspend fun getUserGustos(@Path("userId") userId: Int): Response<List<GustoResponse>>

    @POST("users/{userId}/gustos/{gustoId}")
    suspend fun addGustoToUser(
        @Path("userId") userId: Int,
        @Path("gustoId") gustoId: Int
    ): Response<Unit>

    @DELETE("users/{userId}/gustos/{gustoId}")
    suspend fun removeGustoFromUser(
        @Path("userId") userId: Int,
        @Path("gustoId") gustoId: Int
    ): Response<Unit>
}