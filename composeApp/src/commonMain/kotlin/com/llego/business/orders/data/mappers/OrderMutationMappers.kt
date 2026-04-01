package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderStatus
import com.llego.multiplatform.graphql.AcceptOrderMutation
import com.llego.multiplatform.graphql.AddOrderCommentMutation
import com.llego.multiplatform.graphql.MarkOrderReadyMutation
import com.llego.multiplatform.graphql.ModifyOrderItemsMutation
import com.llego.multiplatform.graphql.RejectOrderMutation
import com.llego.multiplatform.graphql.CancelOrderMutation
import com.llego.multiplatform.graphql.UpdateOrderStatusMutation

/**
 * Mappers para mutations de pedidos.
 */

fun AcceptOrderMutation.AcceptOrder.toPartialDomain(): Order =
    orderAcceptUpdateFields.toPartialDomain()

fun RejectOrderMutation.RejectOrder.toPartialDomain(): Order =
    orderStatusUpdateFields.toPartialDomain().copy(status = OrderStatus.REJECTED_BY_STORE)

fun CancelOrderMutation.CancelOrder.toPartialDomain(): Order =
    orderStatusUpdateFields.toPartialDomain()

fun UpdateOrderStatusMutation.UpdateOrderStatus.toPartialDomain(): Order =
    orderStatusUpdateFields.toPartialDomain()

fun MarkOrderReadyMutation.MarkOrderReady.toPartialDomain(): Order =
    orderStatusUpdateFields.toPartialDomain()

fun ModifyOrderItemsMutation.ModifyOrderItems.toPartialDomain(): Order =
    orderModifyItemsUpdateFields.toPartialDomain()

fun AddOrderCommentMutation.AddOrderComment.toPartialDomain(): Order =
    orderCommentsUpdateFields.toPartialDomain()
