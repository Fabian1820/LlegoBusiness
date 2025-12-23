package com.llego.nichos.restaurant.data.model

import kotlinx.serialization.Serializable

/**
 * Modelo de Pedido para Restaurante
 */
@Serializable
data class Order(
    val id: String,
    val orderNumber: String,
    val customer: Customer,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val createdAt: String,
    val updatedAt: String,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val specialNotes: String? = null,
    val estimatedTime: Int? = null // minutos
    // deliveryType eliminado - solo domicilios
)

@Serializable
data class Customer(
    val name: String,
    val phone: String,
    val address: String? = null,
    val coordinates: Coordinates? = null
)

@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class OrderItem(
    val id: String = "",
    val menuItem: MenuItem,
    val quantity: Int,
    val specialInstructions: String? = null,
    val subtotal: Double
)

@Serializable
enum class ModificationType {
    UNCHANGED,
    QUANTITY_CHANGED,
    INSTRUCTIONS_CHANGED,
    ADDED,
    REMOVED
}

@Serializable
data class ModifiedOrderItem(
    val orderItem: OrderItem,
    val modificationType: ModificationType,
    val originalQuantity: Int? = null,
    val originalInstructions: String? = null
)

@Serializable
enum class OrderStatus {
    PENDING,        // Pendiente - acaba de llegar
    PREPARING,      // Procesando - se está preparando (neutral para todos los nichos)
    READY,          // Listo - estado final para pedidos completados
    CANCELLED       // Cancelado - rechazado
}

@Serializable
enum class PaymentMethod {
    CASH,           // Efectivo
    CARD,           // Tarjeta
    TRANSFER,       // Transferencia
    DIGITAL_WALLET  // Billetera digital
}

// Extension functions para UI
fun OrderStatus.getDisplayName(): String {
    return when (this) {
        OrderStatus.PENDING -> "Pendiente"
        OrderStatus.PREPARING -> "Procesando"
        OrderStatus.READY -> "Listo"
        OrderStatus.CANCELLED -> "Cancelado"
    }
}

fun OrderStatus.getColor(): Long {
    return when (this) {
        OrderStatus.PENDING -> 0xFFFF9800      // Naranja
        OrderStatus.PREPARING -> 0xFF9C27B0    // Morado
        OrderStatus.READY -> 0xFF4CAF50        // Verde claro
        OrderStatus.CANCELLED -> 0xFFF44336    // Rojo
    }
}

fun PaymentMethod.getDisplayName(): String {
    return when (this) {
        PaymentMethod.CASH -> "Efectivo"
        PaymentMethod.CARD -> "Tarjeta"
        PaymentMethod.TRANSFER -> "Transferencia"
        PaymentMethod.DIGITAL_WALLET -> "Billetera Digital"
    }
}
