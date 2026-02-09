package com.llego.business.delivery.data.model

data class LinkedDeliveryPerson(
    val id: String,
    val name: String,
    val phone: String,
    val rating: Double,
    val totalDeliveries: Int,
    val vehicleType: String,
    val vehiclePlate: String? = null,
    val profileImageUrl: String? = null,
    val isOnline: Boolean
)

enum class DeliveryRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    fun toLabel(): String = when (this) {
        PENDING -> "Pendiente"
        ACCEPTED -> "Aceptada"
        REJECTED -> "Rechazada"
    }
}

data class BranchDeliveryRequest(
    val id: String,
    val deliveryPersonId: String,
    val branchId: String,
    val status: DeliveryRequestStatus,
    val message: String? = null,
    val respondedBy: String? = null,
    val respondedAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val deliveryPerson: LinkedDeliveryPerson? = null
)

data class LinkedDriverSummary(
    val requestId: String,
    val linkedAt: String?,
    val deliveryPerson: LinkedDeliveryPerson
)

fun String.toVehicleLabel(): String = when (uppercase()) {
    "MOTO" -> "Moto"
    "BICICLETA" -> "Bicicleta"
    "AUTO", "CARRO" -> "Auto"
    "A_PIE", "CAMINANDO" -> "A pie"
    else -> replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}
