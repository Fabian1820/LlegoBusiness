package com.llego.shared.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

private const val TAG = "TokenManager"
private const val PREFS_NAME = "llego_auth_prefs"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"

/**
 * TokenManager - ImplementaciÃ³n Android usando EncryptedSharedPreferences
 * Los tokens persisten entre reinicios de la app
 */
actual class TokenManager actual constructor() {

    companion object {
        private var sharedPrefs: SharedPreferences? = null
        
        /**
         * Debe llamarse una vez al inicio de la app (en Application o MainActivity)
         */
        fun initialize(context: Context) {
            if (sharedPrefs == null) {
                try {
                    val masterKey = MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()
                    
                    sharedPrefs = EncryptedSharedPreferences.create(
                        context,
                        PREFS_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "initialize: Error creando EncryptedSharedPreferences, usando fallback", e)
                    // Fallback a SharedPreferences normal si falla el encriptado
                    sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                }
            }
        }
        
        /**
         * Verifica si hay un token guardado (Ãºtil para verificar sesiÃ³n sin crear instancia)
         */
        fun hasStoredToken(): Boolean {
            return sharedPrefs?.getString(KEY_ACCESS_TOKEN, null) != null
        }
    }

    actual fun saveToken(token: String) {
        sharedPrefs?.edit()?.putString(KEY_ACCESS_TOKEN, token)?.apply()
            ?: Log.e(TAG, "saveToken: SharedPreferences no inicializado!")
    }

    actual fun getToken(): String? {
        val token = sharedPrefs?.getString(KEY_ACCESS_TOKEN, null)
        return token
    }

    actual fun clearToken() {
        sharedPrefs?.edit()?.remove(KEY_ACCESS_TOKEN)?.apply()
    }

    actual fun saveRefreshToken(refreshToken: String) {
        sharedPrefs?.edit()?.putString(KEY_REFRESH_TOKEN, refreshToken)?.apply()
    }

    actual fun getRefreshToken(): String? {
        return sharedPrefs?.getString(KEY_REFRESH_TOKEN, null)
    }

    actual fun clearRefreshToken() {
        sharedPrefs?.edit()?.remove(KEY_REFRESH_TOKEN)?.apply()
    }

    actual fun clearAll() {
        sharedPrefs?.edit()?.clear()?.apply()
    }
}
