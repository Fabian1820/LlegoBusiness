package com.llego.business.profile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.User

/**
 * Card base para secciones del perfil
 */
@Composable
fun ProfileSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

/**
 * Header de sección con título y botón de edición opcional
 */
@Composable
fun SectionHeader(
    title: String,
    emoji: String = "",
    isEditing: Boolean = false,
    onEditClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (emoji.isNotEmpty()) "$emoji $title" else title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = Color.Black
        )
        
        if (trailing != null) {
            trailing()
        } else if (onEditClick != null) {
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Guardar" else "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Campo editable reutilizable
 */
@Composable
fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    readOnly: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        if (isEditing && !readOnly) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                label = { Text(label) },
                placeholder = { Text(placeholder.ifEmpty { label }) },
                singleLine = true,
                trailingIcon = {
                    Row {
                        IconButton(onClick = onSaveClick) {
                            Icon(Icons.Default.Check, "Guardar", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onCancelClick) {
                            Icon(Icons.Default.Close, "Cancelar", tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = value.ifEmpty { placeholder.ifEmpty { "No configurado" } },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isEmpty()) Color.Gray else Color.Black
                )
            }
            if (!readOnly) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Campo de solo lectura
 */
@Composable
fun ReadOnlyField(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = value.ifEmpty { "No configurado" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isEmpty()) Color.Gray else Color.Black
            )
        }
    }
}

/**
 * Badge de estado
 */
@Composable
fun StatusBadge(status: String?) {
    val (color, text) = when(status) {
        "active" -> Color(0xFF4CAF50) to "Activa"
        "inactive" -> Color.Gray to "Inactiva"
        else -> Color(0xFFFFC107) to "Pendiente"
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

/**
 * Chip/Tag reutilizable
 */
@Composable
fun TagChip(
    text: String,
    onRemove: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (onRemove != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRemove() },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
