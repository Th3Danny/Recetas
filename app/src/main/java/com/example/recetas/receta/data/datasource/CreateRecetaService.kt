package com.example.recetas.receta.data.datasource

import com.example.recetas.receta.data.model.CategoriesResponse
import com.example.recetas.receta.data.model.CategoryDto
import com.example.recetas.receta.data.model.CreateRecetaRequest
import com.example.recetas.receta.data.model.CreateRecetaResponse
import com.example.recetas.receta.data.model.IngredientsResponse
import retrofit2.Response
import retrofit2.http.*

interface CreateRecetaService {

    @POST("recipes")
    suspend fun createReceta(
        @Body createRecetaRequest: CreateRecetaRequest
    ): Response<CreateRecetaResponse>

    @GET("ingredients")
    suspend fun getIngredients(): Response<IngredientsResponse>

    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
}