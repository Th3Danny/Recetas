package com.example.recetas.gustos.data.model

data class Gusto(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val imagen: String? = null
)