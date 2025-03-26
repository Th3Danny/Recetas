package com.example.recetas.gustos.data.repository


import android.content.ContentValues.TAG
import android.util.Log
import com.example.recetas.core.network.RetrofitHelper
import com.example.recetas.core.sesion.SessionManagerImpl
import com.example.recetas.gustos.data.datasource.GustosService
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.gustos.data.model.PreferenceRequest
import com.example.recetas.gustos.data.model.toGusto
import com.example.recetas.register.data.model.Ingredient
import com.example.recetas.register.data.model.toIngredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import kotlin.collections.map
import kotlin.coroutines.cancellation.CancellationException

class GustosRepository(
    private val gustosService: GustosService,
    private val sessionManager: SessionManagerImpl
) {

    suspend fun getAllGustos(): List<Ingredient> {
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
            } catch (e: CancellationException) {
                // No atrapar CancellationException, dejar que se propague
                throw e
            } catch (e: HttpException) {
                Log.e(TAG, "Error HTTP al obtener ingredientes: ${e.code()}")
                emptyList()
            } catch (e: IOException) {
                Log.e(TAG, "Error de red al obtener ingredientes: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al obtener ingredientes: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun postIngredient(name: String, imagePath: String?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val namePart = RequestBody.create("text/plain".toMediaTypeOrNull(), name)

                val imagePart = imagePath?.let {
                    val file = File(it)
                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                }

                val response = gustosService.postIngredient(namePart, imagePart)
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("GustosRepository", "Error al subir ingrediente: ${e.message}")
                false
            }
        }
    }


    suspend fun addGustoToUser(gustoId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Obtener el ID del usuario
                val userId = sessionManager.getUserId()
                if (userId == null) {
                    Log.e(TAG, "addGustoToUser: Usuario no autenticado, userId es null")
                    throw IllegalStateException("Usuario no autenticado")
                }

                Log.d(TAG, "addGustoToUser: Intentando añadir gusto $gustoId para usuario $userId")

                // Crear el objeto de solicitud
                val requestBody = PreferenceRequest(
                    preference_id = gustoId,
                    user_id = userId
                )

                Log.d(TAG, "addGustoToUser: Enviando request con body: $requestBody")

                // Enviar la solicitud
                val response = gustosService.addGustoToUser(requestBody)

                if (response.isSuccessful) {
                    Log.d(TAG, "addGustoToUser: Gusto añadido exitosamente")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "addGustoToUser: Error al añadir gusto: ${response.code()} - ${response.message()}")
                    Log.e(TAG, "addGustoToUser: Error body: $errorBody")
                }

                response.isSuccessful
            } catch (e: Exception) {
                Log.e(TAG, "addGustoToUser: Excepción al añadir gusto: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

    // Obtener los gustos del usuario
    suspend fun getUserGustos(): List<Gusto> {
        return withContext(Dispatchers.IO) {
            try {


                val userId = sessionManager.getUserId()
                if (userId == null) {
                    Log.e(TAG, "getUserGustos: Usuario no autenticado, userId es null")
                    return@withContext emptyList<Gusto>()
                }

                Log.d(TAG, "getUserGustos: Obteniendo gustos para el usuario $userId")

                val response = gustosService.getUserGustos(userId)

                if (response.isSuccessful && response.body() != null) {
                    val gustosResponse = response.body()!!
                    Log.d(TAG, "getUserGustos: Se obtuvieron ${gustosResponse.size} gustos")

                    // Convertir de GustoResponse a Gusto
                    gustosResponse.map { it.toGusto() }
                } else {
                    Log.e(TAG, "getUserGustos: Error al obtener gustos: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            } catch (e: CancellationException) {
                // No atrapar CancellationException, dejar que se propague
                throw e
            } catch (e: HttpException) {
                Log.e(TAG, "getUserGustos: Error HTTP al obtener gustos: ${e.code()}")
                emptyList()
            } catch (e: IOException) {
                Log.e(TAG, "getUserGustos: Error de red al obtener gustos: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "getUserGustos: Excepción al obtener gustos: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }



}

