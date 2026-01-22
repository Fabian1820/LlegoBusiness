package com.llego.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Modelos para Autenticaci?n y Gesti?n de Usuarios
 * Basados en la API documentada en docs/users-api.md y docs/google-auth.md
 */

// ============= INPUT TYPES =============

/**
 * Input para login tradicional (email/password)
 */
@Serializable
data class LoginInput(
    val email: String,
    val password: String
)

/**
 * Input para registro de nuevo usuario
 */
@Serializable
data class RegisterInput(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val role: String = "merchant" // merchant, customer, driver, admin
)

/**
 * Input para login con Google/Apple
 */
@Serializable
data class SocialLoginInput(
    val idToken: String,
    val nonce: String? = null,
    val authorizationCode: String? = null // No usado actualmente en backend
)

/**
 * Input para actualizar perfil de usuario
 */
@Serializable
data class UpdateUserInput(
    val name: String? = null,
    val username: String? = null,
    val phone: String? = null,
    val avatar: String? = null // Path de imagen (ej: "users/avatars/6774abc123_1234567890.jpg")
)

/**
 * Input para agregar sucursal a usuario
 */
@Serializable
data class AddBranchToUserInput(
    val branchId: String
)

// ============= RESPONSE TYPES =============

/**
 * Respuesta de autenticaci?n (login, register, loginWithGoogle)
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val user: User
)

/**
 * Wallet balance type
 */
@Serializable
data class WalletBalance(
    val local: Double,
    val usd: Double
)

/**
 * Modelo de Usuario actualizado seg?n la API
 * Incluye campos para OAuth (Google/Apple) y gesti?n de negocios/sucursales
 */
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val username: String,
    val phone: String? = null,
    val role: String, // merchant, customer, driver, admin
    val avatar: String? = null, // Path en S3
    val businessIds: List<String> = emptyList(), // IDs de negocios que posee/administra
    val branchIds: List<String> = emptyList(), // IDs de sucursales a las que tiene acceso
    val createdAt: String,
    val authProvider: String = "local", // "local", "google", "apple"
    val providerUserId: String? = null, // ID del usuario en el proveedor OAuth
    val applePrivateEmail: String? = null, // Email privado de Apple (si aplica)
    val wallet: WalletBalance,
    val walletStatus: String, // "active", "frozen", "closed"
    val avatarUrl: String? = null // Presigned URL para mostrar (generada por backend)
)

// ============= RESULT TYPES =============

/**
 * Resultado de operaciones de autenticaci?n
 */
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val code: String? = null) : AuthResult<Nothing>()
    data object Loading : AuthResult<Nothing>()
}

/**
 * Estado de UI para autenticaci?n
 */
@Serializable
data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

// ============= EXTENSION FUNCTIONS =============

/**
 * Verifica si el usuario tiene al menos un negocio
 */
fun User.hasBusiness(): Boolean = businessIds.isNotEmpty()

/**
 * Verifica si el usuario tiene acceso a alguna sucursal
 */
fun User.hasBranches(): Boolean = branchIds.isNotEmpty()

/**
 * Verifica si el usuario est? autenticado con Google
 */
fun User.isGoogleAuth(): Boolean = authProvider == "google"

/**
 * Verifica si el usuario est? autenticado con Apple
 */
fun User.isAppleAuth(): Boolean = authProvider == "apple"

/**
 * Verifica si es autenticaci?n local (email/password)
 */
fun User.isLocalAuth(): Boolean = authProvider == "local"
