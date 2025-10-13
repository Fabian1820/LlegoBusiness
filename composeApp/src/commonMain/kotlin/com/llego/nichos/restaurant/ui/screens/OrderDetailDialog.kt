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
    onUpdateStatus: (OrderStatus) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
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
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
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

                    // Tipo de entrega
                    DeliveryTypeSection(deliveryType = order.deliveryType)

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
                            orderStatus = order.status,
                            deliveryType = order.deliveryType,
                            onUpdateStatus = onUpdateStatus
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
        color = Color(order.status.getColor()).copy(alpha = 0.1f),
        border = BorderStroke(2.dp, Color(order.status.getColor()))
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
                    OrderStatus.ACCEPTED -> Icons.Default.CheckCircle
                    OrderStatus.PREPARING -> Icons.Default.Restaurant
                    OrderStatus.READY -> Icons.Default.Done
                    OrderStatus.IN_DELIVERY -> Icons.Default.DeliveryDining
                    OrderStatus.DELIVERED -> Icons.Default.TaskAlt
                    OrderStatus.CANCELLED -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = Color(order.status.getColor()),
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Estado Actual",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = order.status.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(order.status.getColor())
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
private fun DeliveryTypeSection(deliveryType: DeliveryType) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (deliveryType == DeliveryType.DELIVERY)
                Icons.Default.DeliveryDining else Icons.Default.Store,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = deliveryType.getDisplayName(),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
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
                fontWeight = FontWeight.Bold
            )
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFF9E6),
            border = BorderStroke(1.dp, Color(0xFFFFD700))
        ) {
            Text(
                text = notes,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
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
        color = MaterialTheme.colorScheme.primaryContainer
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
                tint = MaterialTheme.colorScheme.primary
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
    orderStatus: OrderStatus,
    deliveryType: DeliveryType,
    onUpdateStatus: (OrderStatus) -> Unit
) {
    Text(
        text = "Acciones",
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold
        )
    )

    when (orderStatus) {
        OrderStatus.PENDING -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.ACCEPTED) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aceptar Pedido")
            }
            OutlinedButton(
                onClick = { onUpdateStatus(OrderStatus.CANCELLED) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rechazar Pedido")
            }
        }
        OrderStatus.ACCEPTED -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.PREPARING) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar Preparación")
            }
        }
        OrderStatus.PREPARING -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.READY) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Icon(Icons.Default.Done, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Marcar como Listo")
            }
        }
        OrderStatus.READY -> {
            if (deliveryType == DeliveryType.DELIVERY) {
                Button(
                    onClick = { onUpdateStatus(OrderStatus.IN_DELIVERY) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0)
                    )
                ) {
                    Icon(Icons.Default.DeliveryDining, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("En Camino")
                }
            } else {
                Button(
                    onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pedido Recogido")
                }
            }
        }
        OrderStatus.IN_DELIVERY -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(Icons.Default.TaskAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pedido Entregado")
            }
        }
        OrderStatus.DELIVERED, OrderStatus.CANCELLED -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (orderStatus == OrderStatus.DELIVERED)
                    Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (orderStatus == OrderStatus.DELIVERED)
                            Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (orderStatus == OrderStatus.DELIVERED)
                            Color(0xFF4CAF50) else Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (orderStatus == OrderStatus.DELIVERED)
                            "Pedido completado" else "Pedido cancelado",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (orderStatus == OrderStatus.DELIVERED)
                                Color(0xFF4CAF50) else Color(0xFFD32F2F)
                        )
                    )
                }
            }
        }
    }
}
