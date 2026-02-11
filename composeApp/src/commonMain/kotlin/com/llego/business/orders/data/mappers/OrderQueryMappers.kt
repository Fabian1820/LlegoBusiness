package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderStats
import com.llego.business.orders.data.model.DashboardStats
import com.llego.business.orders.data.model.TopProductStats
import com.llego.multiplatform.graphql.BranchOrdersQuery
import com.llego.multiplatform.graphql.DashboardStatsQuery
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

fun DashboardStatsQuery.DashboardStats.toDomain(): DashboardStats = DashboardStats(
    totalRevenue = totalRevenue,
    completedOrders = completedOrders,
    cancelledOrders = cancelledOrders,
    topProducts = topProducts.map { it.toDomain() }
)

fun DashboardStatsQuery.TopProduct.toDomain(): TopProductStats = TopProductStats(
    productId = productId,
    name = name,
    imageUrl = imageUrl,
    totalQuantity = totalQuantity,
    totalRevenue = totalRevenue
)
