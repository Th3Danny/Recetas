package com.example.recetas.gustos.data.datasource

import com.example.recetas.gustos.data.model.GustoResponse
import com.example.recetas.gustos.data.model.PreferenceRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface GustosService {

    @GET("ingredients")
    suspend fun getAllGustos(): Response<List<GustoResponse>>

    @GET("preferences")
    suspend fun getUserGustos(
        @Query("userId") userId: Int
    ): Response<List<GustoResponse>>

    @Multipart
    @POST("ingredients/with-image")
    suspend fun postIngredient(
        @Part("name") name: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<Unit>



    @POST("preferences/ingredient")
    suspend fun addGustoToUser(
        @Body requestBody: PreferenceRequest
    ): Response<Unit>


}