package com.example.recetas.core.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.recetas.core.data.local.AppDataBase
import com.example.recetas.core.network.RetrofitHelper
import com.example.recetas.receta.data.model.CreateRecetaRequest
import com.example.recetas.receta.data.model.IngredientRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class SyncRecetaOperationsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val TAG = "SyncRecetasWorker"

    private val database = AppDataBase.getDatabase(context)
    private val pendingRecetaOperationDao = database.pendingRecetaOperationDao()
    private val recetaDao = database.recetaDao()
    private val createRecetaService = RetrofitHelper.createRecetaService

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando sincronización de recetas pendientes...")

            val pendingOperations = pendingRecetaOperationDao.getAllPendingOperations()
            if (pendingOperations.isEmpty()) {
                Log.d(TAG, "No hay recetas pendientes para sincronizar")
                return@withContext Result.success()
            }

            var successCount = 0
            var failureCount = 0

            for (operation in pendingOperations) {
                try {
                    pendingRecetaOperationDao.updateOperationStatus(operation.id, "IN_PROGRESS")

                    when (operation.operationType) {
                        "CREATE" -> {
                            Log.d(TAG, "🆕 Sincronizando creación de receta: ${operation.title}")

                            val ingredientRequestsType = object : TypeToken<List<IngredientRequest>>() {}.type
                            val ingredientRequests: List<IngredientRequest> =
                                Gson().fromJson(operation.ingredients, ingredientRequestsType)

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

                            // Convertir a RequestBody
                            val gson = Gson()
                            val recetaJson = gson.toJson(request)
                            val recetaBody = recetaJson.toRequestBody("application/json".toMediaTypeOrNull())

                            // Imagen (si existe)
                            val imagePart = operation.image?.let { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                                } else {
                                    null
                                }
                            }

                            // Enviar al servidor
                            val response = createRecetaService.createReceta(
                                recipeDto = recetaBody,
                                image = imagePart
                            )

                            if (response.isSuccessful) {
                                Log.d(TAG, "✅ Receta sincronizada correctamente")

                                // Eliminar operación pendiente
                                pendingRecetaOperationDao.deletePendingOperation(operation.id)

                                // Marcar receta local como sincronizada
                                val recetas = recetaDao.getUnsyncedRecetas()
                                val recetaToSync = recetas.find {
                                    it.title == operation.title &&
                                            it.description == operation.description &&
                                            it.userId == operation.userId
                                }

                                recetaToSync?.let {
                                    recetaDao.markRecetaAsSynced(it.id)
                                }

                                successCount++
                            } else {
                                Log.e(TAG, "❌ Error al sincronizar receta: ${response.code()} - ${response.message()}")
                                pendingRecetaOperationDao.updateOperationStatus(operation.id, "ERROR")
                                failureCount++
                            }
                        }

                        else -> {
                            Log.w(TAG, "⚠ Tipo de operación no soportada: ${operation.operationType}")
                            pendingRecetaOperationDao.updateOperationStatus(operation.id, "ERROR")
                            failureCount++
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "💥 Error al sincronizar operación ${operation.id}: ${e.message}")
                    e.printStackTrace()
                    pendingRecetaOperationDao.updateOperationStatus(operation.id, "ERROR")
                    failureCount++
                }
            }

            val resultData = workDataOf(
                "success_count" to successCount,
                "failure_count" to failureCount
            )

            if (failureCount > 0) {
                Log.d(TAG, "✔️ Finalizada con errores: $successCount exitosas, $failureCount fallidas")
                Result.failure(resultData)
            } else {
                Log.d(TAG, "🎉 Todas las recetas sincronizadas con éxito")
                Result.success(resultData)
            }

        } catch (e: Exception) {
            Log.e(TAG, "💣 Error general en Worker: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}
