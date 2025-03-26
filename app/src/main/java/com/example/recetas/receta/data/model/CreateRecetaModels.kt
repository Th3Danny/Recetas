package com.example.recetas.receta.data.model

import com.google.gson.annotations.SerializedName

data class CreateRecetaRequest(
    @SerializedName("userId") val user_id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("instructions") val instructions: String,
    @SerializedName("preparationTime") val preparation_time: Int,
    @SerializedName("cookingTime") val cooking_time: Int,
    @SerializedName("servings") val servings: Int,
    @SerializedName("difficulty") val difficulty: String,
    @SerializedName("categoryIds") val category_ids: List<Int>,
    @SerializedName("ingredients") val ingredients: List<IngredientRequest>
)

data class IngredientRequest(
    @SerializedName("ingredientId") val ingredient_id: Int,
    @SerializedName("quantity") val quantity: String,
    @SerializedName("unit") val unit: String
)

data class CreateRecetaResponse(
    val success: Boolean,
    val message: String,
    val data: RecetaCreatedDto
)

data class RecetaCreatedDto(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("userId") val user_id: Int
)
