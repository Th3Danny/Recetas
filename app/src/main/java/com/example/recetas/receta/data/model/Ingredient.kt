package com.example.recetas.receta.data.model

data class Ingredient(
    val id: Int,
    val name: String,
    var quantity: String = "",
    var unit: String = "g"
)


data class IngredientsResponse(
    val content: List<IngredientDto>?
)


data class IngredientDto(
    val id: Int,
    val name: String
)


fun IngredientDto.toIngredient(): Ingredient {
    return Ingredient(
        id = id,
        name = name,
        quantity = "",
        unit = "g"
    )
}