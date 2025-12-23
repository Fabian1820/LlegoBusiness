package com.llego.nichos.restaurant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.components.orders.EstimatedTimeSection
import com.llego.nichos.restaurant.ui.components.orders.EditableOrderItemsSection
import com.llego.nichos.restaurant.ui.components.orders.OrderActionsSection
import com.llego.nichos.restaurant.ui.components.orders.OrderItemsSection
import com.llego.nichos.restaurant.ui.components.orders.OrderStatusSection
import com.llego.nichos.restaurant.ui.components.orders.OrderTotalsComparisonSection
import com.llego.nichos.restaurant.ui.components.orders.PaymentSummarySection
import com.llego.nichos.restaurant.ui.components.orders.SpecialNotesSection
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel

/**
 * Pantalla completa de detalle del pedido con gestión de estados
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: Order,
    ordersViewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit,
    onAcceptOrder: (Int) -> Unit,
    onSubmitModification: ((Order, OrderModificationState, String) -> Unit)? = null
) {
    val modificationState by ordersViewModel.modificationState.collectAsState()
    val availableMenuItems by ordersViewModel.menuItems.collectAsState()
    val isEditMode = modificationState?.isEditMode == true
    val currentItems = modificationState?.modifiedItems.orEmpty()

    DisposableEffect(order.id) {
        onDispose {
            ordersViewModel.exitEditMode()
        }
    }

    LaunchedEffect(order.id, order.status) {
        if (order.status != OrderStatus.PENDING) {
            ordersViewModel.exitEditMode()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pedido ${order.orderNumber}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Estado actual del pedido
                OrderStatusSection(order = order)

                HorizontalDivider()

                // Items del pedido
                if (isEditMode) {
                    EditableOrderItemsSection(
                        originalItems = modificationState?.originalItems.orEmpty(),
                        items = currentItems,
                        availableMenuItems = availableMenuItems,
                        onIncreaseQuantity = { itemId ->
                            val item = currentItems.firstOrNull { it.id == itemId } ?: return@EditableOrderItemsSection
                            ordersViewModel.modifyItemQuantity(itemId, item.quantity + 1)
                        },
                        onDecreaseQuantity = { itemId ->
                            val item = currentItems.firstOrNull { it.id == itemId } ?: return@EditableOrderItemsSection
                            ordersViewModel.modifyItemQuantity(itemId, item.quantity - 1)
                        },
                        onRemoveItem = { itemId -> ordersViewModel.removeItem(itemId) },
                        onUpdateInstructions = { itemId, instructions ->
                            ordersViewModel.modifyItemInstructions(itemId, instructions)
                        },
                        onAddItem = { menuItem, quantity, instructions ->
                            ordersViewModel.addItem(menuItem, quantity, instructions)
                        }
                    )
                } else {
                    OrderItemsSection(items = order.items)
                }

                HorizontalDivider()

                // Notas especiales
                if (order.specialNotes != null) {
                    SpecialNotesSection(notes = order.specialNotes)
                    HorizontalDivider()
                }

                // Resumen de pago
                PaymentSummarySection(
                    paymentMethod = order.paymentMethod,
                    total = order.total
                )

                if (isEditMode && modificationState != null) {
                    OrderTotalsComparisonSection(modificationState = modificationState!!)
                }

                // Tiempo estimado
                order.estimatedTime?.let { time ->
                    EstimatedTimeSection(estimatedTime = time)
                }
            }

            // Footer con acciones
            OrderActionsSection(
                orderStatus = order.status,
                modificationState = modificationState,
                onAcceptOrder = { minutes -> onAcceptOrder(minutes) },
                onRejectOrder = { onUpdateStatus(OrderStatus.CANCELLED) },
                onUpdateStatus = onUpdateStatus,
                onEnterEditMode = { ordersViewModel.enterEditMode(order) },
                onCancelEdit = { ordersViewModel.cancelEdit() },
                onConfirmModification = { note ->
                    val state = modificationState ?: return@OrderActionsSection
                    onSubmitModification?.invoke(order, state, note)
                    ordersViewModel.applyModification(order.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
