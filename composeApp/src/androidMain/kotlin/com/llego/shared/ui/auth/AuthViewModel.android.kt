package com.llego.shared.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.AuthManager
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.AuthResult
import com.llego.shared.data.model.AuthUiState
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.BusinessType
import com.llego.shared.data.model.User
import com.llego.shared.data.model.BusinessProfile
import com.llego.shared.data.model.BusinessResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

/**
 * Implementación Android del AuthViewModel
 * Usa AndroidViewModel para tener acceso al Application context
 */
actual class AuthViewModel : ViewModel {

    private lateinit var authManager: AuthManager
    private var isInitialized = false

    // Estados internos
    private val _uiState = MutableStateFlow(AuthUiState())
    actual val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Estados específicos para el formulario de login
    private val _email = MutableStateFlow("")
    actual val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    actual val password: StateFlow<String> = _password.asStateFlow()

    private val _selectedBusinessType = MutableStateFlow<BusinessType?>(BusinessType.RESTAURANT)
    actual val selectedBusinessType: StateFlow<BusinessType?> = _selectedBusinessType.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    actual val loginError: StateFlow<String?> = _loginError.asStateFlow()

    /** StateFlow del negocio actual - observable para reactividad en UI */
    actual val currentBusiness: StateFlow<Business?>
        get() {
            ensureInitialized()
            return authManager.currentBusiness
        }

    /** StateFlow de la sucursal actual - observable para reactividad en UI */
    actual val currentBranch: StateFlow<Branch?>
        get() {
            ensureInitialized()
            return authManager.currentBranch
        }

    /** StateFlow de sucursales del usuario */
    actual val branches: StateFlow<List<Branch>>
        get() {
            ensureInitialized()
            return authManager.branches
        }

    actual constructor() : super() {
        // Constructor sin parámetros requerido por expect/actual
        // Inicialización lazy con TokenManager mock
        Log.d(TAG, "constructor: AuthViewModel creado")
    }

    private fun ensureInitialized() {
        if (!isInitialized) {
            Log.d(TAG, "ensureInitialized: inicializando AuthViewModel")

            // Crear TokenManager
            val tokenManager = TokenManager()

            authManager = AuthManager(tokenManager = tokenManager)
            isInitialized = true

            // Observar cambios en el estado de autenticación y business data
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

            // IMPORTANTE: Intentar restaurar sesión automáticamente al inicializar
            restoreSessionIfNeeded()
        }
    }

    init {
        // Asegurar que se inicializa automáticamente al crear el ViewModel
        ensureInitialized()
    }
    
    /**
     * Restaura la sesión del usuario si hay un token guardado
     */
    private fun restoreSessionIfNeeded() {
        viewModelScope.launch {
            Log.d(TAG, "restoreSessionIfNeeded: verificando token guardado")
            
            // Verificar si hay token guardado
            if (TokenManager.hasStoredToken()) {
                Log.d(TAG, "restoreSessionIfNeeded: token encontrado, restaurando sesión...")
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Obtener usuario actual desde el backend
                val result = authManager.getCurrentUser()
                
                when (result) {
                    is AuthResult.Success -> {
                        Log.d(TAG, "restoreSessionIfNeeded: sesión restaurada para ${result.data.email}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            user = result.data,
                            error = null
                        )
                        
                        // Cargar datos de negocio y sucursales
                        loadBusinessData()
                    }
                    is AuthResult.Error -> {
                        Log.w(TAG, "restoreSessionIfNeeded: error restaurando sesión: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            user = null
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } else {
                Log.d(TAG, "restoreSessionIfNeeded: no hay token guardado")
            }
        }
    }

    /**
     * Carga los negocios y sucursales del usuario autenticado
     */
    private suspend fun loadBusinessData() {
        ensureInitialized()

        Log.d(TAG, "loadBusinessData: cargando negocios...")

        // Cargar negocios
        val result = authManager.getBusinesses()

        Log.d(TAG, "loadBusinessData: resultado de getBusinesses = ${result::class.simpleName}")

        when (result) {
            is BusinessResult.Success -> {
                Log.d(TAG, "loadBusinessData: ${result.data.size} negocios cargados")

                // Si hay al menos un negocio, cargar sus sucursales
                val currentBusiness = authManager.currentBusiness.value
                if (currentBusiness != null) {
                    Log.d(TAG, "loadBusinessData: cargando sucursales para negocio ${currentBusiness.id}")
                    authManager.getBranches(currentBusiness.id)
                } else {
                    Log.w(TAG, "loadBusinessData: getBusinesses exitoso pero currentBusiness es null")
                }
            }
            is BusinessResult.Error -> {
                Log.e(TAG, "loadBusinessData: error al cargar negocios: ${result.message} (code: ${result.code})")
            }
            else -> {
                Log.w(TAG, "loadBusinessData: resultado inesperado: $result")
            }
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

    actual fun selectBusinessType(businessType: BusinessType) {
        _selectedBusinessType.value = businessType
        clearLoginError()
    }

    actual fun login() {
        ensureInitialized()

        viewModelScope.launch {
            if (_selectedBusinessType.value == null) {
                _loginError.value = "Selecciona un tipo de negocio"
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)
            _loginError.value = null

            val email = if (_email.value.isBlank()) "test@llego.com" else _email.value.trim()
            val password = if (_password.value.isBlank()) "123456" else _password.value

            val result = authManager.login(
                email = email,
                password = password
            )

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
                else -> {
                    // AuthResult.Loading - no debería llegar aquí
                }
            }
        }
    }

    actual fun loginWithGoogle(idToken: String, nonce: String?) {
        ensureInitialized()

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
        ensureInitialized()

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
     * Autenticación directa con JWT del backend (para Android Apple Sign-In OAuth flow)
     * El token ya viene validado del backend, solo se guarda y se obtiene el usuario
     */
    actual fun authenticateWithToken(token: String) {
        ensureInitialized()
        
        Log.d(TAG, "authenticateWithToken: iniciando con token length=${token.length}")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _loginError.value = null

            val result = authManager.authenticateWithToken(token)

            when (result) {
                is AuthResult.Success -> {
                    Log.d(TAG, "authenticateWithToken: éxito - usuario=${result.data.email}")
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
                    Log.e(TAG, "authenticateWithToken: error - ${result.message}")
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
        ensureInitialized()

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
        _selectedBusinessType.value = null
        _loginError.value = null
    }

    actual fun getCurrentUser(): User? {
        ensureInitialized()
        return authManager.currentUser.value
    }

    actual fun getCurrentBusinessType(): BusinessType? {
        ensureInitialized()
        return authManager.getCurrentBusinessType()
    }

    actual fun getBusinessProfile(): BusinessProfile? {
        ensureInitialized()
        return authManager.getBusinessProfile()
    }

    /**
     * Obtiene el ID de la sucursal actual
     * @return branchId de la sucursal actual o null si no hay sucursal seleccionada
     */
    actual fun getCurrentBranchId(): String? {
        ensureInitialized()
        return authManager.currentBranch.value?.id
    }

    /**
     * Obtiene el ID del negocio actual
     * @return businessId del negocio actual o null si no hay negocio seleccionado
     */
    actual fun getCurrentBusinessId(): String? {
        ensureInitialized()
        return authManager.currentBusiness.value?.id
    }

    /**
     * Establece la sucursal actual
     */
    actual fun setCurrentBranch(branch: Branch) {
        ensureInitialized()
        authManager.setCurrentBranch(branch)
    }
}
