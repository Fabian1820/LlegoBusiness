package com.llego.business.orders.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderItem
import com.llego.business.orders.data.model.OrderStatus

/**
 * SecciÃ³n de estado del pedido con backend
 * Requirements: 5.5
 */
@Composable
fun OrderStatusSection(order: Order) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = order.status.getColor().copy(alpha = 0.15f),
        border = BorderStroke(2.dp, order.status.getColor())
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
                    OrderStatus.PENDING_ACCEPTANCE -> Icons.Default.HourglassEmpty
                    OrderStatus.MODIFIED_BY_STORE -> Icons.Default.Edit
                    OrderStatus.ACCEPTED -> Icons.Default.CheckCircle
                    OrderStatus.PREPARING -> Icons.Default.Restaurant
                    OrderStatus.READY_FOR_PICKUP -> Icons.Default.Done
                    OrderStatus.ON_THE_WAY -> Icons.Default.LocalShipping
                    OrderStatus.DELIVERED -> Icons.Default.CheckCircle
                    OrderStatus.CANCELLED -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = order.status.getColor(),
                modifier = Modifier.size(32.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estado Actual",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = order.status.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = order.status.getColor()
                )
            }
            
            // Tiempo estimado restante si estÃ¡ disponible
            order.estimatedMinutesRemaining?.let { minutes ->
                if (minutes > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$minutes min",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * SecciÃ³n de items del pedido con backend
 */
@Composable
fun OrderItemsSection(items: List<OrderItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Items del Pedido",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        items.forEach { item ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (item.wasModifiedByStore) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                } else {
                    Color(0xFFF5F5F5)
                },
                border = BorderStroke(
                    1.dp, 
                    if (item.wasModifiedByStore) {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    } else {
                        Color.LightGray.copy(alpha = 0.2f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            if (item.wasModifiedByStore) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Modificado",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Cantidad: ${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = "${item.lineTotal}",
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

/**
 * SecciÃ³n de resumen de pago con backend
 */
@Composable
fun PaymentSummarySection(order: Order) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Resumen de Pago",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        // Subtotal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${order.currency} ${order.subtotal}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Delivery fee
        if (order.deliveryFee > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("EnvÃ­o:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${order.currency} ${order.deliveryFee}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Descuentos
        order.discounts.forEach { discount ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = discount.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = discount.type.getColor()
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = discount.type.getColor().copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = discount.type.getDisplayName(),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = discount.type.getColor()
                        )
                    }
                }
                Text(
                    text = "-${order.currency} ${discount.amount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50)
                )
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        
        // MÃ©todo de pago
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("MÃ©todo de Pago:", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = order.paymentMethod,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = order.paymentStatus.getColor().copy(alpha = 0.1f)
                ) {
                    Text(
                        text = order.paymentStatus.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = order.paymentStatus.getColor()
                    )
                }
            }
        }
        
        // Total
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
                text = "${order.currency} ${order.total}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}


