package com.example.recetas.receta.data.repository

import android.content.Context
import android.util.Log
import com.example.recetas.core.data.local.AppDataBase
import com.example.recetas.core.data.local.entities.IngredientEntity
import com.example.recetas.core.data.local.entities.PendingRecetaOperationEntity
import com.example.recetas.core.data.local.entities.RecetaEntity
import com.example.recetas.core.network.RetrofitHelper
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.receta.data.model.Category
import com.example.recetas.receta.data.model.CreateRecetaRequest
import com.example.recetas.receta.data.model.Ingredient
import com.example.recetas.receta.data.model.IngredientRequest
import com.example.recetas.receta.data.model.toCategory
import com.example.recetas.receta.data.model.toIngredient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CreateRecetaRepository(private val context: Context) {
    companion object {
        private const val TAG = "CreateRecetaRepository"
    }
    private val createRecetaService = RetrofitHelper.createRecetaService
    private val gustosService = RetrofitHelper.gustosService
    private val database = AppDataBase.getDatabase(context)
    private val recetaDao = database.recetaDao()
    private val pendingOperationDao = database.pendingRecetaOperationDao()

    suspend fun createReceta(
        title: String,
        description: String,
        instructions: String,
        preparationTime: Int,
        cookingTime: Int,
        servings: Int,
        difficulty: String,
        categoryIds: List<Int>,
        ingredients: List<Ingredient>,
        imagePath: String? = null
    ) {
        return withContext(Dispatchers.IO) {
            // Obtener el ID del usuario de las preferencias compartidas
            val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("userId", -1)

            if (userId == -1) {
                throw Exception("Usuario no autenticado")
            }

            try {
                // Intentar crear la receta en el servidor
                crearRecetaEnServidor(
                    userId, title, description, instructions, preparationTime,
                    cookingTime, servings, difficulty, categoryIds, ingredients, imagePath
                )
            } catch (e: Exception) {
                // Si hay un error de red, guardar la operación pendiente y la receta localmente
                guardarRecetaLocalmente(
                    userId, title, description, instructions, preparationTime,
                    cookingTime, servings, difficulty, categoryIds, ingredients, imagePath
                )
            }
        }
    }

    private suspend fun crearRecetaEnServidor(
        userId: Int,
        title: String,
        description: String,
        instructions: String,
        preparationTime: Int,
        cookingTime: Int,
        servings: Int,
        difficulty: String,
        categoryIds: List<Int>,
        ingredients: List<Ingredient>,
        imagePath: String? = null
    ) {
        // Convertir los ingredientes al formato requerido para la API
        val ingredientRequests = ingredients.map { ingredient ->
            IngredientRequest(
                ingredient_id = ingredient.id,
                quantity = ingredient.quantity,
                unit = ingredient.unit
            )
        }

        if (imagePath != null) {
            // Aquí iría el código para subir la receta con imagen
            // Código comentado en el original
        } else {
            // Crear solicitud sin imagen
            val request = CreateRecetaRequest(
                user_id = userId,
                title = title,
                description = description,
                instructions = instructions,
                preparation_time = preparationTime,
                cooking_time = cookingTime,
                servings = servings,
                difficulty = difficulty,
                category_ids = categoryIds,
                ingredients = ingredientRequests
            )

            val response = createRecetaService.createReceta(request)

            if (!response.isSuccessful) {
                throw Exception("Error al crear receta: ${response.message()}")
            }
        }
    }

    private suspend fun guardarRecetaLocalmente(
        userId: Int,
        title: String,
        description: String,
        instructions: String,
        preparationTime: Int,
        cookingTime: Int,
        servings: Int,
        difficulty: String,
        categoryIds: List<Int>,
        ingredients: List<Ingredient>,
        imagePath: String? = null
    ) {
        // Guardar la receta en la base de datos local
        val ingredientEntities = ingredients.map { ingredient ->
            IngredientEntity(
                ingredientId = ingredient.id,
                quantity = ingredient.quantity,
                unit = ingredient.unit
            )
        }

        val recetaEntity = RecetaEntity(
            userId = userId,
            title = title,
            description = description,
            instructions = instructions,
            preparationTime = preparationTime,
            cookingTime = cookingTime,
            servings = servings,
            difficulty = difficulty,
            image = imagePath,
            categoryIds = categoryIds,
            ingredients = ingredientEntities,
            isSynced = false
        )

        val recetaId = recetaDao.insertReceta(recetaEntity)

        // Crear una operación pendiente para sincronizar cuando haya conexión
        val ingredientJson = Gson().toJson(ingredients.map { ingredient ->
            IngredientRequest(
                ingredient_id = ingredient.id,
                quantity = ingredient.quantity,
                unit = ingredient.unit
            )
        })

        val pendingOperation = PendingRecetaOperationEntity(
            operationType = "CREATE",
            userId = userId,
            title = title,
            description = description,
            instructions = instructions,
            preparationTime = preparationTime,
            cookingTime = cookingTime,
            servings = servings,
            difficulty = difficulty,
            image = imagePath,
            categoryIds = categoryIds,
            ingredients = ingredientJson
        )

        pendingOperationDao.insertPendingOperation(pendingOperation)
    }

    suspend fun getGustos(): List<Gusto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = gustosService.getAllGustos()

                if (response.isSuccessful && response.body() != null) {
                    // Aquí response.body() es directamente una List<GustoResponse>, no tiene una propiedad 'data'
                    response.body()!!.map {
                        Gusto(
                            id = it.id,
                            nombre = it.nombre
                        )
                    }
                } else {
                    throw Exception("Error al obtener gustos: ${response.message()}")
                }
            } catch (e: Exception) {
                // Implementar lógica para obtener gustos en caché si están disponibles
                emptyList()
            }
        }
    }

    suspend fun getIngredients(): List<Ingredient> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitHelper.createRecetaService.getIngredients()

                if (response.isSuccessful && response.body() != null) {
                    val content = response.body()?.content
                    if (content != null) {
                        Log.d(TAG, "Ingredientes obtenidos: ${content.size}")
                        content.map { it.toIngredient() }
                    } else {
                        Log.e(TAG, "Content es nulo en la respuesta de ingredientes")
                        emptyList()
                    }
                } else {
                    Log.e(TAG, "Error al obtener ingredientes: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al obtener ingredientes: ${e.message}")
                e.printStackTrace()
                // Si hay un error, intentar obtener ingredientes localmente si están disponibles
                emptyList()
            }
        }
    }

    suspend fun getCategories(): List<Category> {
        return withContext(Dispatchers.IO) {
            try {
                val response = createRecetaService.getCategories()

                if (response.isSuccessful && response.body() != null) {
                    // Si la respuesta es directamente una lista de categorías (sin wrapper)
                    val categoriesDto = response.body()!!
                    Log.d("CreateRecetaRepository", "Categorías obtenidas: ${categoriesDto.size}")
                    categoriesDto.map { it.toCategory() }
                } else {
                    Log.e("CreateRecetaRepository", "Error en la respuesta: ${response.code()} ${response.message()}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("CreateRecetaRepository", "Excepción al obtener categorías", e)
                emptyList()
            }
        }
    }

    // Métodos para manejar la sincronización de datos

    fun getRecetasByUserId(userId: Int): Flow<List<RecetaEntity>> {
        return recetaDao.getRecetasByUserId(userId)
    }

    suspend fun sincronizarRecetasPendientes() {
        withContext(Dispatchers.IO) {
            val pendingOperations = pendingOperationDao.getAllPendingOperations()

            for (operation in pendingOperations) {
                try {
                    // Marcar la operación como en progreso
                    pendingOperationDao.updateOperationStatus(operation.id, "IN_PROGRESS")

                    when (operation.operationType) {
                        "CREATE" -> {
                            // Convertir de JSON a lista de IngredientRequest
                            val ingredientRequestsType = object : com.google.gson.reflect.TypeToken<List<IngredientRequest>>() {}.type
                            val ingredientRequests: List<IngredientRequest> = Gson().fromJson(operation.ingredients, ingredientRequestsType)

                            val request = CreateRecetaRequest(
                                user_id = operation.userId,
                                title = operation.title,
                                description = operation.description,
                                instructions = operation.instructions,
                                preparation_time = operation.preparationTime,
                                cooking_time = operation.cookingTime,
                                servings = operation.servings,
                                difficulty = operation.difficulty,
                                category_ids = operation.categoryIds,
                                ingredients = ingredientRequests
                            )

                            val response = createRecetaService.createReceta(request)

                            if (response.isSuccessful) {
                                // Eliminar la operación pendiente
                                pendingOperationDao.deletePendingOperation(operation.id)

                                // Buscar y marcar la receta como sincronizada
                                // Esto es una simplificación, necesitarías encontrar la receta correspondiente
                                // en una implementación real
                                val recetas = recetaDao.getUnsyncedRecetas()
                                val recetaToSync = recetas.find {
                                    it.title == operation.title &&
                                            it.userId == operation.userId &&
                                            it.description == operation.description
                                }

                                recetaToSync?.let {
                                    recetaDao.markRecetaAsSynced(it.id)
                                }
                            } else {
                                // Si falló la sincronización, marcar como pendiente nuevamente
                                pendingOperationDao.updateOperationStatus(operation.id, "PENDING")
                            }
                        }
                        // Implementar otros tipos de operaciones (UPDATE, DELETE) si es necesario
                    }
                } catch (e: Exception) {
                    // Si hay un error, incrementar el contador de intentos y marcar como pendiente
                    pendingOperationDao.updateOperationStatus(operation.id, "PENDING")
                }
            }
        }
    }

    suspend fun contarOperacionesPendientes(): Int {
        return pendingOperationDao.getPendingOperationsCount()
    }
}