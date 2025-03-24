package com.example.recetas.core.service.notification

import android.content.Context
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object FirebaseHelper {
    private const val TAG = "FCM"

    // Manejo de errores mejorado para el envío de tokens FCM
    fun sendTokenToServer(context: Context, token: String) {
        try {
            Log.d(TAG, "📡 Intentando enviar token FCM al backend: $token")

            val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val authToken = sharedPreferences.getString("authToken", "")

            if (authToken.isNullOrEmpty()) {
                Log.e(TAG, "No hay token de autenticación disponible")
                return
            }

            // Crear el JSON para el cuerpo de la solicitud
            val jsonBody = JSONObject().apply {
                put("fcmToken", token)
            }

            val client = OkHttpClient()

            // Asumiendo que la URL es correcta - verifica esto con tu backend
            val url = "http://0.tcp.ngrok.io:14047/api/notifications/register-token"

            // Crear el cuerpo de la solicitud con MediaType para OkHttp 3.x
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val requestBody = RequestBody.create(JSON, jsonBody.toString())

            // Crear la solicitud con el token de autenticación en el encabezado
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $authToken")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            // Ejecutar la solicitud de forma asíncrona
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Error al enviar token FCM: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            Log.d(TAG, "Token FCM enviado exitosamente al servidor")
                        } else {
                            Log.e(TAG, "Fallo al enviar token: Código HTTP ${response.code()} - ${response.message()}")

                            // Manejo seguro del cuerpo de la respuesta
                            val responseBody = response.body()?.string()
                            if (!responseBody.isNullOrEmpty()) {
                                Log.e(TAG, "Respuesta del servidor: $responseBody")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al procesar la respuesta: ${e.message}")
                    } finally {
                        // Cerrar el cuerpo de la respuesta para evitar fugas de memoria
                        response.body()?.close()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al enviar token FCM: ${e.message}")
        }
    }
}