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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.llego.business.orders.data.model.OrderComboModifier
import com.llego.business.orders.data.model.OrderComboSelectedOption
import com.llego.business.orders.data.model.OrderComboSelection
import com.llego.business.orders.data.model.OrderItem
import com.llego.business.orders.data.model.OrderModificationState
import com.llego.business.orders.data.model.MenuItem
import com.llego.shared.data.model.Combo
import com.llego.shared.data.model.ComboOption
import com.llego.shared.data.model.ComboSlot
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
    val isRefreshing by ordersViewModel.isRefreshing.collectAsState()
    val orders by ordersViewModel.orders.collectAsState()
    val modificationState by ordersViewModel.modificationState.collectAsState()
    val menuItemsState by ordersViewModel.menuItemsState.collectAsState()
    val comboDefinitions by ordersViewModel.comboDefinitions.collectAsState()
    val loadingComboIds by ordersViewModel.loadingComboIds.collectAsState()
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
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        // Número corto destacado + fecha en la misma línea
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "#${orderShortNumber(currentOrder.orderNumber)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatOrderDate(currentOrder.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Prefijo del número completo en gris pequeño
                        val prefix = orderNumberPrefix(currentOrder.orderNumber)
                        if (prefix.isNotBlank()) {
                            Text(
                                text = prefix,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                ordersViewModel.refreshOrderDetail(
                    orderId = currentOrder.id,
                    branchId = currentOrder.branchId.ifBlank { currentBranch?.id.orEmpty() }
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                            onClick = {
                                ordersViewModel.loadMenuItems(currentOrder.branchId, forceRefresh = true)
                                ordersViewModel.enterEditMode(currentOrder)
                            },
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
    }

    var comboBeingEdited by remember(order.id) { mutableStateOf<OrderItem?>(null) }

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
            onEditCombo = { item ->
                val comboId = item.comboId ?: item.itemId
                ordersViewModel.loadComboDefinition(comboId)
                comboBeingEdited = item
            },
            onSave = { reason ->
                ordersViewModel.applyModification(currentOrder.id, reason.ifBlank { "Modificado por el negocio" })
            },
            onCancel = {
                ordersViewModel.cancelEdit()
            }
        )
    }

    // Diálogo dedicado de edición de opciones del combo (superpuesto al diálogo principal)
    comboBeingEdited?.let { editingItem ->
        // Busca la versión actualizada del item en modificationState (puede haber cambiado)
        val currentItem = modificationState?.modifiedItems?.find { it.itemId == editingItem.itemId }
            ?: editingItem
        val comboId = currentItem.comboId ?: currentItem.itemId
        ComboOptionsDialog(
            item = currentItem,
            comboDef = comboDefinitions[comboId],
            isLoading = comboId in loadingComboIds,
            onConfirm = { newSelections ->
                ordersViewModel.updateComboItemSelections(currentItem.itemId, newSelections)
                comboBeingEdited = null
            },
            onDismiss = { comboBeingEdited = null }
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
    onEditCombo: (OrderItem) -> Unit,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var reason by rememberSaveable(order.id) { mutableStateOf("Modificado por el negocio") }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onCancel() },
        title = { Text("Modificar items") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
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
                                if (item.quantity > 1) onChangeQuantity(item.itemId, item.quantity - 1)
                            },
                            onIncrease = { onChangeQuantity(item.itemId, item.quantity + 1) },
                            onRemove = { onRemoveItem(item.itemId) },
                            onEditCombo = if (item.isCombo) ({ onEditCombo(item) }) else null
                        )
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Total original: ${formatAmount(state.originalTotal, order.currency)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Nuevo total: ${formatAmount(state.newTotal, order.currency)}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
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
            TextButton(onClick = onCancel, enabled = !isSaving) { Text("Cerrar") }
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
    onRemove: () -> Unit,
    onEditCombo: (() -> Unit)? = null
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

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (item.isCombo) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Combo",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (item.isCombo && item.comboSelections.isNotEmpty()) {
                        val summary = item.comboSelections.joinToString("  ·  ") { slot ->
                            "${slot.slotName}: ${slot.selectedOptions.joinToString(", ") { it.name }}"
                        }
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                    } else {
                        Text(
                            text = "$${formatDouble("%.2f", item.finalPrice)} c/u",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onEditCombo != null) {
                        TextButton(onClick = onEditCombo) {
                            Text(
                                text = "Opciones",
                                style = MaterialTheme.typography.labelMedium
                            )
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
}

// ---------------------------------------------------------------------------
// Diálogo dedicado de edición de opciones del combo
// ---------------------------------------------------------------------------

@Composable
private fun ComboOptionsDialog(
    item: OrderItem,
    comboDef: Combo?,
    isLoading: Boolean,
    onConfirm: (List<OrderComboSelection>) -> Unit,
    onDismiss: () -> Unit
) {
    // Estado local: el usuario edita aquí sin tocar el ViewModel hasta confirmar
    var localSelections by remember(item.itemId) { mutableStateOf(item.comboSelections) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Cambiar opciones", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            when {
                isLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Cargando opciones disponibles...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                comboDef == null -> {
                    Text(
                        text = "No se pudieron cargar las opciones del combo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        comboDef.slots.sortedBy { it.displayOrder }.forEach { slot ->
                            val slotKey = slot.id ?: slot.name
                            val currentSelected = localSelections
                                .find { it.slotId == slotKey }
                                ?.selectedOptions ?: emptyList()

                            ComboSlotEditor(
                                slot = slot,
                                currentSelected = currentSelected,
                                onToggleOption = { option, shouldSelect ->
                                    val updated = currentSelected.toMutableList()
                                    if (shouldSelect && updated.size < slot.maxSelections) {
                                        updated.add(
                                            OrderComboSelectedOption(
                                                productId = option.productId,
                                                name = option.product?.name ?: option.productId,
                                                price = option.product?.price ?: 0.0,
                                                quantity = 1,
                                                priceAdjustment = option.priceAdjustment,
                                                modifiers = emptyList()
                                            )
                                        )
                                    } else if (!shouldSelect && updated.size > slot.minSelections) {
                                        updated.removeAll { it.productId == option.productId }
                                    }
                                    localSelections = buildComboSelections(
                                        localSelections, comboDef, slotKey, updated
                                    )
                                },
                                onToggleModifier = { productId, modName, isActive ->
                                    val updated = currentSelected.toMutableList()
                                    val idx = updated.indexOfFirst { it.productId == productId }
                                    if (idx >= 0) {
                                        val opt = updated[idx]
                                        val mods = opt.modifiers.toMutableList()
                                        if (isActive) {
                                            if (mods.none { it.name == modName }) {
                                                mods.add(OrderComboModifier(name = modName, priceAdjustment = 0.0))
                                            }
                                        } else {
                                            mods.removeAll { it.name == modName }
                                        }
                                        updated[idx] = opt.copy(modifiers = mods)
                                    }
                                    localSelections = buildComboSelections(
                                        localSelections, comboDef, slotKey, updated
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(localSelections) },
                enabled = comboDef != null && !isLoading
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ComboSlotEditor(
    slot: ComboSlot,
    currentSelected: List<OrderComboSelectedOption>,
    onToggleOption: (ComboOption, Boolean) -> Unit,
    onToggleModifier: (productId: String, modifierName: String, isActive: Boolean) -> Unit
) {
    val selectedCount = currentSelected.size
    val maxReached = selectedCount >= slot.maxSelections
    val atMin = selectedCount <= slot.minSelections

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Encabezado del slot con regla
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = slot.name,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
            val ruleText = when {
                slot.minSelections == slot.maxSelections -> "Elige ${slot.maxSelections}"
                slot.minSelections == 0 -> "Hasta ${slot.maxSelections}"
                else -> "${slot.minSelections}–${slot.maxSelections}"
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = ruleText,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Opciones disponibles
        slot.options.forEach { option ->
            val optName = option.product?.name ?: option.productId
            val isSelected = currentSelected.any { it.productId == option.productId }
            val canToggle = if (isSelected) !atMin else !maxReached

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
                    maxReached -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = canToggle) {
                        onToggleOption(option, !isSelected)
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = optName,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (maxReached && !isSelected)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (option.priceAdjustment != 0.0) {
                                val sign = if (option.priceAdjustment > 0) "+" else ""
                                Text(
                                    text = "$sign${formatDouble("%.2f", option.priceAdjustment)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // Indicador de selección (checkbox visual)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(18.dp)
                            ) {
                                if (isSelected) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Done,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Modificadores (solo cuando la opción está seleccionada y tiene modificadores disponibles)
                    if (isSelected && option.availableModifiers.isNotEmpty()) {
                        val selectedOpt = currentSelected.find { it.productId == option.productId }
                        val activeMods = selectedOpt?.modifiers?.map { it.name } ?: emptyList()
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Agregos:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            option.availableModifiers.forEach { modifier ->
                                val isModActive = activeMods.contains(modifier.name)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isModActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isModActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onToggleModifier(option.productId, modifier.name, !isModActive)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = modifier.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isModActive) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (modifier.priceAdjustment != 0.0) {
                                            val sign = if (modifier.priceAdjustment > 0) "+" else ""
                                            Text(
                                                text = "$sign${formatDouble("%.2f", modifier.priceAdjustment)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mensajes de validación
        when {
            selectedCount < slot.minSelections -> Text(
                text = "Mínimo ${slot.minSelections} requerido(s) en este slot",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
            maxReached -> Text(
                text = "Máximo ${slot.maxSelections} alcanzado — quita uno para cambiar",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Reconstruye la lista completa de selecciones del combo actualizando solo el slot modificado.
 * [currentSelections] es el estado local actual (independiente del item del ViewModel).
 */
private fun buildComboSelections(
    currentSelections: List<OrderComboSelection>,
    comboDef: Combo,
    updatedSlotKey: String,
    updatedOptions: List<OrderComboSelectedOption>
): List<OrderComboSelection> {
    return comboDef.slots.mapNotNull { slot ->
        val key = slot.id ?: slot.name
        val selected = if (key == updatedSlotKey) updatedOptions
        else currentSelections.find { it.slotId == key }?.selectedOptions ?: emptyList()
        if (selected.isEmpty()) null
        else OrderComboSelection(slotId = key, slotName = slot.name, selectedOptions = selected)
    }
}

/** Extrae el sufijo corto: "EL.TABL-20260420-005" → "005" */
private fun orderShortNumber(orderNumber: String): String {
    val parts = orderNumber.split("-")
    return if (parts.size >= 2) parts.last() else orderNumber
}

/** Extrae el prefijo sin el último segmento: "EL.TABL-20260420-005" → "EL.TABL-20260420" */
private fun orderNumberPrefix(orderNumber: String): String {
    val lastDash = orderNumber.lastIndexOf("-")
    return if (lastDash > 0) orderNumber.substring(0, lastDash) else ""
}

private fun formatAmount(amount: Double, currency: String): String {
    val rounded = (amount * 100).toLong()
    val intPortion = rounded / 100
    val decPortion = rounded % 100
    val formatted = "$intPortion.${decPortion.toString().padStart(2, '0')}"
    val parts = formatted.split(".")
    val intPart = parts[0]
    val decPart = if (parts.size > 1) parts[1] else "00"
    val spaced = intPart.reversed().chunked(3).joinToString(" ").reversed()
    return "$spaced.$decPart $currency"
}

private fun formatOrderDate(timestamp: String): String {
    return try {
        val parts = timestamp.split("T")
        if (parts.size == 2) {
            val dateParts = parts[0].split("-")
            val timePart = parts[1].substringBefore(".")
            val timeComponents = timePart.split(":")
            if (dateParts.size == 3 && timeComponents.size >= 2) {
                val year = dateParts[0].takeLast(2)
                val month = dateParts[1]
                val day = dateParts[2]
                val hourInt = timeComponents[0].toIntOrNull() ?: 0
                val minute = timeComponents[1]
                val ampm = if (hourInt < 12) "am" else "pm"
                val hour12 = when {
                    hourInt == 0 -> 12
                    hourInt > 12 -> hourInt - 12
                    else -> hourInt
                }
                "$day-$month-$year $hour12:$minute$ampm"
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
