package com.example.recetas.receta.data.model

data class CreateRecetaRequest(
    val user_id: Int,
    val title: String,
    val description: String,
    val instructions: String,
    val preparation_time: Int,
    val cooking_time: Int,
    val servings: Int,
    val difficulty: String,
    val category_ids: List<Int>,
    val ingredients: List<IngredientRequest>
)

data class IngredientRequest(
    val ingredient_id: Int,
    val quantity: String,
    val unit: String
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
    val user_id: Int
)