package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.*
import com.llego.multiplatform.graphql.AcceptOrderMutation
import com.llego.multiplatform.graphql.RejectOrderMutation
import com.llego.multiplatform.graphql.UpdateOrderStatusMutation
import com.llego.multiplatform.graphql.MarkOrderReadyMutation
import com.llego.multiplatform.graphql.ModifyOrderItemsMutation
import com.llego.multiplatform.graphql.AddOrderCommentMutation

/**
 * Mappers para convertir tipos Apollo GraphQL a modelos Kotlin del dominio
 *
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6
 */
// ==================== MUTATION MAPPERS ====================

/**
 * Convierte AcceptOrder mutation result a Order parcial
 * Solo contiene campos actualizados
 */
fun AcceptOrderMutation.AcceptOrder.toPartialDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = "",
    branchId = "",
    businessId = "",
    subtotal = 0.0,
    deliveryFee = 0.0,
    total = 0.0,
    currency = "",
    status = status.toDomain(),
    paymentMethod = "",
    paymentStatus = PaymentStatus.PENDING,
    createdAt = "",
    updatedAt = updatedAt.toString(),
    lastStatusAt = lastStatusAt.toString(),
    deliveryAddress = DeliveryAddress(street = ""),
    estimatedDeliveryTime = estimatedDeliveryTime?.toString(),
    estimatedMinutesRemaining = estimatedMinutesRemaining,
    timeline = timeline.map { it.toDomain() }
)

fun AcceptOrderMutation.Timeline.toDomain(): OrderTimelineEntry = OrderTimelineEntry(
    status = status.toDomain(),
    timestamp = timestamp.toString(),
    message = message,
    actor = actor.toDomain()
)

/**
 * Convierte RejectOrder mutation result a Order parcial
 */
fun RejectOrderMutation.RejectOrder.toPartialDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = "",
    branchId = "",
    businessId = "",
    subtotal = 0.0,
    deliveryFee = 0.0,
    total = 0.0,
    currency = "",
    status = status.toDomain(),
    paymentMethod = "",
    paymentStatus = PaymentStatus.PENDING,
    createdAt = "",
    updatedAt = updatedAt.toString(),
    lastStatusAt = lastStatusAt.toString(),
    deliveryAddress = DeliveryAddress(street = ""),
    timeline = timeline.map { it.toDomain() }
)

fun RejectOrderMutation.Timeline.toDomain(): OrderTimelineEntry = OrderTimelineEntry(
    status = status.toDomain(),
    timestamp = timestamp.toString(),
    message = message,
    actor = actor.toDomain()
)

/**
 * Convierte UpdateOrderStatus mutation result a Order parcial
 */
fun UpdateOrderStatusMutation.UpdateOrderStatus.toPartialDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = "",
    branchId = "",
    businessId = "",
    subtotal = 0.0,
    deliveryFee = 0.0,
    total = 0.0,
    currency = "",
    status = status.toDomain(),
    paymentMethod = "",
    paymentStatus = PaymentStatus.PENDING,
    createdAt = "",
    updatedAt = updatedAt.toString(),
    lastStatusAt = lastStatusAt.toString(),
    deliveryAddress = DeliveryAddress(street = ""),
    timeline = timeline.map { it.toDomain() }
)

fun UpdateOrderStatusMutation.Timeline.toDomain(): OrderTimelineEntry = OrderTimelineEntry(
    status = status.toDomain(),
    timestamp = timestamp.toString(),
    message = message,
    actor = actor.toDomain()
)

/**
 * Convierte MarkOrderReady mutation result a Order parcial
 */
fun MarkOrderReadyMutation.MarkOrderReady.toPartialDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = "",
    branchId = "",
    businessId = "",
    subtotal = 0.0,
    deliveryFee = 0.0,
    total = 0.0,
    currency = "",
    status = status.toDomain(),
    paymentMethod = "",
    paymentStatus = PaymentStatus.PENDING,
    createdAt = "",
    updatedAt = updatedAt.toString(),
    lastStatusAt = lastStatusAt.toString(),
    deliveryAddress = DeliveryAddress(street = ""),
    timeline = timeline.map { it.toDomain() }
)

fun MarkOrderReadyMutation.Timeline.toDomain(): OrderTimelineEntry = OrderTimelineEntry(
    status = status.toDomain(),
    timestamp = timestamp.toString(),
    message = message,
    actor = actor.toDomain()
)

/**
 * Convierte ModifyOrderItems mutation result a Order parcial
 */
fun ModifyOrderItemsMutation.ModifyOrderItems.toPartialDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = "",
    branchId = "",
    businessId = "",
    subtotal = subtotal,
    deliveryFee = deliveryFee,
    total = total,
    currency = "",
    status = status.toDomain(),
    paymentMethod = "",
    paymentStatus = PaymentStatus.PENDING,
    createdAt = "",
    updatedAt = updatedAt.toString(),
    lastStatusAt = lastStatusAt.toString(),
    deliveryAddress = DeliveryAddress(street = ""),
    items = items.map { it.toDomain() },
    discounts = discounts.map { it.toDomain() },
    timeline = timeline.map { it.toDomain() },
    isEditable = isEditable
)

fun ModifyOrderItemsMutation.Item.toDomain(): OrderItem = OrderItem(
    productId = productId,
    name = name,
    price = price,
    quantity = quantity,
    imageUrl = imageUrl,
    wasModifiedByStore = wasModifiedByStore
)

fun ModifyOrderItemsMutation.Discount.toDomain(): OrderDiscount = OrderDiscount(
    id = id,
    title = title,
    amount = amount,
    type = type.toDomain()
)

fun ModifyOrderItemsMutation.Timeline.toDomain(): OrderTimelineEntry = OrderTimelineEntry(
    status = status.toDomain(),
    timestamp = timestamp.toString(),
    message = message,
    actor = actor.toDomain()
)

/**
 * Convierte AddOrderComment mutation result a Order parcial
 */
fun AddOrderCommentMutation.AddOrderComment.toPartialDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = "",
    branchId = "",
    businessId = "",
    subtotal = 0.0,
    deliveryFee = 0.0,
    total = 0.0,
    currency = "",
    status = OrderStatus.PENDING_ACCEPTANCE,
    paymentMethod = "",
    paymentStatus = PaymentStatus.PENDING,
    createdAt = "",
    updatedAt = "",
    lastStatusAt = "",
    deliveryAddress = DeliveryAddress(street = ""),
    comments = comments.map { it.toDomain() }
)

fun AddOrderCommentMutation.Comment.toDomain(): OrderComment = OrderComment(
    id = id,
    author = author.toDomain(),
    message = message,
    timestamp = timestamp.toString()
)


