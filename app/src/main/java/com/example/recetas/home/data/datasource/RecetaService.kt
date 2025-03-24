package com.example.recetas.home.data.datasource


import com.example.recetas.home.data.model.RecetasContentResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface RecetaService {

    @GET("recipes")
    suspend fun getRecetas(): Response<RecetasContentResponse>

    @GET("recetas/{id}")
    suspend fun getReceta(@Path("id") recetaId: Int): Response<RecetasContentResponse>
}