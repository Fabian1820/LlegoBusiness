package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.*
import com.llego.multiplatform.graphql.BranchOrdersQuery
import com.llego.multiplatform.graphql.PendingBranchOrdersQuery
import com.llego.multiplatform.graphql.GetOrderQuery
import com.llego.multiplatform.graphql.OrderStatsQuery

/**
 * Mappers para convertir tipos Apollo GraphQL a modelos Kotlin del dominio
 *
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6
 */
// ==================== BRANCHORDERS QUERY MAPPERS ====================

/**
 * Convierte Order de BranchOrdersQuery a Order
 */
fun BranchOrdersQuery.Order.toDomain(): Order = Order(
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
    paymentId = paymentId,
    rating = rating,
    ratingComment = ratingComment,
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

fun BranchOrdersQuery.Item.toDomain(): OrderItem = OrderItem(
    productId = productId,
    name = name,
    price = price,
    quantity = quantity,
    imageUrl = imageUrl,
    wasModifiedByStore = wasModifiedByStore
)

fun BranchOrdersQuery.Discount.toDomain(): OrderDiscount = OrderDiscount(
    id = id,
    title = title,
    amount = amount,
    type = type.toDomain()
)

fun BranchOrdersQuery.Timeline.toDomain(): OrderTimelineEntry = OrderTimelineEntry(
    status = status.toDomain(),
    timestamp = timestamp.toString(),
    message = message,
    actor = actor.toDomain()
)

fun BranchOrdersQuery.Comment.toDomain(): OrderComment = OrderComment(
    id = id,
    author = author.toDomain(),
    message = message,
    timestamp = timestamp.toString()
)

fun BranchOrdersQuery.DeliveryAddress.toDomain(): DeliveryAddress = DeliveryAddress(
    street = street,
    city = city,
    reference = reference,
    coordinates = coordinates?.toDomain()
)

fun BranchOrdersQuery.Coordinates.toDomain(): Coordinates = Coordinates(
    type = type,
    coordinates = coordinates
)

fun BranchOrdersQuery.Customer.toDomain(): CustomerInfo = CustomerInfo(
    id = id,
    name = name,
    phone = phone,
    avatarUrl = avatarUrl
)

fun BranchOrdersQuery.Branch.toDomain(): BranchInfo = BranchInfo(
    id = id,
    businessId = "", // Not available in this query
    name = name,
    address = address,
    phone = phone,
    avatarUrl = avatarUrl
)

fun BranchOrdersQuery.Business.toDomain(): BusinessInfo = BusinessInfo(
    id = id,
    name = name,
    avatarUrl = avatarUrl
)

fun BranchOrdersQuery.DeliveryPerson.toDomain(): DeliveryPersonInfo = DeliveryPersonInfo(
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

fun BranchOrdersQuery.CurrentLocation.toDomain(): Coordinates = Coordinates(
    type = type,
    coordinates = coordinates
)

// ==================== PENDINGBRANCHORDERS QUERY MAPPERS ====================

/**
 * Convierte PendingBranchOrder a Order (versiÃ³n simplificada)
 */
fun PendingBranchOrdersQuery.PendingBranchOrder.toDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = "",
    branchId = branchId,
    businessId = "",
    subtotal = 0.0,
    deliveryFee = 0.0,
    total = total,
    currency = currency,
    status = status.toDomain(),
    paymentMethod = paymentMethod,
    paymentStatus = paymentStatus.toDomain(),
    createdAt = createdAt.toString(),
    updatedAt = createdAt.toString(),
    lastStatusAt = createdAt.toString(),
    items = items.map { it.toDomain() },
    discounts = emptyList(),
    timeline = emptyList(),
    comments = emptyList(),
    deliveryAddress = deliveryAddress.toDomain(),
    customer = customer?.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining
)

fun PendingBranchOrdersQuery.Item.toDomain(): OrderItem = OrderItem(
    productId = productId,
    name = name,
    price = price,
    quantity = quantity,
    imageUrl = imageUrl
)

fun PendingBranchOrdersQuery.DeliveryAddress.toDomain(): DeliveryAddress = DeliveryAddress(
    street = street,
    city = city,
    reference = reference
)

fun PendingBranchOrdersQuery.Customer.toDomain(): CustomerInfo = CustomerInfo(
    id = id,
    name = name,
    phone = phone
)


// ==================== GETORDER QUERY MAPPERS ====================

/**
 * Convierte Order de GetOrderQuery a Order
 */
fun GetOrderQuery.Order.toDomain(): Order = Order(
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
    paymentId = paymentId,
    rating = rating,
    ratingComment = ratingComment,
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

fun GetOrderQuery.Item.toDomain(): OrderItem = OrderItem(
    productId = productId,
    name = name,
    price = price,
    quantity = quantity,
    imageUrl = imageUrl,
    wasModifiedByStore = wasModifiedByStore
)

fun GetOrderQuery.Discount.toDomain(): OrderDiscount = OrderDiscount(
    id = id,
    title = title,
    amount = amount,
    type = type.toDomain()
)

fun GetOrderQuery.Timeline.toDomain(): OrderTimelineEntry = OrderTimelineEntry(
    status = status.toDomain(),
    timestamp = timestamp.toString(),
    message = message,
    actor = actor.toDomain()
)

fun GetOrderQuery.Comment.toDomain(): OrderComment = OrderComment(
    id = id,
    author = author.toDomain(),
    message = message,
    timestamp = timestamp.toString()
)

fun GetOrderQuery.DeliveryAddress.toDomain(): DeliveryAddress = DeliveryAddress(
    street = street,
    city = city,
    reference = reference,
    coordinates = coordinates?.toDomain()
)

fun GetOrderQuery.Coordinates.toDomain(): Coordinates = Coordinates(
    type = type,
    coordinates = coordinates
)

fun GetOrderQuery.Customer.toDomain(): CustomerInfo = CustomerInfo(
    id = id,
    name = name,
    phone = phone,
    avatarUrl = avatarUrl
)

fun GetOrderQuery.Branch.toDomain(): BranchInfo = BranchInfo(
    id = id,
    businessId = "",
    name = name,
    address = address,
    phone = phone,
    avatarUrl = avatarUrl
)

fun GetOrderQuery.Business.toDomain(): BusinessInfo = BusinessInfo(
    id = id,
    name = name,
    avatarUrl = avatarUrl
)

fun GetOrderQuery.DeliveryPerson.toDomain(): DeliveryPersonInfo = DeliveryPersonInfo(
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

fun GetOrderQuery.CurrentLocation.toDomain(): Coordinates = Coordinates(
    type = type,
    coordinates = coordinates
)

// ==================== ORDERSTATS QUERY MAPPER ====================

/**
 * Convierte OrderStats de OrderStatsQuery a OrderStats
 */
fun OrderStatsQuery.OrderStats.toDomain(): OrderStats = OrderStats(
    totalOrders = totalOrders,
    completedOrders = completedOrders,
    cancelledOrders = cancelledOrders,
    totalRevenue = totalRevenue,
    averageOrderValue = averageOrderValue,
    averageDeliveryTime = averageDeliveryTime
)


