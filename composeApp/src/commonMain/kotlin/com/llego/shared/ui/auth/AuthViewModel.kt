package com.llego.shared.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.AuthManager
import com.llego.shared.data.auth.AuthResult
import com.llego.shared.data.auth.AuthUiState
import com.llego.shared.data.model.BusinessType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar la autenticación en las apps de Llego
 * Coordina la lógica de login, logout y validación de sesión
 */

class AuthViewModel : ViewModel() {

    private val authManager = AuthManager.getInstance()

    // Estados internos
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Estados específicos para el formulario de login
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _selectedBusinessType = MutableStateFlow<BusinessType?>(null)
    val selectedBusinessType: StateFlow<BusinessType?> = _selectedBusinessType.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    init {
        // Observar cambios en el estado de autenticación
        viewModelScope.launch {
            combine(
                authManager.isAuthenticated,
                authManager.currentUser
            ) { isAuth, user ->
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = isAuth,
                    currentUser = user,
                    isLoading = false
                )
            }.collect()
        }

        // Validar sesión actual al inicializar
        validateCurrentSession()
    }

    /**
     * Actualiza el email del formulario
     */
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        clearLoginError()
    }

    /**
     * Actualiza la contraseña del formulario
     */
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        clearLoginError()
    }

    /**
     * Selecciona el tipo de negocio
     */
    fun selectBusinessType(businessType: BusinessType) {
        _selectedBusinessType.value = businessType
        clearLoginError()
    }

    /**
     * Realiza el login del usuario
     * NOTA: Validaciones deshabilitadas para testing - cualquier usuario puede entrar
     */
    fun login() {
        viewModelScope.launch {
            // Validaciones básicas deshabilitadas - solo requerimos tipo de negocio
            if (_selectedBusinessType.value == null) {
                _loginError.value = "Selecciona un tipo de negocio"
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)
            _loginError.value = null

            // Usar email genérico si está vacío
            val email = if (_email.value.isBlank()) "test@llego.com" else _email.value.trim()
            val password = if (_password.value.isBlank()) "123456" else _password.value

            val result = authManager.login(
                email = email,
                password = password,
                businessType = _selectedBusinessType.value!!
            )

            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = result.data,
                        error = null
                    )
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
                    // No debería llegar aquí en este caso
                }
            }
        }
    }

    /**
     * Realiza el logout del usuario
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = authManager.logout()
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState() // Reset completo del estado
                    clearLoginForm()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    // No debería llegar aquí
                }
            }
        }
    }

    /**
     * Valida la sesión actual
     */
    fun validateCurrentSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = authManager.validateCurrentSession()
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = result.data,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        error = result.message
                    )
                }
                else -> {
                    // No debería llegar aquí
                }
            }
        }
    }

    /**
     * Limpia el error de login
     */
    fun clearLoginError() {
        _loginError.value = null
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Limpia el formulario de login
     */
    private fun clearLoginForm() {
        _email.value = ""
        _password.value = ""
        _selectedBusinessType.value = null
        _loginError.value = null
    }

    /**
     * Valida los datos del formulario de login
     */
    private fun isValidLoginInput(): Boolean {
        val email = _email.value.trim()
        val password = _password.value

        return when {
            _selectedBusinessType.value == null -> {
                _loginError.value = "Selecciona un tipo de negocio"
                false
            }
            email.isEmpty() -> {
                _loginError.value = "El email es requerido"
                false
            }
            !isValidEmail(email) -> {
                _loginError.value = "Formato de email inválido"
                false
            }
            password.isEmpty() -> {
                _loginError.value = "La contraseña es requerida"
                false
            }
            password.length < 6 -> {
                _loginError.value = "La contraseña debe tener al menos 6 caracteres"
                false
            }
            else -> true
        }
    }

    /**
     * Valida el formato del email
     */
    private fun isValidEmail(email: String): Boolean {
        return PlatformEmailValidator.isValidEmail(email)
    }

    /**
     * Obtiene información del usuario actual
     */
    fun getCurrentUser() = authManager.getCurrentUser()
    fun getCurrentBusinessType() = authManager.getCurrentBusinessType()
    fun getBusinessProfile() = authManager.getBusinessProfile()
}

// Importación necesaria para la validación de email en Android
expect object PlatformEmailValidator {
    fun isValidEmail(email: String): Boolean
}