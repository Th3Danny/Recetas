package com.example.recetas.home.data.repository

import android.util.Log
import com.example.recetas.core.network.RetrofitHelper
import com.example.recetas.home.data.model.Receta
import com.example.recetas.home.data.model.toReceta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecetaRepository {

    private val recetaService = RetrofitHelper.recetaService
    private val TAG = "RecetaRepository"

    suspend fun getRecetas(): List<Receta> {
        return withContext(Dispatchers.IO) {
            try {
                val response = recetaService.getRecetas()

                if (response.isSuccessful && response.body() != null) {
                    val content = response.body()?.content
                    if (content != null) {
                        // Verificamos que content no sea nulo antes de mapearlo
                        val recetas = content.mapNotNull { dto ->
                            try {
                                dto.toReceta()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al mapear receta: ${e.message}")
                                null // Saltamos esta receta si hay un error
                            }
                        }
                        recetas
                    } else {
                        Log.e(TAG, "Content es nulo en la respuesta")
                        emptyList()
                    }
                } else {
                    throw Exception("Error al obtener recetas: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al obtener recetas: ${e.message}")
                throw e
            }
        }
    }

//    suspend fun getReceta(recetaId: Int): Receta {
//        return withContext(Dispatchers.IO) {
//            val response = recetaService.getReceta(recetaId)
//
//            if (response.isSuccessful && response.body() != null) {
//                response.body()!!.data.toReceta()
//            } else {
//                throw Exception("Error al obtener receta: ${response.message()}")
//            }
//        }
//    }
}