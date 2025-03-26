package com.example.recetas.home.data.model

data class Receta(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val autor: String,
    val gustos: List<Gusto>
) {
    val imagenUrl: String
        get() = "http://34.194.243.51:8080/api/recipes/$id/image"
}

data class Gusto(
    val id: Int,
    val nombre: String
)