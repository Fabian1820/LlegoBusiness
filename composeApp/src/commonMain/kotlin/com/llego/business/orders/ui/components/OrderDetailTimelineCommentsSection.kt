package com.llego.business.orders.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.OrderActor
import com.llego.business.orders.data.model.OrderComment
import com.llego.business.orders.data.model.OrderTimelineEntry

/**
 * SecciÃ³n de timeline del pedido
 * Requirements: 8.1, 8.2, 8.3, 8.4
 * 
 * @param timeline Lista de entradas del timeline
 */
@Composable
fun OrderTimelineSection(
    timeline: List<OrderTimelineEntry>
) {
    if (timeline.isEmpty()) return
    
    // Ordenar cronolÃ³gicamente (mÃ¡s reciente primero) - Requirements: 8.3
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
                text = "Historial del pedido",
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
        // LÃ­nea vertical y punto
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LÃ­nea superior (si no es el primero)
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
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getActorIcon(entry.actor),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // LÃ­nea inferior (si no es el Ãºltimo)
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
                // Estado
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = entry.status.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Actor
                Text(
                    text = entry.actor.getDisplayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    return try {
        val parts = timestamp.split("T")
        if (parts.size == 2) {
            val dateParts = parts[0].split("-")
            val timePart = parts[1].substringBefore(".")
            val timeComponents = timePart.split(":")
            if (dateParts.size == 3 && timeComponents.size >= 2) {
                val year = dateParts[0].takeLast(2)
                val month = dateParts[1]
                val day = dateParts[2]
                val hourInt = timeComponents[0].toIntOrNull() ?: 0
                val minute = timeComponents[1]
                val ampm = if (hourInt < 12) "am" else "pm"
                val hour12 = when {
                    hourInt == 0 -> 12
                    hourInt > 12 -> hourInt - 12
                    else -> hourInt
                }
                "$hour12:$minute$ampm · $day/$month/$year"
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
 * SecciÃ³n de comentarios del pedido
 * Requirements: 7.1, 7.2, 7.3, 7.4
 * 
 * @param comments Lista de comentarios
 * @param onAddComment Callback para agregar un nuevo comentario
 * @param isAddingComment Si se estÃ¡ agregando un comentario (loading)
 */
@Composable
fun OrderCommentsSection(
    comments: List<OrderComment>,
    onAddComment: ((String) -> Unit)? = null,
    isAddingComment: Boolean = false
) {
    var newCommentText by remember { mutableStateOf("") }
    
    // Ordenar por timestamp descendente (mÃ¡s reciente primero) - Requirements: 7.1
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
                    text = "No hay comentarios aun",
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
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getActorIcon(comment.author),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = comment.author.getDisplayName(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
