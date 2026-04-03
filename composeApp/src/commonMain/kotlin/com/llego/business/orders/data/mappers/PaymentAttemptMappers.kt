package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.PaymentAttempt
import com.llego.business.orders.data.model.PaymentAttemptStatus
import com.llego.multiplatform.graphql.ActivePaymentAttemptQuery
import com.llego.multiplatform.graphql.ConfirmPaymentReceivedMutation
import com.llego.multiplatform.graphql.type.PaymentAttemptStatusEnum

fun ActivePaymentAttemptQuery.ActivePaymentAttempt.toDomain(): PaymentAttempt = PaymentAttempt(
    id = id,
    orderId = orderId,
    paymentMethodId = paymentMethodId,
    status = status.toDomain(),
    sendsSmsNotification = sendsSmsNotification,
    proofUrl = proofUrl,
    customerConfirmedAt = customerConfirmedAt?.toString(),
    businessConfirmedAt = businessConfirmedAt?.toString(),
    completedAt = completedAt?.toString(),
    failedAt = failedAt?.toString(),
    failedReason = failedReason,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

fun ConfirmPaymentReceivedMutation.ConfirmPaymentReceived.toDomain(): PaymentAttempt = PaymentAttempt(
    id = id,
    orderId = orderId,
    paymentMethodId = paymentMethodId,
    status = status.toDomain(),
    sendsSmsNotification = sendsSmsNotification,
    proofUrl = proofUrl,
    customerConfirmedAt = customerConfirmedAt?.toString(),
    businessConfirmedAt = businessConfirmedAt?.toString(),
    completedAt = completedAt?.toString(),
    failedAt = failedAt?.toString(),
    failedReason = failedReason,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

private fun PaymentAttemptStatusEnum.toDomain(): PaymentAttemptStatus = when (this) {
    PaymentAttemptStatusEnum.PENDING -> PaymentAttemptStatus.PENDING
    PaymentAttemptStatusEnum.PROCESSING -> PaymentAttemptStatus.PROCESSING
    PaymentAttemptStatusEnum.AWAITING_PROOF -> PaymentAttemptStatus.AWAITING_PROOF
    PaymentAttemptStatusEnum.AWAITING_BUSINESS -> PaymentAttemptStatus.AWAITING_BUSINESS
    PaymentAttemptStatusEnum.AWAITING_DELIVERY -> PaymentAttemptStatus.AWAITING_DELIVERY
    PaymentAttemptStatusEnum.COMPLETED -> PaymentAttemptStatus.COMPLETED
    PaymentAttemptStatusEnum.FAILED -> PaymentAttemptStatus.FAILED
    PaymentAttemptStatusEnum.EXPIRED -> PaymentAttemptStatus.EXPIRED
    PaymentAttemptStatusEnum.CANCELLED -> PaymentAttemptStatus.CANCELLED
    PaymentAttemptStatusEnum.DISPUTED -> PaymentAttemptStatus.DISPUTED
    PaymentAttemptStatusEnum.REFUND_REQUESTED -> PaymentAttemptStatus.REFUND_REQUESTED
    PaymentAttemptStatusEnum.REFUND_PROCESSING -> PaymentAttemptStatus.REFUND_PROCESSING
    PaymentAttemptStatusEnum.REFUNDED -> PaymentAttemptStatus.REFUNDED
    PaymentAttemptStatusEnum.UNKNOWN__ -> PaymentAttemptStatus.PENDING
}
