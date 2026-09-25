package com.llego.shared.data.push

import com.apollographql.apollo.api.Optional
import com.llego.multiplatform.graphql.RegisterDeviceTokenMutation
import com.llego.multiplatform.graphql.type.DevicePlatformEnum
import com.llego.multiplatform.graphql.type.RegisterDeviceTokenInput
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.network.GraphQLClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Registra el token de push del dispositivo en el backend (`registerDeviceToken`).
 *
 * El backend enruta las pushes por `bundleId`: los tokens con bundle
 * `com.llego.business*` reciben las pushes de negocio (nuevo pedido, KYC, etc.).
 *
 * Se re-registra en cada cambio de sesión:
 * - Login / sesión restaurada → con JWT, el token queda asociado al usuario.
 * - Logout / cuenta eliminada → sin JWT, el backend deja `userId = null` y el
 *   dispositivo deja de recibir pushes del usuario anterior.
 */
object PushTokenRegistrar {

    data class DeviceInfo(
        val token: String,
        val platform: DevicePlatformEnum,
        val bundleId: String?,
        val appVersion: String?,
        val osVersion: String?,
    )

    private var jwtProvider: (() -> String?)? = null
    private var device: DeviceInfo? = null

    // Reemplazables en tests para no tocar la red ni depender de hilos reales.
    internal var sender: suspend (DeviceInfo, String?) -> Unit = ::sendToBackend
    internal var scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Serializa los envíos para que un logout seguido de login no llegue desordenado.
    private val mutex = Mutex()

    fun initialize(tokenManager: TokenManager) = initialize { tokenManager.getToken() }

    internal fun initialize(jwtProvider: () -> String?) {
        // En Android lo inicializa LlegoBusinessApplication antes que AppContainer
        if (this.jwtProvider != null) return
        this.jwtProvider = jwtProvider
        // El token del sistema pudo llegar antes que la inicialización.
        if (device != null) sync(jwt = jwtProvider())
    }

    fun onDeviceToken(info: DeviceInfo) {
        if (device == info) return
        device = info
        sync(jwt = jwtProvider?.invoke())
    }

    fun onSessionStarted() {
        sync(jwt = jwtProvider?.invoke())
    }

    fun onSessionEnded() {
        // JWT explícitamente nulo: desvincula el token del usuario en el backend.
        sync(jwt = null)
    }

    internal fun resetForTests() {
        jwtProvider = null
        device = null
    }

    private fun sync(jwt: String?) {
        val info = device ?: return
        scope.launch {
            mutex.withLock {
                try {
                    sender(info, jwt)
                } catch (t: Throwable) {
                    println("[PushTokenRegistrar] registerDeviceToken failed: ${t.message}")
                }
            }
        }
    }

    private suspend fun sendToBackend(info: DeviceInfo, jwt: String?) {
        val response = GraphQLClient.apolloClient.mutation(
            RegisterDeviceTokenMutation(
                input = RegisterDeviceTokenInput(
                    token = info.token,
                    platform = info.platform,
                    appVersion = Optional.presentIfNotNull(info.appVersion),
                    osVersion = Optional.presentIfNotNull(info.osVersion),
                    bundleId = Optional.presentIfNotNull(info.bundleId),
                ),
                jwt = Optional.present(jwt),
            )
        ).execute()
        if (response.hasErrors()) {
            println("[PushTokenRegistrar] registerDeviceToken error: ${response.errors?.firstOrNull()?.message}")
        }
    }
}
