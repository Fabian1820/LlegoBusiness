package com.llego.shared.data.auth

import java.io.File
import java.util.Properties

/**
 * TokenManager - Implementación Desktop/JVM usando Properties file
 */
actual class TokenManager {

    private val propsFile = File(System.getProperty("user.home"), ".llego_auth")
    private val properties = Properties()

    init {
        if (propsFile.exists()) {
            propsFile.inputStream().use { properties.load(it) }
        }
    }

    actual fun saveToken(token: String) {
        properties.setProperty(KEY_TOKEN, token)
        saveProperties()
    }

    actual fun getToken(): String? {
        return properties.getProperty(KEY_TOKEN)
    }

    actual fun clearToken() {
        properties.remove(KEY_TOKEN)
        saveProperties()
    }

    actual fun saveRefreshToken(refreshToken: String) {
        properties.setProperty(KEY_REFRESH_TOKEN, refreshToken)
        saveProperties()
    }

    actual fun getRefreshToken(): String? {
        return properties.getProperty(KEY_REFRESH_TOKEN)
    }

    actual fun clearRefreshToken() {
        properties.remove(KEY_REFRESH_TOKEN)
        saveProperties()
    }

    actual fun clearAll() {
        properties.clear()
        saveProperties()
    }

    actual fun saveLastSelectedBranchId(branchId: String) {
        properties.setProperty(KEY_LAST_BRANCH_ID, branchId)
        saveProperties()
    }

    actual fun getLastSelectedBranchId(): String? {
        return properties.getProperty(KEY_LAST_BRANCH_ID)
    }

    actual fun clearLastSelectedBranchId() {
        properties.remove(KEY_LAST_BRANCH_ID)
        saveProperties()
    }

    actual fun saveLastHomeTabIndex(index: Int) {
        properties.setProperty(KEY_LAST_HOME_TAB_INDEX, index.toString())
        saveProperties()
    }

    actual fun getLastHomeTabIndex(): Int? {
        return properties.getProperty(KEY_LAST_HOME_TAB_INDEX)?.toIntOrNull()
    }

    actual fun clearLastHomeTabIndex() {
        properties.remove(KEY_LAST_HOME_TAB_INDEX)
        saveProperties()
    }

    private fun saveProperties() {
        propsFile.outputStream().use { properties.store(it, "Llego Auth Tokens") }
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_LAST_BRANCH_ID = "last_branch_id"
        private const val KEY_LAST_HOME_TAB_INDEX = "last_home_tab_index"
    }
}
