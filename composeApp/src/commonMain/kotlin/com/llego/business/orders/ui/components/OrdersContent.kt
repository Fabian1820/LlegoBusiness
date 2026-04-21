package com.llego.business.orders.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.ui.viewmodel.DateRangeFilter
import com.llego.shared.ui.theme.LlegoCustomShapes

/**
 * Contenido principal de la pantalla de pedidos
 */
@Composable
fun OrdersContent(
    animateContent: Boolean,
    filteredOrders: List<Order>,
    selectedFilter: OrderStatus?,
    selectedDateRange: DateRangeFilter,
    ordersListState: LazyListState,
    searchQuery: String,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    actionInProgressOrderId: String?,
    onDateRangeSelected: (DateRangeFilter) -> Unit,
    onStatusSelected: (OrderStatus) -> Unit,
    onStatusCleared: () -> Unit
) {
    AnimatedVisibility(
        visible = animateContent,
        enter = fadeIn(animationSpec = tween(600)) +
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(600, easing = EaseOutCubic)
                )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            iOSStyleFilters(
                selectedDateRange = selectedDateRange,
                onDateRangeSelected = onDateRangeSelected,
                selectedStatus = selectedFilter,
                onStatusSelected = onStatusSelected,
                onStatusCleared = onStatusCleared,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    EmptyOrdersView(
                        hasFilter = selectedFilter != null ||
                            selectedDateRange != DateRangeFilter.TODAY ||
                            searchQuery.isNotBlank(),
                        hasSearchQuery = searchQuery.isNotBlank()
                    )
                }
            } else {
                LazyColumn(
                    state = ordersListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredOrders,
                        key = { it.id }
                    ) { order ->
                        OrderCard(
                            order = order,
                            onClick = { onNavigateToOrderDetail(order.id) },
                            isActionInProgress = order.id == actionInProgressOrderId,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    if (canLoadMore) {
                        item {
                            Button(
                                onClick = onLoadMore,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text("Cargar mas")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de Pedido con diseno elegante
 * Actualizado para usar Order del nuevo modelo
 * Requirements: 5.5 - Mostrar status con colores correctos, paymentStatus, estimatedMinutesRemaining
 */
@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    isActionInProgress: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isPickup = order.isPickupOrder()
    val shortNum = orderCardShortNumber(order.orderNumber)
    val prefix = orderCardPrefix(order.orderNumber)
    val dateStr = orderCardFormatDate(order.createdAt)
    val statusColor = order.status.getColor()

    Card(
        onClick = onClick,
        enabled = !isActionInProgress,
        modifier = modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.productCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 3.dp
        )
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cabecera: icono + número/fecha + estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ícono de orden (bolsa)
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                imageVector = if (isPickup) Icons.Default.Storefront else Icons.Default.LocalMall,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(34.dp)
                                    .padding(7.dp)
                            )
                        }

                        // Número corto + fecha en la misma línea / prefijo debajo
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "#$shortNum",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                if (dateStr.isNotBlank()) {
                                    Text(
                                        text = "·",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (prefix.isNotBlank()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prefix,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                    )
                                    Text(
                                        text = "·",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        text = if (isPickup) "Recogida" else "Delivery",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                    )
                                }
                            }
                        }
                    }

                    // Badge de estado
                    Surface(
                        shape = LlegoCustomShapes.secondaryButton,
                        color = statusColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                        modifier = Modifier.widthIn(max = 150.dp)
                    ) {
                        Text(
                            text = order.status.getDisplayName(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

                // Sección inferior: total + método de pago + countdown
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = LlegoCustomShapes.infoCard,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${order.total} ${order.currency}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            order.customer?.let { customer ->
                                Text(
                                    text = "${customer.deliveredOrdersCount} pedidos entregados",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.widthIn(max = 160.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = LlegoCustomShapes.secondaryButton,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = order.paymentMethodDisplayName(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val showDeadline = order.status in listOf(
                                OrderStatus.PENDING_ACCEPTANCE,
                                OrderStatus.MODIFIED_BY_STORE,
                                OrderStatus.REJECTED_BY_STORE,
                                OrderStatus.AWAITING_DELIVERY_ACCEPTANCE,
                                OrderStatus.PENDING_PAYMENT,
                                OrderStatus.PAYMENT_IN_PROGRESS
                            )
                            val countdownText = rememberDeadlineCountdownText(
                                deadlineAt = order.deadlineAt,
                                enabled = showDeadline
                            )
                            if (countdownText != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = if (countdownText == "Vencido") MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = countdownText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (countdownText == "Vencido") MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isActionInProgress) {
                Surface(
                    modifier = Modifier.matchParentSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        }
    }
}

private fun orderCardShortNumber(orderNumber: String): String {
    val parts = orderNumber.split("-")
    return if (parts.size >= 2) parts.last() else orderNumber
}

private fun orderCardPrefix(orderNumber: String): String {
    val lastDash = orderNumber.lastIndexOf("-")
    return if (lastDash > 0) orderNumber.substring(0, lastDash) else ""
}

private fun orderCardFormatDate(timestamp: String): String {
    return try {
        val parts = timestamp.split("T")
        if (parts.size == 2) {
            val dateParts = parts[0].split("-")
            val timePart = parts[1].substringBefore(".")
            val timeComponents = timePart.split(":")
            if (dateParts.size == 3 && timeComponents.size >= 2) {
                val year = dateParts[0].takeLast(2)
                val month = dateParts[1]
                val day = dateParts[2]
                val hourInt = timeComponents[0].toIntOrNull() ?: 0
                val minute = timeComponents[1]
                val ampm = if (hourInt < 12) "am" else "pm"
                val hour12 = when {
                    hourInt == 0 -> 12
                    hourInt > 12 -> hourInt - 12
                    else -> hourInt
                }
                "$hour12:$minute$ampm $day/$month/$year"
            } else ""
        } else ""
    } catch (_: Exception) { "" }
}

/**
 * Vista cuando no hay pedidos
 */
@Composable
fun EmptyOrdersView(hasFilter: Boolean, hasSearchQuery: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = if (hasFilter) {
                    if (hasSearchQuery) "No hay pedidos que coincidan con tu busqueda" else "No hay pedidos con este filtro"
                } else {
                    "No hay pedidos activos"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (hasFilter) {
                    if (hasSearchQuery) "Prueba con otro termino de busqueda" else "Prueba ajustando los filtros"
                } else {
                    "Los nuevos pedidos apareceran aqui"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}
