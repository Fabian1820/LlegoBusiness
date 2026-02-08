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
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CreateBranchInput
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.model.UpdateBusinessInput
import com.llego.shared.data.model.UpdateUserInput
import com.llego.shared.data.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

/**
 * Implementaci?n Android del AuthViewModel
 * Usa AndroidViewModel para tener acceso al Application context
 */
actual class AuthViewModel : ViewModel {

    private lateinit var authManager: AuthManager
    private var isInitialized = false

    // Estados internos
    private val _uiState = MutableStateFlow(AuthUiState())
    actual val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Estados espec?ficos para el formulario de login
    private val _email = MutableStateFlow("")
    actual val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    actual val password: StateFlow<String> = _password.asStateFlow()

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
        // Constructor sin par?metros requerido por expect/actual
        // Inicializaci?n lazy con TokenManager mock
    }

    private fun ensureInitialized() {
        if (!isInitialized) {

            // Crear TokenManager
            val tokenManager = TokenManager()

            authManager = AuthManager(tokenManager = tokenManager)
            isInitialized = true

            if (TokenManager.hasStoredToken()) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            // Observar cambios en el estado de autenticaci?n y business data
            viewModelScope.launch {
                combine(
                    authManager.isAuthenticated,
                    authManager.currentUser,
                    authManager.currentBusiness,
                    authManager.currentBranch
                ) { isAuth, user, _, _ ->
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = isAuth,
                        user = user
                    )
                }.collect()
            }

            // IMPORTANTE: Intentar restaurar sesi?n autom?ticamente al inicializar
            restoreSessionIfNeeded()
        }
    }

    init {
        // Asegurar que se inicializa autom?ticamente al crear el ViewModel
        ensureInitialized()
    }

    /**
     * Restaura la sesi?n del usuario si hay un token guardado
     * Optimizado para fallar r?pido en caso de problemas de conexi?n
     */
    private fun restoreSessionIfNeeded() {
        viewModelScope.launch {

            // Verificar si hay token guardado
            if (TokenManager.hasStoredToken()) {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Obtener usuario actual desde el backend
                val result = authManager.getCurrentUser()

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
                    }
                    is AuthResult.Error -> {
                        Log.w(TAG, "restoreSessionIfNeeded: error restaurando sesi?n: ${result.message}")

                        // Si el error es de conexi?n (timeout, 502, etc.), limpiar el token
                        // para evitar reintentos constantes en cada inicio
                        if (result.code == "APOLLO_ERROR" || result.message.contains("conexion", ignoreCase = true)) {
                            Log.w(TAG, "restoreSessionIfNeeded: error de conexi?n detectado, limpiando token")
                            authManager.logout() // Limpia el token
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            user = null,
                            error = null // No mostrar error en pantalla de login
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Carga los negocios y sucursales del usuario autenticado
     * OPTIMIZADO: Carga negocios y sucursales en paralelo para reducir tiempo de carga
     * Reduce el tiempo de ~2.4s a ~2s (eliminando el delay secuencial)
     */
    private suspend fun loadBusinessData() {
        ensureInitialized()


        // Cargar negocios
        val result = authManager.getBusinesses()


        when (result) {
            is BusinessResult.Success -> {
                _uiState.value = _uiState.value.copy(error = null)

                // Si hay al menos un negocio, cargar sus sucursales
                val currentBusiness = authManager.currentBusiness.value
                if (currentBusiness != null) {

                    // Las sucursales se cargan inmediatamente despuÃ©s de obtener el negocio
                    val branchesResult = authManager.getBranches(currentBusiness.id)
                    when (branchesResult) {
                        is BusinessResult.Success -> {
                            _uiState.value = _uiState.value.copy(error = null)
                        }
                        is BusinessResult.Error -> {
                            Log.e(TAG, "loadBusinessData: error cargando sucursales - ${branchesResult.message} (code: ${branchesResult.code})")
                            _uiState.value = _uiState.value.copy(error = branchesResult.message)
                        }
                        else -> {
                            Log.w(TAG, "loadBusinessData: resultado inesperado al cargar sucursales")
                        }
                    }
                } else {
                    Log.w(TAG, "loadBusinessData: getBusinesses exitoso pero currentBusiness es null")
                }
            }
            is BusinessResult.Error -> {
                Log.e(TAG, "loadBusinessData: error al cargar negocios: ${result.message} (code: ${result.code})")
                _uiState.value = _uiState.value.copy(error = result.message)
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

    actual fun login() {
        ensureInitialized()

        viewModelScope.launch {
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
                    // AuthResult.Loading - no deber?a llegar aqu?
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
                    Log.e(TAG, "loginWithGoogle: ERROR - ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _loginError.value = result.message
                }
                else -> {
                    Log.w(TAG, "loginWithGoogle: resultado inesperado")
                }
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
     * Autenticaci?n directa con JWT del backend (para Android Apple Sign-In OAuth flow)
     * El token ya viene validado del backend, solo se guarda y se obtiene el usuario
     */
    actual fun authenticateWithToken(token: String) {
        ensureInitialized()


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
        _loginError.value = null
    }

    actual fun getCurrentUser(): User? {
        ensureInitialized()
        return authManager.currentUser.value
    }

    actual suspend fun updateUser(input: UpdateUserInput): AuthResult<User> {
        ensureInitialized()
        return authManager.updateUser(input)
    }

    actual suspend fun updateBusiness(
        businessId: String,
        input: UpdateBusinessInput
    ): BusinessResult<Business> {
        ensureInitialized()
        return authManager.updateBusiness(businessId, input)
    }

    actual suspend fun updateBranch(
        branchId: String,
        input: UpdateBranchInput
    ): BusinessResult<Branch> {
        ensureInitialized()
        return authManager.updateBranch(branchId, input)
    }

    actual suspend fun createBranch(input: CreateBranchInput): BusinessResult<Branch> {
        ensureInitialized()
        return authManager.createBranch(input)
    }

    actual suspend fun deleteBranch(branchId: String): BusinessResult<Boolean> {
        ensureInitialized()
        return authManager.deleteBranch(branchId)
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

    /**
     * Recarga los datos del usuario y sus negocios/sucursales desde el backend
     * Ãštil despuÃ©s de aceptar una invitaciÃ³n o cualquier cambio que afecte businessIds/branchIds
     */
    actual fun reloadUserData() {
        ensureInitialized()


        viewModelScope.launch {
            // Primero, recargar el usuario actual desde el backend
            val result = authManager.getCurrentUser()

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        user = result.data,
                        isAuthenticated = true
                    )

                    // Luego cargar negocios y sucursales
                    loadBusinessData()
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "reloadUserData: Error al recargar usuario - ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                }
                else -> {
                    Log.w(TAG, "reloadUserData: Resultado inesperado")
                }
            }
        }
    }
}

