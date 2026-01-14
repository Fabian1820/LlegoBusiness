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
import com.llego.business.orders.ui.components.EstimatedTimeSection
import com.llego.business.orders.ui.components.EditableOrderItemsSection
import com.llego.business.orders.ui.components.OrderActionsSection
import com.llego.business.orders.ui.components.OrderItemsSection
import com.llego.business.orders.ui.components.OrderStatusSection
import com.llego.business.orders.ui.components.OrderTotalsComparisonSection
import com.llego.business.orders.ui.components.PaymentSummarySection
import com.llego.business.orders.ui.components.SpecialNotesSection
import com.llego.business.orders.ui.viewmodel.OrdersViewModel

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
                            fontWeight = FontWeight.SemiBold
                        )
                    )
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
        containerColor = MaterialTheme.colorScheme.background
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

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

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

                // Notas especiales
                if (order.specialNotes != null) {
                    SpecialNotesSection(notes = order.specialNotes)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
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
