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
            Log.d(TAG, "⬇️ Iniciando proceso de creación de receta: $title")

            // Obtener el ID del usuario de las preferencias compartidas
            val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("userId", -1)

            if (userId == -1) {
                Log.e(TAG, "❌ Usuario no autenticado")
                throw Exception("Usuario no autenticado")
            }

            try {
                // Intentar crear la receta en el servidor
                Log.d(TAG, "⬆️ Intentando crear receta en el servidor...")
                crearRecetaEnServidor(
                    userId, title, description, instructions, preparationTime,
                    cookingTime, servings, difficulty, categoryIds, ingredients, imagePath
                )
                Log.d(TAG, "✅ Receta creada exitosamente en el servidor")
            } catch (e: Exception) {
                // Si hay un error de red, guardar la operación pendiente y la receta localmente
                Log.w(TAG, "⚠️ Error al crear receta en servidor: ${e.message}")
                Log.d(TAG, "💾 Guardando receta localmente...")
                guardarRecetaLocalmente(
                    userId, title, description, instructions, preparationTime,
                    cookingTime, servings, difficulty, categoryIds, ingredients, imagePath
                )
                Log.d(TAG, "✅ Receta guardada localmente con éxito")
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

        Log.d(TAG, "📋 Preparando solicitud para el servidor:")
        Log.d(TAG, "  - Usuario: $userId")
        Log.d(TAG, "  - Título: $title")
        Log.d(TAG, "  - Categorías: $categoryIds")
        Log.d(TAG, "  - Ingredientes: ${ingredientRequests.size}")

        if (imagePath != null) {
            Log.d(TAG, "🖼️ Imagen incluida: $imagePath")
            // Aquí iría el código para subir la receta con imagen
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

            Log.d(TAG, "⬆️ Enviando solicitud al servidor...")
            val response = createRecetaService.createReceta(request)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Respuesta exitosa del servidor: ${response.code()}")
            } else {
                Log.e(TAG, "❌ Error del servidor: ${response.code()} - ${response.message()}")
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
        Log.d(TAG, "💾 Iniciando guardado local de receta")

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

        try {
            Log.d(TAG, "📝 Guardando receta en la base de datos local...")
            val recetaId = recetaDao.insertReceta(recetaEntity)
            Log.d(TAG, "✅ Receta guardada en la base de datos con ID: $recetaId")

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

            Log.d(TAG, "📝 Creando operación pendiente para sincronización futura...")
            val operationId = pendingOperationDao.insertPendingOperation(pendingOperation)
            Log.d(TAG, "✅ Operación pendiente creada con ID: $operationId")

            // Contar operaciones pendientes para verificación
            val pendingCount = pendingOperationDao.getPendingOperationsCount()
            Log.d(TAG, "🔄 Total de operaciones pendientes: $pendingCount")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar localmente: ${e.message}")
            e.printStackTrace()
            throw e  // Re-lanzar para que pueda ser manejada arriba
        }
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
            Log.d(TAG, "🔄 Iniciando sincronización de recetas pendientes")
            val pendingOperations = pendingOperationDao.getAllPendingOperations()
            Log.d(TAG, "📋 Operaciones pendientes encontradas: ${pendingOperations.size}")

            for (operation in pendingOperations) {
                try {
                    // Marcar la operación como en progreso
                    Log.d(TAG, "⏳ Procesando operación ID: ${operation.id}, tipo: ${operation.operationType}")
                    pendingOperationDao.updateOperationStatus(operation.id, "IN_PROGRESS")

                    when (operation.operationType) {
                        "CREATE" -> {
                            Log.d(TAG, "🆕 Sincronizando creación de receta: ${operation.title}")

                            // Convertir de JSON a lista de IngredientRequest
                            val ingredientRequestsType = object : com.google.gson.reflect.TypeToken<List<IngredientRequest>>() {}.type
                            val ingredientRequests: List<IngredientRequest> = Gson().fromJson(operation.ingredients, ingredientRequestsType)
                            Log.d(TAG, "📋 Ingredientes recuperados: ${ingredientRequests.size}")

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

                            Log.d(TAG, "⬆️ Enviando receta pendiente al servidor...")
                            val response = createRecetaService.createReceta(request)

                            if (response.isSuccessful) {
                                Log.d(TAG, "✅ Sincronización exitosa para receta: ${operation.id}")

                                // Eliminar la operación pendiente
                                pendingOperationDao.deletePendingOperation(operation.id)
                                Log.d(TAG, "🗑️ Operación pendiente eliminada: ${operation.id}")

                                // Buscar y marcar la receta como sincronizada
                                val recetas = recetaDao.getUnsyncedRecetas()
                                Log.d(TAG, "🔍 Buscando receta para marcar como sincronizada entre ${recetas.size} recetas no sincronizadas")

                                val recetaToSync = recetas.find {
                                    it.title == operation.title &&
                                            it.userId == operation.userId &&
                                            it.description == operation.description
                                }

                                recetaToSync?.let {
                                    Log.d(TAG, "✓ Receta encontrada para marcar como sincronizada: ${it.id}")
                                    recetaDao.markRecetaAsSynced(it.id)
                                    Log.d(TAG, "✅ Receta marcada como sincronizada: ${it.id}")
                                } ?: Log.w(TAG, "⚠️ No se encontró la receta correspondiente para marcar como sincronizada")

                            } else {
                                // Si falló la sincronización, marcar como pendiente nuevamente
                                Log.e(TAG, "❌ Error al sincronizar: ${response.code()} - ${response.message()}")
                                pendingOperationDao.updateOperationStatus(operation.id, "PENDING")
                                Log.d(TAG, "🔄 Operación marcada como pendiente nuevamente: ${operation.id}")
                            }
                        }
                        // Implementar otros tipos de operaciones (UPDATE, DELETE) si es necesario
                        else -> {
                            Log.w(TAG, "⚠️ Tipo de operación no soportada: ${operation.operationType}")
                        }
                    }
                } catch (e: Exception) {
                    // Si hay un error, incrementar el contador de intentos y marcar como pendiente
                    Log.e(TAG, "❌ Error al sincronizar operación ${operation.id}: ${e.message}")
                    e.printStackTrace()
                    pendingOperationDao.updateOperationStatus(operation.id, "PENDING")
                    Log.d(TAG, "🔄 Operación marcada como pendiente después de error: ${operation.id}")
                }
            }

            // Verificar conteo final
            val remainingCount = pendingOperationDao.getPendingOperationsCount()
            Log.d(TAG, "🔄 Sincronización completada. Operaciones pendientes restantes: $remainingCount")
        }
    }

    suspend fun contarOperacionesPendientes(): Int {
        val count = pendingOperationDao.getPendingOperationsCount()
        Log.d(TAG, "🔢 Total de operaciones pendientes: $count")
        return count
    }
}