package com.llego.shared.data.cache

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

/**
 * Caché local para datos de la app
 * Mejora la UX mostrando datos previos mientras se refresca en background
 */
object AppCache {
    private const val PREFS_NAME = "llego_app_cache"
    private const val KEY_LAST_BUSINESS_ID = "last_business_id"
    private const val KEY_LAST_BRANCH_ID = "last_branch_id"
    private const val KEY_CACHED_USER_EMAIL = "cached_user_email"

    private lateinit var prefs: SharedPreferences
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun init(context: Context) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback a SharedPreferences normal si hay error con encriptación
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // ============= BUSINESS & BRANCH CACHE =============

    fun saveLastSelectedBusiness(businessId: String) {
        prefs.edit().putString(KEY_LAST_BUSINESS_ID, businessId).apply()
    }

    fun getLastSelectedBusinessId(): String? {
        return prefs.getString(KEY_LAST_BUSINESS_ID, null)
    }

    fun saveLastSelectedBranch(branchId: String) {
        prefs.edit().putString(KEY_LAST_BRANCH_ID, branchId).apply()
    }

    fun getLastSelectedBranchId(): String? {
        return prefs.getString(KEY_LAST_BRANCH_ID, null)
    }

    fun saveCachedUserEmail(email: String) {
        prefs.edit().putString(KEY_CACHED_USER_EMAIL, email).apply()
    }

    fun getCachedUserEmail(): String? {
        return prefs.getString(KEY_CACHED_USER_EMAIL, null)
    }

    // ============= CLEAR CACHE =============

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun clearBusinessData() {
        prefs.edit()
            .remove(KEY_LAST_BUSINESS_ID)
            .remove(KEY_LAST_BRANCH_ID)
            .apply()
    }
}
