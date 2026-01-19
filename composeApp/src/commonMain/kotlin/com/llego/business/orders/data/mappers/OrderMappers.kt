package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.*
import com.llego.multiplatform.graphql.BranchOrdersQuery
import com.llego.multiplatform.graphql.PendingBranchOrdersQuery
import com.llego.multiplatform.graphql.GetOrderQuery
import com.llego.multiplatform.graphql.OrderStatsQuery
import com.llego.multiplatform.graphql.AcceptOrderMutation
import com.llego.multiplatform.graphql.RejectOrderMutation
import com.llego.multiplatform.graphql.UpdateOrderStatusMutation
import com.llego.multiplatform.graphql.MarkOrderReadyMutation
import com.llego.multiplatform.graphql.ModifyOrderItemsMutation
import com.llego.multiplatform.graphql.AddOrderCommentMutation
import com.llego.multiplatform.graphql.NewBranchOrderSubscription
import com.llego.multiplatform.graphql.BranchOrderUpdatedSubscription
import com.llego.multiplatform.graphql.type.OrderStatusEnum
import com.llego.multiplatform.graphql.type.PaymentStatusEnum
import com.llego.multiplatform.graphql.type.OrderActorEnum
import com.llego.multiplatform.graphql.type.DiscountTypeEnum
import com.llego.multiplatform.graphql.type.VehicleTypeEnum

/**
 * Mappers para convertir tipos Apollo GraphQL a modelos Kotlin del dominio
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6
 */
object OrderMappers {

    // ==================== ENUM MAPPERS ====================

    /**
     * Convierte OrderStatusEnum de GraphQL a OrderStatus
     */
    fun OrderStatusEnum.toDomain(): OrderStatus = when (this) {
        OrderStatusEnum.PENDING_ACCEPTANCE -> OrderStatus.PENDING_ACCEPTANCE
        OrderStatusEnum.MODIFIED_BY_STORE -> OrderStatus.MODIFIED_BY_STORE
        OrderStatusEnum.ACCEPTED -> OrderStatus.ACCEPTED
        OrderStatusEnum.PREPARING -> OrderStatus.PREPARING
        OrderStatusEnum.READY_FOR_PICKUP -> OrderStatus.READY_FOR_PICKUP
        OrderStatusEnum.ON_THE_WAY -> OrderStatus.ON_THE_WAY
        OrderStatusEnum.DELIVERED -> OrderStatus.DELIVERED
        OrderStatusEnum.CANCELLED -> OrderStatus.CANCELLED
        OrderStatusEnum.UNKNOWN__ -> OrderStatus.PENDING_ACCEPTANCE
    }

    /**
     * Convierte OrderStatus a OrderStatusEnum de GraphQL
     */
    fun OrderStatus.toGraphQL(): OrderStatusEnum = when (this) {
        OrderStatus.PENDING_ACCEPTANCE -> OrderStatusEnum.PENDING_ACCEPTANCE
        OrderStatus.MODIFIED_BY_STORE -> OrderStatusEnum.MODIFIED_BY_STORE
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
     * Convierte PendingBranchOrder a Order (versión simplificada)
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
}