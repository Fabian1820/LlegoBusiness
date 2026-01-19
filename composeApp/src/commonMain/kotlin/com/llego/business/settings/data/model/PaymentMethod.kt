package com.llego.business.settings.data.model

import kotlinx.serialization.Serializable

/**
 * Métodos de pago aceptados por el negocio
 */
@Serializable
enum class PaymentMethod {
    CASH,           // Efectivo
    CARD,           // Tarjeta
    TRANSFER,       // Transferencia
    DIGITAL_WALLET  // Billetera digital
}

/**
 * Obtiene el nombre para mostrar del método de pago
 */
fun PaymentMethod.getDisplayName(): String = when (this) {
    PaymentMethod.CASH -> "Efectivo"
    PaymentMethod.CARD -> "Tarjeta"
    PaymentMethod.TRANSFER -> "Transferencia"
    PaymentMethod.DIGITAL_WALLET -> "Billetera Digital"
}
