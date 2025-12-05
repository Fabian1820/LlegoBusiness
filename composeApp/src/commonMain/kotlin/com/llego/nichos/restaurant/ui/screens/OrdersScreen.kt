package com.llego.nichos.restaurant.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersUiState
import kotlinx.coroutines.delay

/**
 * Pantalla de Pedidos con diseño moderno y profesional
 */
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    onNavigateToOrderDetail: (String) -> Unit = {},
    onNavigateToChat: ((String, String, String) -> Unit)? = null, // orderId, orderNumber, customerName
    onShowConfirmation: ((ConfirmationType, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredOrders by viewModel.filteredOrders.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var animateContent by remember { mutableStateOf(false) }

    // Animación de entrada idéntica a Perfil, Gestión y Menú
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
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
                AnimatedVisibility(
                    visible = animateContent,
                    enter = fadeIn(animationSpec = tween(600)) +
                            slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(600, easing = EaseOutCubic)
                            )
                ) {
                    if (filteredOrders.isEmpty()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Filtros de estado
                            StatusFilterChips(
                                selectedFilter = selectedFilter,
                                onFilterSelected = { viewModel.setFilter(it) },
                                onClearFilter = { viewModel.clearFilter() }
                            )
                            EmptyOrdersView(hasFilter = selectedFilter != null)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Filtros de estado como primer item
                            item {
                                StatusFilterChips(
                                    selectedFilter = selectedFilter,
                                    onFilterSelected = { viewModel.setFilter(it) },
                                    onClearFilter = { viewModel.clearFilter() }
                                )
                            }

                            // Lista de pedidos
                            items(
                                items = filteredOrders,
                                key = { it.id }
                            ) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = { onNavigateToOrderDetail(order.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chips de filtro por estado contenidos en un card con fade en ambos lados
 */
@Composable
private fun StatusFilterChips(
    selectedFilter: OrderStatus?,
    onFilterSelected: (OrderStatus) -> Unit,
    onClearFilter: () -> Unit
) {
    val listState = rememberLazyListState()
    val allStatuses = OrderStatus.values().toList()


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box {
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        // Fade izquierdo
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = 60f
                            )
                        )
                        // Fade derecho
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White
                                ),
                                startX = size.width - 80f,
                                endX = size.width
                            )
                        )
                    },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chip "Todos"
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = onClearFilter,
                        label = {
                            Text(
                                "Todos",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedFilter == null) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        leadingIcon = if (selectedFilter == null) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null,
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Chips por estado - Todos con color primary como el menú
                items(OrderStatus.values()) { status ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { onFilterSelected(status) },
                        label = {
                            Text(
                                text = status.getDisplayName(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedFilter == status) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        leadingIcon = if (selectedFilter == status) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null,
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
            // Header: Icono moto + Número de pedido + Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de moto + Número de pedido
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = "Domicilio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(6.dp)
                        )
                    }
                    Text(
                        text = order.orderNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Badge de estado con colores Llego
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (order.status) {
                        OrderStatus.PENDING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        OrderStatus.PREPARING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        OrderStatus.READY -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    },
                    border = BorderStroke(
                        1.5.dp,
                        when (order.status) {
                            OrderStatus.PENDING -> MaterialTheme.colorScheme.secondary
                            OrderStatus.PREPARING -> MaterialTheme.colorScheme.primary
                            OrderStatus.READY -> Color(0xFF4CAF50)
                            OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                        }
                    )
                ) {
                    Text(
                        text = order.status.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (order.status) {
                                OrderStatus.PENDING -> MaterialTheme.colorScheme.secondary
                                OrderStatus.PREPARING -> MaterialTheme.colorScheme.primary
                                OrderStatus.READY -> Color(0xFF4CAF50)
                                OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                            }
                        )
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Footer: Total + Método de pago + Tiempo estimado
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = order.paymentMethod.getDisplayName(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        order.estimatedTime?.let { time ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$time min",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
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
