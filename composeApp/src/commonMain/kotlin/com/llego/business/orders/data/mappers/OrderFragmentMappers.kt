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
    serviceCharge = serviceCharge,
    deliveryMode = deliveryMode,
    total = total,
    currency = currency,
    status = status.toDomain(),
    paymentMethod = paymentMethod,
    paymentMethodName = paymentMethodName,
    paymentStatus = paymentStatus.toDomain(),
    paidAt = paidAt?.toString(),
    deadlineAt = deadlineAt?.toString(),
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
    pickupAddress = pickupAddress?.pickupAddressFields?.toDomain(),
    customer = customer.userOrderCustomerFields.toDomain(),
    branch = branch.branchOrderInfoFields.toDomain(),
    business = business.businessOrderInfoFields.toDomain(),
    deliveryPerson = deliveryPerson?.deliveryPersonFields?.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining,
    estimatedMinutes = estimatedMinutes
)

internal fun OrderUpdatedFields.toDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = customerId,
    branchId = branchId,
    businessId = businessId,
    subtotal = subtotal,
    deliveryFee = deliveryFee,
    serviceCharge = serviceCharge,
    deliveryMode = deliveryMode,
    total = total,
    currency = currency,
    status = status.toDomain(),
    paymentMethod = paymentMethod,
    paymentMethodName = paymentMethodName,
    paymentStatus = paymentStatus.toDomain(),
    paidAt = paidAt?.toString(),
    deadlineAt = deadlineAt?.toString(),
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
    pickupAddress = pickupAddress?.pickupAddressFields?.toDomain(),
    customer = customer.userOrderCustomerFields.toDomain(),
    branch = branch.branchOrderInfoFields.toDomain(),
    business = business.businessOrderInfoFields.toDomain(),
    deliveryPerson = deliveryPerson?.deliveryPersonFields?.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining,
    estimatedMinutes = estimatedMinutes
)

internal fun OrderNewBranchFields.toDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = customerId,
    branchId = branchId,
    businessId = businessId,
    subtotal = subtotal,
    deliveryFee = deliveryFee,
    serviceCharge = serviceCharge,
    deliveryMode = deliveryMode,
    total = total,
    currency = currency,
    status = status.toDomain(),
    paymentMethod = paymentMethod,
    paymentMethodName = paymentMethodName,
    paymentStatus = paymentStatus.toDomain(),
    paidAt = paidAt?.toString(),
    deadlineAt = deadlineAt?.toString(),
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    lastStatusAt = lastStatusAt.toString(),
    items = items.map { it.orderItemFields.toDomain() },
    discounts = discounts.map { it.orderDiscountFields.toDomain() },
    timeline = timeline.map { it.orderTimelineFields.toDomain() },
    comments = comments.map { it.orderCommentFields.toDomain() },
    deliveryAddress = deliveryAddress.deliveryAddressFields.toDomain(),
    pickupAddress = pickupAddress?.pickupAddressFields?.toDomain(),
    customer = customer.userOrderCustomerFields.toDomain(),
    branch = branch.branchOrderInfoFields.toDomain(),
    business = business.businessOrderInfoFields.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining,
    estimatedMinutes = estimatedMinutes
)

