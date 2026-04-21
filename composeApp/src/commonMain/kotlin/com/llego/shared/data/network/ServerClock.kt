@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.llego.shared.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

@Serializable
private data class ServerTimeResponse(val utc: String)

/**
 * Reloj sincronizado con el servidor para calcular countdowns correctamente
 * independientemente del reloj del dispositivo.
 *
 * Llama a [sync] una vez al arrancar la app. Después usa [nowMs] en lugar de
 * System.currentTimeMillis() para calcular tiempos relativos.
 */
object ServerClock {

    /** Diferencia en ms entre servidor y dispositivo. Positivo = servidor adelantado. */
    private var offsetMs: Long = 0L

    private val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    /**
     * Sincroniza el offset con el servidor.
     * Llámalo una vez en Application.onCreate o al iniciar sesión.
     * Si falla, [offsetMs] queda en 0 y se usa el reloj del dispositivo.
     */
    suspend fun sync() {
        try {
            val requestedAt = Clock.System.now().toEpochMilliseconds()
            val response: ServerTimeResponse =
                httpClient.get("${BackendConfig.BASE_URL}/time").body()
            val receivedAt = Clock.System.now().toEpochMilliseconds()

            val serverMs = Instant.parse(response.utc).toEpochMilliseconds()
            val networkLatency = (receivedAt - requestedAt) / 2
            offsetMs = serverMs - (requestedAt + networkLatency)
        } catch (_: Exception) {
            // Fallo silencioso: offsetMs = 0, se usa reloj local
        }
    }

    /** Hora actual en ms, corregida según el reloj del servidor. */
    fun nowMs(): Long = Clock.System.now().toEpochMilliseconds() + offsetMs
}
