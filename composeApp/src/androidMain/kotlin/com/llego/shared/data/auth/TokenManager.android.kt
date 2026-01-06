package com.llego.shared.data.auth

/**
 * TokenManager - Implementación Android usando almacenamiento en memoria
 * En producción, esto debería usar SharedPreferences con Context inyectado
 * Por ahora usa memoria para simplicidad y compatibilidad con expect/actual
 */
actual class TokenManager actual constructor() {

    // In-memory storage para testing/desarrollo
    private var memoryToken: String? = null
    private var memoryRefreshToken: String? = null

    actual fun saveToken(token: String) {
        memoryToken = token
    }

    actual fun getToken(): String? {
        return memoryToken
    }

    actual fun clearToken() {
        memoryToken = null
    }

    actual fun saveRefreshToken(refreshToken: String) {
        memoryRefreshToken = refreshToken
    }

    actual fun getRefreshToken(): String? {
        return memoryRefreshToken
    }

    actual fun clearRefreshToken() {
        memoryRefreshToken = null
    }

    actual fun clearAll() {
        memoryToken = null
        memoryRefreshToken = null
    }
}
