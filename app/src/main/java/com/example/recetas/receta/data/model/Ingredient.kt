package com.example.recetas.receta.data.model

import com.example.recetas.register.data.model.Ingredient
import com.example.recetas.register.data.model.IngredientDto

data class Ingredient(
    val id: Int,
    val name: String,
    var quantity: String = "",
    var unit: String = "g"
) {
    val imageUrl: String
        get() = "http://34.194.243.51:8080/api/ingredients/$id/image"
}




fun IngredientDto.toIngredient(): Ingredient {
    return Ingredient(
        id = id,
        name = name,
        quantity = "",
        unit = "g"
    )
}





