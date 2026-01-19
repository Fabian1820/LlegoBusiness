package com.llego.business.orders.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.llego.business.orders.data.model.*
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.orders.ui.viewmodel.OrdersUiState
import com.llego.business.orders.ui.viewmodel.DateRangeFilter
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoSuccess
import kotlinx.coroutines.delay

/**
 * Pantalla de Pedidos con diseño moderno y profesional
 * Actualizada para usar Order y OrderStatus del nuevo modelo
 * Requirements: 9.1, 9.5, 5.5, 2.7, 2.8, 5.6, 5.7
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
    val ordersListState = rememberLazyListState()

    var animateContent by remember { mutableStateOf(false) }

    // Scroll to top when filters change
    LaunchedEffect(selectedFilter, selectedDateRange) {
        if (filteredOrders.isNotEmpty()) {
            ordersListState.animateScrollToItem(0)
        }
    }

    // Animación de entrada idéntica a Perfil, Gestión y Menú
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val currentState = uiState) {
            is OrdersUiState.Loading -> {
                // Requirements: 2.7 - Mostrar indicador de carga durante queries
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is OrdersUiState.Error -> {
                // Requirements: 2.8 - Mostrar error con opción de reintentar
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
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadOrders() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is OrdersUiState.ActionInProgress -> {
                // Requirements: 5.6 - Mostrar loading durante acciones
                // Mostrar contenido con overlay de carga
                OrdersContent(
                    animateContent = animateContent,
                    filteredOrders = filteredOrders,
                    selectedFilter = selectedFilter,
                    selectedDateRange = selectedDateRange,
                    ordersListState = ordersListState,
                    viewModel = viewModel,
                    onNavigateToOrderDetail = onNavigateToOrderDetail,
                    actionInProgressOrderId = currentState.orderId
                )
            }
            is OrdersUiState.ActionError -> {
                // Requirements: 5.7 - Mostrar error de acción
                OrdersContent(
                    animateContent = animateContent,
                    filteredOrders = filteredOrders,
                    selectedFilter = selectedFilter,
                    selectedDateRange = selectedDateRange,
                    ordersListState = ordersListState,
                    viewModel = viewModel,
                    onNavigateToOrderDetail = onNavigateToOrderDetail,
                    actionInProgressOrderId = null
                )
                // Mostrar snackbar de error
                LaunchedEffect(currentState.message) {
                    // El error se mostrará en un Snackbar manejado por el scaffold padre
                    delay(3000)
                    viewModel.clearActionError()
                }
            }
            is OrdersUiState.Success -> {
                OrdersContent(
                    animateContent = animateContent,
                    filteredOrders = filteredOrders,
                    selectedFilter = selectedFilter,
                    selectedDateRange = selectedDateRange,
                    ordersListState = ordersListState,
                    viewModel = viewModel,
                    onNavigateToOrderDetail = onNavigateToOrderDetail,
                    actionInProgressOrderId = null
                )
            }
        }
    }
}

/**
 * Contenido principal de la pantalla de pedidos
 */
@Composable
private fun OrdersContent(
    animateContent: Boolean,
    filteredOrders: List<Order>,
    selectedFilter: OrderStatus?,
    selectedDateRange: DateRangeFilter,
    ordersListState: LazyListState,
    viewModel: OrdersViewModel,
    onNavigateToOrderDetail: (String) -> Unit,
    actionInProgressOrderId: String?
) {
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
                    state = ordersListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 100.dp, // Espacio mayor para los filtros
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
                            isActionInProgress = order.id == actionInProgressOrderId,
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

/**
 * Filtros estilo iOS 16 - Dos selectores lado a lado que despliegan opciones animadas
 * Actualizado para usar OrderStatus del nuevo modelo
 * Requirements: 9.1, 9.5
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

        // Selector de estado - Requirements: 9.1, 9.5 - Usar OrderStatus con nombres localizados
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
 * Selector estilo iOS 16 con animación de despliegue - Sin ripple para evitar delay visual
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
    // Animación suave para la elevación
    val elevation by animateDpAsState(
        targetValue = if (isExpanded) 4.dp else 1.dp,
        animationSpec = tween(150),
        label = "picker_elevation"
    )

    Box(modifier = modifier) {
        // Botón principal - Surface en lugar de Card para mejor control
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onToggle,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = LlegoCustomShapes.secondaryButton,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = elevation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedValue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Opciones desplegables con animación rápida
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(200, easing = EaseOutCubic),
                expandFrom = Alignment.Top
            ) + fadeIn(animationSpec = tween(150)),
            exit = shrinkVertically(
                animationSpec = tween(150, easing = EaseInCubic),
                shrinkTowards = Alignment.Top
            ) + fadeOut(animationSpec = tween(100)),
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 60.dp)
                .zIndex(20f)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = LlegoCustomShapes.secondaryButton,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
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
            .clickable(
                onClick = onClick,
                indication = null, // Quitar ripple
                interactionSource = remember { MutableInteractionSource() }
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
            modifier = Modifier.padding(horizontal = 14.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
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

    val surfaceColor = MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = LlegoCustomShapes.secondaryButton,
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
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
                                    surfaceColor,
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
                                    surfaceColor
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
 * Actualizado para usar OrderStatus del nuevo modelo
 * Requirements: 9.1, 9.5
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

    val surfaceColor = MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = LlegoCustomShapes.secondaryButton,
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
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
                                    surfaceColor,
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
                                    surfaceColor
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
 * Actualizado para usar Order del nuevo modelo
 * Requirements: 5.5 - Mostrar status con colores correctos, paymentStatus, estimatedMinutesRemaining
 */
@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    isActionInProgress: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isActionInProgress, onClick = onClick),
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
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = "Domicilio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(6.dp)
                        )
                    }
                    Text(
                        text = order.orderNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Badge de estado con colores del backend - Requirements: 5.5
                val statusColor = order.status.getColor()
                Surface(
                    shape = LlegoCustomShapes.secondaryButton,
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        statusColor.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = order.status.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            // Footer: Total + Método de pago + Estado de pago + Tiempo estimado
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
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${order.currency} ${order.total}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        // Método de pago
                        Surface(
                            shape = LlegoCustomShapes.secondaryButton,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = order.paymentMethod,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        // Estado de pago - Requirements: 5.5
                        val paymentStatusColor = order.paymentStatus.getColor()
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = when (order.paymentStatus) {
                                    PaymentStatus.COMPLETED -> Icons.Default.CheckCircle
                                    PaymentStatus.VALIDATED -> Icons.Default.Verified
                                    PaymentStatus.PENDING -> Icons.Default.Schedule
                                    PaymentStatus.FAILED -> Icons.Default.Error
                                },
                                contentDescription = null,
                                tint = paymentStatusColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = order.paymentStatus.getDisplayName(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = paymentStatusColor
                            )
                        }
                        
                        // Tiempo estimado restante - Requirements: 5.5
                        order.estimatedMinutesRemaining?.let { minutes ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (minutes <= 5) MaterialTheme.colorScheme.error 
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$minutes min",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (minutes <= 5) MaterialTheme.colorScheme.error 
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Overlay de carga durante acciones - Requirements: 5.6
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = if (hasFilter) {
                    "No hay pedidos con este filtro"
                } else {
                    "No hay pedidos activos"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasFilter) {
                Text(
                    text = "Intenta con otro filtro",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

