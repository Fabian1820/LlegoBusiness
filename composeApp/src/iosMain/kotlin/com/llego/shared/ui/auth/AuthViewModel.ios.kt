package com.llego.shared.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.AuthManager
import com.llego.shared.data.auth.TokenManager
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

actual class AuthViewModel actual constructor() : ViewModel() {

    private val tokenManager = TokenManager()
    private val authManager = AuthManager(tokenManager = tokenManager)

    private val _uiState = MutableStateFlow(AuthUiState())
    actual val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    actual val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    actual val password: StateFlow<String> = _password.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    actual val loginError: StateFlow<String?> = _loginError.asStateFlow()

    /** StateFlow del negocio actual - observable para reactividad en UI */
    actual val currentBusiness: StateFlow<Business?> = authManager.currentBusiness

    /** StateFlow de la sucursal actual - observable para reactividad en UI */
    actual val currentBranch: StateFlow<Branch?> = authManager.currentBranch

    /** StateFlow de sucursales del usuario */
    actual val branches: StateFlow<List<Branch>> = authManager.branches

    init {
        viewModelScope.launch {
            combine(
                authManager.isAuthenticated,
                authManager.currentUser,
                authManager.currentBusiness,
                authManager.currentBranch
            ) { isAuth, user, business, branch ->
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = isAuth,
                    user = user,
                    isLoading = false
                )
            }.collect()
        }
    }

    /**
     * Carga los negocios y sucursales del usuario autenticado
     */
    private suspend fun loadBusinessData() {
        // Cargar negocios
        authManager.getBusinesses()

        // Si hay al menos un negocio, cargar sus sucursales
        val currentBusiness = authManager.currentBusiness.value
        if (currentBusiness != null) {
            authManager.getBranches(currentBusiness.id)
        }
    }

    actual fun updateEmail(newEmail: String) {
        _email.value = newEmail
        clearLoginError()
    }

    actual fun updatePassword(newPassword: String) {
        _password.value = newPassword
        clearLoginError()
    }

    actual fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _loginError.value = null

            val email = if (_email.value.isBlank()) "test@llego.com" else _email.value.trim()
            val password = if (_password.value.isBlank()) "123456" else _password.value

            val result = authManager.login(email = email, password = password)

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = result.data,
                        error = null
                    )

                    // Cargar datos de negocio y sucursales
                    loadBusinessData()

                    clearLoginForm()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _loginError.value = result.message
                }
                else -> {}
            }
        }
    }

    actual fun loginWithGoogle(idToken: String, nonce: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _loginError.value = null

            val result = authManager.loginWithGoogle(idToken, nonce)

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = result.data,
                        error = null
                    )

                    // Cargar datos de negocio y sucursales
                    loadBusinessData()

                    clearLoginForm()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _loginError.value = result.message
                }
                else -> {}
            }
        }
    }

    actual fun loginWithApple(identityToken: String, nonce: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _loginError.value = null

            val result = authManager.loginWithApple(identityToken, nonce)

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = result.data,
                        error = null
                    )

                    // Cargar datos de negocio y sucursales
                    loadBusinessData()

                    clearLoginForm()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _loginError.value = result.message
                }
                else -> {}
            }
        }
    }

    /**
     * Autenticaci?n directa con JWT del backend (para Android Apple Sign-In OAuth flow)
     * En iOS no se usa este m?todo, Apple Sign-In usa el SDK nativo
     */
    actual fun authenticateWithToken(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _loginError.value = null

            val result = authManager.authenticateWithToken(token)

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = result.data,
                        error = null
                    )
                    loadBusinessData()
                    clearLoginForm()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _loginError.value = result.message
                }
                else -> {}
            }
        }
    }

    actual fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = authManager.logout()
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState()
                    clearLoginForm()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    actual fun clearLoginError() {
        _loginError.value = null
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun clearLoginForm() {
        _email.value = ""
        _password.value = ""
        _loginError.value = null
    }

    actual fun getCurrentUser(): User? = authManager.currentUser.value

    actual suspend fun updateUser(input: UpdateUserInput): AuthResult<User> {
        return authManager.updateUser(input)
    }

    actual suspend fun updateBusiness(
        businessId: String,
        input: UpdateBusinessInput
    ): BusinessResult<Business> {
        return authManager.updateBusiness(businessId, input)
    }

    actual suspend fun updateBranch(
        branchId: String,
        input: UpdateBranchInput
    ): BusinessResult<Branch> {
        return authManager.updateBranch(branchId, input)
    }

    actual suspend fun createBranch(input: CreateBranchInput): BusinessResult<Branch> {
        return authManager.createBranch(input)
    }

    actual suspend fun deleteBranch(branchId: String): BusinessResult<Boolean> {
        return authManager.deleteBranch(branchId)
    }

    actual fun getCurrentBranchId(): String? = authManager.currentBranch.value?.id

    actual fun getCurrentBusinessId(): String? = authManager.currentBusiness.value?.id

    actual fun setCurrentBranch(branch: Branch) {
        authManager.setCurrentBranch(branch)
    }
}
