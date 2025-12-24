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
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersUiState
import com.llego.nichos.restaurant.ui.viewmodel.DateRangeFilter
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
    val selectedDateRange by viewModel.selectedDateRange.collectAsState()
    val statusFilterListState = rememberLazyListState()
    val dateFilterListState = rememberLazyListState()

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
                            // Filtros estilo iOS 16
                            iOSStyleFilters(
                                selectedDateRange = selectedDateRange,
                                onDateRangeSelected = { viewModel.setDateRangeFilter(it) },
                                selectedStatus = selectedFilter,
                                onStatusSelected = { viewModel.setFilter(it) },
                                onStatusCleared = { viewModel.clearFilter() },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            EmptyOrdersView(hasFilter = selectedFilter != null || selectedDateRange != DateRangeFilter.TODAY)
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    top = 80.dp, // Espacio para los filtros
                                    bottom = 16.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
                            
                            // Filtros flotantes por encima
                            iOSStyleFilters(
                                selectedDateRange = selectedDateRange,
                                onDateRangeSelected = { viewModel.setDateRangeFilter(it) },
                                selectedStatus = selectedFilter,
                                onStatusSelected = { viewModel.setFilter(it) },
                                onStatusCleared = { viewModel.clearFilter() },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .zIndex(10f) // Por encima del contenido
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filtros estilo iOS 16 - Dos selectores lado a lado que despliegan opciones animadas
 */
@Composable
private fun iOSStyleFilters(
    selectedDateRange: DateRangeFilter,
    onDateRangeSelected: (DateRangeFilter) -> Unit,
    selectedStatus: OrderStatus?,
    onStatusSelected: (OrderStatus) -> Unit,
    onStatusCleared: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStatusPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Selector de fecha
        iOSStylePicker(
            label = "Fecha",
            selectedValue = selectedDateRange.displayName,
            isExpanded = showDatePicker,
            onToggle = { showDatePicker = !showDatePicker },
            modifier = Modifier.weight(1f)
        ) {
            DateRangeFilter.values().filter { it != DateRangeFilter.CUSTOM }.forEach { range ->
                iOSStylePickerOption(
                    text = range.displayName,
                    isSelected = selectedDateRange == range,
                    onClick = {
                        onDateRangeSelected(range)
                        showDatePicker = false
                    }
                )
            }
        }

        // Selector de estado
        iOSStylePicker(
            label = "Estado",
            selectedValue = selectedStatus?.getDisplayName() ?: "Todos",
            isExpanded = showStatusPicker,
            onToggle = { showStatusPicker = !showStatusPicker },
            modifier = Modifier.weight(1f)
        ) {
            iOSStylePickerOption(
                text = "Todos",
                isSelected = selectedStatus == null,
                onClick = {
                    onStatusCleared()
                    showStatusPicker = false
                }
            )
            OrderStatus.values().forEach { status ->
                iOSStylePickerOption(
                    text = status.getDisplayName(),
                    isSelected = selectedStatus == status,
                    onClick = {
                        onStatusSelected(status)
                        showStatusPicker = false
                    }
                )
            }
        }
    }
}

/**
 * Selector estilo iOS 16 con animación de despliegue
 */
@Composable
private fun iOSStylePicker(
    label: String,
    selectedValue: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Botón principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isExpanded) 8.dp else 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = selectedValue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Opciones desplegables con animación - Overlay por encima
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                expandFrom = Alignment.Top
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                shrinkTowards = Alignment.Top
            ) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 60.dp)
                .zIndex(20f) // Por encima de todo
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * Opción individual del picker estilo iOS
 */
@Composable
private fun iOSStylePickerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
                )
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    if (!isSelected) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color(0xFFE0E0E0),
            thickness = 0.5.dp
        )
    }
}

/**
 * Chips de filtro por rango de fecha (deprecated - usar iOSStyleFilters)
 */
@Composable
private fun DateRangeFilterChips(
    selectedRange: DateRangeFilter,
    onRangeSelected: (DateRangeFilter) -> Unit,
    listState: LazyListState
) {
    val allRanges = DateRangeFilter.values().filter { it != DateRangeFilter.CUSTOM }

    LaunchedEffect(selectedRange, allRanges) {
        val targetIndex = allRanges.indexOf(selectedRange).coerceAtLeast(0)
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val targetInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }
            if (targetInfo != null) {
                val start = targetInfo.offset
                val end = targetInfo.offset + targetInfo.size
                val viewportStart = layoutInfo.viewportStartOffset
                val viewportEnd = layoutInfo.viewportEndOffset
                if (start < viewportStart || end > viewportEnd) {
                    listState.animateScrollToItem(targetIndex)
                }
            }
        }
    }

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
                items(allRanges) { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { onRangeSelected(range) },
                        label = {
                            Text(
                                range.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedRange == range) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        leadingIcon = if (selectedRange == range) {
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
 * Chips de filtro por estado contenidos en un card con fade en ambos lados
 */
@Composable
private fun StatusFilterChips(
    selectedFilter: OrderStatus?,
    onFilterSelected: (OrderStatus) -> Unit,
    onClearFilter: () -> Unit,
    listState: LazyListState
) {
    val allStatuses = OrderStatus.values().toList()

    LaunchedEffect(selectedFilter, allStatuses) {
        val targetIndex = if (selectedFilter == null) {
            0
        } else {
            val index = allStatuses.indexOf(selectedFilter)
            if (index >= 0) index + 1 else 0 // +1 para compensar el chip "Todos"
        }
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isEmpty()) {
            listState.scrollToItem(targetIndex)
            return@LaunchedEffect
        }
        val targetInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }
        if (targetInfo != null) {
            val start = targetInfo.offset
            val end = targetInfo.offset + targetInfo.size
            val viewportStart = layoutInfo.viewportStartOffset
            val viewportEnd = layoutInfo.viewportEndOffset
            if (start >= viewportStart && end <= viewportEnd) {
                return@LaunchedEffect
            }
        }
        listState.animateScrollToItem(targetIndex)
    }

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
                // Chip "Todos" para estado
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
