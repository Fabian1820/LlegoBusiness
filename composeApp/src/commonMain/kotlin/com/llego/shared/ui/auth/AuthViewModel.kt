package com.llego.shared.ui.auth

import androidx.lifecycle.ViewModel
import com.llego.shared.data.model.AuthResult
import com.llego.shared.data.model.AuthUiState
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CreateBranchInput
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.model.UpdateBusinessInput
import com.llego.shared.data.model.UpdateUserInput
import com.llego.shared.data.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel para manejar la autenticaci?n en las apps de Llego
 * Coordina la l?gica de login, logout y validaci?n de sesi?n
 *
 * NOTA: Esta es una implementaci?n expect/actual para soportar diferentes plataformas
 */

expect class AuthViewModel() : ViewModel {
    val uiState: StateFlow<AuthUiState>
    val email: StateFlow<String>
    val password: StateFlow<String>
    val loginError: StateFlow<String?>

    /** StateFlow del negocio actual - observable para reactividad en UI */
    val currentBusiness: StateFlow<Business?>

    /** StateFlow de la sucursal actual - observable para reactividad en UI */
    val currentBranch: StateFlow<Branch?>

    /** StateFlow de sucursales del usuario */
    val branches: StateFlow<List<Branch>>

    fun updateEmail(newEmail: String)
    fun updatePassword(newPassword: String)
    fun login()
    fun loginWithGoogle(idToken: String, nonce: String? = null)
    fun loginWithApple(identityToken: String, nonce: String? = null)

    /**
     * Autenticaci?n directa con JWT del backend (para Android Apple Sign-In OAuth flow)
     * El token ya viene validado del backend, solo se guarda y se obtiene el usuario
     */
    fun authenticateWithToken(token: String)

    fun logout()
    fun clearLoginError()
    fun getCurrentUser(): User?
    suspend fun updateUser(input: UpdateUserInput): AuthResult<User>
    suspend fun updateBusiness(businessId: String, input: UpdateBusinessInput): BusinessResult<Business>
    suspend fun updateBranch(branchId: String, input: UpdateBranchInput): BusinessResult<Branch>
    suspend fun createBranch(input: CreateBranchInput): BusinessResult<Branch>
    suspend fun deleteBranch(branchId: String): BusinessResult<Boolean>

    /** Obtiene el ID de la sucursal actual */
    fun getCurrentBranchId(): String?

    /** Obtiene el ID del negocio actual */
    fun getCurrentBusinessId(): String?

    /** Establece la sucursal actual */
    fun setCurrentBranch(branch: Branch)

    /** Recarga los datos del usuario y sus negocios/sucursales desde el backend */
    fun reloadUserData()
}
