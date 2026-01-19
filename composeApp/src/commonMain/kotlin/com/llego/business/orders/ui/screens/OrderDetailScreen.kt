package com.llego.business.orders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.*
import com.llego.business.orders.ui.components.*
import com.llego.business.orders.ui.viewmodel.OrdersUiState
import com.llego.business.orders.ui.viewmodel.OrdersViewModel

/**
 * Pantalla de detalle del pedido con datos del backend
 * 
 * Requirements:
 * - 10.1, 10.5: Información del cliente
 * - 10.2, 10.3: Dirección de entrega con mapa
 * - 10.4: Información del repartidor
 * - 8.1, 8.2, 8.3, 8.4: Timeline del pedido
 * - 7.1, 7.2, 7.3, 7.4: Comentarios
 * - 5.5, 6.1: Botones de acción según estado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: Order,
    ordersViewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    onCallPhone: ((String) -> Unit)? = null
) {
    val uiState by ordersViewModel.uiState.collectAsState()
    val isActionInProgress = uiState is OrdersUiState.ActionInProgress &&
            (uiState as OrdersUiState.ActionInProgress).orderId == order.id
    
    // Obtener el pedido actualizado de la lista (para reflejar cambios en tiempo real)
    val currentOrder by remember(order.id) {
        derivedStateOf {
            ordersViewModel.getOrderById(order.id) ?: order
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pedido ${currentOrder.orderNumber}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = formatOrderDate(currentOrder.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // Acciones del pedido según estado - Requirements: 5.5, 6.1
            OrderActionsSection(
                order = currentOrder,
                isActionInProgress = isActionInProgress,
                onAcceptOrder = { minutes ->
                    ordersViewModel.acceptOrder(currentOrder.id, minutes)
                },
                onRejectOrder = { reason ->
                    ordersViewModel.rejectOrder(currentOrder.id, reason)
                },
                onMarkReady = {
                    ordersViewModel.markOrderReady(currentOrder.id)
                },
                onEditItems = {
                    ordersViewModel.enterEditMode(currentOrder)
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado actual del pedido - Requirements: 5.5
            OrderStatusSection(order = currentOrder)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            // Información del cliente - Requirements: 10.1, 10.5
            CustomerInfoSection(
                customer = currentOrder.customer,
                onCallCustomer = onCallPhone
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            // Dirección de entrega - Requirements: 10.2, 10.3
            DeliveryAddressSection(
                deliveryAddress = currentOrder.deliveryAddress,
                showMap = true
            )

            // Información del repartidor si está asignado - Requirements: 10.4
            currentOrder.deliveryPerson?.let { deliveryPerson ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                DeliveryPersonSection(
                    deliveryPerson = deliveryPerson,
                    onCallDeliveryPerson = onCallPhone
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            // Items del pedido
            OrderItemsSection(items = currentOrder.items)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            // Resumen de pago
            PaymentSummarySection(order = currentOrder)

            // Timeline del pedido - Requirements: 8.1, 8.2, 8.3, 8.4
            if (currentOrder.timeline.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                OrderTimelineSection(timeline = currentOrder.timeline)
            }

            // Comentarios - Requirements: 7.1, 7.2, 7.3, 7.4
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            OrderCommentsSection(
                comments = currentOrder.comments,
                onAddComment = { message ->
                    ordersViewModel.addOrderComment(currentOrder.id, message)
                },
                isAddingComment = isActionInProgress
            )

            // Espacio extra para el bottom bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Mostrar error si hay
    val actionError = (uiState as? OrdersUiState.ActionError)?.message
    if (actionError != null) {
        LaunchedEffect(actionError) {
            // El error se mostrará en un Snackbar manejado por el scaffold padre
            kotlinx.coroutines.delay(3000)
            ordersViewModel.clearActionError()
        }
    }
}

/**
 * Formatea la fecha del pedido para mostrar en el header
 */
private fun formatOrderDate(timestamp: String): String {
    return try {
        val parts = timestamp.split("T")
        if (parts.size == 2) {
            val datePart = parts[0]
            val timePart = parts[1].substringBefore(".")
            val timeComponents = timePart.split(":")
            if (timeComponents.size >= 2) {
                "$datePart ${timeComponents[0]}:${timeComponents[1]}"
            } else {
                timestamp
            }
        } else {
            timestamp
        }
    } catch (e: Exception) {
        timestamp
    }
}


