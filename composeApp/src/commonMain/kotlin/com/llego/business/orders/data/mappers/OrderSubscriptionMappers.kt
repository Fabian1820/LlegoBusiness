package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.Order
import com.llego.multiplatform.graphql.BranchOrderUpdatedSubscription
import com.llego.multiplatform.graphql.NewBranchOrderSubscription

/**
 * Mappers para subscriptions de pedidos.
 */

fun NewBranchOrderSubscription.NewBranchOrder.toDomain(): Order =
    orderNewBranchFields.toDomain()

fun BranchOrderUpdatedSubscription.BranchOrderUpdated.toDomain(): Order =
    orderUpdatedFields.toDomain()
