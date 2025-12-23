package com.llego.nichos.restaurant.ui.components.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import com.llego.nichos.restaurant.data.model.Customer
import com.llego.nichos.restaurant.data.model.MenuItem
import com.llego.nichos.restaurant.data.model.Order
import com.llego.nichos.restaurant.data.model.OrderItem
import com.llego.nichos.restaurant.data.model.OrderModificationState
import com.llego.nichos.restaurant.data.model.OrderStatus
import com.llego.nichos.restaurant.data.model.PaymentMethod
import com.llego.nichos.restaurant.data.model.getDisplayName

@Composable
internal fun OrderStatusSection(order: Order) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = when (order.status) {
            OrderStatus.PENDING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            OrderStatus.PREPARING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            OrderStatus.READY -> Color(0xFF4CAF50).copy(alpha = 0.15f)
            OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        },
        border = BorderStroke(
            2.dp,
            when (order.status) {
                OrderStatus.PENDING -> MaterialTheme.colorScheme.secondary
                OrderStatus.PREPARING -> MaterialTheme.colorScheme.primary
                OrderStatus.READY -> Color(0xFF4CAF50)
                OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (order.status) {
                    OrderStatus.PENDING -> Icons.Default.HourglassEmpty
                    OrderStatus.PREPARING -> Icons.Default.Restaurant
                    OrderStatus.READY -> Icons.Default.Done
                    OrderStatus.CANCELLED -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = when (order.status) {
                    OrderStatus.PENDING -> MaterialTheme.colorScheme.secondary
                    OrderStatus.PREPARING -> MaterialTheme.colorScheme.primary
                    OrderStatus.READY -> Color(0xFF4CAF50)
                    OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Estado Actual",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = order.status.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium.copy(
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
    }
}

@Composable
internal fun CustomerInfoSection(customer: Customer) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Cliente",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(customer.name)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Phone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(customer.phone)
        }
        customer.address?.let { address ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
internal fun OrderItemsSection(items: List<OrderItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Items del Pedido",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        items.forEach { orderItem ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF5F5F5),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = orderItem.menuItem.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Cantidad: ${orderItem.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        orderItem.specialInstructions?.let { note ->
                            Text(
                                text = "Nota: $note",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }
                    }
                    Text(
                        text = "$${orderItem.subtotal}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
internal fun EditableOrderItemsSection(
    originalItems: List<OrderItem>,
    items: List<OrderItem>,
    availableMenuItems: List<MenuItem>,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onUpdateInstructions: (String, String?) -> Unit,
    onAddItem: (MenuItem, Int, String?) -> Unit
) {
    var showAddItemDialog by remember { mutableStateOf(false) }
    val originalById = remember(originalItems) { originalItems.associateBy { it.id } }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Items del Pedido (edicion)",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        items.forEach { orderItem ->
            val originalItem = originalById[orderItem.id]
            val isModified = originalItem == null ||
                originalItem.quantity != orderItem.quantity ||
                originalItem.specialInstructions != orderItem.specialInstructions
            val statusLabel = when {
                originalItem == null -> "Nuevo"
                isModified -> "Editado"
                else -> null
            }
            val containerColor = if (isModified) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
            } else {
                Color(0xFFF5F5F5)
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = containerColor,
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = orderItem.menuItem.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (statusLabel != null) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        }
                        IconButton(onClick = { onRemoveItem(orderItem.id) }) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { onDecreaseQuantity(orderItem.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Disminuir cantidad"
                                )
                            }
                            Text(
                                text = orderItem.quantity.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            IconButton(onClick = { onIncreaseQuantity(orderItem.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Aumentar cantidad"
                                )
                            }
                        }
                        Text(
                            text = "$${orderItem.subtotal}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = orderItem.specialInstructions.orEmpty(),
                        onValueChange = { onUpdateInstructions(orderItem.id, it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Instrucciones") },
                        placeholder = { Text("Ej: sin cebolla") },
                        maxLines = 2
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { showAddItemDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = availableMenuItems.isNotEmpty()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar item")
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            menuItems = availableMenuItems,
            onAddItem = onAddItem,
            onDismiss = { showAddItemDialog = false }
        )
    }
}

@Composable
private fun AddItemDialog(
    menuItems: List<MenuItem>,
    onAddItem: (MenuItem, Int, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedItem by remember(menuItems) { mutableStateOf(menuItems.firstOrNull()) }
    var quantity by remember { mutableStateOf(1) }
    var instructions by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (menuItems.isEmpty()) {
                    Text("No hay items disponibles")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(menuItems) { item ->
                            val isSelected = item.id == selectedItem?.id
                            Surface(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable { selectedItem = item },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                        Text(
                                            text = "$${item.price}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cantidad",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { quantity = (quantity - 1).coerceAtLeast(1) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos")
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(onClick = { quantity += 1 }) {
                            Icon(Icons.Default.Add, contentDescription = "Mas")
                        }
                    }
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instrucciones") },
                    placeholder = { Text("Opcional") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val item = selectedItem ?: return@Button
                    val normalizedInstructions = instructions.trim().takeIf { it.isNotEmpty() }
                    onAddItem(item, quantity, normalizedInstructions)
                    onDismiss()
                },
                enabled = selectedItem != null
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
internal fun OrderTotalsComparisonSection(modificationState: OrderModificationState) {
    val difference = modificationState.totalDifference
    val differenceColor = when {
        difference > 0 -> MaterialTheme.colorScheme.error
        difference < 0 -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Comparacion de totales",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total original", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "$${modificationState.originalTotal}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total nuevo", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "$${modificationState.newTotal}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            if (modificationState.hasPriceChange) {
                Text(
                    text = "Diferencia: $${difference}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = differenceColor
                )
            }
        }
    }
}

@Composable
internal fun SpecialNotesSection(notes: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Notas Especiales",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        ) {
            Text(
                text = notes,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
internal fun PaymentSummarySection(paymentMethod: PaymentMethod, total: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Resumen de Pago",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Método de Pago:", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = paymentMethod.getDisplayName(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total:",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "$${total}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
internal fun EstimatedTimeSection(estimatedTime: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tiempo estimado: $estimatedTime minutos",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
internal fun OrderActionsSection(
    orderStatus: OrderStatus,
    modificationState: OrderModificationState?,
    onAcceptOrder: (Int) -> Unit,
    onRejectOrder: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit,
    onEnterEditMode: () -> Unit,
    onCancelEdit: () -> Unit,
    onConfirmModification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = modificationState?.isEditMode == true
    val hasChanges = modificationState?.hasChanges == true
    var showAcceptDialog by remember { mutableStateOf(false) }
    var prepTimeInput by remember { mutableStateOf("") }
    var showModificationNote by remember { mutableStateOf(false) }
    var modificationNote by remember { mutableStateOf("") }
    var showRejectConfirm by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        when (orderStatus) {
            OrderStatus.PENDING -> {
                if (isEditMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showModificationNote = true },
                            modifier = Modifier.wrapContentWidth(),
                            enabled = hasChanges,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar")
                        }

                        OutlinedButton(
                            onClick = onCancelEdit,
                            modifier = Modifier.wrapContentWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            ),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restablecer")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.ElevatedButton(
                            onClick = { showAcceptDialog = true },
                            modifier = Modifier.wrapContentWidth(),
                            enabled = !hasChanges,
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.elevatedButtonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Aceptar",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        androidx.compose.material3.ElevatedButton(
                            onClick = onEnterEditMode,
                            modifier = Modifier.wrapContentWidth(),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.elevatedButtonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Modificar")
                        }

                        IconButton(onClick = { showRejectConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Rechazar pedido",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            OrderStatus.PREPARING -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.ElevatedButton(
                        onClick = { onUpdateStatus(OrderStatus.READY) },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Marcar como Listo",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            OrderStatus.READY, OrderStatus.CANCELLED -> {
                // Sin acciones disponibles
            }
        }
    }

    if (showModificationNote) {
        ModificationNoteDialog(
            note = modificationNote,
            onNoteChange = { modificationNote = it },
            onConfirm = { note ->
                modificationNote = ""
                showModificationNote = false
                onConfirmModification(note)
            },
            onDismiss = {
                showModificationNote = false
            }
        )
    }

    if (showAcceptDialog) {
        AcceptOrderDialog(
            prepTimeInput = prepTimeInput,
            onPrepTimeChange = { prepTimeInput = it },
            onConfirm = { minutes ->
                showAcceptDialog = false
                prepTimeInput = ""
                onAcceptOrder(minutes)
                onCancelEdit()
            },
            onDismiss = {
                showAcceptDialog = false
            }
        )
    }

    if (showRejectConfirm) {
        AlertDialog(
            onDismissRequest = { showRejectConfirm = false },
            title = { Text("Rechazar pedido") },
            text = { Text("Estas seguro de rechazar este pedido?") },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectConfirm = false
                        onRejectOrder()
                        onCancelEdit()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ModificationNoteDialog(
    note: String,
    onNoteChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val trimmedNote = note.trim()
    val isValid = trimmedNote.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comentario de modificacion") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Comentario obligatorio para explicar los cambios.")
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    placeholder = { Text("Ej: cambiamos el item por falta de stock") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(trimmedNote) },
                enabled = isValid
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun AcceptOrderDialog(
    prepTimeInput: String,
    onPrepTimeChange: (String) -> Unit,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val minutes = prepTimeInput.trim().toIntOrNull()
    val isValid = minutes != null && minutes > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tiempo estimado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ingresa el tiempo aproximado de elaboracion (minutos).")
                OutlinedTextField(
                    value = prepTimeInput,
                    onValueChange = onPrepTimeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Minutos") },
                    placeholder = { Text("Ej: 25") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 1
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(minutes ?: return@Button) },
                enabled = isValid
            ) {
                Text("Aceptar pedido")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
