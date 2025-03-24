package com.example.recetas.register.data.repository

import android.util.Log
import com.example.recetas.core.network.RetrofitHelper
import com.example.recetas.register.data.model.Ingredient
import com.example.recetas.register.data.model.toIngredient
import com.example.recetas.register.data.model.UserRegister
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterRepository {

    companion object {
        internal const val TAG = "RegisterRepository"
    }

    // Obtén los servicios desde RetrofitHelper
    private val registerService = RetrofitHelper.registerService

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

    // Función suspendida para obtener los ingredientes desde el servicio remoto
    suspend fun getIngredients(): List<Ingredient> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitHelper.registerService.ingredients()

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

}