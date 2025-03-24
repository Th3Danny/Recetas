package com.example.recetas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.recetas.core.service.worker.NetworkMonitorService
import com.example.recetas.core.navigation.NavigationWrapper
import com.example.recetas.ui.theme.MyApplicationTheme
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MyApplicationTheme {
                NavigationWrapper()
            }
        }


        //  Iniciar el servicio en segundo plano para monitorear la conexión
        val serviceIntent = Intent(this, NetworkMonitorService::class.java)
        startForegroundService(serviceIntent)


        //  Configurar Firebase Messaging para recibir notificaciones
        setupFirebaseMessaging()
    }


    private fun setupFirebaseMessaging() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val tokenFCM = task.result
                Log.d("FCM", "📡 Token de FCM obtenido: $tokenFCM")
                saveFCMToken(tokenFCM)
            } else {
                Log.w("FCM", "⚠ Error al obtener token de FCM", task.exception)
            }
        }
    }

    //  Guardar el token en SharedPreferences
    private fun saveFCMToken(token: String) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("fcmToken", token)
            apply()
        }
    }

}