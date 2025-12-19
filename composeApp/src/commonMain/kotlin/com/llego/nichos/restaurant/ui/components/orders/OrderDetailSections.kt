package com.llego.nichos.restaurant.ui.components.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.llego.nichos.restaurant.data.model.Customer
import com.llego.nichos.restaurant.data.model.Order
import com.llego.nichos.restaurant.data.model.OrderItem
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
    orderId: String,
    orderNumber: String,
    customerName: String,
    orderStatus: OrderStatus,
    onUpdateStatus: (OrderStatus) -> Unit,
    onNavigateToChat: ((String, String, String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    var showCancelConfirmation by remember { mutableStateOf(false) }
    var showAcceptConfirmation by remember { mutableStateOf(false) }
    var acceptNotes by remember { mutableStateOf("") }
    var cancelNotes by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        when (orderStatus) {
            OrderStatus.PENDING -> {
                // Dos botones lado a lado con sombra
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.ElevatedButton(
                        onClick = { showAcceptConfirmation = true },
                        modifier = Modifier.weight(1f),
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
                        onClick = { showCancelConfirmation = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Rechazar",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            OrderStatus.PREPARING -> {
                // Un botón centrado con sombra, ajustado al contenido
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
                // No mostrar ningún cartel cuando no hay acciones disponibles
            }
        }
    }

    // Diálogo de confirmación para aceptar pedido
    if (showAcceptConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showAcceptConfirmation = false
                acceptNotes = ""
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Aceptar pedido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Deseas agregar alguna nota sobre este pedido?")
                    androidx.compose.material3.OutlinedTextField(
                        value = acceptNotes,
                        onValueChange = { acceptNotes = it },
                        placeholder = { Text("Ejemplo: Aceptado pero sin cebolla disponible") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        text = "Opcional",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAcceptConfirmation = false
                        // TODO: Guardar las notas (acceptNotes) en el backend
                        acceptNotes = ""
                        onUpdateStatus(OrderStatus.PREPARING)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Aceptar pedido")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAcceptConfirmation = false
                        acceptNotes = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para cancelar pedido
    if (showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showCancelConfirmation = false
                cancelNotes = ""
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("¿Cancelar pedido?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Por qué deseas cancelar este pedido?")
                    androidx.compose.material3.OutlinedTextField(
                        value = cancelNotes,
                        onValueChange = { cancelNotes = it },
                        placeholder = { Text("Ejemplo: Cancelado por falta de ingredientes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.error,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        text = "Opcional",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmation = false
                        // TODO: Guardar las notas (cancelNotes) en el backend
                        // TODO: Chat aún no implementado
                        // onNavigateToChat?.invoke(orderId, orderNumber, customerName)
                        cancelNotes = ""
                        onUpdateStatus(OrderStatus.CANCELLED)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sí, cancelar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showCancelConfirmation = false
                        cancelNotes = ""
                    }
                ) {
                    Text("No, mantener")
                }
            }
        )
    }
}
