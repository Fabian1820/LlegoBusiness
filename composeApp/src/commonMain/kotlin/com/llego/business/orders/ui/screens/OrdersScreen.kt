package com.llego.business.orders.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llego.business.orders.ui.components.OrdersContent
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.orders.ui.viewmodel.OrdersUiState
import kotlinx.coroutines.delay

/**
 * Pantalla de Pedidos con diseÃ±o moderno y profesional
 * Actualizada para usar Order y OrderStatus del nuevo modelo
 * Requirements: 9.1, 9.5, 5.5, 2.7, 2.8, 5.6, 5.7
 */
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
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

    // Scroll to top when filters change
    LaunchedEffect(selectedFilter, selectedDateRange) {
        if (filteredOrders.isNotEmpty()) {
            ordersListState.animateScrollToItem(0)
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

    val refreshFromGesture = {
        if (uiState !is OrdersUiState.Loading && !isPullRefreshing) {
            isPullRefreshing = true
            viewModel.refreshOrders()
        }
    }

    PullToRefreshBox(
        isRefreshing = isPullRefreshing,
        onRefresh = refreshFromGesture,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val currentState = uiState) {
            is OrdersUiState.Loading -> {
                if (filteredOrders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    OrdersContent(
                        animateContent = animateContent,
                        filteredOrders = filteredOrders,
                        selectedFilter = selectedFilter,
                        selectedDateRange = selectedDateRange,
                        ordersListState = ordersListState,
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
                    filteredOrders = filteredOrders,
                    selectedFilter = selectedFilter,
                    selectedDateRange = selectedDateRange,
                    ordersListState = ordersListState,
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
                    filteredOrders = filteredOrders,
                    selectedFilter = selectedFilter,
                    selectedDateRange = selectedDateRange,
                    ordersListState = ordersListState,
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
                    filteredOrders = filteredOrders,
                    selectedFilter = selectedFilter,
                    selectedDateRange = selectedDateRange,
                    ordersListState = ordersListState,
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
