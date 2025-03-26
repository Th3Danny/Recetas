package com.example.recetas.register.data.model

data class Ingredient(
    val id: Int,
    val name: String,
    var quantity: String = "",
    var unit: String = "g"
) {
    val imageUrl: String
        get() = "http://34.194.243.51:8080/api/ingredients/$id/image"
}


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