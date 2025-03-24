package com.example.recetas.core.service.notification

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class PushNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token de FCM: $token")

        //  Guardamos el nuevo token de FCM
        saveFCMToken(token)

        //  Enviamos el token al backend si hay usuario autenticado
        FirebaseHelper.sendTokenToServer(this, token)

        //  Suscribirse al topic "global"
        subscribeToGlobalTopic()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        //  Verificar si la notificación contiene datos
        remoteMessage.notification?.let {
            showNotification(it.title ?: "Nueva Notificación", it.body ?: "Mensaje recibido")
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "firebase_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // Crear canal de notificación en Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones Firebase",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para recibir notificaciones push de Firebase"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        //  Construcción de la notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        // 🔹 Verificar permisos en Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("FCM", " Permiso de notificaciones no concedido, no se mostrará la notificación.")
            return
        }

        //  Mostrar la notificación
        notificationManager.notify(notificationId, notification)
    }

    private fun saveFCMToken(token: String) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("fcmToken", token)
            apply()
        }
    }

    //  Método para suscribirse al topic "global"
    private fun subscribeToGlobalTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("global")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Suscripción exitosa al topic 'global'")
                } else {
                    Log.e("FCM", "Error al suscribirse al topic", task.exception)
                }
            }
    }
}