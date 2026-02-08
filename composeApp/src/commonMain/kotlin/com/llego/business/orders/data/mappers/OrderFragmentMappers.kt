package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.*
import com.llego.multiplatform.graphql.fragment.*

internal fun OrderFullFields.toDomain(): Order = Order(
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
    items = items.map { it.orderItemFields.toDomain() },
    discounts = discounts.map { it.orderDiscountFields.toDomain() },
    timeline = timeline.map { it.orderTimelineFields.toDomain() },
    comments = comments.map { it.orderCommentFields.toDomain() },
    deliveryAddress = deliveryAddress.deliveryAddressFields.toDomain(),
    customer = customer.userOrderCustomerFields.toDomain(),
    branch = branch.branchOrderInfoFields.toDomain(),
    business = business.businessOrderInfoFields.toDomain(),
    deliveryPerson = deliveryPerson?.deliveryPersonFields?.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining
)

internal fun OrderUpdatedFields.toDomain(): Order = Order(
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
    items = items.map { it.orderItemFields.toDomain() },
    discounts = discounts.map { it.orderDiscountFields.toDomain() },
    timeline = timeline.map { it.orderTimelineFields.toDomain() },
    comments = comments.map { it.orderCommentFields.toDomain() },
    deliveryAddress = deliveryAddress.deliveryAddressFields.toDomain(),
    customer = customer.userOrderCustomerFields.toDomain(),
    branch = branch.branchOrderInfoFields.toDomain(),
    business = business.businessOrderInfoFields.toDomain(),
    deliveryPerson = deliveryPerson?.deliveryPersonFields?.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining
)

internal fun OrderNewBranchFields.toDomain(): Order = Order(
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
    items = items.map { it.orderItemFields.toDomain() },
    discounts = discounts.map { it.orderDiscountFields.toDomain() },
    timeline = timeline.map { it.orderTimelineFields.toDomain() },
    comments = comments.map { it.orderCommentFields.toDomain() },
    deliveryAddress = deliveryAddress.deliveryAddressFields.toDomain(),
    customer = customer.userOrderCustomerFields.toDomain(),
    branch = branch.branchOrderInfoFields.toDomain(),
    business = business.businessOrderInfoFields.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining
)

internal fun OrderPendingFields.toDomain(): Order = Order(
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
    items = items.map { it.orderItemPendingFields.toDomain() },
    discounts = emptyList(),
    timeline = emptyList(),
    comments = emptyList(),
    deliveryAddress = deliveryAddress.deliveryAddressPendingFields.toDomain(),
    customer = customer.userOrderPendingCustomerFields.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining
)

internal fun OrderAcceptUpdateFields.toPartialDomain(): Order = Order(
    id = orderStatusUpdateFields.id,
    orderNumber = orderStatusUpdateFields.orderNumber,
    customerId = "",
    branchId = "",
    businessId = "",
    subtotal = 0.0,
    deliveryFee = 0.0,
    total = 0.0,
    currency = "",
    status = orderStatusUpdateFields.status.toDomain(),
    paymentMethod = "",
    paymentStatus = PaymentStatus.PENDING,
    createdAt = "",
    updatedAt = orderStatusUpdateFields.updatedAt.toString(),
    lastStatusAt = orderStatusUpdateFields.lastStatusAt.toString(),
    deliveryAddress = DeliveryAddress(street = ""),
    estimatedDeliveryTime = estimatedDeliveryTime?.toString(),
    estimatedMinutesRemaining = estimatedMinutesRemaining,
    timeline = orderStatusUpdateFields.timeline.map { it.orderTimelineFields.toDomain() }
)

internal fun OrderStatusUpdateFields.toPartialDomain(): Order = Order(
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
    timeline = timeline.map { it.orderTimelineFields.toDomain() }
)

internal fun OrderModifyItemsUpdateFields.toPartialDomain(): Order = Order(
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
    items = items.map { it.orderItemFields.toDomain() },
    discounts = discounts.map { it.orderDiscountFields.toDomain() },
    timeline = timeline.map { it.orderTimelineFields.toDomain() },
    isEditable = isEditable
)

internal fun OrderCommentsUpdateFields.toPartialDomain(): Order = Order(
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
    comments = comments.map { it.orderCommentFields.toDomain() }
)

private fun OrderItemFields.toDomain(): OrderItem = OrderItem(
    productId = productId,
    name = name,
    price = price,
    quantity = quantity,
    imageUrl = imageUrl,
    wasModifiedByStore = wasModifiedByStore
)

private fun OrderItemPendingFields.toDomain(): OrderItem = OrderItem(
    productId = productId,
    name = name,
    price = price,
    quantity = quantity,
    imageUrl = imageUrl
)

private fun OrderDiscountFields.toDomain(): OrderDiscount = OrderDiscount(
    id = id,
    title = title,
    amount = amount,
    type = type.toDomain()
)

private fun OrderTimelineFields.toDomain(): OrderTimelineEntry = OrderTimelineEntry(
    status = status.toDomain(),
    timestamp = timestamp.toString(),
    message = message,
    actor = actor.toDomain()
)

private fun OrderCommentFields.toDomain(): OrderComment = OrderComment(
    id = id,
    author = author.toDomain(),
    message = message,
    timestamp = timestamp.toString()
)

private fun DeliveryAddressFields.toDomain(): DeliveryAddress = DeliveryAddress(
    street = street,
    city = city,
    reference = reference,
    coordinates = coordinates.coordinatesFields.toDomain()
)

private fun DeliveryAddressPendingFields.toDomain(): DeliveryAddress = DeliveryAddress(
    street = street,
    city = city,
    reference = reference
)

private fun CoordinatesFields.toDomain(): Coordinates = Coordinates(
    type = type,
    coordinates = coordinates
)

private fun UserOrderCustomerFields.toDomain(): CustomerInfo = CustomerInfo(
    id = id,
    name = name,
    phone = phone,
    avatarUrl = avatarUrl
)

private fun UserOrderPendingCustomerFields.toDomain(): CustomerInfo = CustomerInfo(
    id = id,
    name = name,
    phone = phone
)

private fun BranchOrderInfoFields.toDomain(): BranchInfo = BranchInfo(
    id = id,
    businessId = "",
    name = name,
    address = address,
    phone = phone,
    avatarUrl = avatarUrl
)

private fun BusinessOrderInfoFields.toDomain(): BusinessInfo = BusinessInfo(
    id = id,
    name = name,
    avatarUrl = avatarUrl
)

private fun DeliveryPersonFields.toDomain(): DeliveryPersonInfo = DeliveryPersonInfo(
    id = id,
    name = name,
    phone = phone,
    rating = rating,
    totalDeliveries = totalDeliveries,
    vehicleType = vehicleType.toDomain(),
    vehiclePlate = vehiclePlate,
    profileImageUrl = profileImageUrl,
    isOnline = isOnline,
    currentLocation = currentLocation?.coordinatesFields?.toDomain()
)
