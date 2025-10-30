package com.llego.nichos.restaurant.ui.screens

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
import com.llego.nichos.restaurant.ui.components.orders.CustomerInfoSection
import com.llego.nichos.restaurant.ui.components.orders.EstimatedTimeSection
import com.llego.nichos.restaurant.ui.components.orders.OrderActionsSection
import com.llego.nichos.restaurant.ui.components.orders.OrderItemsSection
import com.llego.nichos.restaurant.ui.components.orders.OrderStatusSection
import com.llego.nichos.restaurant.ui.components.orders.PaymentSummarySection
import com.llego.nichos.restaurant.ui.components.orders.SpecialNotesSection

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

