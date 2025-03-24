package com.example.recetas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.recetas.core.service.worker.NetworkMonitorService
import com.example.recetas.core.navigation.NavigationWrapper
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("Permisos", " Permiso de notificaciones concedido")
        } else {
            Log.d("Permisos", " Permiso de notificaciones denegado")
        }
    }

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavigationWrapper()
        }

        // Iniciar el servicio en segundo plano para monitorear la conexión
        val serviceIntent = Intent(this, NetworkMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Configurar Firebase Messaging para recibir notificaciones
        setupFirebaseMessaging()

        // Solicitar permiso de notificaciones
        checkAndRequestNotificationPermission()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
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