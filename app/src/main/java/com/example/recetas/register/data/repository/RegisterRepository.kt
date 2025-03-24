package com.example.recetas.register.data.repository

import com.example.recetas.core.network.RetrofitHelper
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.gustos.data.model.GustoResponse
import com.example.recetas.register.data.model.UserRegister
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterRepository {

    // Obtén los servicios desde RetrofitHelper
    private val registerService = RetrofitHelper.registerService
    private val gustosService = RetrofitHelper.gustosService

    suspend fun registerUser(userRegister: UserRegister): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Llama al servicio de registro
                val response = registerService.registerUser(userRegister)
                response.isSuccessful && response.body()?.success == true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun fetchGustos(): List<Gusto> {
        return withContext(Dispatchers.IO) {
            try {
                // Llama al servicio para obtener los gustos
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

    // Convierte GustoResponse a Gusto (modelo de dominio)
    private fun GustoResponse.toDomainModel(): Gusto {
        return Gusto(
            id = id,
            nombre = nombre
        )
    }
}