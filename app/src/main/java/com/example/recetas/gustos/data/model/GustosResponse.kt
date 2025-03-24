package com.example.recetas.gustos.data.model

data class GustoResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val imagen: String? = null
) {
    fun toDomainModel(): Gusto {
        return Gusto(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            imagen = imagen
        )
    }
}


data class UserGustosResponse(
    val userId: Int,
    val gustos: List<GustoResponse>
)

data class GustoOperationResponse(
    val success: Boolean,
    val message: String? = null
)