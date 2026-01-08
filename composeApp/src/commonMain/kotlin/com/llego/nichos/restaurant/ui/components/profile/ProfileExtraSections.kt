package com.llego.nichos.restaurant.ui.components.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============= SOCIAL LINKS SECTION =============

@Composable
fun SocialLinksSection(
    socialMedia: Map<String, String>? = null,
    onSave: (Map<String, String>) -> Unit = {}
) {
    var instagram by remember(socialMedia) { mutableStateOf(socialMedia?.get("instagram") ?: "") }
    var facebook by remember(socialMedia) { mutableStateOf(socialMedia?.get("facebook") ?: "") }
    var whatsapp by remember(socialMedia) { mutableStateOf(socialMedia?.get("whatsapp") ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    ProfileSectionCard {
        SectionHeader(
            title = "Redes Sociales",
            emoji = "🌐",
            isEditing = isEditing,
            onEditClick = { isEditing = !isEditing }
        )

        if (isEditing) {
            OutlinedTextField(
                value = instagram,
                onValueChange = { instagram = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Instagram") },
                leadingIcon = { Text("📷") },
                singleLine = true
            )
            OutlinedTextField(
                value = facebook,
                onValueChange = { facebook = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Facebook") },
                leadingIcon = { Text("📘") },
                singleLine = true
            )
            OutlinedTextField(
                value = whatsapp,
                onValueChange = { whatsapp = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("WhatsApp") },
                leadingIcon = { Text("📱") },
                singleLine = true
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Instagram
                Button(
                    onClick = { /* TODO: Abrir Instagram */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE91E63),
                                        Color(0xFF9C27B0),
                                        Color(0xFFFF9800)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📷", modifier = Modifier.padding(end = 8.dp))
                            Text(
                                text = "Instagram",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    }
                }

                // Facebook
                Button(
                    onClick = { /* TODO: Abrir Facebook */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "f",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Facebook",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }
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
        "wednesday" to "Miércoles",
        "thursday" to "Jueves",
        "friday" to "Viernes",
        "saturday" to "Sábado",
        "sunday" to "Domingo"
    )

    ProfileSectionCard {
        SectionHeader(
            title = "Horarios de Atención",
            emoji = "🕐",
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
                    color = Color.Black
                )
                Text(
                    text = hours,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hours == "Cerrado") Color.Gray else MaterialTheme.colorScheme.primary
                )
            }
            if (key != "sunday") {
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
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
            emoji = "🏷️",
            isEditing = isEditing,
            onEditClick = { isEditing = !isEditing }
        )

        if (currentTags.isEmpty() && !isEditing) {
            Text(
                text = "No hay etiquetas configuradas",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currentTags) { tag ->
                    TagChip(
                        text = tag,
                        onRemove = if (isEditing) {{ currentTags = currentTags - tag }} else null
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
                    singleLine = true
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
        "wifi" to "📶 WiFi",
        "parking" to "🅿️ Parking",
        "delivery" to "🛵 Delivery",
        "takeaway" to "🥡 Para Llevar",
        "dine_in" to "🍽️ Comer Aquí",
        "outdoor" to "🌳 Terraza",
        "ac" to "❄️ A/C",
        "wheelchair" to "♿ Accesible",
        "pets" to "🐕 Pet Friendly",
        "cards" to "💳 Tarjetas"
    )

    ProfileSectionCard {
        SectionHeader(
            title = "Facilidades",
            emoji = "✨",
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
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                            else 
                                Color.Gray.copy(alpha = 0.1f),
                            border = BorderStroke(
                                1.dp, 
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
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
                    color = Color.Gray
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentFacilities) { facility ->
                        val label = availableFacilities.find { it.first == facility }?.second ?: facility
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
