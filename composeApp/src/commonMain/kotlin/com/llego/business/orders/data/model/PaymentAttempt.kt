package com.llego.business.orders.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentAttempt(
    val id: String,
    val orderId: String,
    val paymentMethodId: String,
    val status: PaymentAttemptStatus,
    val sendsSmsNotification: Boolean = false,
    val proofUrl: String? = null,
    val customerConfirmedAt: String? = null,
    val businessConfirmedAt: String? = null,
    val completedAt: String? = null,
    val failedAt: String? = null,
    val failedReason: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
enum class PaymentAttemptStatus {
    PENDING,
    PROCESSING,
    AWAITING_PROOF,
    AWAITING_BUSINESS,
    AWAITING_DELIVERY,
    COMPLETED,
    FAILED,
    EXPIRED,
    CANCELLED,
    DISPUTED,
    REFUND_REQUESTED,
    REFUND_PROCESSING,
    REFUNDED
}
