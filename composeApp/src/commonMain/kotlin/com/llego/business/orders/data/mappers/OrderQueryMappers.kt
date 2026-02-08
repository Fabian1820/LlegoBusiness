package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderStats
import com.llego.multiplatform.graphql.BranchOrdersQuery
import com.llego.multiplatform.graphql.GetOrderQuery
import com.llego.multiplatform.graphql.OrderStatsQuery
import com.llego.multiplatform.graphql.PendingBranchOrdersQuery

/**
 * Mappers para queries de pedidos.
 */

fun BranchOrdersQuery.Order.toDomain(): Order =
    orderFullFields.toDomain()

fun PendingBranchOrdersQuery.PendingBranchOrder.toDomain(): Order =
    orderPendingFields.toDomain()

fun GetOrderQuery.Order.toDomain(): Order =
    orderFullFields.toDomain()

fun OrderStatsQuery.OrderStats.toDomain(): OrderStats = OrderStats(
    totalOrders = totalOrders,
    completedOrders = completedOrders,
    cancelledOrders = cancelledOrders,
    totalRevenue = totalRevenue,
    averageOrderValue = averageOrderValue,
    averageDeliveryTime = averageDeliveryTime
)
