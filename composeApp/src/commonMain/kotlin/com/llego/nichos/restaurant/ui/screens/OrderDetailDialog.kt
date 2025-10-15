package com.llego.nichos.restaurant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.llego.nichos.restaurant.data.model.*

/**
 * Diálogo de detalle del pedido con gestión de estados
 */
@Composable
fun OrderDetailDialog(
    order: Order,
    onDismiss: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit,
    onNavigateToChat: ((String, String, String) -> Unit)? = null // orderId, orderNumber, customerName para navegar al chat
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header con colores Llego
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pedido ${order.orderNumber}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

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

                    // Información del cliente
                    CustomerInfoSection(customer = order.customer)

                    HorizontalDivider()

                    // Items del pedido
                    OrderItemsSection(items = order.items)

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

                    // Tiempo estimado
                    order.estimatedTime?.let { time ->
                        EstimatedTimeSection(estimatedTime = time)
                    }
                }

                // Footer con acciones
                Surface(
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OrderActionsSection(
                            orderId = order.id,
                            orderNumber = order.orderNumber,
                            customerName = order.customer.name,
                            orderStatus = order.status,
                            onUpdateStatus = onUpdateStatus,
                            onNavigateToChat = onNavigateToChat
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusSection(order: Order) {
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
private fun CustomerInfoSection(customer: Customer) {
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
        if (customer.address != null) {
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
                    text = customer.address,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
private fun OrderItemsSection(items: List<OrderItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Items del Pedido",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        items.forEach { orderItem ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
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
                        if (orderItem.specialInstructions != null) {
                            Text(
                                text = "Nota: ${orderItem.specialInstructions}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
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
private fun SpecialNotesSection(notes: String) {
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
private fun PaymentSummarySection(paymentMethod: PaymentMethod, total: Double) {
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
private fun EstimatedTimeSection(estimatedTime: Int) {
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
private fun OrderActionsSection(
    orderId: String,
    orderNumber: String,
    customerName: String,
    orderStatus: OrderStatus,
    onUpdateStatus: (OrderStatus) -> Unit,
    onNavigateToChat: ((String, String, String) -> Unit)?
) {
    var showCancelConfirmation by remember { mutableStateOf(false) }

    Text(
        text = "Acciones",
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold
        )
    )

    when (orderStatus) {
        OrderStatus.PENDING -> {
            // Aceptar pedido - Color Primary Llego
            Button(
                onClick = { onUpdateStatus(OrderStatus.PREPARING) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Aceptar Pedido",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Rechazar pedido - muestra confirmación
            OutlinedButton(
                onClick = { showCancelConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Rechazar Pedido",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
        OrderStatus.PREPARING -> {
            // Marcar como listo - Color Success
            Button(
                onClick = { onUpdateStatus(OrderStatus.READY) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
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
        OrderStatus.READY -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pedido listo para entregar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    )
                }
            }
        }
        OrderStatus.CANCELLED -> {
            // Pedido cancelado - mostrar info y botón para ir al chat
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pedido cancelado",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD32F2F)
                            )
                        )
                    }

                    if (onNavigateToChat != null) {
                        TextButton(
                            onClick = { onNavigateToChat.invoke(orderId, orderNumber, customerName) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ir al chat con el cliente")
                        }
                    }
                }
            }
        }
    }

    // Dialog de confirmación de cancelación
    if (showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("¿Cancelar pedido?")
            },
            text = {
                Text("¿Deseas explicar el motivo de la cancelación al cliente?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmation = false
                        onUpdateStatus(OrderStatus.CANCELLED)
                        // Redirigir al chat para explicar el motivo
                        onNavigateToChat?.invoke(orderId, orderNumber, customerName)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Sí, ir al chat")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showCancelConfirmation = false
                        onUpdateStatus(OrderStatus.CANCELLED)
                    }
                ) {
                    Text("Solo cancelar")
                }
            }
        )
    }
}
