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
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String,
    val approvalStatus: String = "approved",
    val rejectionReason: String? = null,
    val avatarUrl: String? = null,
    val avatarUrlBaja: String? = null,
    val avatarUrlAlta: String? = null
)

/**
 * Modelo de negocio con sucursales anidadas (getMyBusinessesWithBranches)
 * Incluye metadata de permisos del usuario actual
 */
@Serializable
data class BusinessWithBranches(
    val id: String,
    val name: String,
    val ownerId: String,
    val isOwner: Boolean = false,
    val role: String = "employee",
    val globalRating: Double = 0.0,
    val avatar: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String,
    val approvalStatus: String = "approved",
    val rejectionReason: String? = null,
    val avatarUrl: String? = null,
    val avatarUrlBaja: String? = null,
    val avatarUrlAlta: String? = null,
    val branches: List<Branch> = emptyList()
) {
    fun toBusiness(): Business {
        return Business(
            id = id,
            name = name,
            ownerId = ownerId,
            globalRating = globalRating,
            avatar = avatar,
            description = description,
            tags = tags,
            isActive = isActive,
            createdAt = createdAt,
            approvalStatus = approvalStatus,
            rejectionReason = rejectionReason,
            avatarUrl = avatarUrl,
            avatarUrlBaja = avatarUrlBaja,
            avatarUrlAlta = avatarUrlAlta
        )
    }
}

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
    val pickupEnabled: Boolean = false,
    val useAppMessaging: Boolean = true,
    val vehicles: List<BranchVehicle> = emptyList(),
    val paymentMethodIds: List<String> = emptyList(),  // IDs of accepted payment methods
    val managerIds: List<String> = emptyList(),
    val isActive: Boolean = true,
    val avatar: String? = null,
    val coverImage: String? = null,
    val socialMedia: Map<String, String>? = null,
    val accounts: List<TransferAccount> = emptyList(),
    val qrPayments: List<QrPayment> = emptyList(),
    val phones: List<TransferPhone> = emptyList(),
    val createdAt: String,
    val avatarUrl: String? = null,
    val avatarUrlBaja: String? = null,
    val avatarUrlAlta: String? = null,
    val coverUrl: String? = null,
    val coverUrlBaja: String? = null,
    val coverUrlAlta: String? = null,
    val wallet: WalletBalance = WalletBalance(local = 0.0, usd = 0.0),  // Balance de la billetera
    val walletStatus: String = "active",  // Estado de la billetera: "active", "suspended", etc.
    val exchangeRate: Int? = null,
    val catalogOnly: Boolean = false
)

/**
 * Enum de tipos de sucursal (BranchTipo) según backend
 * Sincronizado con schema GraphQL
 */
@Serializable
enum class BranchTipo {
    RESTAURANTE,
    DULCERIA,
    TIENDA,
    PERFUMERIA
}

@Serializable
enum class BranchVehicle {
    MOTO,
    BICICLETA,
    CARRO,
    CAMIONETA,
    CAMINANDO
}

/**
 * Convierte BranchTipo a string legible
 */
fun BranchTipo.toDisplayName(): String = when (this) {
    BranchTipo.RESTAURANTE -> "Restaurante"
    BranchTipo.DULCERIA -> "Dulcería"
    BranchTipo.TIENDA -> "Tienda"
    BranchTipo.PERFUMERIA -> "Perfumería"
}

fun BranchVehicle.toDisplayName(): String = when (this) {
    BranchVehicle.MOTO -> "Moto"
    BranchVehicle.BICICLETA -> "Bicicleta"
    BranchVehicle.CARRO -> "Carro"
    BranchVehicle.CAMIONETA -> "Camioneta"
    BranchVehicle.CAMINANDO -> "Caminando"
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

/**
 * Payment method model
 */
@Serializable
data class PaymentMethod(
    val id: String,
    val name: String,      // Display name from backend, e.g., "Transferencia Bancaria Cuba"
    val currency: String,  // e.g., "CUP", "USD"
    val method: String     // e.g., "tarjeta", "efectivo", "transferencia"
)

@Serializable
data class TransferAccount(
    val cardNumber: String,
    val confirmPhone: String,
    val cardHolderName: String? = null,
    val pagoQr: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class QrPayment(
    val value: String,
    val isActive: Boolean = true
)

@Serializable
data class TransferPhone(
    val phone: String,
    val isActive: Boolean = true
)

/**
 * Returns a display name for the payment method using the backend-provided name.
 */
fun PaymentMethod.toDisplayName(): String = name

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
    val pickupEnabled: Boolean = false,
    val useAppMessaging: Boolean = true,
    val vehicles: List<BranchVehicle> = emptyList(),
    val paymentMethodIds: List<String>,  // Requerido: IDs of accepted payment methods
    val address: String? = null,
    val managerIds: List<String>? = null,
    val avatar: String? = null,
    val coverImage: String? = null,
    val isActive: Boolean = true,
    val socialMedia: Map<String, String>? = null,
    val exchangeRate: Int? = null,
    val accounts: List<TransferAccount>? = null,
    val qrPayments: List<QrPayment>? = null,
    val phones: List<TransferPhone>? = null,
    val catalogOnly: Boolean = false
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
    val useAppMessaging: Boolean = true,
    val vehicles: List<BranchVehicle> = emptyList(),
    val paymentMethodIds: List<String>,  // Requerido: IDs of accepted payment methods
    val address: String? = null,
    val managerIds: List<String>? = null,
    val avatar: String? = null,
    val coverImage: String? = null,
    val isActive: Boolean = true,
    val socialMedia: Map<String, String>? = null,
    val exchangeRate: Int? = null,
    val accounts: List<TransferAccount>? = null,
    val qrPayments: List<QrPayment>? = null,
    val phones: List<TransferPhone>? = null,
    val catalogOnly: Boolean = false
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
    val pickupEnabled: Boolean? = null,
    val useAppMessaging: Boolean? = null,
    val vehicles: List<BranchVehicle>? = null,
    val paymentMethodIds: List<String>? = null,  // IDs of accepted payment methods
    val address: String? = null,
    val avatar: String? = null,
    val coverImage: String? = null,
    val isActive: Boolean? = null,
    val socialMedia: Map<String, String>? = null,
    val exchangeRate: Int? = null,
    val managerIds: List<String>? = null,
    val accounts: List<TransferAccount>? = null,
    val qrPayments: List<QrPayment>? = null,
    val phones: List<TransferPhone>? = null,
    val catalogOnly: Boolean? = null
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
