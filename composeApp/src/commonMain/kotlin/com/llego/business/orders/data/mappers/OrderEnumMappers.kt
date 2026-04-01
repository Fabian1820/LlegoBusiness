package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.DiscountType
import com.llego.business.orders.data.model.OrderActor
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.data.model.PaymentStatus
import com.llego.business.orders.data.model.VehicleType
import com.llego.multiplatform.graphql.type.DiscountTypeEnum
import com.llego.multiplatform.graphql.type.OrderActorEnum
import com.llego.multiplatform.graphql.type.OrderStatusEnum
import com.llego.multiplatform.graphql.type.PaymentStatusEnum
import com.llego.multiplatform.graphql.type.VehicleTypeEnum

/**
 * Mappers para convertir tipos Apollo GraphQL a modelos Kotlin del dominio
 *
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6
 */
// ==================== ENUM MAPPERS ====================

/**
 * Convierte OrderStatusEnum de GraphQL a OrderStatus
 */
fun OrderStatusEnum.toDomain(): OrderStatus = when (this) {
    OrderStatusEnum.AWAITING_DELIVERY_ACCEPTANCE -> OrderStatus.AWAITING_DELIVERY_ACCEPTANCE
    OrderStatusEnum.PENDING_PAYMENT -> OrderStatus.PENDING_PAYMENT
    OrderStatusEnum.PAYMENT_IN_PROGRESS -> OrderStatus.PAYMENT_IN_PROGRESS
    OrderStatusEnum.PENDING_ACCEPTANCE -> OrderStatus.PENDING_ACCEPTANCE
    OrderStatusEnum.MODIFIED_BY_STORE -> OrderStatus.MODIFIED_BY_STORE
    OrderStatusEnum.REJECTED_BY_STORE -> OrderStatus.REJECTED_BY_STORE
    OrderStatusEnum.ACCEPTED -> OrderStatus.ACCEPTED
    OrderStatusEnum.PREPARING -> OrderStatus.PREPARING
    OrderStatusEnum.READY_FOR_PICKUP -> OrderStatus.READY_FOR_PICKUP
    OrderStatusEnum.ON_THE_WAY -> OrderStatus.ON_THE_WAY
    OrderStatusEnum.DELIVERED -> OrderStatus.DELIVERED
    OrderStatusEnum.CANCELLED -> OrderStatus.CANCELLED
    OrderStatusEnum.UNKNOWN__ -> OrderStatus.REJECTED_BY_STORE
}

/**
 * Convierte OrderStatus a OrderStatusEnum de GraphQL
 */
fun OrderStatus.toGraphQL(): OrderStatusEnum = when (this) {
    OrderStatus.AWAITING_DELIVERY_ACCEPTANCE -> OrderStatusEnum.AWAITING_DELIVERY_ACCEPTANCE
    OrderStatus.PENDING_PAYMENT -> OrderStatusEnum.PENDING_PAYMENT
    OrderStatus.PAYMENT_IN_PROGRESS -> OrderStatusEnum.PAYMENT_IN_PROGRESS
    OrderStatus.PENDING_ACCEPTANCE -> OrderStatusEnum.PENDING_ACCEPTANCE
    OrderStatus.MODIFIED_BY_STORE -> OrderStatusEnum.MODIFIED_BY_STORE
    OrderStatus.REJECTED_BY_STORE -> OrderStatusEnum.REJECTED_BY_STORE
    OrderStatus.ACCEPTED -> OrderStatusEnum.ACCEPTED
    OrderStatus.PREPARING -> OrderStatusEnum.PREPARING
    OrderStatus.READY_FOR_PICKUP -> OrderStatusEnum.READY_FOR_PICKUP
    OrderStatus.ON_THE_WAY -> OrderStatusEnum.ON_THE_WAY
    OrderStatus.DELIVERED -> OrderStatusEnum.DELIVERED
    OrderStatus.CANCELLED -> OrderStatusEnum.CANCELLED
}

/**
 * Convierte PaymentStatusEnum de GraphQL a PaymentStatus
 */
fun PaymentStatusEnum.toDomain(): PaymentStatus = when (this) {
    PaymentStatusEnum.PENDING -> PaymentStatus.PENDING
    PaymentStatusEnum.VALIDATED -> PaymentStatus.VALIDATED
    PaymentStatusEnum.COMPLETED -> PaymentStatus.COMPLETED
    PaymentStatusEnum.FAILED -> PaymentStatus.FAILED
    PaymentStatusEnum.UNKNOWN__ -> PaymentStatus.PENDING
}

/**
 * Convierte OrderActorEnum de GraphQL a OrderActor
 */
fun OrderActorEnum.toDomain(): OrderActor = when (this) {
    OrderActorEnum.CUSTOMER -> OrderActor.CUSTOMER
    OrderActorEnum.BUSINESS -> OrderActor.BUSINESS
    OrderActorEnum.SYSTEM -> OrderActor.SYSTEM
    OrderActorEnum.DELIVERY -> OrderActor.DELIVERY
    OrderActorEnum.UNKNOWN__ -> OrderActor.SYSTEM
}

/**
 * Convierte DiscountTypeEnum de GraphQL a DiscountType
 */
fun DiscountTypeEnum.toDomain(): DiscountType = when (this) {
    DiscountTypeEnum.PREMIUM -> DiscountType.PREMIUM
    DiscountTypeEnum.LEVEL -> DiscountType.LEVEL
    DiscountTypeEnum.PROMO -> DiscountType.PROMO
    DiscountTypeEnum.UNKNOWN__ -> DiscountType.PROMO
}

/**
 * Convierte VehicleTypeEnum de GraphQL a VehicleType
 */
fun VehicleTypeEnum.toDomain(): VehicleType = when (this) {
    VehicleTypeEnum.MOTO -> VehicleType.MOTO
    VehicleTypeEnum.BICICLETA -> VehicleType.BICICLETA
    VehicleTypeEnum.AUTO -> VehicleType.AUTO
    VehicleTypeEnum.A_PIE -> VehicleType.A_PIE
    VehicleTypeEnum.UNKNOWN__ -> VehicleType.MOTO
}


