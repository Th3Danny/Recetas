package com.example.recetas.gustos.data.repository


import com.example.recetas.core.sesion.SessionManagerImpl
import com.example.recetas.gustos.data.datasource.GustosService
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.gustos.data.model.GustoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GustosRepository(
    private val gustosService: GustosService,
    private val sessionManager: SessionManagerImpl
) {

    suspend fun fetchAllGustos(): List<Gusto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = gustosService.getAllGustos()

                if (response.isSuccessful) {
                    response.body()?.map { it.toDomainModel() } ?: emptyList()
                } else {
                    emptyList()
                }

            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getUserGustos(): List<Gusto> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = sessionManager.getUserId() ?: throw IllegalStateException("Usuario no autenticado")
                val response = gustosService.getUserGustos(userId)

                if (response.isSuccessful) {
                    response.body()?.map { it.toDomainModel() } ?: emptyList()
                } else {
                    emptyList()
                }

            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun addGustoToUser(gustoId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val userId = sessionManager.getUserId() ?: throw IllegalStateException("Usuario no autenticado")
                val response = gustosService.addGustoToUser(userId, gustoId)
                response.isSuccessful

            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun removeGustoFromUser(gustoId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val userId = sessionManager.getUserId() ?: throw IllegalStateException("Usuario no autenticado")
                val response = gustosService.removeGustoFromUser(userId, gustoId)
                response.isSuccessful

            } catch (e: Exception) {
                false
            }
        }
    }

    private fun GustoResponse.toDomainModel(): Gusto {
        return Gusto(
            id = id,
            nombre = nombre
        )
    }
}

// Clase para manejar la sesión del usuario
interface SessionManager {
    fun getUserId(): Int?
}