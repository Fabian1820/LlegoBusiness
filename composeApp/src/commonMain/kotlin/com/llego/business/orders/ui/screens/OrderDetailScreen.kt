package com.llego.business.orders.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderItem
import com.llego.business.orders.data.model.OrderModificationState
import com.llego.business.orders.data.model.MenuItem
import com.llego.business.orders.data.model.PaymentAttemptStatus
import com.llego.business.orders.data.model.PaymentStatus
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.ui.components.OrderActionsSection
import com.llego.business.orders.ui.components.CustomerInfoSection
import com.llego.business.orders.ui.components.OrderItemsSection
import com.llego.business.orders.ui.components.OrderStatusSection
import com.llego.business.orders.ui.components.OrderTimelineSection
import com.llego.business.orders.ui.components.PaymentSummarySection
import com.llego.business.orders.ui.viewmodel.MenuItemsUiState
import com.llego.business.orders.ui.viewmodel.OrdersUiState
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.shared.ui.components.NetworkImage
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.utils.formatDouble
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PICKUP_DISABLED_REJECTION_REASON =
    "No estamos aceptando recogida en tienda en estos momentos"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OrderDetailScreen(
    order: Order,
    ordersViewModel: OrdersViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onCallPhone: ((String) -> Unit)? = null
) {
    val uiState by ordersViewModel.uiState.collectAsState()
    val orders by ordersViewModel.orders.collectAsState()
    val modificationState by ordersViewModel.modificationState.collectAsState()
    val menuItemsState by ordersViewModel.menuItemsState.collectAsState()
    val activePaymentAttempt by ordersViewModel.activePaymentAttempt.collectAsState()
    val customerCashKycStatus by ordersViewModel.customerCashKycStatus.collectAsState()
    val currentBranch by authViewModel.currentBranch.collectAsState()
    val currentOrder = orders.firstOrNull { it.id == order.id } ?: order
    val canEditItems = currentOrder.canBusinessModifyItems()
    val coroutineScope = rememberCoroutineScope()
    val shouldShowDeliveryFeeInput = !currentOrder.isPickupOrder() && currentBranch?.useAppMessaging == false
    val shouldLoadActivePaymentAttempt = currentOrder.status in setOf(
        OrderStatus.PENDING_PAYMENT,
        OrderStatus.PAYMENT_IN_PROGRESS
    ) && currentOrder.paymentStatus != PaymentStatus.COMPLETED
    val shouldShowConfirmPaymentReceived = shouldLoadActivePaymentAttempt &&
        activePaymentAttempt?.status == PaymentAttemptStatus.AWAITING_BUSINESS
    val paymentProofUrl = activePaymentAttempt?.proofUrl?.takeIf { it.isNotBlank() }

    val isActionInProgress = (uiState as? OrdersUiState.ActionInProgress)?.orderId == currentOrder.id
    var showPickupEnableDialog by rememberSaveable(order.id) { mutableStateOf(false) }
    var pickupPromptHandledForOrder by rememberSaveable(order.id) { mutableStateOf(false) }
    var isPickupPromptActionInProgress by remember { mutableStateOf(false) }
    var pickupPromptError by remember { mutableStateOf<String?>(null) }
    var showPaymentProofDialog by rememberSaveable(order.id) { mutableStateOf(false) }

    LaunchedEffect(
        currentOrder.id,
        currentOrder.deliveryMode,
        currentOrder.status,
        currentBranch?.id,
        currentBranch?.pickupEnabled
    ) {
        val shouldPromptPickupActivation = !pickupPromptHandledForOrder &&
            currentOrder.isPickupOrder() &&
            currentOrder.status == OrderStatus.PENDING_ACCEPTANCE &&
            currentBranch?.pickupEnabled == false

        if (shouldPromptPickupActivation) {
            pickupPromptHandledForOrder = true
            pickupPromptError = null
            showPickupEnableDialog = true
        }
    }

    LaunchedEffect(
        currentOrder.id,
        currentOrder.status,
        currentOrder.paymentStatus
    ) {
        if (shouldLoadActivePaymentAttempt) {
            ordersViewModel.loadActivePaymentAttempt(currentOrder.id)
        } else {
            ordersViewModel.clearActivePaymentAttempt()
        }
    }

    LaunchedEffect(
        currentOrder.id,
        currentOrder.customerId,
        currentOrder.branchId,
        currentOrder.businessId,
        currentBranch?.id,
        currentBranch?.businessId
    ) {
        val merchantId = currentOrder.businessId.takeIf { it.isNotBlank() } ?: currentBranch?.businessId
        val branchId = currentOrder.branchId.takeIf { it.isNotBlank() } ?: currentBranch?.id
        val customerId = currentOrder.customer?.id?.takeIf { it.isNotBlank() }
            ?: currentOrder.customerId.takeIf { it.isNotBlank() }

        if (merchantId.isNullOrBlank() || customerId.isNullOrBlank()) {
            ordersViewModel.clearCustomerCashKycStatus()
        } else {
            ordersViewModel.loadCustomerCashKycStatus(
                merchantId = merchantId,
                branchId = branchId,
                customerId = customerId
            )
        }
    }

    LaunchedEffect(currentOrder.branchId) {
        ordersViewModel.loadMenuItems(currentOrder.branchId)
    }

    LaunchedEffect(canEditItems, modificationState != null) {
        if (!canEditItems && modificationState != null) {
            ordersViewModel.cancelEdit()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pedido ${currentOrder.orderNumber}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
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
            OrderActionsSection(
                order = currentOrder,
                isActionInProgress = isActionInProgress,
                showDeliveryFeeInput = shouldShowDeliveryFeeInput,
                showConfirmPaymentReceived = shouldShowConfirmPaymentReceived,
                onConfirmPaymentReceived = {
                    val paymentAttemptId = activePaymentAttempt?.id ?: return@OrderActionsSection
                    ordersViewModel.confirmPaymentReceived(currentOrder.id, paymentAttemptId)
                },
                onAcceptOrder = { minutes, deliveryFee ->
                    ordersViewModel.acceptOrder(currentOrder.id, minutes, deliveryFee)
                },
                onRejectOrder = { reason ->
                    ordersViewModel.rejectOrder(currentOrder.id, reason)
                },
                onCancelOrder = { reason ->
                    ordersViewModel.cancelOrder(currentOrder.id, reason)
                },
                onStartPreparing = {
                    ordersViewModel.startPreparingOrder(currentOrder.id)
                },
                onMarkReady = {
                    ordersViewModel.markOrderReady(currentOrder.id)
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
            OrderStatusSection(order = currentOrder)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            CustomerInfoSection(
                customer = currentOrder.customer,
                customerCashKycStatus = customerCashKycStatus,
                onCallCustomer = onCallPhone
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            OrderItemsSection(items = currentOrder.items)
            if (canEditItems) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { ordersViewModel.enterEditMode(currentOrder) },
                        enabled = !isActionInProgress
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Modificar items")
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            PaymentSummarySection(order = currentOrder)

            if (shouldLoadActivePaymentAttempt) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                PaymentProofSection(
                    paymentAttemptStatus = activePaymentAttempt?.status,
                    proofUrl = paymentProofUrl,
                    onOpenProof = { showPaymentProofDialog = true }
                )
            }

            if (currentOrder.timeline.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                OrderTimelineSection(timeline = currentOrder.timeline)
            }
        }
    }

    if (modificationState != null && currentOrder.id == order.id) {
        EditOrderItemsDialog(
            order = currentOrder,
            state = modificationState!!,
            isSaving = isActionInProgress,
            onChangeQuantity = { itemId, quantity ->
                ordersViewModel.modifyItemQuantity(itemId, quantity)
            },
            onRemoveItem = { itemId ->
                ordersViewModel.removeItem(itemId)
            },
            onAddItem = { productId, name, price, quantity, imageUrl ->
                ordersViewModel.addItem(productId, name, price, quantity, imageUrl)
            },
            menuItemsState = menuItemsState,
            onSave = { reason ->
                ordersViewModel.applyModification(currentOrder.id, reason.ifBlank { "Modificado por el negocio" })
            },
            onCancel = {
                ordersViewModel.cancelEdit()
            }
        )
    }

    val actionError = (uiState as? OrdersUiState.ActionError)?.message
    if (actionError != null) {
        LaunchedEffect(actionError) {
            delay(3000)
            ordersViewModel.clearActionError()
        }
    }

    if (showPickupEnableDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Recogida en tienda desactivada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Esta sucursal tiene desactivada la recogida en tienda para pedidos pickup. " +
                            "Desea activarla ahora?"
                    )
                    pickupPromptError?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val branch = currentBranch ?: return@Button
                        coroutineScope.launch {
                            isPickupPromptActionInProgress = true
                            pickupPromptError = null
                            when (val result = authViewModel.updateBranch(
                                branchId = branch.id,
                                input = UpdateBranchInput(pickupEnabled = true)
                            )) {
                                is BusinessResult.Success -> {
                                    showPickupEnableDialog = false
                                }
                                is BusinessResult.Error -> {
                                    pickupPromptError = result.message
                                }
                                BusinessResult.Loading -> Unit
                            }
                            isPickupPromptActionInProgress = false
                        }
                    },
                    enabled = !isPickupPromptActionInProgress && currentBranch != null
                ) {
                    Text("Activar pickup")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPickupEnableDialog = false
                        ordersViewModel.rejectOrder(
                            orderId = currentOrder.id,
                            reason = PICKUP_DISABLED_REJECTION_REASON
                        )
                    },
                    enabled = !isPickupPromptActionInProgress
                ) {
                    Text("No activar y rechazar")
                }
            }
        )
    }

    if (showPaymentProofDialog && paymentProofUrl != null) {
        AlertDialog(
            onDismissRequest = { showPaymentProofDialog = false },
            title = { Text("Comprobante de pago") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activePaymentAttempt?.let { attempt ->
                        Text(
                            text = "Estado: ${attempt.status.getDisplayName()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    NetworkImage(
                        url = paymentProofUrl,
                        contentDescription = "Comprobante de pago",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaymentProofDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun PaymentProofSection(
    paymentAttemptStatus: PaymentAttemptStatus?,
    proofUrl: String?,
    onOpenProof: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Comprobante de pago",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        paymentAttemptStatus?.let { status ->
            Text(
                text = "Estado: ${status.getDisplayName()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (proofUrl.isNullOrBlank()) {
            Text(
                text = "Cliente confirmo sin comprobante.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Surface(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenProof() }
            ) {
                Column {
                    NetworkImage(
                        url = proofUrl,
                        contentDescription = "Vista previa del comprobante de pago",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = "Tocar para ampliar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

private fun PaymentAttemptStatus.getDisplayName(): String = when (this) {
    PaymentAttemptStatus.PENDING -> "Pendiente"
    PaymentAttemptStatus.PROCESSING -> "Procesando"
    PaymentAttemptStatus.AWAITING_PROOF -> "Esperando comprobante"
    PaymentAttemptStatus.AWAITING_BUSINESS -> "Esperando confirmacion del negocio"
    PaymentAttemptStatus.AWAITING_DELIVERY -> "Esperando confirmacion de reparto"
    PaymentAttemptStatus.COMPLETED -> "Completado"
    PaymentAttemptStatus.FAILED -> "Fallido"
    PaymentAttemptStatus.EXPIRED -> "Expirado"
    PaymentAttemptStatus.CANCELLED -> "Cancelado"
    PaymentAttemptStatus.DISPUTED -> "Disputado"
    PaymentAttemptStatus.REFUND_REQUESTED -> "Reembolso solicitado"
    PaymentAttemptStatus.REFUND_PROCESSING -> "Reembolso en proceso"
    PaymentAttemptStatus.REFUNDED -> "Reembolsado"
}

@Composable
private fun EditOrderItemsDialog(
    order: Order,
    state: OrderModificationState,
    isSaving: Boolean,
    onChangeQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onAddItem: (String, String, Double, Int, String) -> Unit,
    menuItemsState: MenuItemsUiState,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var reason by rememberSaveable(order.id) { mutableStateOf("Modificado por el negocio") }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onCancel()
        },
        title = { Text("Modificar items") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                AddOrderItemSection(
                    currency = order.currency,
                    menuItemsState = menuItemsState,
                    currentItems = state.modifiedItems,
                    onAddItem = onAddItem
                )

                if (state.modifiedItems.isEmpty()) {
                    Text(
                        text = "Debes dejar al menos un item en el pedido.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    state.modifiedItems.forEach { item ->
                        EditableOrderItemRow(
                            item = item,
                            onDecrease = {
                                if (item.quantity > 1) {
                                    onChangeQuantity(item.itemId, item.quantity - 1)
                                }
                            },
                            onIncrease = {
                                onChangeQuantity(item.itemId, item.quantity + 1)
                            },
                            onRemove = {
                                onRemoveItem(item.itemId)
                            }
                        )
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Total original: ${order.currency} ${formatDouble("%.2f", state.originalTotal)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Nuevo total: ${order.currency} ${formatDouble("%.2f", state.newTotal)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(reason) },
                enabled = !isSaving && state.hasChanges && state.modifiedItems.isNotEmpty()
            ) {
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !isSaving) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun AddOrderItemSection(
    currency: String,
    menuItemsState: MenuItemsUiState,
    currentItems: List<OrderItem>,
    onAddItem: (String, String, Double, Int, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf<String?>(null) }
    var quantityToAdd by remember { mutableStateOf(1) }

    val selectedItem = menuItemsState.items.firstOrNull { it.id == selectedProductId }
    val visibleItems = remember(searchQuery, menuItemsState.items) {
        val trimmedQuery = searchQuery.trim()
        val baseItems = if (trimmedQuery.isBlank()) {
            menuItemsState.items
        } else {
            menuItemsState.items.filter { item ->
                item.name.contains(trimmedQuery, ignoreCase = true)
            }
        }
        baseItems.take(8)
    }
    val alreadyInOrder = selectedItem?.id?.let { selectedId ->
        currentItems.any { item ->
            item.itemType.equals("PRODUCT", ignoreCase = true) && item.productId == selectedId
        }
    } == true

    Surface(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Agregar item",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                OutlinedButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(if (expanded) "Ocultar" else "Abrir")
                }
            }

            if (expanded) {
                when {
                    menuItemsState.isLoading -> {
                        Text(
                            text = "Cargando productos disponibles...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    menuItemsState.error != null -> {
                        Text(
                            text = menuItemsState.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    menuItemsState.items.isEmpty() -> {
                        Text(
                            text = "No hay productos disponibles para agregar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    else -> {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                if (selectedItem?.name != it) {
                                    selectedProductId = null
                                }
                            },
                            label = { Text("Buscar producto") },
                            placeholder = { Text("Escribe para filtrar") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (visibleItems.isNotEmpty()) {
                            Text(
                                text = if (searchQuery.isBlank()) "Sugeridos" else "Resultados",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                visibleItems.forEach { menuItem ->
                                    MenuItemPreviewRow(
                                        menuItem = menuItem,
                                        currency = currency,
                                        isSelected = menuItem.id == selectedProductId,
                                        onClick = {
                                            selectedProductId = menuItem.id
                                            searchQuery = menuItem.name
                                        }
                                    )
                                }
                            }
                        } else if (searchQuery.isNotBlank() && selectedItem == null) {
                            Text(
                                text = "Sin coincidencias para \"$searchQuery\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        selectedItem?.let { menuItem ->
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                            Text(
                                text = "Seleccionado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            MenuItemPreviewRow(
                                menuItem = menuItem,
                                currency = currency,
                                isSelected = true,
                                onClick = null
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { quantityToAdd = (quantityToAdd - 1).coerceAtLeast(1) }
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Reducir cantidad")
                                    }
                                    Text(
                                        text = quantityToAdd.toString(),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(onClick = { quantityToAdd += 1 }) {
                                        Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad")
                                    }
                                }

                                Button(
                                    onClick = {
                                        onAddItem(
                                            menuItem.id,
                                            menuItem.name,
                                            menuItem.price,
                                            quantityToAdd,
                                            menuItem.imageUrl.orEmpty()
                                        )
                                        searchQuery = ""
                                        selectedProductId = null
                                        quantityToAdd = 1
                                    }
                                ) {
                                    Text(if (alreadyInOrder) "Sumar" else "Agregar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemPreviewRow(
    menuItem: MenuItem,
    currency: String,
    isSelected: Boolean,
    onClick: (() -> Unit)?
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .let { base ->
                if (onClick != null) base.clickable(onClick = onClick) else base
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = menuItem.imageUrl?.takeIf { it.isNotBlank() }
            if (!imageUrl.isNullOrBlank()) {
                NetworkImage(
                    url = imageUrl,
                    contentDescription = menuItem.name,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = menuItem.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menuItem.name,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "$currency ${formatDouble("%.2f", menuItem.price)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Text(
                    text = "OK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EditableOrderItemRow(
    item: OrderItem,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val itemImageUrl = item.imageUrl?.takeIf { it.isNotBlank() }
                    ?: item.previewProducts.firstOrNull()?.imageUrl?.takeIf { it.isNotBlank() }
                if (!itemImageUrl.isNullOrBlank()) {
                    NetworkImage(
                        url = itemImageUrl,
                        contentDescription = item.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.name.take(1).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "$${formatDouble("%.2f", item.finalPrice)} c/u",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease) {
                        Icon(Icons.Default.Remove, contentDescription = "Reducir cantidad")
                    }
                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = onIncrease) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad")
                    }
                }

                Text(
                    text = "$${formatDouble("%.2f", item.lineTotal)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

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
    } catch (_: Exception) {
        timestamp
    }
}
