package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.*
import com.llego.multiplatform.graphql.NewBranchOrderSubscription
import com.llego.multiplatform.graphql.BranchOrderUpdatedSubscription

/**
 * Mappers para convertir tipos Apollo GraphQL a modelos Kotlin del dominio
 *
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6
 */
// ==================== SUBSCRIPTION MAPPERS ====================

/**
 * Convierte NewBranchOrder subscription result a Order
 */
fun NewBranchOrderSubscription.NewBranchOrder.toDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = customerId,
    branchId = branchId,
    businessId = businessId,
    subtotal = subtotal,
    deliveryFee = deliveryFee,
    total = total,
    currency = currency,
    status = status.toDomain(),
    paymentMethod = paymentMethod,
    paymentStatus = paymentStatus.toDomain(),
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    lastStatusAt = lastStatusAt.toString(),
    items = items.map { it.toDomain() },
    discounts = discounts.map { it.toDomain() },
    timeline = timeline.map { it.toDomain() },
    comments = comments.map { it.toDomain() },
    deliveryAddress = deliveryAddress.toDomain(),
    customer = customer?.toDomain(),
    branch = branch?.toDomain(),
    business = business?.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining
)

fun NewBranchOrderSubscription.Item.toDomain(): OrderItem = OrderItem(
    productId = productId,
    name = name,
    price = price,
    quantity = quantity,
    imageUrl = imageUrl,
    wasModifiedByStore = wasModifiedByStore
)

fun NewBranchOrderSubscription.Discount.toDomain(): OrderDiscount = OrderDiscount(
    id = id,
    title = title,
    amount = amount,
    type = type.toDomain()
)

fun NewBranchOrderSubscription.Timeline.toDomain(): OrderTimelineEntry = OrderTimelineEntry(
    status = status.toDomain(),
    timestamp = timestamp.toString(),
    message = message,
    actor = actor.toDomain()
)

fun NewBranchOrderSubscription.Comment.toDomain(): OrderComment = OrderComment(
    id = id,
    author = author.toDomain(),
    message = message,
    timestamp = timestamp.toString()
)

fun NewBranchOrderSubscription.DeliveryAddress.toDomain(): DeliveryAddress = DeliveryAddress(
    street = street,
    city = city,
    reference = reference,
    coordinates = coordinates?.toDomain()
)

fun NewBranchOrderSubscription.Coordinates.toDomain(): Coordinates = Coordinates(
    type = type,
    coordinates = coordinates
)

fun NewBranchOrderSubscription.Customer.toDomain(): CustomerInfo = CustomerInfo(
    id = id,
    name = name,
    phone = phone,
    avatarUrl = avatarUrl
)

fun NewBranchOrderSubscription.Branch.toDomain(): BranchInfo = BranchInfo(
    id = id,
    businessId = "",
    name = name,
    address = address,
    phone = phone,
    avatarUrl = avatarUrl
)

fun NewBranchOrderSubscription.Business.toDomain(): BusinessInfo = BusinessInfo(
    id = id,
    name = name,
    avatarUrl = avatarUrl
)

/**
 * Convierte BranchOrderUpdated subscription result a Order
 */
fun BranchOrderUpdatedSubscription.BranchOrderUpdated.toDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = customerId,
    branchId = branchId,
    businessId = businessId,
    subtotal = subtotal,
    deliveryFee = deliveryFee,
    total = total,
    currency = currency,
    status = status.toDomain(),
    paymentMethod = paymentMethod,
    paymentStatus = paymentStatus.toDomain(),
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    lastStatusAt = lastStatusAt.toString(),
    deliveryPersonId = deliveryPersonId,
    estimatedDeliveryTime = estimatedDeliveryTime?.toString(),
    items = items.map { it.toDomain() },
    discounts = discounts.map { it.toDomain() },
    timeline = timeline.map { it.toDomain() },
    comments = comments.map { it.toDomain() },
    deliveryAddress = deliveryAddress.toDomain(),
    customer = customer?.toDomain(),
    branch = branch?.toDomain(),
    business = business?.toDomain(),
    deliveryPerson = deliveryPerson?.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining
)

fun BranchOrderUpdatedSubscription.Item.toDomain(): OrderItem = OrderItem(
    productId = productId,
    name = name,
    price = price,
    quantity = quantity,
    imageUrl = imageUrl,
    wasModifiedByStore = wasModifiedByStore
)

fun BranchOrderUpdatedSubscription.Discount.toDomain(): OrderDiscount = OrderDiscount(
    id = id,
    title = title,
    amount = amount,
    type = type.toDomain()
)

fun BranchOrderUpdatedSubscription.Timeline.toDomain(): OrderTimelineEntry = OrderTimelineEntry(
    status = status.toDomain(),
    timestamp = timestamp.toString(),
    message = message,
    actor = actor.toDomain()
)

fun BranchOrderUpdatedSubscription.Comment.toDomain(): OrderComment = OrderComment(
    id = id,
    author = author.toDomain(),
    message = message,
    timestamp = timestamp.toString()
)

fun BranchOrderUpdatedSubscription.DeliveryAddress.toDomain(): DeliveryAddress = DeliveryAddress(
    street = street,
    city = city,
    reference = reference,
    coordinates = coordinates?.toDomain()
)

fun BranchOrderUpdatedSubscription.Coordinates.toDomain(): Coordinates = Coordinates(
    type = type,
    coordinates = coordinates
)

fun BranchOrderUpdatedSubscription.Customer.toDomain(): CustomerInfo = CustomerInfo(
    id = id,
    name = name,
    phone = phone,
    avatarUrl = avatarUrl
)

fun BranchOrderUpdatedSubscription.Branch.toDomain(): BranchInfo = BranchInfo(
    id = id,
    businessId = "",
    name = name,
    address = address,
    phone = phone,
    avatarUrl = avatarUrl
)

fun BranchOrderUpdatedSubscription.Business.toDomain(): BusinessInfo = BusinessInfo(
    id = id,
    name = name,
    avatarUrl = avatarUrl
)

fun BranchOrderUpdatedSubscription.DeliveryPerson.toDomain(): DeliveryPersonInfo = DeliveryPersonInfo(
    id = id,
    name = name,
    phone = phone,
    rating = rating,
    totalDeliveries = totalDeliveries,
    vehicleType = vehicleType.toDomain(),
    vehiclePlate = vehiclePlate,
    profileImageUrl = profileImageUrl,
    isOnline = isOnline,
    currentLocation = currentLocation?.toDomain()
)

fun BranchOrderUpdatedSubscription.CurrentLocation.toDomain(): Coordinates = Coordinates(
    type = type,
    coordinates = coordinates
)
