package com.llego.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Modelos para Autenticación y Gestión de Usuarios
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
 * Respuesta de autenticación (login, register, loginWithGoogle)
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val user: User
)

/**
 * Modelo de Usuario actualizado según la API
 * Incluye campos para OAuth (Google/Apple) y gestión de negocios/sucursales
 */
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val role: String, // merchant, customer, driver, admin
    val avatar: String? = null, // Path en S3
    val businessIds: List<String> = emptyList(), // IDs de negocios que posee/administra
    val branchIds: List<String> = emptyList(), // IDs de sucursales a las que tiene acceso
    val createdAt: String,
    val authProvider: String = "local", // "local", "google", "apple"
    val providerUserId: String? = null, // ID del usuario en el proveedor OAuth
    val applePrivateEmail: String? = null, // Email privado de Apple (si aplica)
    val avatarUrl: String? = null // Presigned URL para mostrar (generada por backend)
)

// ============= RESULT TYPES =============

/**
 * Resultado de operaciones de autenticación
 */
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val code: String? = null) : AuthResult<Nothing>()
    data object Loading : AuthResult<Nothing>()
}

/**
 * Estado de UI para autenticación
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
 * Verifica si el usuario está autenticado con Google
 */
fun User.isGoogleAuth(): Boolean = authProvider == "google"

/**
 * Verifica si el usuario está autenticado con Apple
 */
fun User.isAppleAuth(): Boolean = authProvider == "apple"

/**
 * Verifica si es autenticación local (email/password)
 */
fun User.isLocalAuth(): Boolean = authProvider == "local"

/**
 * Obtiene el BusinessType del primer negocio (asumiendo un negocio por usuario)
 * DEPRECADO: No usar directamente. Usar AuthManager.getCurrentBusinessType() en su lugar
 * que consulta el Business real desde el backend
 *
 * @return null si el usuario no tiene negocios, BusinessType si tiene
 */
@Deprecated(
    "No usar directamente. Usar AuthManager.getCurrentBusinessType() que consulta datos reales del backend",
    ReplaceWith("authManager.getCurrentBusinessType()")
)
fun User.getBusinessType(): BusinessType? {
    // Retornar null si no hay negocios - esto fuerza a usar datos reales del backend
    if (businessIds.isEmpty()) return null

    // Si hay businessIds pero no tenemos el Business cargado, retornar null
    // para forzar la carga desde AuthManager
    return null
}

/**
 * Obtiene el BusinessProfile temporal del usuario
 * DEPRECADO: No usar directamente. Usar AuthManager.getBusinessProfile() en su lugar
 * que consulta el Business y Branch real desde el backend
 */
@Deprecated(
    "No usar directamente. Usar AuthManager.getBusinessProfile() que consulta datos reales del backend",
    ReplaceWith("authManager.getBusinessProfile()")
)
fun User.getBusinessProfile(): BusinessProfile? {
    if (businessIds.isEmpty()) return null

    // Temporal: retornamos mock data con BusinessType default
    return BusinessProfile(
        businessId = businessIds.first(),
        businessName = "Mi Negocio",
        businessType = BusinessType.RESTAURANT, // Default temporal
        address = "Dirección pendiente",
        city = "Ciudad",
        state = "Estado",
        zipCode = "00000",
        businessPhone = phone ?: "Sin teléfono",
        description = "Descripción pendiente",
        isVerified = false,
        operatingHours = OperatingHours(),
        deliveryRadius = 5.0,
        averageRating = 4.5,
        totalOrders = 0
    )
}

// ============= BUSINESS PROFILE (TEMPORARY) =============
// BusinessProfile temporal hasta implementar BusinessRepository
@Serializable
data class BusinessProfile(
    val businessId: String,
    val businessName: String,
    val businessType: BusinessType,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val businessPhone: String,
    val description: String? = null,
    val isVerified: Boolean = false,
    val operatingHours: OperatingHours,
    val deliveryRadius: Double = 5.0,
    val averageRating: Double = 0.0,
    val totalOrders: Int = 0
)

@Serializable
data class OperatingHours(
    val monday: DaySchedule = DaySchedule(),
    val tuesday: DaySchedule = DaySchedule(),
    val wednesday: DaySchedule = DaySchedule(),
    val thursday: DaySchedule = DaySchedule(),
    val friday: DaySchedule = DaySchedule(),
    val saturday: DaySchedule = DaySchedule(),
    val sunday: DaySchedule = DaySchedule()
)

@Serializable
data class DaySchedule(
    val isOpen: Boolean = true,
    val openTime: String = "09:00",
    val closeTime: String = "21:00"
)
