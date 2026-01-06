package com.llego.shared.data.auth

import platform.Foundation.NSUserDefaults

/**
 * TokenManager - Implementación iOS usando NSUserDefaults
 *
 * Nota: Para producción, considerar usar Keychain para mayor seguridad
 */
actual class TokenManager {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun saveToken(token: String) {
        userDefaults.setObject(token, forKey = KEY_TOKEN)
        userDefaults.synchronize()
    }

    actual fun getToken(): String? {
        return userDefaults.stringForKey(KEY_TOKEN)
    }

    actual fun clearToken() {
        userDefaults.removeObjectForKey(KEY_TOKEN)
        userDefaults.synchronize()
    }

    actual fun saveRefreshToken(refreshToken: String) {
        userDefaults.setObject(refreshToken, forKey = KEY_REFRESH_TOKEN)
        userDefaults.synchronize()
    }

    actual fun getRefreshToken(): String? {
        return userDefaults.stringForKey(KEY_REFRESH_TOKEN)
    }

    actual fun clearRefreshToken() {
        userDefaults.removeObjectForKey(KEY_REFRESH_TOKEN)
        userDefaults.synchronize()
    }

    actual fun clearAll() {
        userDefaults.removeObjectForKey(KEY_TOKEN)
        userDefaults.removeObjectForKey(KEY_REFRESH_TOKEN)
        userDefaults.synchronize()
    }

    companion object {
        private const val KEY_TOKEN = "com.llego.jwt_token"
        private const val KEY_REFRESH_TOKEN = "com.llego.refresh_token"
    }
}
