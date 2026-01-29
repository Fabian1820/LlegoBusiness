package com.llego.business.profile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.shared.ui.components.NetworkImage
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.User
import com.llego.shared.data.model.toDisplayName
import com.llego.shared.utils.formatDouble
import com.llego.shared.ui.components.molecules.SchedulePicker
import com.llego.shared.ui.components.molecules.toBackendSchedule
import com.llego.shared.ui.components.molecules.toDaySchedule
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes

// ============= BUSINESS TAGS SECTION =============

@Composable
fun BusinessTagsSection(
    business: Business?,
    onSave: (List<String>) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var currentTags by remember(business) { mutableStateOf(business?.tags ?: emptyList()) }
    var newTag by remember { mutableStateOf("") }

    ProfileSectionCard {
        SectionHeader(
            title = "Etiquetas del negocio",
            isEditing = isEditing,
            onEditClick = {
                if (isEditing) {
                    onSave(currentTags)
                } else {
                    currentTags = business?.tags ?: emptyList()
                }
                isEditing = !isEditing
            }
        )

        if (currentTags.isEmpty() && !isEditing) {
            Text(
                text = "Sin etiquetas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(currentTags) { tag ->
                    TagChip(
                        text = tag,
                        onRemove = if (isEditing) {{
                            currentTags = currentTags - tag
                        }} else null
                    )
                }
            }
        }

        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nueva etiqueta") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = LlegoCustomShapes.inputField
                )
                IconButton(
                    onClick = {
                        val tag = newTag.trim()
                        if (tag.isNotEmpty() && !currentTags.contains(tag)) {
                            currentTags = currentTags + tag
                            newTag = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, "Agregar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

