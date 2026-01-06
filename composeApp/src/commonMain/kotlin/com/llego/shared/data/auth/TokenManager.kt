package com.llego.shared.data.auth

/**
 * TokenManager - Gestiona el almacenamiento seguro de JWT tokens
 *
 * Patrón expect/actual para implementación específica por plataforma:
 * - Android: SharedPreferences (o in-memory si context es null)
 * - iOS: NSUserDefaults (Keychain para producción)
 * - Desktop: Properties file
 */
expect class TokenManager() {
    /**
     * Guarda el JWT token de forma persistente
     */
    fun saveToken(token: String)

    /**
     * Recupera el JWT token guardado
     * @return Token JWT o null si no existe
     */
    fun getToken(): String?

    /**
     * Elimina el token guardado (usado en logout)
     */
    fun clearToken()

    /**
     * Guarda el refresh token (opcional, para futuro)
     */
    fun saveRefreshToken(refreshToken: String)

    /**
     * Recupera el refresh token
     */
    fun getRefreshToken(): String?

    /**
     * Elimina el refresh token
     */
    fun clearRefreshToken()

    /**
     * Limpia todos los datos de autenticación
     */
    fun clearAll()
}
