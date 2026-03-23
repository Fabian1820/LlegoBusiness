package com.llego.business.orders.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.llego.business.orders.ui.components.OrdersContent
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.orders.ui.viewmodel.OrdersUiState
import kotlinx.coroutines.delay

private const val ORDERS_AUTO_REFRESH_INTERVAL_MS = 2 * 60 * 1000L
private const val ORDERS_PULL_REFRESH_THRESHOLD_PX = 120f
private const val ORDERS_LOCAL_PAGE_SIZE = 20

/**
 * Pantalla de Pedidos con diseÃ±o moderno y profesional
 * Actualizada para usar Order y OrderStatus del nuevo modelo
 * Requirements: 9.1, 9.5, 5.5, 2.7, 2.8, 5.6, 5.7
 */
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    searchQuery: String = "",
    onNavigateToOrderDetail: (String) -> Unit = {},
    onShowConfirmation: ((ConfirmationType, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredOrders by viewModel.filteredOrders.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedDateRange by viewModel.selectedDateRange.collectAsState()
    val ordersListState = rememberLazyListState()

    var animateContent by remember { mutableStateOf(false) }
    var isPullRefreshing by remember { mutableStateOf(false) }
    var gesturePullDistance by remember { mutableStateOf(0f) }
    var shouldScrollToTopAfterRefresh by remember { mutableStateOf(false) }
    var visibleItemsByFilter by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val latestUiState by rememberUpdatedState(uiState)
    val latestPullRefreshing by rememberUpdatedState(isPullRefreshing)
    val paginationKey = "${selectedDateRange.name}_${selectedFilter?.name ?: "ALL"}"
    val normalizedSearchQuery = searchQuery.trim().lowercase()
    val searchedOrders = if (normalizedSearchQuery.isBlank()) {
        filteredOrders
    } else {
        filteredOrders.filter { order ->
            order.orderNumber.contains(normalizedSearchQuery, ignoreCase = true) ||
                order.items.any { item ->
                    item.name.contains(normalizedSearchQuery, ignoreCase = true)
                }
        }
    }
    val visibleCount = visibleItemsByFilter[paginationKey] ?: ORDERS_LOCAL_PAGE_SIZE
    val paginatedOrders = if (normalizedSearchQuery.isBlank()) {
        searchedOrders.take(visibleCount)
    } else {
        searchedOrders
    }
    val canLoadMore = normalizedSearchQuery.isBlank() && paginatedOrders.size < searchedOrders.size
    val canTriggerGestureRefresh = uiState !is OrdersUiState.Loading &&
        uiState !is OrdersUiState.ActionInProgress &&
        !isPullRefreshing
    val isAtTop = paginatedOrders.isEmpty() ||
        (ordersListState.firstVisibleItemIndex == 0 && ordersListState.firstVisibleItemScrollOffset == 0)

    // Scroll to top when filters or search change.
    LaunchedEffect(selectedFilter, selectedDateRange, normalizedSearchQuery) {
        if (ordersListState.firstVisibleItemIndex != 0 || ordersListState.firstVisibleItemScrollOffset != 0) {
            ordersListState.scrollToItem(0)
        }
    }

    LaunchedEffect(paginationKey) {
        if (!visibleItemsByFilter.containsKey(paginationKey)) {
            visibleItemsByFilter = visibleItemsByFilter + (paginationKey to ORDERS_LOCAL_PAGE_SIZE)
        }
    }

    // AnimaciÃ³n de entrada idÃ©ntica a Perfil, GestiÃ³n y MenÃº
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    LaunchedEffect(uiState, isPullRefreshing) {
        if (isPullRefreshing && uiState !is OrdersUiState.Loading) {
            isPullRefreshing = false
        }
    }

    LaunchedEffect(
        shouldScrollToTopAfterRefresh,
        isPullRefreshing,
        uiState,
        paginatedOrders.firstOrNull()?.id
    ) {
        val refreshCompleted = !isPullRefreshing && uiState !is OrdersUiState.Loading
        if (shouldScrollToTopAfterRefresh && refreshCompleted) {
            if (paginatedOrders.isNotEmpty()) {
                ordersListState.animateScrollToItem(0)
            }
            shouldScrollToTopAfterRefresh = false
        }
    }

    LaunchedEffect(Unit) {
        // Refresco automatico cada 2 minutos mientras la pantalla esta activa.
        while (true) {
            delay(ORDERS_AUTO_REFRESH_INTERVAL_MS)
            val canRefresh = latestUiState !is OrdersUiState.Loading &&
                latestUiState !is OrdersUiState.ActionInProgress &&
                !latestPullRefreshing

            if (canRefresh) {
                viewModel.refreshOrders()
            }
        }
    }

    val refreshFromGesture = {
        if (uiState !is OrdersUiState.Loading && !isPullRefreshing) {
            shouldScrollToTopAfterRefresh = true
            isPullRefreshing = true
            viewModel.refreshOrders()
        }
    }

    PullToRefreshBox(
        isRefreshing = isPullRefreshing,
        onRefresh = refreshFromGesture,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(canTriggerGestureRefresh, isAtTop) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        if (!canTriggerGestureRefresh || !isAtTop) return@detectVerticalDragGestures

                        if (dragAmount > 0f) {
                            gesturePullDistance += dragAmount
                            if (gesturePullDistance >= ORDERS_PULL_REFRESH_THRESHOLD_PX) {
                                refreshFromGesture()
                                gesturePullDistance = 0f
                            }
                        } else if (dragAmount < 0f) {
                            gesturePullDistance = 0f
                        }
                    },
                    onDragEnd = {
                        gesturePullDistance = 0f
                    },
                    onDragCancel = {
                        gesturePullDistance = 0f
                    }
                )
            }
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val currentState = uiState) {
            is OrdersUiState.Loading -> {
                if (paginatedOrders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    OrdersContent(
                        animateContent = animateContent,
                        filteredOrders = paginatedOrders,
                        selectedFilter = selectedFilter,
                        selectedDateRange = selectedDateRange,
                        ordersListState = ordersListState,
                        searchQuery = searchQuery,
                        canLoadMore = canLoadMore,
                        onLoadMore = {
                            val updatedCount = (visibleItemsByFilter[paginationKey]
                                ?: ORDERS_LOCAL_PAGE_SIZE) + ORDERS_LOCAL_PAGE_SIZE
                            visibleItemsByFilter = visibleItemsByFilter + (paginationKey to updatedCount)
                        },
                        onNavigateToOrderDetail = onNavigateToOrderDetail,
                        actionInProgressOrderId = null,
                        onDateRangeSelected = { viewModel.setDateRangeFilter(it) },
                        onStatusSelected = { viewModel.setFilter(it) },
                        onStatusCleared = { viewModel.clearFilter() }
                    )
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
                    filteredOrders = paginatedOrders,
                    selectedFilter = selectedFilter,
                    selectedDateRange = selectedDateRange,
                    ordersListState = ordersListState,
                    searchQuery = searchQuery,
                    canLoadMore = canLoadMore,
                    onLoadMore = {
                        val updatedCount = (visibleItemsByFilter[paginationKey]
                            ?: ORDERS_LOCAL_PAGE_SIZE) + ORDERS_LOCAL_PAGE_SIZE
                        visibleItemsByFilter = visibleItemsByFilter + (paginationKey to updatedCount)
                    },
                    onNavigateToOrderDetail = onNavigateToOrderDetail,
                    actionInProgressOrderId = currentState.orderId,
                    onDateRangeSelected = { viewModel.setDateRangeFilter(it) },
                    onStatusSelected = { viewModel.setFilter(it) },
                    onStatusCleared = { viewModel.clearFilter() }
                )
            }
            is OrdersUiState.ActionError -> {
                // Requirements: 5.7 - Mostrar error de acciÃ³n
                OrdersContent(
                    animateContent = animateContent,
                    filteredOrders = paginatedOrders,
                    selectedFilter = selectedFilter,
                    selectedDateRange = selectedDateRange,
                    ordersListState = ordersListState,
                    searchQuery = searchQuery,
                    canLoadMore = canLoadMore,
                    onLoadMore = {
                        val updatedCount = (visibleItemsByFilter[paginationKey]
                            ?: ORDERS_LOCAL_PAGE_SIZE) + ORDERS_LOCAL_PAGE_SIZE
                        visibleItemsByFilter = visibleItemsByFilter + (paginationKey to updatedCount)
                    },
                    onNavigateToOrderDetail = onNavigateToOrderDetail,
                    actionInProgressOrderId = null,
                    onDateRangeSelected = { viewModel.setDateRangeFilter(it) },
                    onStatusSelected = { viewModel.setFilter(it) },
                    onStatusCleared = { viewModel.clearFilter() }
                )
                // Mostrar snackbar de error
                LaunchedEffect(currentState.message) {
                    // El error se mostrarÃ¡ en un Snackbar manejado por el scaffold padre
                    delay(3000)
                    viewModel.clearActionError()
                }
            }
            is OrdersUiState.Success -> {
                OrdersContent(
                    animateContent = animateContent,
                    filteredOrders = paginatedOrders,
                    selectedFilter = selectedFilter,
                    selectedDateRange = selectedDateRange,
                    ordersListState = ordersListState,
                    searchQuery = searchQuery,
                    canLoadMore = canLoadMore,
                    onLoadMore = {
                        val updatedCount = (visibleItemsByFilter[paginationKey]
                            ?: ORDERS_LOCAL_PAGE_SIZE) + ORDERS_LOCAL_PAGE_SIZE
                        visibleItemsByFilter = visibleItemsByFilter + (paginationKey to updatedCount)
                    },
                    onNavigateToOrderDetail = onNavigateToOrderDetail,
                    actionInProgressOrderId = null,
                    onDateRangeSelected = { viewModel.setDateRangeFilter(it) },
                    onStatusSelected = { viewModel.setFilter(it) },
                    onStatusCleared = { viewModel.clearFilter() }
                )
            }
        }
    }
}
