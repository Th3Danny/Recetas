package com.example.recetas.home.data.model

// Respuesta API con formato de "content"
data class RecetasContentResponse(
    val content: List<RecetaDtoApi>
)

// DTO que coincide con la estructura real de la API
data class RecetaDtoApi(
    val id: Int,
    val title: String,
    val description: String,
    val preparation_time: Int,
    val cooking_time: Int?,
    val difficulty: String,
    val image_url: String?,
    val author_name: String,
    val created_at: String,
    val category_names: List<String>?
)

// Extensión para mapear RecetaDtoApi a Receta
fun RecetaDtoApi.toReceta(): Receta {
    return Receta(
        id = id,
        nombre = title,
        descripcion = description,
        autor = author_name,
        gustos = category_names?.mapIndexed { index, categoryName ->
            Gusto(
                id = index,  // O un ID predeterminado
                nombre = categoryName
            )
        } ?: emptyList()
    )
}