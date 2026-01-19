package com.llego.business.orders.data.model

import kotlinx.serialization.Serializable

/**
 * Información del cliente alineada con backend UserType (campos relevantes para pedidos)
 */
@Serializable
data class CustomerInfo(
    val id: String,
    val name: String,
    val phone: String? = null,
    val avatarUrl: String? = null
)

/**
 * Información del repartidor alineada con backend DeliveryPersonType
 */
@Serializable
data class DeliveryPersonInfo(
    val id: String,
    val name: String,
    val phone: String,
    val rating: Double,
    val totalDeliveries: Int,
    val vehicleType: VehicleType,
    val vehiclePlate: String? = null,
    val profileImageUrl: String? = null,
    val isOnline: Boolean,
    val currentLocation: Coordinates? = null
)

/**
 * Estadísticas de pedidos alineadas con backend OrderStatsType
 */
@Serializable
data class OrderStats(
    val totalOrders: Int,
    val completedOrders: Int,
    val cancelledOrders: Int,
    val totalRevenue: Double,
    val averageOrderValue: Double,
    val averageDeliveryTime: Int
) {
    /**
     * Tasa de completitud (pedidos completados / total)
     */
    val completionRate: Double
        get() = if (totalOrders > 0) completedOrders.toDouble() / totalOrders else 0.0
    
    /**
     * Tasa de cancelación (pedidos cancelados / total)
     */
    val cancellationRate: Double
        get() = if (totalOrders > 0) cancelledOrders.toDouble() / totalOrders else 0.0
    
    /**
     * Pedidos activos (total - completados - cancelados)
     */
    val activeOrders: Int
        get() = totalOrders - completedOrders - cancelledOrders
}

/**
 * Tracking de pedido alineado con backend OrderTrackingType
 */
@Serializable
data class OrderTracking(
    val order: Order,
    val storeLocation: Coordinates,
    val deliveryLocation: Coordinates,
    val deliveryPersonLocation: Coordinates? = null,
    val estimatedMinutes: Int? = null,
    val distanceKm: Double? = null,
    val routePolyline: String? = null
)

/**
 * Actualización de ubicación de entrega alineada con backend DeliveryLocationUpdateType
 */
@Serializable
data class DeliveryLocationUpdate(
    val orderId: String,
    val location: Coordinates,
    val timestamp: String,
    val estimatedMinutesRemaining: Int? = null,
    val distanceRemainingKm: Double? = null
)
