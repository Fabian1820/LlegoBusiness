package com.llego.shared.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Modelos de dominio para Business y Branches
 * Basados en businesses-branches-api.md del backend
 */

// ============= BUSINESS MODELS =============

/**
 * Modelo de negocio (Business)
 */
@Serializable
data class Business(
    val id: String,
    val name: String,
    val type: String,  // "restaurant", "market", "agromarket", "clothing_store", "pharmacy"
    val ownerId: String,
    val globalRating: Double = 0.0,
    val avatar: String? = null,
    val coverImage: String? = null,
    val description: String? = null,
    val socialMedia: Map<String, String>? = null,  // JSON map
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String,
    val avatarUrl: String? = null,
    val coverUrl: String? = null
)

/**
 * Modelo de sucursal (Branch)
 */
@Serializable
data class Branch(
    val id: String,
    val businessId: String,
    val name: String,
    val address: String? = null,
    val coordinates: Coordinates,
    val phone: String,
    val schedule: Map<String, String> = emptyMap(),  // JSON map
    val managerIds: List<String> = emptyList(),
    val status: String = "active",  // "active", "inactive", "pending"
    val avatar: String? = null,
    val coverImage: String? = null,
    val deliveryRadius: Double? = null,
    val facilities: List<String> = emptyList(),
    val createdAt: String,
    val avatarUrl: String? = null,
    val coverUrl: String? = null
)

/**
 * Coordenadas geográficas (GeoJSON Point)
 */
@Serializable
data class Coordinates(
    val type: String = "Point",
    val coordinates: List<Double>  // [longitude, latitude]
) {
    val latitude: Double get() = coordinates.getOrNull(1) ?: 0.0
    val longitude: Double get() = coordinates.getOrNull(0) ?: 0.0

    companion object {
        fun fromLatLng(lat: Double, lng: Double): Coordinates {
            return Coordinates(
                type = "Point",
                coordinates = listOf(lng, lat)  // GeoJSON es [lng, lat]
            )
        }
    }
}

// ============= INPUT MODELS =============

/**
 * Input para crear un negocio
 */
@Serializable
data class CreateBusinessInput(
    val name: String,
    val type: String,
    val avatar: String? = null,
    val coverImage: String? = null,
    val description: String? = null,
    val tags: List<String>? = null
)

/**
 * Input para actualizar un negocio
 */
@Serializable
data class UpdateBusinessInput(
    val name: String? = null,
    val avatar: String? = null,
    val coverImage: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val isActive: Boolean? = null
)

/**
 * Input para registrar una sucursal durante creación de negocio
 */
@Serializable
data class RegisterBranchInput(
    val name: String,
    val coordinates: CoordinatesInput,
    val phone: String,
    @Contextual
    val schedule: Any,  // JSON - puede ser Map<String, String> o Map<String, List<String>>
    val address: String? = null,
    val avatar: String? = null,
    val coverImage: String? = null,
    val deliveryRadius: Double? = null,
    val facilities: List<String>? = null
)

/**
 * Input para crear una nueva sucursal
 */
@Serializable
data class CreateBranchInput(
    val businessId: String,
    val name: String,
    val coordinates: CoordinatesInput,
    val phone: String,
    @Contextual
    val schedule: Any,  // JSON - puede ser Map<String, String> o Map<String, List<String>>
    val address: String? = null,
    val avatar: String? = null,
    val coverImage: String? = null,
    val deliveryRadius: Double? = null,
    val facilities: List<String>? = null
)

/**
 * Input para actualizar una sucursal
 */
@Serializable
data class UpdateBranchInput(
    val name: String? = null,
    val coordinates: CoordinatesInput? = null,
    val phone: String? = null,
    val schedule: Map<String, String>? = null,
    val address: String? = null,
    val avatar: String? = null,
    val coverImage: String? = null,
    val status: String? = null,
    val deliveryRadius: Double? = null,
    val facilities: List<String>? = null
)

/**
 * Input para coordenadas
 */
@Serializable
data class CoordinatesInput(
    val lat: Double,
    val lng: Double
)

// ============= RESULT TYPE =============

/**
 * Resultado de operaciones de Business
 * Sealed class para manejo type-safe de resultados
 */
sealed class BusinessResult<out T> {
    data class Success<T>(val data: T) : BusinessResult<T>()
    data class Error(val message: String, val code: String? = null) : BusinessResult<Nothing>()
    data object Loading : BusinessResult<Nothing>()
}

// ============= EXTENSION FUNCTIONS =============

/**
 * Convierte Business a BusinessProfile para compatibilidad
 */
fun Business.toBusinessProfile(): BusinessProfile {
    return BusinessProfile(
        businessId = id,
        businessName = name,
        businessType = type.toBusinessType() ?: BusinessType.RESTAURANT,
        address = "Dirección pendiente",  // Se llenará desde Branch
        city = "Ciudad",
        state = "Estado",
        zipCode = "00000",
        businessPhone = "Teléfono pendiente",  // Se llenará desde Branch
        description = description,
        isVerified = true,
        operatingHours = OperatingHours(),  // Se llenará desde Branch schedule
        deliveryRadius = 5.0,  // Se llenará desde Branch
        averageRating = globalRating,
        totalOrders = 0  // TODO: Obtener de estadísticas
    )
}

/**
 * Convierte Business + Branch a BusinessProfile completo
 */
fun Business.toBusinessProfile(branch: Branch?): BusinessProfile {
    return BusinessProfile(
        businessId = id,
        businessName = name,
        businessType = type.toBusinessType() ?: BusinessType.RESTAURANT,
        address = branch?.address ?: "Sin dirección",
        city = "Ciudad",  // TODO: Extraer de address
        state = "Estado",
        zipCode = "00000",
        businessPhone = branch?.phone ?: "Sin teléfono",
        description = description,
        isVerified = true,
        operatingHours = branch?.schedule?.toOperatingHours() ?: OperatingHours(),
        deliveryRadius = branch?.deliveryRadius ?: 5.0,
        averageRating = globalRating,
        totalOrders = 0
    )
}

/**
 * Convierte Map de schedule a OperatingHours
 */
fun Map<String, String>.toOperatingHours(): OperatingHours {
    // TODO: Parsear el schedule JSON del backend
    return OperatingHours()
}
