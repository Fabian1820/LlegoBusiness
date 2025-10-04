package com.llego.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Modelos de datos para el sistema de autenticación Llego
 * Maneja diferentes tipos de negocios (nichos): Restaurantes, Supermercados, Farmacias, etc.
 * La app Driver es independiente y no forma parte de este modelo
 */

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val businessType: BusinessType, // Tipo de nicho del negocio
    val profileImage: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val businessProfile: BusinessProfile? = null
)

@Serializable
enum class UserType {
    BUSINESS,
    @Deprecated("Driver app is now independent", ReplaceWith("BusinessType"))
    DRIVER
}

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
    val taxId: String? = null,
    val description: String? = null,
    val logo: String? = null,
    val banner: String? = null,
    val isVerified: Boolean = false,
    val operatingHours: OperatingHours,
    val deliveryRadius: Double = 5.0, // en kilómetros
    val averageRating: Double = 0.0,
    val totalOrders: Int = 0
)

@Serializable
enum class BusinessType {
    RESTAURANT,
    GROCERY,
    PHARMACY,
    ELECTRONICS,
    CLOTHING,
    OTHER
}

@Serializable
data class OperatingHours(
    val monday: DaySchedule,
    val tuesday: DaySchedule,
    val wednesday: DaySchedule,
    val thursday: DaySchedule,
    val friday: DaySchedule,
    val saturday: DaySchedule,
    val sunday: DaySchedule
)

@Serializable
data class DaySchedule(
    val isOpen: Boolean = true,
    val openTime: String = "09:00", // formato HH:mm
    val closeTime: String = "21:00"
)

@Serializable
data class DriverProfile(
    val driverId: String,
    val licenseNumber: String,
    val vehicleType: VehicleType,
    val vehiclePlate: String,
    val vehicleModel: String? = null,
    val vehicleColor: String? = null,
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val currentLocation: Location? = null,
    val averageRating: Double = 0.0,
    val totalDeliveries: Int = 0,
    val documentsStatus: DocumentsStatus = DocumentsStatus()
)

@Serializable
enum class VehicleType {
    MOTORCYCLE,
    CAR,
    BICYCLE,
    WALKING
}

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

@Serializable
data class DocumentsStatus(
    val driverLicense: DocumentStatus = DocumentStatus.PENDING,
    val vehicleRegistration: DocumentStatus = DocumentStatus.PENDING,
    val insurance: DocumentStatus = DocumentStatus.PENDING,
    val backgroundCheck: DocumentStatus = DocumentStatus.PENDING
)

@Serializable
enum class DocumentStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}

/**
 * Datos de autenticación
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val businessType: BusinessType // Ahora usa el tipo de nicho en lugar de userType
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: User? = null
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
    val businessType: BusinessType, // Ahora usa el tipo de nicho
    val businessProfile: BusinessRegistration? = null
)

@Serializable
data class BusinessRegistration(
    val businessName: String,
    val businessType: BusinessType,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val businessPhone: String,
    val taxId: String? = null,
    val description: String? = null
)

@Serializable
data class DriverRegistration(
    val licenseNumber: String,
    val vehicleType: VehicleType,
    val vehiclePlate: String,
    val vehicleModel: String? = null,
    val vehicleColor: String? = null
)

@Serializable
data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)