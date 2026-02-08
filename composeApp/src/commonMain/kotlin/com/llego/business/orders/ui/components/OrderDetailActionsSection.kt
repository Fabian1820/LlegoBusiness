package com.llego.business.orders.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderStatus

@Composable
fun OrderActionsSection(
    order: Order,
    isActionInProgress: Boolean = false,
    onAcceptOrder: ((Int) -> Unit)? = null,
    onCancelOrder: ((String) -> Unit)? = null,
    onMarkReady: (() -> Unit)? = null,
    onEditItems: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var estimatedMinutes by remember { mutableStateOf("30") }
    var cancelReason by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (order.status) {
                OrderStatus.PENDING_ACCEPTANCE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAcceptDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = !isActionInProgress && onAcceptOrder != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            if (isActionInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Aceptar")
                            }
                        }

                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = !isActionInProgress && onCancelOrder != null,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Cancelar")
                        }
                    }

                    if (order.isEditable && onEditItems != null) {
                        OutlinedButton(
                            onClick = onEditItems,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isActionInProgress
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Modificar items")
                        }
                    }
                }

                OrderStatus.MODIFIED_BY_STORE -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "Pedido modificado, pendiente de confirmacion del cliente",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isActionInProgress && onCancelOrder != null,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Cancelar pedido")
                    }
                }

                OrderStatus.ACCEPTED -> {
                    Button(
                        onClick = { onMarkReady?.invoke() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isActionInProgress && onMarkReady != null
                    ) {
                        if (isActionInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Marcar como listo")
                        }
                    }

                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isActionInProgress && onCancelOrder != null,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Cancelar pedido")
                    }
                }

                OrderStatus.READY_FOR_PICKUP -> {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isActionInProgress && onCancelOrder != null,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Cancelar pedido")
                    }
                }

                else -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = order.status.getColor().copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = order.status.getColor())
                            Text(
                                text = order.status.getDisplayName(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = order.status.getColor()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAcceptDialog) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text("Aceptar pedido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tiempo estimado de preparacion (minutos)")
                    OutlinedTextField(
                        value = estimatedMinutes,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() }) {
                                estimatedMinutes = value
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
                    enabled = estimatedMinutes.isNotBlank()
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAcceptDialog = false }) {
                    Text("Volver")
                }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar pedido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Motivo de cancelacion")
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Motivo") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelOrder?.invoke(cancelReason.ifBlank { "Cancelado por el negocio" })
                        cancelReason = ""
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar pedido")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Volver")
                }
            }
        )
    }
}
