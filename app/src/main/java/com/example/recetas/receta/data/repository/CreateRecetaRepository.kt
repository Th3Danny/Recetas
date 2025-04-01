package com.example.recetas.receta.data.repository

import android.content.Context
import android.util.Log
import com.example.recetas.core.data.local.AppDataBase
import com.example.recetas.core.data.local.entities.IngredientEntity
import com.example.recetas.core.data.local.entities.PendingRecetaOperationEntity
import com.example.recetas.core.data.local.entities.RecetaEntity
import com.example.recetas.core.network.RetrofitHelper
import com.example.recetas.receta.data.model.Category
import com.example.recetas.receta.data.model.CreateRecetaRequest
import com.example.recetas.register.data.model.Ingredient
import com.example.recetas.receta.data.model.IngredientRequest
import com.example.recetas.receta.data.model.toCategory
import com.example.recetas.register.data.model.toIngredient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CreateRecetaRepository(private val context: Context) {
    companion object {
        internal const val TAG = "CreateRecetaRepository"
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
            Log.d(TAG, " Iniciando proceso de creación de receta: $title")

            // Obtener el ID del usuario de las preferencias compartidas
            val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("userId", -1)

            if (userId == -1) {
                Log.e(TAG, " Usuario no autenticado")
                throw Exception("Usuario no autenticado")
            }

            try {
                // Intentar crear la receta en el servidor
                Log.d(TAG, " Intentando crear receta en el servidor...")
                crearRecetaEnServidor(
                    userId, title, description, instructions, preparationTime,
                    cookingTime, servings, difficulty, categoryIds, ingredients, imagePath
                )
                Log.d(TAG, " Receta creada exitosamente en el servidor")
            } catch (e: Exception) {
                // Si hay un error de red, guardar la operación pendiente y la receta localmente
                Log.w(TAG, "️ Error al crear receta en servidor: ${e.message}")
                Log.d(TAG, " Guardando receta localmente...")
                guardarRecetaLocalmente(
                    userId, title, description, instructions, preparationTime,
                    cookingTime, servings, difficulty, categoryIds, ingredients, imagePath
                )
                Log.d(TAG, " Receta guardada localmente con éxito")
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
        val ingredientRequests = ingredients.map {
            IngredientRequest(
                ingredient_id = it.id,
                quantity = it.quantity,
                unit = it.unit
            )
        }

        val recetaRequest = CreateRecetaRequest(
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

        val gson = Gson()
        val recetaJson = gson.toJson(recetaRequest)

        //  Log del JSON que se enviará
        Log.d(TAG, " JSON enviado en recipeDTO:")
        Log.d(TAG, recetaJson)

        //  Imprimir bien formateado el JSON enviado
        val prettyJson = gson.toJson(
            gson.fromJson(recetaJson, Any::class.java)
        )
        Log.d(TAG, "JSON enviado en recipeDTO:\n$prettyJson")

        val recetaBody = recetaJson.toRequestBody("text/plain".toMediaTypeOrNull())


        // Crear la parte de imagen si se envía
        val imagePart: MultipartBody.Part? = if (!imagePath.isNullOrEmpty()) {
            val file = File(imagePath)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        } else {
            // Mandamos un campo vacío como "image" si no se seleccionó imagen
            MultipartBody.Part.createFormData("image", "", "".toRequestBody("text/plain".toMediaTypeOrNull()))
        }


        Log.d(TAG, " Enviando datos al backend...")
        Log.d(TAG, " Imagen incluida: ${imagePart != null}, Path: $imagePath")

        val response = createRecetaService.createReceta(
            recipeDto = recetaBody,
            image = imagePart
        )

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            Log.e(TAG, " Error al crear receta: ${response.code()} - ${response.message()}")
            Log.e(TAG, " Error body: $errorBody")
            throw Exception("Error al crear receta: ${response.message()}")
        } else {
            Log.d(TAG, " Receta creada con éxito. ID: ${response.body()?.data?.id}")
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
        Log.d(TAG, " Iniciando guardado local de receta")

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
            Log.d(TAG, " Guardando receta en la base de datos local...")
            val recetaId = recetaDao.insertReceta(recetaEntity)
            Log.d(TAG, " Receta guardada en la base de datos con ID: $recetaId")

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

            Log.d(TAG, " Creando operación pendiente para sincronización futura...")
            val operationId = pendingOperationDao.insertPendingOperation(pendingOperation)
            Log.d(TAG, " Operación pendiente creada con ID: $operationId")

            // Contar operaciones pendientes para verificación
            val pendingCount = pendingOperationDao.getPendingOperationsCount()
            Log.d(TAG, " Total de operaciones pendientes: $pendingCount")
        } catch (e: Exception) {
            Log.e(TAG, " Error al guardar localmente: ${e.message}")
            e.printStackTrace()
            throw e  // Re-lanzar para que pueda ser manejada arriba
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
            Log.d(TAG, " Iniciando sincronización de recetas pendientes")
            val pendingOperations = pendingOperationDao.getAllPendingOperations()
            Log.d(TAG, " Operaciones pendientes encontradas: ${pendingOperations.size}")

            for (operation in pendingOperations) {
                try {
                    // Marcar operación como IN_PROGRESS
                    Log.d(TAG, "⏳ Procesando operación ID: ${operation.id}, tipo: ${operation.operationType}")
                    pendingOperationDao.updateOperationStatus(operation.id, "IN_PROGRESS")

                    if (operation.operationType == "CREATE") {
                        Log.d(TAG, "🆕 Sincronizando creación de receta: ${operation.title}")

                        // Convertir ingredientes desde JSON
                        val ingredientRequestsType = object : com.google.gson.reflect.TypeToken<List<IngredientRequest>>() {}.type
                        val ingredientRequests: List<IngredientRequest> =
                            Gson().fromJson(operation.ingredients, ingredientRequestsType)

                        // Crear objeto de receta
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

                        // Construir JSON como RequestBody con contentType application/json
                        val gson = Gson()
                        val recetaJson = gson.toJson(request)
                        val recetaBody = recetaJson.toRequestBody("application/json".toMediaTypeOrNull())

                        // Adjuntar imagen si existe
                        val imagePart = operation.image?.let { path ->
                            val file = File(path)
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("image", file.name, requestFile)
                        }

                        // Llamada a la API
                        val response = createRecetaService.createReceta(
                            recipeDto = recetaBody,
                            image = imagePart
                        )

                        if (response.isSuccessful) {
                            Log.d(TAG, " Receta sincronizada correctamente. ID: ${response.body()?.data?.id}")
                            pendingOperationDao.deletePendingOperation(operation.id)

                            // Marcar como sincronizada en local
                            val recetas = recetaDao.getUnsyncedRecetas()
                            val recetaToSync = recetas.find {
                                it.title == operation.title &&
                                        it.userId == operation.userId &&
                                        it.description == operation.description
                            }

                            recetaToSync?.let {
                                recetaDao.markRecetaAsSynced(it.id)
                                Log.d(TAG, " Receta marcada como sincronizada localmente: ${it.id}")
                            } ?: Log.w(TAG, " No se encontró la receta para marcar como sincronizada")
                        } else {
                            Log.e(TAG, " Error al sincronizar receta: ${response.code()} - ${response.message()}")
                            pendingOperationDao.updateOperationStatus(operation.id, "PENDING")
                        }
                    } else {
                        Log.w(TAG, " Tipo de operación no soportada: ${operation.operationType}")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, " Error al sincronizar operación ${operation.id}: ${e.message}")
                    e.printStackTrace()
                    pendingOperationDao.updateOperationStatus(operation.id, "PENDING")
                }
            }

            val remainingCount = pendingOperationDao.getPendingOperationsCount()
            Log.d(TAG, " Sincronización finalizada. Pendientes restantes: $remainingCount")
        }
    }


    suspend fun contarOperacionesPendientes(): Int {
        val count = pendingOperationDao.getPendingOperationsCount()
        Log.d(TAG, " Total de operaciones pendientes: $count")
        return count
    }
}