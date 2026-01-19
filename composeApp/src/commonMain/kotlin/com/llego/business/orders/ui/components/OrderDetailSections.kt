package com.llego.business.orders.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.*

/**
 * Sección de información del cliente con datos del backend
 * Requirements: 10.1, 10.5
 * 
 * @param customer Información del cliente
 * @param onCallCustomer Callback para llamar al cliente
 */
@Composable
fun CustomerInfoSection(
    customer: CustomerInfo?,
    onCallCustomer: ((String) -> Unit)? = null
) {
    if (customer == null) return
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Cliente",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del cliente
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (customer.avatarUrl != null) {
                            // TODO: Cargar imagen con Coil
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    customer.phone?.let { phone ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(enabled = onCallCustomer != null) {
                                    onCallCustomer?.invoke(phone)
                                }
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Botón de llamar
                customer.phone?.let { phone ->
                    if (onCallCustomer != null) {
                        FilledIconButton(
                            onClick = { onCallCustomer(phone) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Llamar al cliente",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * Sección de dirección de entrega con mapa opcional
 * Requirements: 10.2, 10.3
 * 
 * @param deliveryAddress Dirección de entrega
 * @param showMap Si se debe mostrar el mapa (requiere coordenadas)
 */
@Composable
fun DeliveryAddressSection(
    deliveryAddress: DeliveryAddress,
    showMap: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Dirección de Entrega",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            // Dirección principal
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = deliveryAddress.street,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    
                    deliveryAddress.city?.let { city ->
                        Text(
                            text = city,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Referencia
            deliveryAddress.reference?.let { reference ->
                if (reference.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Referencia",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = reference,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            
            // Mapa con coordenadas
            if (showMap && deliveryAddress.coordinates != null) {
                val coords = deliveryAddress.coordinates
                if (coords.latitude != 0.0 && coords.longitude != 0.0) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        // Placeholder para el mapa - se puede integrar con BusinessLocationMap
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Lat: ${String.format("%.6f", coords.latitude)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Lng: ${String.format("%.6f", coords.longitude)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * Sección de información del repartidor
 * Requirements: 10.4
 * 
 * @param deliveryPerson Información del repartidor
 * @param onCallDeliveryPerson Callback para llamar al repartidor
 */
@Composable
fun DeliveryPersonSection(
    deliveryPerson: DeliveryPersonInfo?,
    onCallDeliveryPerson: ((String) -> Unit)? = null
) {
    if (deliveryPerson == null) return
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Repartidor Asignado",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
                
                // Indicador de estado online
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (deliveryPerson.isOnline) {
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                    } else {
                        Color.Gray.copy(alpha = 0.2f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (deliveryPerson.isOnline) Color(0xFF4CAF50) else Color.Gray,
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = if (deliveryPerson.isOnline) "En línea" else "Desconectado",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (deliveryPerson.isOnline) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del repartidor
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deliveryPerson.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    // Rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = String.format("%.1f", deliveryPerson.rating),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "(${deliveryPerson.totalDeliveries} entregas)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Tipo de vehículo
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getVehicleIcon(deliveryPerson.vehicleType),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = deliveryPerson.vehicleType.getDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        deliveryPerson.vehiclePlate?.let { plate ->
                            Text(
                                text = "• $plate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Botón de llamar
                if (onCallDeliveryPerson != null) {
                    FilledIconButton(
                        onClick = { onCallDeliveryPerson(deliveryPerson.phone) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Llamar al repartidor",
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Teléfono
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(enabled = onCallDeliveryPerson != null) {
                        onCallDeliveryPerson?.invoke(deliveryPerson.phone)
                    }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = deliveryPerson.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/**
 * Obtiene el icono correspondiente al tipo de vehículo
 */
private fun getVehicleIcon(vehicleType: VehicleType): ImageVector {
    return when (vehicleType) {
        VehicleType.MOTO -> Icons.Default.TwoWheeler
        VehicleType.BICICLETA -> Icons.Default.PedalBike
        VehicleType.AUTO -> Icons.Default.DirectionsCar
        VehicleType.A_PIE -> Icons.Default.DirectionsWalk
    }
}


/**
 * Sección de timeline del pedido
 * Requirements: 8.1, 8.2, 8.3, 8.4
 * 
 * @param timeline Lista de entradas del timeline
 */
@Composable
fun OrderTimelineSection(
    timeline: List<OrderTimelineEntry>
) {
    if (timeline.isEmpty()) return
    
    // Ordenar cronológicamente (más reciente primero) - Requirements: 8.3
    val sortedTimeline = remember(timeline) {
        timeline.sortedByDescending { it.timestamp }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Historial del Pedido",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            sortedTimeline.forEachIndexed { index, entry ->
                TimelineEntryItem(
                    entry = entry,
                    isFirst = index == 0,
                    isLast = index == sortedTimeline.lastIndex
                )
            }
        }
    }
}

/**
 * Item individual del timeline
 */
@Composable
private fun TimelineEntryItem(
    entry: OrderTimelineEntry,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Línea vertical y punto
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Línea superior (si no es el primero)
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Punto con icono del actor - Requirements: 8.4
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = entry.actor.getColor().copy(alpha = 0.2f),
                border = BorderStroke(2.dp, entry.actor.getColor())
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getActorIcon(entry.actor),
                        contentDescription = null,
                        tint = entry.actor.getColor(),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Línea inferior (si no es el último)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }
        
        // Contenido del evento
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (!isLast) 8.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estado con color
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = entry.status.getColor().copy(alpha = 0.15f)
                ) {
                    Text(
                        text = entry.status.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = entry.status.getColor()
                    )
                }
                
                // Actor
                Text(
                    text = entry.actor.getDisplayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = entry.actor.getColor()
                )
            }
            
            // Mensaje - Requirements: 8.2
            if (entry.message.isNotBlank()) {
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Timestamp formateado - Requirements: 8.2
            Text(
                text = formatTimestamp(entry.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Obtiene el icono correspondiente al actor
 */
private fun getActorIcon(actor: OrderActor): ImageVector {
    return when (actor) {
        OrderActor.CUSTOMER -> Icons.Default.Person
        OrderActor.BUSINESS -> Icons.Default.Store
        OrderActor.SYSTEM -> Icons.Default.Settings
        OrderActor.DELIVERY -> Icons.Default.DeliveryDining
    }
}

/**
 * Formatea un timestamp ISO a formato legible
 */
private fun formatTimestamp(timestamp: String): String {
    // Formato simple - en producción usar librería de fechas
    return try {
        // Asume formato ISO: "2024-01-15T14:30:00"
        val parts = timestamp.split("T")
        if (parts.size == 2) {
            val datePart = parts[0]
            val timePart = parts[1].substringBefore(".")
            val timeComponents = timePart.split(":")
            if (timeComponents.size >= 2) {
                "${timeComponents[0]}:${timeComponents[1]} - $datePart"
            } else {
                timestamp
            }
        } else {
            timestamp
        }
    } catch (e: Exception) {
        timestamp
    }
}


/**
 * Sección de comentarios del pedido
 * Requirements: 7.1, 7.2, 7.3, 7.4
 * 
 * @param comments Lista de comentarios
 * @param onAddComment Callback para agregar un nuevo comentario
 * @param isAddingComment Si se está agregando un comentario (loading)
 */
@Composable
fun OrderCommentsSection(
    comments: List<OrderComment>,
    onAddComment: ((String) -> Unit)? = null,
    isAddingComment: Boolean = false
) {
    var newCommentText by remember { mutableStateOf("") }
    
    // Ordenar por timestamp descendente (más reciente primero) - Requirements: 7.1
    val sortedComments = remember(comments) {
        comments.sortedByDescending { it.timestamp }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Comentarios",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            // Campo para agregar nuevo comentario - Requirements: 7.3
            if (onAddComment != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un comentario...") },
                        maxLines = 3,
                        enabled = !isAddingComment,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    FilledIconButton(
                        onClick = {
                            val trimmedText = newCommentText.trim()
                            if (trimmedText.isNotEmpty()) {
                                onAddComment(trimmedText)
                                newCommentText = ""
                            }
                        },
                        enabled = newCommentText.trim().isNotEmpty() && !isAddingComment,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isAddingComment) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar comentario",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            
            // Lista de comentarios existentes
            if (sortedComments.isEmpty()) {
                Text(
                    text = "No hay comentarios aún",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            } else {
                sortedComments.forEach { comment ->
                    CommentItem(comment = comment)
                }
            }
        }
    }
}

/**
 * Item individual de comentario
 * Requirements: 7.2
 */
@Composable
private fun CommentItem(comment: OrderComment) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Actor con icono distintivo - Requirements: 7.2
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = comment.author.getColor().copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getActorIcon(comment.author),
                                contentDescription = null,
                                tint = comment.author.getColor(),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = comment.author.getDisplayName(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = comment.author.getColor()
                    )
                }
                
                // Timestamp
                Text(
                    text = formatTimestamp(comment.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Mensaje del comentario
            Text(
                text = comment.message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


/**
 * Sección de acciones del pedido según estado
 * Requirements: 5.5, 6.1
 * 
 * @param order Pedido actual
 * @param isActionInProgress Si hay una acción en progreso
 * @param onAcceptOrder Callback para aceptar pedido (con minutos estimados)
 * @param onRejectOrder Callback para rechazar pedido (con razón)
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
                        // Botón Aceptar
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
                        
                        // Botón Rechazar
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
                    
                    // Botón Editar items si es editable - Requirements: 6.1
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
                    
                    // Botón Editar items si es editable
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
    
    // Diálogo para aceptar pedido
    if (showAcceptDialog) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text("Aceptar Pedido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa el tiempo estimado de preparación:")
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
    
    // Diálogo para rechazar pedido
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Rechazar Pedido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa la razón del rechazo:")
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Razón") },
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

/**
 * Sección de estado del pedido con backend
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
            
            // Tiempo estimado restante si está disponible
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
 * Sección de items del pedido con backend
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
 * Sección de resumen de pago con backend
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
                Text("Envío:", style = MaterialTheme.typography.bodyMedium)
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
        
        // Método de pago
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Método de Pago:", style = MaterialTheme.typography.bodyMedium)
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


