package com.llego.nichos.restaurant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersUiState

/**
 * Pantalla de Pedidos con diseño moderno y profesional
 */
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredOrders by viewModel.filteredOrders.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var selectedOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    val allOrders = (uiState as? OrdersUiState.Success)?.orders.orEmpty()
    val selectedOrder = selectedOrderId?.let { id ->
        allOrders.firstOrNull { it.id == id }
    }

    LaunchedEffect(selectedOrderId, selectedOrder) {
        if (selectedOrderId != null && selectedOrder == null) {
            selectedOrderId = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Filtros de estado
        StatusFilterChips(
            selectedFilter = selectedFilter,
            onFilterSelected = { viewModel.setFilter(it) },
            onClearFilter = { viewModel.clearFilter() }
        )

        // Lista de pedidos
        when (uiState) {
            is OrdersUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is OrdersUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = (uiState as OrdersUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadOrders() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is OrdersUiState.Success -> {
                if (filteredOrders.isEmpty()) {
                    EmptyOrdersView(hasFilter = selectedFilter != null)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredOrders,
                            key = { it.id }
                        ) { order ->
                            OrderCard(
                                order = order,
                                onClick = { selectedOrderId = order.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog de detalle del pedido
    // TODO: Reactivar cuando se corrija el diálogo

    selectedOrder?.let { order ->
        OrderDetailDialog(
            order = order,
            onDismiss = { selectedOrderId = null },
            onUpdateStatus = { newStatus ->
                viewModel.updateOrderStatus(order.id, newStatus)
                selectedOrderId = null
            }
        )
    }

}

/**
 * Chips de filtro por estado
 */
@Composable
private fun StatusFilterChips(
    selectedFilter: OrderStatus?,
    onFilterSelected: (OrderStatus) -> Unit,
    onClearFilter: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chip "Todos"
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = onClearFilter,
                    label = { Text("Todos") },
                    leadingIcon = if (selectedFilter == null) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null
                )
            }

            // Chips por estado
            items(OrderStatus.values()) { status ->
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { onFilterSelected(status) },
                    label = {
                        Text(
                            text = status.getDisplayName(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = if (selectedFilter == status) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(status.getColor()).copy(alpha = 0.2f),
                        selectedLabelColor = Color(status.getColor())
                    )
                )
            }
        }
    }
}

/**
 * Card de Pedido con diseño elegante
 */
@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Número de pedido + Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido ${order.orderNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                // Badge de estado
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(order.status.getColor()).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(order.status.getColor()))
                ) {
                    Text(
                        text = order.status.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(order.status.getColor())
                        )
                    )
                }
            }

            Divider(color = Color(0xFFE0E0E0))

            // Cliente
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = order.customer.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = order.customer.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Tipo de entrega y dirección
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = if (order.deliveryType == DeliveryType.DELIVERY)
                        Icons.Default.DeliveryDining else Icons.Default.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = order.deliveryType.getDisplayName(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (order.customer.address != null) {
                        Text(
                            text = order.customer.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Items del pedido (resumen)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Items del pedido",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF666666)
                    )

                    order.items.take(2).forEach { orderItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${orderItem.quantity}x ${orderItem.menuItem.name}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$${orderItem.subtotal}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    if (order.items.size > 2) {
                        Text(
                            text = "+${order.items.size - 2} items más",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Divider(color = Color(0xFFE0E0E0))

            // Footer: Total + Método de pago + Tiempo estimado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "$${order.total}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = order.paymentMethod.getDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    order.estimatedTime?.let { time ->
                        Text(
                            text = "⏱️ $time min",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

// TODO: Reconstruir OrderDetailDialog en próxima sesión
// Dialog temporalmente deshabilitado por error de sintaxis en AlertDialog

/**
 * Vista cuando no hay pedidos
 */
@Composable
private fun EmptyOrdersView(hasFilter: Boolean) {
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
                tint = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = if (hasFilter) {
                    "No hay pedidos con este filtro"
                } else {
                    "No hay pedidos activos"
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            if (hasFilter) {
                Text(
                    text = "Intenta con otro filtro",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
