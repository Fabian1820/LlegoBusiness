package com.llego.business.orders.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.llego.business.orders.ui.components.OrderActionsSection
import com.llego.business.orders.ui.components.OrderCommentsSection
import com.llego.business.orders.ui.components.OrderItemsSection
import com.llego.business.orders.ui.components.OrderStatusSection
import com.llego.business.orders.ui.components.OrderTimelineSection
import com.llego.business.orders.ui.components.PaymentSummarySection
import com.llego.business.orders.ui.viewmodel.OrdersUiState
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.shared.ui.components.NetworkImage
import com.llego.shared.utils.formatDouble
import kotlinx.coroutines.delay

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OrderDetailScreen(
    order: Order,
    ordersViewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    onCallPhone: ((String) -> Unit)? = null
) {
    val uiState by ordersViewModel.uiState.collectAsState()
    val orders by ordersViewModel.orders.collectAsState()
    val modificationState by ordersViewModel.modificationState.collectAsState()
    val currentOrder = orders.firstOrNull { it.id == order.id } ?: order

    val isActionInProgress = (uiState as? OrdersUiState.ActionInProgress)?.orderId == currentOrder.id

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
            OrderStatusSection(order = currentOrder)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            OrderItemsSection(items = currentOrder.items)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            PaymentSummarySection(order = currentOrder)

            if (currentOrder.timeline.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                OrderTimelineSection(timeline = currentOrder.timeline)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            OrderCommentsSection(
                comments = currentOrder.comments,
                onAddComment = { message ->
                    ordersViewModel.addOrderComment(currentOrder.id, message)
                },
                isAddingComment = isActionInProgress
            )

            Spacer(modifier = Modifier.height(80.dp))
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
}

@Composable
private fun EditOrderItemsDialog(
    order: Order,
    state: OrderModificationState,
    isSaving: Boolean,
    onChangeQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
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
