package com.llego.business.orders.ui.screens

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
import com.llego.business.orders.data.model.*
import com.llego.business.orders.ui.components.CustomerInfoSection
import com.llego.business.orders.ui.components.OrderItemsSection
import com.llego.business.orders.ui.components.OrderStatusSection
import com.llego.business.orders.ui.components.PaymentSummarySection
import com.llego.business.orders.ui.viewmodel.OrdersViewModel

/**
 * Diálogo de detalle del pedido con gestión de estados
 * 
 * TODO: Reconstruir con componentes de edición cuando estén disponibles
 * Por ahora solo muestra información básica del pedido
 */
@Composable
fun OrderDetailDialog(
    order: Order,
    ordersViewModel: OrdersViewModel,
    onDismiss: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit,
    onAcceptOrder: (Int) -> Unit,
    onSubmitModification: ((Order, OrderModificationState, String) -> Unit)? = null
) {
    val modificationState by ordersViewModel.modificationState.collectAsState()

    DisposableEffect(order.id) {
        onDispose {
            ordersViewModel.exitEditMode()
        }
    }

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

                    // Resumen de pago
                    PaymentSummarySection(order = order)
                }

                // Footer con acciones básicas
                Surface(
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón de aceptar (si está pendiente)
                        if (order.status == OrderStatus.PENDING_ACCEPTANCE) {
                            Button(
                                onClick = { onAcceptOrder(30) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Aceptar")
                            }
                            OutlinedButton(
                                onClick = { onUpdateStatus(OrderStatus.CANCELLED) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Rechazar")
                            }
                        } else {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }
        }
    }
}
