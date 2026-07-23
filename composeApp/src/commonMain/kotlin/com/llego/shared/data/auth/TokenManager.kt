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

    /**
     * Owner del último branch guardado. Se usa como guard: si el user que arranca
     * la app no coincide con éste, ignoramos el last_branch_id restaurado.
     */
    fun saveLastBranchOwnerUserId(userId: String)
    fun getLastBranchOwnerUserId(): String?
    fun clearLastBranchOwnerUserId()

    fun saveLastHomeTabIndex(index: Int)
    fun getLastHomeTabIndex(): Int?
    fun clearLastHomeTabIndex()
}
