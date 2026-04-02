package com.llego.business.orders.data.model

import kotlinx.serialization.Serializable

/**
 * Modelo de Pedido alineado con backend OrderType
 * Incluye todos los campos del schema GraphQL
 */
@Serializable
data class Order(
    val id: String,
    val orderNumber: String,
    val customerId: String,
    val branchId: String,
    val businessId: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val deliveryMode: String = "",
    val total: Double,
    val currency: String,
    val status: OrderStatus,
    val paymentMethod: String,
    val paymentStatus: PaymentStatus,
    val paidAt: String? = null,
    val deadlineAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val lastStatusAt: String,
    val deliveryPersonId: String? = null,
    val estimatedDeliveryTime: String? = null,
    val paymentId: String? = null,
    val rating: Int? = null,
    val ratingComment: String? = null,
    // Listas
    val items: List<OrderItem> = emptyList(),
    val discounts: List<OrderDiscount> = emptyList(),
    val timeline: List<OrderTimelineEntry> = emptyList(),
    val comments: List<OrderComment> = emptyList(),
    // Objetos anidados
    val deliveryAddress: DeliveryAddress,
    val pickupAddress: PickupAddress? = null,
    val customer: CustomerInfo? = null,
    val branch: BranchInfo? = null,
    val business: BusinessInfo? = null,
    val deliveryPerson: DeliveryPersonInfo? = null,
    // Campos computados del backend
    val isEditable: Boolean = false,
    val canCancel: Boolean = false,
    val estimatedMinutesRemaining: Int? = null
) {
    fun isPickupOrder(): Boolean = deliveryMode.equals("pickup", ignoreCase = true)

    fun isCashPaymentMethod(): Boolean = PaymentMethodClassifier.isCash(paymentMethod)

    fun requiresCompletedPaymentBeforePreparing(): Boolean = !isCashPaymentMethod()

    fun canStartPreparingAccordingToPaymentRule(): Boolean =
        isCashPaymentMethod() || paymentStatus == PaymentStatus.COMPLETED
}

private object PaymentMethodClassifier {
    private val cashPaymentMethods = setOf(
        "cash",
        "efectivo",
        "contraentrega",
        "contra_entrega",
        "cash_on_delivery",
        "cod"
    )

    private val nonCashPaymentMethods = setOf(
        "transfer",
        "transferencia",
        "card",
        "tarjeta",
        "pasarela",
        "gateway",
        "online",
        "wallet",
        "qvapay",
        "enzona",
        "stripe",
        "bank"
    )

    fun isCash(rawPaymentMethod: String): Boolean {
        val normalized = normalize(rawPaymentMethod)
        if (normalized in cashPaymentMethods) return true
        if (normalized in nonCashPaymentMethods) return false
        if (cashPaymentMethods.any { normalized.contains(it) }) return true
        if (nonCashPaymentMethods.any { normalized.contains(it) }) return false

        // Ambiguous values are treated as non-cash for safety.
        return false
    }

    private fun normalize(rawPaymentMethod: String): String =
        rawPaymentMethod
            .lowercase()
            .replace("-", "_")
            .replace(" ", "_")
            .trim()
}

/**
 * Información básica de la sucursal
 */
@Serializable
data class BranchInfo(
    val id: String,
    val businessId: String,
    val name: String,
    val address: String? = null,
    val phone: String,
    val avatarUrl: String? = null,
    val coverUrl: String? = null
)

/**
 * Información básica del negocio
 */
@Serializable
data class BusinessInfo(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val description: String? = null
)
