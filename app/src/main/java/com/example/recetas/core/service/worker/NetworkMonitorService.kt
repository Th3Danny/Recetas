package com.example.recetas.core.service.worker

import android.Manifest
import android.app.*
import android.content.Intent
import android.net.*
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.recetas.R
import com.example.recetas.core.workers.SyncRopaOperationsWorker


class NetworkMonitorService : Service() {

    companion object {
        private const val CHANNEL_ID = "NetworkMonitorServiceChannel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_STOP_SERVICE = "com.example.recetas.STOP_SERVICE"
        private const val TAG = "NetworkMonitorService"
    }

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        startForeground(NOTIFICATION_ID, getNotification("Monitoreando conexión..."))

        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
            override fun onAvailable(network: Network) {
                if (isInternetAvailable()) {
                    Log.d(TAG, "Internet detectado, ejecutando sincronización inmediata...")

                    val workRequest = OneTimeWorkRequestBuilder<SyncRopaOperationsWorker>().build()
                    WorkManager.getInstance(applicationContext).enqueue(workRequest)

                    // Actualizar notificación
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, getNotification("Conectado: Sincronizando datos..."))
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Se perdió la conexión a Internet.")

                // Actualizar notificación
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, getNotification("Desconectado: Esperando conexión..."))
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        Log.d(TAG, "Servicio detenido.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Monitoreo de Conexión",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitorea la conexión a internet para sincronizar datos pendientes"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun getNotification(content: String): Notification {
        val stopIntent = Intent(this, NetworkMonitorService::class.java).apply {
            action = ACTION_STOP_SERVICE // Usar la propiedad `action`
        }
        val pendingStopIntent = PendingIntent.getService(
            this, 0, stopIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Catálogo App - Sincronización")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(R.drawable.ic_launcher_foreground, "Detener", pendingStopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isInternetAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}