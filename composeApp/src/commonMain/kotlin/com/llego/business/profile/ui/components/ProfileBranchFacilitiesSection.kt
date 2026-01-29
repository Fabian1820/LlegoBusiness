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

// ============= BRANCH FACILITIES SECTION =============

@Composable
fun BranchFacilitiesSection(
    branch: Branch?,
    onSave: (List<String>) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var currentFacilities by remember(branch) { mutableStateOf(branch?.facilities ?: emptyList()) }

    val facilityOptions = listOf(
        "parking" to "Estacionamiento",
        "wifi" to "WiFi gratis",
        "ac" to "Aire acondicionado",
        "wheelchair" to "Acceso sillas de ruedas",
        "terrace" to "Terraza",
        "kids_area" to "Zona para ninos",
        "pet_friendly" to "Pet friendly",
        "takeout" to "Para llevar",
        "card_payment" to "Pago con tarjeta",
        "delivery" to "Delivery propio"
    )

    ProfileSectionCard {
        SectionHeader(
            title = "Instalaciones",
            isEditing = isEditing,
            onEditClick = {
                if (isEditing) {
                    onSave(currentFacilities)
                } else {
                    currentFacilities = branch?.facilities ?: emptyList()
                }
                isEditing = !isEditing
            }
        )

        if (isEditing) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                facilityOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = currentFacilities.contains(key),
                        onClick = {
                            currentFacilities = if (currentFacilities.contains(key)) {
                                currentFacilities - key
                            } else {
                                currentFacilities + key
                            }
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = currentFacilities.contains(key),
                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        } else {
            if (currentFacilities.isEmpty()) {
                Text(
                    text = "Sin instalaciones especificadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(currentFacilities) { facility ->
                        val label = facilityOptions.firstOrNull { it.first == facility }?.second ?: facility
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(label) },
                            enabled = false,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = false,
                                selected = false,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    }
}

