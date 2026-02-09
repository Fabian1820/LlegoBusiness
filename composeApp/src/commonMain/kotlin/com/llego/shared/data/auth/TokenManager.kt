package com.llego.shared.data.auth

/**
 * TokenManager - Gestiona el almacenamiento seguro de JWT tokens.
 *
 * Patron expect/actual por plataforma.
 */
expect class TokenManager() {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()

    fun saveRefreshToken(refreshToken: String)
    fun getRefreshToken(): String?
    fun clearRefreshToken()

    fun clearAll()

    fun saveLastSelectedBranchId(branchId: String)
    fun getLastSelectedBranchId(): String?
    fun clearLastSelectedBranchId()

    fun saveLastHomeTabIndex(index: Int)
    fun getLastHomeTabIndex(): Int?
    fun clearLastHomeTabIndex()
}
