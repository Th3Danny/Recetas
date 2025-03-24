package com.example.recetas.home.data.model

data class Receta(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val autor: String,
    val imagen: String? = null,
    val gustos: List<Gusto>
)

data class Gusto(
    val id: Int,
    val nombre: String
)