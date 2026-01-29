package com.llego.business.orders.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderStatus

/**
 * SecciÃ³n de acciones del pedido segÃºn estado
 * Requirements: 5.5, 6.1
 * 
 * @param order Pedido actual
 * @param isActionInProgress Si hay una acciÃ³n en progreso
 * @param onAcceptOrder Callback para aceptar pedido (con minutos estimados)
 * @param onRejectOrder Callback para rechazar pedido (con razÃ³n)
 * @param onMarkReady Callback para marcar como listo
 * @param onEditItems Callback para editar items
 */
@Composable
fun OrderActionsSection(
    order: Order,
    isActionInProgress: Boolean = false,
    onAcceptOrder: ((Int) -> Unit)? = null,
    onRejectOrder: ((String) -> Unit)? = null,
    onMarkReady: (() -> Unit)? = null,
    onEditItems: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var estimatedMinutes by remember { mutableStateOf("30") }
    var rejectReason by remember { mutableStateOf("") }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (order.status) {
                // PENDING_ACCEPTANCE: Aceptar/Rechazar - Requirements: 5.5
                OrderStatus.PENDING_ACCEPTANCE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // BotÃ³n Aceptar
                        Button(
                            onClick = { showAcceptDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = !isActionInProgress && onAcceptOrder != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            if (isActionInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aceptar")
                            }
                        }
                        
                        // BotÃ³n Rechazar
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = !isActionInProgress && onRejectOrder != null,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rechazar")
                        }
                    }
                    
                    // BotÃ³n Editar items si es editable - Requirements: 6.1
                    if (order.isEditable && onEditItems != null) {
                        OutlinedButton(
                            onClick = onEditItems,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isActionInProgress
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Modificar Items")
                        }
                    }
                }
                
                // PREPARING: Marcar como listo - Requirements: 5.5
                OrderStatus.PREPARING -> {
                    Button(
                        onClick = { onMarkReady?.invoke() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isActionInProgress && onMarkReady != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isActionInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Marcar como Listo")
                        }
                    }
                    
                    // BotÃ³n Editar items si es editable
                    if (order.isEditable && onEditItems != null) {
                        OutlinedButton(
                            onClick = onEditItems,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isActionInProgress
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Modificar Items")
                        }
                    }
                }
                
                // ACCEPTED: Puede editar items si es editable
                OrderStatus.ACCEPTED -> {
                    if (order.isEditable && onEditItems != null) {
                        OutlinedButton(
                            onClick = onEditItems,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isActionInProgress
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Modificar Items")
                        }
                    }
                }
                
                // Otros estados: sin acciones disponibles
                else -> {
                    // No mostrar nada o mostrar estado informativo
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = order.status.getColor().copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (order.status) {
                                    OrderStatus.READY_FOR_PICKUP -> Icons.Default.CheckCircle
                                    OrderStatus.ON_THE_WAY -> Icons.Default.LocalShipping
                                    OrderStatus.DELIVERED -> Icons.Default.Done
                                    OrderStatus.CANCELLED -> Icons.Default.Cancel
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = order.status.getColor()
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = order.status.getDisplayName(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = order.status.getColor()
                            )
                        }
                    }
                }
            }
        }
    }
    
    // DiÃ¡logo para aceptar pedido
    if (showAcceptDialog) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text("Aceptar Pedido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa el tiempo estimado de preparaciÃ³n:")
                    OutlinedTextField(
                        value = estimatedMinutes,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() }) {
                                estimatedMinutes = it
                            }
                        },
                        label = { Text("Minutos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minutes = estimatedMinutes.toIntOrNull() ?: 30
                        onAcceptOrder?.invoke(minutes)
                        showAcceptDialog = false
                    },
                    enabled = estimatedMinutes.isNotEmpty()
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAcceptDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // DiÃ¡logo para rechazar pedido
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Rechazar Pedido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa la razÃ³n del rechazo:")
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("RazÃ³n") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectOrder?.invoke(rejectReason.ifBlank { "Rechazado por el negocio" })
                        showRejectDialog = false
                        rejectReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
