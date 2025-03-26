package com.example.recetas.receta.data.datasource

import com.example.recetas.receta.data.model.CategoryDto
import com.example.recetas.receta.data.model.CreateRecetaRequest
import com.example.recetas.receta.data.model.CreateRecetaResponse
import com.example.recetas.register.data.model.IngredientsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface CreateRecetaService {

    @Multipart
    @POST("recipes")
    suspend fun createReceta(
        @Part("recipeDTO") recipeDto: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<CreateRecetaResponse>


    @GET("ingredients")
    suspend fun getIngredients(): Response<IngredientsResponse>

    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
}