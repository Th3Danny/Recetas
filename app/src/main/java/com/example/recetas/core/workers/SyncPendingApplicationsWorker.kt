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

class SyncRopaOperationsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val TAG = "SyncRecetasWorker"

    // Utilizamos AppDataBase en lugar de AppDatabase
    private val database = AppDataBase.getDatabase(context)
    private val pendingRecetaOperationDao = database.pendingRecetaOperationDao()
    private val recetaDao = database.recetaDao()

    // Utilizamos el servicio ya configurado en RetrofitHelper
    private val createRecetaService = RetrofitHelper.createRecetaService

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando sincronización de recetas pendientes...")

            val pendingOperations = pendingRecetaOperationDao.getAllPendingOperations()

            if (pendingOperations.isEmpty()) {
                Log.d(TAG, "No hay recetas pendientes para sincronizar")
                return@withContext Result.success()
            }

            Log.d(TAG, "Encontradas ${pendingOperations.size} recetas pendientes")

            var successCount = 0
            var failureCount = 0

            for (operation in pendingOperations) {
                try {
                    // Marcar la operación como en progreso
                    pendingRecetaOperationDao.updateOperationStatus(operation.id, "IN_PROGRESS")

                    when (operation.operationType) {
                        "CREATE" -> {
                            Log.d(TAG, "Sincronizando creación de receta: ${operation.title}")

                            // Convertir JSON de ingredientes a lista de IngredientRequest
                            val ingredientRequestsType = object : TypeToken<List<IngredientRequest>>() {}.type
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
                                Log.d(TAG, "Sincronización exitosa para receta: ${operation.id}")
                                pendingRecetaOperationDao.deletePendingOperation(operation.id)

                                // Buscar y marcar la receta como sincronizada
                                val recetas = recetaDao.getUnsyncedRecetas()
                                val recetaToSync = recetas.find {
                                    it.title == operation.title &&
                                            it.userId == operation.userId &&
                                            it.description == operation.description
                                }

                                recetaToSync?.let {
                                    recetaDao.markRecetaAsSynced(it.id)
                                }

                                successCount++
                            } else {
                                // Si la respuesta no es exitosa, marcar como error
                                val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                                Log.e(TAG, "Error al sincronizar receta: ${response.code()} - $errorMessage")
                                pendingRecetaOperationDao.updateOperationStatus(operation.id, "ERROR")
                                failureCount++
                            }
                        }
                        // Aquí se pueden agregar más tipos de operaciones como UPDATE, DELETE, etc.
                        else -> {
                            Log.w(TAG, "Tipo de operación no soportada: ${operation.operationType}")
                            pendingRecetaOperationDao.updateOperationStatus(operation.id, "ERROR")
                            failureCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Excepción al sincronizar receta ${operation.id}: ${e.message}")
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
                Log.d(TAG, "Sincronización de recetas completada con errores: $successCount éxitos, $failureCount fallos")
                Result.failure(resultData)
            } else {
                Log.d(TAG, "Sincronización de recetas completada con éxito: $successCount operaciones")
                Result.success(resultData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error general en el worker de sincronización de recetas: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}