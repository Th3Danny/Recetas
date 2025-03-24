package com.example.recetas.receta.data.model

// Respuesta API de categorías
data class CategoriesResponse(
    val content: List<CategoryDto>?
)

// DTO que coincide con la estructura real de la API
data class CategoryDto(
    val id: Int,
    val name: String
)

// Modelo de dominio para las categorías
data class Category(
    val id: Int,
    val name: String
)

// Extensión para mapear CategoryDto a Category
fun CategoryDto.toCategory(): Category {
    return Category(
        id = id,
        name = name
    )
}