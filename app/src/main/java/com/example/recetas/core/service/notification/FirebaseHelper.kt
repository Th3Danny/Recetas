package com.example.recetas.core.service.notification

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

object FirebaseHelper {
    private const val TAG = "FCM"

    // Manejo de errores mejorado para el envío de tokens FCM
    fun sendTokenToServer(context: Context, token: String) {
        try {
            Log.d(TAG, "Intentando enviar token FCM al backend: $token")

            val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val authToken = sharedPreferences.getString("authToken", "")

            if (authToken.isNullOrEmpty()) {
                Log.e(TAG, "No hay token de autenticación disponible")
                return
            }

            // Cambiar la estructura del JSON
            val jsonBody = JSONObject().apply {
                put("token", token) // Cambiar de "fcmToken" a "token"
            }

            val client = OkHttpClient()

            // Verificar la URL del backend
            val url = "http://4.tcp.ngrok.io:15583/api/token"

            val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = RequestBody.create(JSON, jsonBody.toString())

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $authToken")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Error al enviar token FCM: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        Log.d(TAG, "Código de respuesta: ${response.code}")
                        Log.d(TAG, "Cuerpo de respuesta: $responseBody")

                        if (response.isSuccessful) {
                            Log.d(TAG, "Token FCM enviado exitosamente al servidor")
                        } else {
                            Log.e(TAG, "Fallo al enviar token: Código HTTP ${response.code} - ${response.message}")
                            Log.e(TAG, "Detalles de la respuesta: $responseBody")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al procesar la respuesta: ${e.message}")
                    } finally {
                        response.body?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al enviar token FCM: ${e.message}")
        }
    }

}