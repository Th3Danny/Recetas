package com.example.recetas.gustos.domain


import android.util.Log
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.gustos.data.repository.GustosRepository
import com.example.recetas.register.data.model.Ingredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


class GetIngredientsUseCase(private val repository: GustosRepository) {

    companion object {
        private const val TAG = "GetIngredientsUseCase"
        private const val TIMEOUT_MS = 15000L // 15 segundos de timeout
    }

    suspend operator fun invoke(): Result<List<Ingredient>> {
        return try {
            withContext(Dispatchers.IO) {
                // Usar withTimeoutOrNull para evitar bloqueos indefinidos
                val ingredients = withTimeoutOrNull(TIMEOUT_MS) {
                    repository.getAllGustos()
                } ?: emptyList()

                Result.success(ingredients)
            }
        } catch (e: CancellationException) {
            // Propagar las excepciones de cancelación para manejarlas en el ViewModel
            Log.w(TAG, "La operación fue cancelada", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener ingredientes", e)
            Result.failure(e)
        }
    }
}

    class AddGustoToUserUseCase @Inject constructor(
        private val gustosRepository: GustosRepository
    ) {
        suspend operator fun invoke(gustoId: Int): Boolean {
            return gustosRepository.addGustoToUser(gustoId)
        }
    }

class GetUserGustosUseCase(private val repository: GustosRepository) {

    companion object {
        private const val TAG = "GetUserGustosUseCase"
        private const val TIMEOUT_MS = 10000L // 10 segundos de timeout
    }

    suspend operator fun invoke(): Result<List<Gusto>> {
        return try {
            withContext(Dispatchers.IO) {
                // Usar withTimeoutOrNull para evitar bloqueos indefinidos
                val gustos = withTimeoutOrNull(TIMEOUT_MS) {
                    repository.getUserGustos()
                } ?: emptyList()

                Result.success(gustos)
            }
        } catch (e: CancellationException) {
            // Propagar las excepciones de cancelación para manejarlas en el ViewModel
            Log.w(TAG, "La operación fue cancelada", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener gustos del usuario", e)
            Result.failure(e)
        }
    }
}

