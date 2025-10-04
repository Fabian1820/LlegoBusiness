package com.llego.shared.data.auth

import com.llego.shared.data.model.*
import com.llego.shared.data.repositories.AuthRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Manager centralizado para la autenticación en las apps de Llego
 * Coordina entre el AuthRepository y las diferentes pantallas
 */

class AuthManager private constructor() {

    private val authRepository = AuthRepository()

    // Exposición de los flows del repository
    val currentUser: StateFlow<User?> = authRepository.currentUser
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated
    val authToken: StateFlow<AuthToken?> = authRepository.authToken

    /**
     * Realiza el login del usuario
     */
    suspend fun login(email: String, password: String, businessType: BusinessType): AuthResult<User> {
        return try {
            val loginRequest = LoginRequest(email, password, businessType)
            val result = authRepository.login(loginRequest)

            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.success == true && response.user != null) {
                    AuthResult.Success(response.user)
                } else {
                    AuthResult.Error(response?.message ?: "Error desconocido en login")
                }
            } else {
                AuthResult.Error(result.exceptionOrNull()?.message ?: "Error de conexión")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado")
        }
    }

    /**
     * Realiza el logout del usuario
     */
    suspend fun logout(): AuthResult<Unit> {
        return try {
            val result = authRepository.logout()
            if (result.isSuccess) {
                AuthResult.Success(Unit)
            } else {
                AuthResult.Error("Error al cerrar sesión")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado al cerrar sesión")
        }
    }

    /**
     * Valida el token actual
     */
    suspend fun validateCurrentSession(): AuthResult<Boolean> {
        return try {
            val result = authRepository.validateToken()
            if (result.isSuccess) {
                val isValid = result.getOrNull() ?: false
                if (!isValid) {
                    // Si el token no es válido, hacer logout automático
                    authRepository.logout()
                }
                AuthResult.Success(isValid)
            } else {
                AuthResult.Error("Error al validar sesión")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado")
        }
    }

    /**
     * Refresca el token de autenticación
     */
    suspend fun refreshAuthToken(): AuthResult<AuthToken> {
        return try {
            val result = authRepository.refreshToken()
            if (result.isSuccess) {
                val token = result.getOrNull()
                if (token != null) {
                    AuthResult.Success(token)
                } else {
                    AuthResult.Error("No se pudo refrescar el token")
                }
            } else {
                AuthResult.Error("Error al refrescar token")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado")
        }
    }

    /**
     * Obtiene el usuario actual de forma sincrónica
     */
    fun getCurrentUser(): User? {
        return authRepository.getCurrentUser()
    }

    /**
     * Verifica si el usuario está autenticado
     */
    fun isUserAuthenticated(): Boolean {
        return authRepository.isUserAuthenticated()
    }

    /**
     * Obtiene el tipo de negocio actual
     */
    fun getCurrentBusinessType(): BusinessType? {
        return authRepository.getCurrentBusinessType()
    }

    /**
     * Obtiene el perfil de negocio del usuario actual
     */
    fun getBusinessProfile(): BusinessProfile? {
        return getCurrentUser()?.businessProfile
    }

    companion object {
        private var INSTANCE: AuthManager? = null

        fun getInstance(): AuthManager {
            return INSTANCE ?: AuthManager().also { INSTANCE = it }
        }
    }
}

/**
 * Sealed class para manejar los resultados de autenticación
 */
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    data object Loading : AuthResult<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun errorMessage(): String? = when (this) {
        is Error -> message
        else -> null
    }
}

/**
 * Estados de la UI para la autenticación
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null
) {
    val businessType: BusinessType? = currentUser?.businessType
    val isRestaurant: Boolean = businessType == BusinessType.RESTAURANT
    val isMarket: Boolean = businessType == BusinessType.GROCERY
    val isPharmacy: Boolean = businessType == BusinessType.PHARMACY
}