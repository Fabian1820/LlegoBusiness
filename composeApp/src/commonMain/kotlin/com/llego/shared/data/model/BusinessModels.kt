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
 * IMPORTANTE: Ya no tiene campo 'type' - La diferenciaci?n se hace por Branch.tipos
 */
@Serializable
data class Business(
    val id: String,
    val name: String,
    val ownerId: String,
    val globalRating: Double = 0.0,
    val avatar: String? = null,
    val description: String? = null,
    val socialMedia: Map<String, String>? = null,  // JSON map
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String,
    val avatarUrl: String? = null
)

/**
 * Modelo de sucursal (Branch)
 * Alineado con backend: tipos campo requerido
 */
@Serializable
data class Branch(
    val id: String,
    val businessId: String,
    val name: String,
    val address: String? = null,
    val coordinates: Coordinates,
    val phone: String,
    val schedule: Map<String, List<String>> = emptyMap(),  // JSON map: {"mon": ["08:00-12:00", "14:00-20:00"]}
    val tipos: List<BranchTipo> = emptyList(),  // RESTAURANTE, DULCERIA, TIENDA
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
 * Enum de tipos de sucursal (BranchTipo) seg?n backend
 * MVP: RESTAURANTE, TIENDA, DULCERIA
 */
@Serializable
enum class BranchTipo {
    RESTAURANTE,
    DULCERIA,
    TIENDA
}

/**
 * Convierte BranchTipo a string legible
 */
fun BranchTipo.toDisplayName(): String = when (this) {
    BranchTipo.RESTAURANTE -> "Restaurante"
    BranchTipo.DULCERIA -> "Dulcer?a"
    BranchTipo.TIENDA -> "Tienda"
}

/**
 * Coordenadas geogr?ficas (GeoJSON Point)
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
 * Input para crear un negocio (alineado con backend)
 * IMPORTANTE: Ya no tiene campo 'type'
 */
@Serializable
data class CreateBusinessInput(
    val name: String,
    val avatar: String? = null,
    val description: String? = null,
    val socialMedia: Map<String, String>? = null,  // {"facebook": "...", "instagram": "..."}
    val tags: List<String>? = null
)

/**
 * Input para actualizar un negocio (alineado con backend)
 * IMPORTANTE: Ya no tiene campo 'type'
 */
@Serializable
data class UpdateBusinessInput(
    val name: String? = null,
    val avatar: String? = null,
    val description: String? = null,
    val socialMedia: Map<String, String>? = null,
    val tags: List<String>? = null,
    val isActive: Boolean? = null
)

/**
 * Input para registrar una sucursal durante creaci?n de negocio (alineado con backend)
 */
@Serializable
data class RegisterBranchInput(
    val name: String,
    val coordinates: CoordinatesInput,
    val phone: String,
    @Contextual
    val schedule: Any,  // JSON - Map<String, List<String>>: {"mon": ["08:00-12:00", "14:00-20:00"]}
    val tipos: List<BranchTipo>,  // Requerido: RESTAURANTE, DULCERIA, TIENDA
    val address: String? = null,
    val managerIds: List<String>? = null,
    val avatar: String? = null,
    val coverImage: String? = null,
    val deliveryRadius: Double? = null,
    val facilities: List<String>? = null
)

/**
 * Input para crear una nueva sucursal (alineado con backend)
 */
@Serializable
data class CreateBranchInput(
    val businessId: String,
    val name: String,
    val coordinates: CoordinatesInput,
    val phone: String,
    @Contextual
    val schedule: Any,  // JSON - Map<String, List<String>>: {"mon": ["08:00-12:00", "14:00-20:00"]}
    val tipos: List<BranchTipo>,  // Requerido: RESTAURANTE, DULCERIA, TIENDA
    val address: String? = null,
    val managerIds: List<String>? = null,
    val avatar: String? = null,
    val coverImage: String? = null,
    val deliveryRadius: Double? = null,
    val facilities: List<String>? = null
)

/**
 * Input para actualizar una sucursal (alineado con backend)
 */
@Serializable
data class UpdateBranchInput(
    val name: String? = null,
    val coordinates: CoordinatesInput? = null,
    val phone: String? = null,
    val schedule: Map<String, List<String>>? = null,  // {"mon": ["08:00-12:00", "14:00-20:00"]}
    val tipos: List<BranchTipo>? = null,
    val address: String? = null,
    val avatar: String? = null,
    val coverImage: String? = null,
    val status: String? = null,
    val deliveryRadius: Double? = null,
    val facilities: List<String>? = null,
    val managerIds: List<String>? = null
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
