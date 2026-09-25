package com.llego.shared.data.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.llego.app.BuildConfig
import com.llego.multiplatform.graphql.type.DevicePlatformEnum

/**
 * Integración de FCM para Android. Firebase se inicializa a mano con los valores de
 * google-services.json expuestos en BuildConfig (sin el plugin de Google Services).
 */
object AndroidPush {

    /** Canal que el backend pone en todas las pushes Android (FCM_ANDROID_CHANNEL_ID). */
    const val CHANNEL_ID = "llego_orders"

    private const val TAG = "AndroidPush"

    /** Inicializa Firebase si hay configuración. Devuelve false si el push queda inactivo. */
    fun initialize(context: Context): Boolean {
        createChannel(context)

        if (BuildConfig.FIREBASE_APP_ID.isBlank()) {
            Log.w(TAG, "Sin google-services.json: push Android inactivo")
            return false
        }
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
                .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                .setApiKey(BuildConfig.FIREBASE_API_KEY)
                .build()
            FirebaseApp.initializeApp(context, options)
        }
        return true
    }

    /** Pide el token actual (onNewToken solo se dispara cuando cambia). */
    fun fetchCurrentToken(context: Context) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> onToken(context, token) }
            .addOnFailureListener { e -> Log.w(TAG, "No se pudo obtener el token FCM: ${e.message}") }
    }

    fun onToken(context: Context, token: String) {
        val appVersion = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull()

        PushTokenRegistrar.onDeviceToken(
            PushTokenRegistrar.DeviceInfo(
                token = token,
                platform = DevicePlatformEnum.ANDROID,
                bundleId = context.packageName,
                appVersion = appVersion,
                osVersion = Build.VERSION.RELEASE,
            )
        )
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pedidos y pagos",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Nuevos pedidos, cambios de estado y pagos"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
