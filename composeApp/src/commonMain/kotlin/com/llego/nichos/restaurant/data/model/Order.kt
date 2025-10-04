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
    val deliveryType: DeliveryType,
    val specialNotes: String? = null,
    val estimatedTime: Int? = null // minutos
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
    val menuItem: MenuItem,
    val quantity: Int,
    val specialInstructions: String? = null,
    val subtotal: Double
)

@Serializable
enum class OrderStatus {
    PENDING,        // Pendiente - acaba de llegar
    ACCEPTED,       // Aceptado - confirmado por el restaurante
    PREPARING,      // En Elaboración - se está preparando
    READY,          // Listo - esperando entrega
    IN_DELIVERY,    // En Camino - el repartidor lo recogió
    DELIVERED,      // Entregado - completado
    CANCELLED       // Cancelado
}

@Serializable
enum class PaymentMethod {
    CASH,           // Efectivo
    CARD,           // Tarjeta
    TRANSFER,       // Transferencia
    DIGITAL_WALLET  // Billetera digital
}

@Serializable
enum class DeliveryType {
    DELIVERY,       // A domicilio
    PICKUP          // Recoger en restaurante
}

// Extension functions para UI
fun OrderStatus.getDisplayName(): String {
    return when (this) {
        OrderStatus.PENDING -> "Pendiente"
        OrderStatus.ACCEPTED -> "Aceptado"
        OrderStatus.PREPARING -> "En Elaboración"
        OrderStatus.READY -> "Listo"
        OrderStatus.IN_DELIVERY -> "En Camino"
        OrderStatus.DELIVERED -> "Entregado"
        OrderStatus.CANCELLED -> "Cancelado"
    }
}

fun OrderStatus.getColor(): Long {
    return when (this) {
        OrderStatus.PENDING -> 0xFFFF9800      // Naranja
        OrderStatus.ACCEPTED -> 0xFF2196F3     // Azul
        OrderStatus.PREPARING -> 0xFF9C27B0    // Morado
        OrderStatus.READY -> 0xFF4CAF50        // Verde claro
        OrderStatus.IN_DELIVERY -> 0xFF00BCD4  // Cyan
        OrderStatus.DELIVERED -> 0xFF4CAF50    // Verde
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

fun DeliveryType.getDisplayName(): String {
    return when (this) {
        DeliveryType.DELIVERY -> "A Domicilio"
        DeliveryType.PICKUP -> "Recoger en Restaurante"
    }
}