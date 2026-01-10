package com.llego.shared.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.model.AuthResult
import com.llego.shared.data.model.AuthUiState
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.BusinessType
import com.llego.shared.data.model.BusinessProfile
import com.llego.shared.data.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar la autenticación en las apps de Llego
 * Coordina la lógica de login, logout y validación de sesión
 *
 * NOTA: Esta es una implementación expect/actual para soportar diferentes plataformas
 */

expect class AuthViewModel() : ViewModel {
    val uiState: StateFlow<AuthUiState>
    val email: StateFlow<String>
    val password: StateFlow<String>
    val selectedBusinessType: StateFlow<BusinessType?>
    val loginError: StateFlow<String?>
    
    /** StateFlow del negocio actual - observable para reactividad en UI */
    val currentBusiness: StateFlow<Business?>

    /** StateFlow de la sucursal actual - observable para reactividad en UI */
    val currentBranch: StateFlow<Branch?>

    /** StateFlow de sucursales del usuario */
    val branches: StateFlow<List<Branch>>

    fun updateEmail(newEmail: String)
    fun updatePassword(newPassword: String)
    fun selectBusinessType(businessType: BusinessType)
    fun login()
    fun loginWithGoogle(idToken: String, nonce: String? = null)
    fun loginWithApple(identityToken: String, nonce: String? = null)
    fun logout()
    fun clearLoginError()
    fun getCurrentUser(): User?
    fun getCurrentBusinessType(): BusinessType?
    fun getBusinessProfile(): BusinessProfile?

    /** Obtiene el ID de la sucursal actual */
    fun getCurrentBranchId(): String?

    /** Obtiene el ID del negocio actual */
    fun getCurrentBusinessId(): String?

    /** Establece la sucursal actual */
    fun setCurrentBranch(branch: Branch)
}
