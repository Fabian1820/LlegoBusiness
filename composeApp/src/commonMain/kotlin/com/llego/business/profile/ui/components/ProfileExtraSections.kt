package com.llego.business.profile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import llegobusiness.composeapp.generated.resources.Res
import llegobusiness.composeapp.generated.resources.social_facebook
import llegobusiness.composeapp.generated.resources.social_instagram
import llegobusiness.composeapp.generated.resources.social_tripadvisor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// ============= SOCIAL LINKS SECTION =============

@Composable
fun SocialLinksSection(
    socialMedia: Map<String, String>? = null,
    onSave: (Map<String, String>) -> Unit = {}
) {
    var instagram by remember(socialMedia) { mutableStateOf(socialMedia?.get("instagram") ?: "") }
    var facebook by remember(socialMedia) { mutableStateOf(socialMedia?.get("facebook") ?: "") }
    var tripadvisor by remember(socialMedia) { mutableStateOf(socialMedia?.get("tripadvisor") ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    ProfileSectionCard {
        SectionHeader(
            title = "Redes sociales",
            sectionIcon = Icons.Default.Share,
            sectionIconTint = Color(0xFF1976D2),
            isEditing = isEditing,
            onEditClick = {
                if (isEditing) {
                    val updated = mutableMapOf<String, String>()
                    val instagramValue = instagram.trim()
                    val facebookValue = facebook.trim()
                    val tripadvisorValue = tripadvisor.trim()
                    if (instagramValue.isNotEmpty()) updated["instagram"] = instagramValue
                    if (facebookValue.isNotEmpty()) updated["facebook"] = facebookValue
                    if (tripadvisorValue.isNotEmpty()) updated["tripadvisor"] = tripadvisorValue
                    onSave(updated)
                }
                isEditing = !isEditing
            }
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SocialRow(
                icon = Res.drawable.social_instagram,
                value = instagram,
                placeholder = "@usuario",
                isEditing = isEditing,
                onValueChange = { instagram = it }
            )
            SocialRow(
                icon = Res.drawable.social_facebook,
                value = facebook,
                placeholder = "Página de Facebook",
                isEditing = isEditing,
                onValueChange = { facebook = it }
            )
            SocialRow(
                icon = Res.drawable.social_tripadvisor,
                value = tripadvisor,
                placeholder = "Perfil de TripAdvisor",
                isEditing = isEditing,
                onValueChange = { tripadvisor = it }
            )
        }
    }
}

@Composable
private fun SocialRow(
    icon: DrawableResource,
    value: String,
    placeholder: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = Color.Unspecified
        )

        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            )
        } else {
            Text(
                text = value.ifEmpty { "No configurado" },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (value.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============= SCHEDULE SECTION =============

@Composable
fun ScheduleSection(
    schedule: Map<String, String>? = null,
    onSave: (Map<String, String>) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(
        "monday" to "Lunes",
        "tuesday" to "Martes",
        "wednesday" to "Miercoles",
        "thursday" to "Jueves",
        "friday" to "Viernes",
        "saturday" to "Sabado",
        "sunday" to "Domingo"
    )

    ProfileSectionCard {
        SectionHeader(
            title = "Horarios de atencion",
            isEditing = isEditing,
            onEditClick = { isEditing = !isEditing }
        )

        daysOfWeek.forEach { (key, dayName) ->
            val hours = schedule?.get(key) ?: "Cerrado"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = hours,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hours == "Cerrado") {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            if (key != "sunday") {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }
    }
}

// ============= TAGS SECTION =============

@Composable
fun TagsSection(
    tags: List<String>,
    onSave: (List<String>) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }
    var currentTags by remember(tags) { mutableStateOf(tags) }

    ProfileSectionCard {
        SectionHeader(
            title = "Etiquetas",
            isEditing = isEditing,
            onEditClick = { isEditing = !isEditing }
        )

        if (currentTags.isEmpty() && !isEditing) {
            Text(
                text = "No hay etiquetas configuradas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currentTags) { tag ->
                    TagChip(
                        text = tag,
                        onRemove = if (isEditing) {
                            { currentTags = currentTags - tag }
                        } else null
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
                        if (newTag.isNotBlank()) {
                            currentTags = currentTags + newTag.trim()
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

// ============= FACILITIES SECTION =============

@Composable
fun FacilitiesSection(
    facilities: List<String>,
    onSave: (List<String>) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var currentFacilities by remember(facilities) { mutableStateOf(facilities) }

    val availableFacilities = listOf(
        "wifi" to "WiFi",
        "parking" to "Parking",
        "delivery" to "Delivery",
        "takeaway" to "Para llevar",
        "dine_in" to "Comer aqui",
        "outdoor" to "Terraza",
        "ac" to "A/C",
        "wheelchair" to "Accesible",
        "pets" to "Pet friendly",
        "cards" to "Tarjetas"
    )

    ProfileSectionCard {
        SectionHeader(
            title = "Facilidades",
            isEditing = isEditing,
            onEditClick = { isEditing = !isEditing }
        )

        if (isEditing) {
            availableFacilities.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (key, label) ->
                        val isSelected = currentFacilities.contains(key)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currentFacilities = if (isSelected) {
                                        currentFacilities - key
                                    } else {
                                        currentFacilities + key
                                    }
                                },
                            shape = LlegoShapes.small,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                }
                            )
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        } else {
            if (currentFacilities.isEmpty()) {
                Text(
                    text = "No hay facilidades configuradas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentFacilities) { facility ->
                        val label = availableFacilities.find { it.first == facility }?.second ?: facility
                        Surface(
                            shape = LlegoShapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
