package com.llego.business.settings.data.model

import kotlinx.serialization.Serializable

/**
 * Configuración y reglas del Restaurante
 */
@Serializable
data class BusinessSettings(
    val businessHours: BusinessHours,
    val acceptedPaymentMethods: List<PaymentMethod>,
    val deliverySettings: DeliverySettings,
    val orderSettings: OrderSettings,
    val notifications: NotificationSettings
)

@Serializable
data class BusinessHours(
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
    val isOpen: Boolean,
    val openTime: String,  // "09:00"
    val closeTime: String  // "22:00"
)

@Serializable
data class DeliverySettings(
    val isDeliveryEnabled: Boolean,
    val isPickupEnabled: Boolean,
    val deliveryRadius: Double, // km
    val minimumOrderAmount: Double,
    val deliveryFee: Double,
    val freeDeliveryThreshold: Double? = null, // null = sin delivery gratis
    val estimatedDeliveryTime: Int = 30 // minutos
)

@Serializable
data class OrderSettings(
    val autoAcceptOrders: Boolean = false,
    val maxOrdersPerHour: Int? = null,
    val prepTimeBuffer: Int = 5, // minutos extra de preparación
    val allowScheduledOrders: Boolean = true,
    val cancelationPolicy: String = "Los pedidos pueden cancelarse hasta 10 minutos después de realizados"
)

@Serializable
data class NotificationSettings(
    val newOrderSound: Boolean = true,
    val orderStatusUpdates: Boolean = true,
    val customerMessages: Boolean = true,
    val dailySummary: Boolean = true
)

// Extension functions
fun DaySchedule.getDisplayText(): String {
    return if (isOpen) {
        "$openTime - $closeTime"
    } else {
        "Cerrado"
    }
}

fun BusinessHours.getCurrentDaySchedule(): DaySchedule {
    // TODO: Implementar lógica real para obtener el día actual
    return monday // Por ahora retorna lunes
}

fun BusinessHours.isCurrentlyOpen(): Boolean {
    // TODO: Implementar lógica real
    return true
}
