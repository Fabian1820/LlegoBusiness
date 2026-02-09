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
        userDefaults.removeObjectForKey(KEY_LAST_BRANCH_ID)
        userDefaults.removeObjectForKey(KEY_LAST_HOME_TAB_INDEX)
        userDefaults.synchronize()
    }

    actual fun saveLastSelectedBranchId(branchId: String) {
        userDefaults.setObject(branchId, forKey = KEY_LAST_BRANCH_ID)
        userDefaults.synchronize()
    }

    actual fun getLastSelectedBranchId(): String? {
        return userDefaults.stringForKey(KEY_LAST_BRANCH_ID)
    }

    actual fun clearLastSelectedBranchId() {
        userDefaults.removeObjectForKey(KEY_LAST_BRANCH_ID)
        userDefaults.synchronize()
    }

    actual fun saveLastHomeTabIndex(index: Int) {
        userDefaults.setInteger(index.toLong(), forKey = KEY_LAST_HOME_TAB_INDEX)
        userDefaults.synchronize()
    }

    actual fun getLastHomeTabIndex(): Int? {
        return if (userDefaults.objectForKey(KEY_LAST_HOME_TAB_INDEX) != null) {
            userDefaults.integerForKey(KEY_LAST_HOME_TAB_INDEX).toInt()
        } else {
            null
        }
    }

    actual fun clearLastHomeTabIndex() {
        userDefaults.removeObjectForKey(KEY_LAST_HOME_TAB_INDEX)
        userDefaults.synchronize()
    }

    companion object {
        private const val KEY_TOKEN = "com.llego.jwt_token"
        private const val KEY_REFRESH_TOKEN = "com.llego.refresh_token"
        private const val KEY_LAST_BRANCH_ID = "com.llego.last_branch_id"
        private const val KEY_LAST_HOME_TAB_INDEX = "com.llego.last_home_tab_index"
    }
}