internal fun OrderPendingFields.toDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = "",
    branchId = branchId,
    businessId = "",
    subtotal = 0.0,
    deliveryFee = 0.0,
    deliveryMode = deliveryMode,
    total = total,
    currency = currency,
    status = status.toDomain(),
    paymentMethod = paymentMethod,
    paymentMethodName = paymentMethodName,
    paymentStatus = paymentStatus.toDomain(),
    paidAt = paidAt?.toString(),
    deadlineAt = deadlineAt?.toString(),
    createdAt = createdAt.toString(),
    updatedAt = createdAt.toString(),
    lastStatusAt = createdAt.toString(),
    deliveryPersonId = deliveryPersonId,
    estimatedDeliveryTime = estimatedDeliveryTime?.toString(),
    items = items.map { it.orderItemPendingFields.toDomain() },
    discounts = emptyList(),
    timeline = emptyList(),
    comments = emptyList(),
    deliveryAddress = deliveryAddress.deliveryAddressPendingFields.toDomain(),
    customer = customer.userOrderPendingCustomerFields.toDomain(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining,
    estimatedMinutes = estimatedMinutes
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
    paymentMethod = orderStatusUpdateFields.paymentMethod,
    paymentMethodName = orderStatusUpdateFields.paymentMethodName,
    paymentStatus = orderStatusUpdateFields.paymentStatus.toDomain(),
    paidAt = orderStatusUpdateFields.paidAt?.toString(),
    deadlineAt = orderStatusUpdateFields.deadlineAt?.toString(),
    deliveryMode = orderStatusUpdateFields.deliveryMode,
    createdAt = "",
    updatedAt = orderStatusUpdateFields.updatedAt.toString(),
    lastStatusAt = orderStatusUpdateFields.lastStatusAt.toString(),
    deliveryAddress = DeliveryAddress(street = ""),
    deliveryPersonId = orderStatusUpdateFields.deliveryPersonId,
    estimatedDeliveryTime = orderStatusUpdateFields.estimatedDeliveryTime?.toString(),
    isEditable = orderStatusUpdateFields.isEditable,
    canCancel = orderStatusUpdateFields.canCancel,
    estimatedMinutesRemaining = orderStatusUpdateFields.estimatedMinutesRemaining,
    estimatedMinutes = orderStatusUpdateFields.estimatedMinutes,
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
    paymentMethod = paymentMethod,
    paymentMethodName = paymentMethodName,
    paymentStatus = paymentStatus.toDomain(),
    paidAt = paidAt?.toString(),
    deadlineAt = deadlineAt?.toString(),
    deliveryMode = deliveryMode,
    createdAt = "",
    updatedAt = updatedAt.toString(),
    lastStatusAt = lastStatusAt.toString(),
    deliveryAddress = DeliveryAddress(street = ""),
    deliveryPersonId = deliveryPersonId,
    estimatedDeliveryTime = estimatedDeliveryTime?.toString(),
    isEditable = isEditable,
    canCancel = canCancel,
    estimatedMinutesRemaining = estimatedMinutesRemaining,
    estimatedMinutes = estimatedMinutes,
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
    serviceCharge = serviceCharge,
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
    itemId = itemId,
    itemType = itemType,
    productId = productId.takeIf { it.isNotBlank() },
    comboId = if (itemType.equals("COMBO", ignoreCase = true)) {
        itemId.takeIf { it.isNotBlank() }
    } else {
        null
    },
    name = name,
    price = price,
    basePrice = basePrice,
    finalPrice = finalPrice,
    quantity = quantity,
    imageUrl = imageUrl,
    hasGift = hasGift,
    previewProducts = previewProducts?.map { preview ->
        OrderPreviewProduct(
            productId = preview.productId,
            name = preview.name,
            imageUrl = preview.imageUrl
        )
    } ?: emptyList(),
    requestDescription = requestDescription,
    wasModifiedByStore = wasModifiedByStore,
    comboSelections = comboSelections?.map { it.orderComboSelectionFields.toDomain() } ?: emptyList(),
    discountType = discountType,
    discountValue = discountValue
)

private fun OrderItemPendingFields.toDomain(): OrderItem = OrderItem(
    itemId = itemId,
    itemType = itemType,
    productId = productId.takeIf { it.isNotBlank() },
    comboId = if (itemType.equals("COMBO", ignoreCase = true)) {
        itemId.takeIf { it.isNotBlank() }
    } else {
        null
    },
    name = name,
    price = price,
    basePrice = basePrice,
    finalPrice = finalPrice,
    quantity = quantity,
    imageUrl = imageUrl,
    hasGift = hasGift,
    previewProducts = previewProducts?.map { preview ->
        OrderPreviewProduct(
            productId = preview.productId,
            name = preview.name,
            imageUrl = preview.imageUrl
        )
    } ?: emptyList(),
    requestDescription = requestDescription,
    comboSelections = comboSelections?.map { it.orderComboSelectionFields.toDomain() } ?: emptyList(),
    discountType = discountType,
    discountValue = discountValue
)

private fun OrderComboSelectionFields.toDomain(): OrderComboSelection = OrderComboSelection(
    slotId = slotId,
    slotName = slotName,
    selectedOptions = selectedOptions.map { it.orderComboSelectedOptionFields.toDomain() }
)

private fun OrderComboSelectedOptionFields.toDomain(): OrderComboSelectedOption = OrderComboSelectedOption(
    productId = productId,
    name = name,
    price = price,
    quantity = quantity,
    priceAdjustment = priceAdjustment,
    modifiers = modifiers.map { it.orderComboModifierFields.toDomain() }
)

private fun OrderComboModifierFields.toDomain(): OrderComboModifier = OrderComboModifier(
    name = name,
    priceAdjustment = priceAdjustment
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

private fun PickupAddressFields.toDomain(): PickupAddress = PickupAddress(
    street = street,
    coordinates = coordinates.coordinatesFields.toDomain()
)

private fun CoordinatesFields.toDomain(): Coordinates = Coordinates(
    type = type,
    coordinates = coordinates
)

private fun UserOrderCustomerFields.toDomain(): CustomerInfo = CustomerInfo(
    id = id,
    name = name,
    phone = phone,
    avatarUrl = avatarUrl,
    deliveredOrdersCount = deliveredOrdersCount,
    walletStatus = walletStatus
)

private fun UserOrderPendingCustomerFields.toDomain(): CustomerInfo = CustomerInfo(
    id = id,
    name = name,
    phone = phone,
    deliveredOrdersCount = deliveredOrdersCount,
    walletStatus = walletStatus
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
