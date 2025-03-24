package com.example.recetas.gustos.data.model
import com.google.gson.annotations.SerializedName
data class GustoResponse(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("ingredient_id")
    val ingredientId: Int,
    @SerializedName("ingredient_name")
    val ingredientName: String,
    val preferred: Boolean
)

// Extension function para convertir GustoResponse a Gusto
fun GustoResponse.toGusto(): Gusto {
    return Gusto(
        id = this.ingredientId,  // Usamos el ID del ingrediente como ID del gusto
        nombre = this.ingredientName
    )
}


data class UserGustosResponse(
    val userId: Int,
    val gustos: List<GustoResponse>
)

data class GustoOperationResponse(
    val success: Boolean,
    val message: String? = null
)