package com.llego.business.orders.data.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Estado del pedido alineado con backend OrderStatusEnum
 */
@Serializable
enum class OrderStatus {
    @SerialName("PENDING_ACCEPTANCE")
    PENDING_ACCEPTANCE,
    
    @SerialName("MODIFIED_BY_STORE")
    MODIFIED_BY_STORE,
    
    @SerialName("ACCEPTED")
    ACCEPTED,
    
    @SerialName("PREPARING")
    PREPARING,
    
    @SerialName("READY_FOR_PICKUP")
    READY_FOR_PICKUP,
    
    @SerialName("ON_THE_WAY")
    ON_THE_WAY,
    
    @SerialName("DELIVERED")
    DELIVERED,
    
    @SerialName("CANCELLED")
    CANCELLED;

    fun getDisplayName(): String = when (this) {
        PENDING_ACCEPTANCE -> "Pendiente"
        MODIFIED_BY_STORE -> "Modificado"
        ACCEPTED -> "Aceptado"
        PREPARING -> "Preparando"
        READY_FOR_PICKUP -> "Listo"
        ON_THE_WAY -> "En camino"
        DELIVERED -> "Entregado"
        CANCELLED -> "Cancelado"
    }

    fun getColor(): Color = when (this) {
        PENDING_ACCEPTANCE -> Color(0xFFFF9800)  // Naranja
        MODIFIED_BY_STORE -> Color(0xFF2196F3)   // Azul
        ACCEPTED -> Color(0xFF8BC34A)            // Verde claro
        PREPARING -> Color(0xFF9C27B0)           // Morado
        READY_FOR_PICKUP -> Color(0xFF4CAF50)    // Verde
        ON_THE_WAY -> Color(0xFF00BCD4)          // Cyan
        DELIVERED -> Color(0xFF4CAF50)           // Verde
        CANCELLED -> Color(0xFFF44336)           // Rojo
    }
}

/**
 * Estado del pago alineado con backend PaymentStatusEnum
 */
@Serializable
enum class PaymentStatus {
    @SerialName("PENDING")
    PENDING,
    
    @SerialName("VALIDATED")
    VALIDATED,
    
    @SerialName("COMPLETED")
    COMPLETED,
    
    @SerialName("FAILED")
    FAILED;

    fun getDisplayName(): String = when (this) {
        PENDING -> "Pendiente"
        VALIDATED -> "Validado"
        COMPLETED -> "Completado"
        FAILED -> "Fallido"
    }

    fun getColor(): Color = when (this) {
        PENDING -> Color(0xFFFF9800)    // Naranja
        VALIDATED -> Color(0xFF2196F3)  // Azul
        COMPLETED -> Color(0xFF4CAF50)  // Verde
        FAILED -> Color(0xFFF44336)     // Rojo
    }
}

/**
 * Actor del pedido alineado con backend OrderActorEnum
 */
@Serializable
enum class OrderActor {
    @SerialName("CUSTOMER")
    CUSTOMER,
    
    @SerialName("BUSINESS")
    BUSINESS,
    
    @SerialName("SYSTEM")
    SYSTEM,
    
    @SerialName("DELIVERY")
    DELIVERY;

    fun getDisplayName(): String = when (this) {
        CUSTOMER -> "Cliente"
        BUSINESS -> "Negocio"
        SYSTEM -> "Sistema"
        DELIVERY -> "Repartidor"
    }

    fun getColor(): Color = when (this) {
        CUSTOMER -> Color(0xFF2196F3)   // Azul
        BUSINESS -> Color(0xFF9C27B0)   // Morado
        SYSTEM -> Color(0xFF607D8B)     // Gris azulado
        DELIVERY -> Color(0xFF4CAF50)   // Verde
    }
}

/**
 * Tipo de descuento alineado con backend DiscountTypeEnum
 */
@Serializable
enum class DiscountType {
    @SerialName("PREMIUM")
    PREMIUM,
    
    @SerialName("LEVEL")
    LEVEL,
    
    @SerialName("PROMO")
    PROMO;

    fun getDisplayName(): String = when (this) {
        PREMIUM -> "Premium"
        LEVEL -> "Nivel"
        PROMO -> "Promoción"
    }

    fun getColor(): Color = when (this) {
        PREMIUM -> Color(0xFFFFD700)    // Dorado
        LEVEL -> Color(0xFF9C27B0)      // Morado
        PROMO -> Color(0xFF4CAF50)      // Verde
    }
}

/**
 * Tipo de vehículo alineado con backend VehicleTypeEnum
 */
@Serializable
enum class VehicleType {
    @SerialName("MOTO")
    MOTO,
    
    @SerialName("BICICLETA")
    BICICLETA,
    
    @SerialName("AUTO")
    AUTO,
    
    @SerialName("A_PIE")
    A_PIE;

    fun getDisplayName(): String = when (this) {
        MOTO -> "Moto"
        BICICLETA -> "Bicicleta"
        AUTO -> "Auto"
        A_PIE -> "A pie"
    }

    fun getColor(): Color = when (this) {
        MOTO -> Color(0xFFFF5722)       // Naranja oscuro
        BICICLETA -> Color(0xFF4CAF50)  // Verde
        AUTO -> Color(0xFF2196F3)       // Azul
        A_PIE -> Color(0xFF9E9E9E)      // Gris
    }
}
