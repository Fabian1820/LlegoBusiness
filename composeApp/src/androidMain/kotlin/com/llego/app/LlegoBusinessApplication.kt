package com.llego.app

import android.app.Application
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.push.AndroidPush
import com.llego.shared.data.push.PushTokenRegistrar

/**
 * Inicializa el push al arrancar el proceso, antes que cualquier Activity: el sistema puede
 * levantar LlegoBusinessFirebaseMessagingService sin que MainActivity exista.
 */
class LlegoBusinessApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // El registro necesita el JWT guardado para asociar el token al usuario
        TokenManager.initialize(applicationContext)
        PushTokenRegistrar.initialize(TokenManager())

        if (AndroidPush.initialize(applicationContext)) {
            AndroidPush.fetchCurrentToken(applicationContext)
        }
    }
}
