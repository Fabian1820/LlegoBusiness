package com.llego.shared.ui.components.molecules

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Selector múltiple de instalaciones y servicios del negocio
 *
 * Permite seleccionar múltiples instalaciones/servicios que ofrece el negocio
 * mediante chips interactivos con emojis.
 *
 * @param selectedFacilities Lista de instalaciones seleccionadas (keys)
 * @param onFacilitiesChange Callback cuando cambian las instalaciones
 * @param modifier Modificador opcional
 */
@Composable
fun FacilitiesSelector(
    selectedFacilities: List<String>,
    onFacilitiesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Instalaciones disponibles con key, emoji y label
    val availableFacilities = listOf(
        Triple("parking", "🅿️", "Estacionamiento"),
        Triple("wifi", "📶", "WiFi gratis"),
        Triple("ac", "❄️", "Aire acondicionado"),
        Triple("wheelchair", "♿", "Acceso para sillas de ruedas"),
        Triple("terrace", "🌳", "Terraza"),
        Triple("kids_area", "👶", "Zona para niños"),
        Triple("pet_friendly", "🐕", "Pet friendly"),
        Triple("takeout", "🥡", "Para llevar"),
        Triple("card_payment", "💳", "Pago con tarjeta"),
        Triple("delivery", "🚚", "Delivery propio")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = primaryColor
            )
            Text(
                text = "Instalaciones y Servicios",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            )
        }

        Text(
            text = "Selecciona las instalaciones y servicios que ofrece tu negocio",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )

        // Grid de chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableFacilities.forEach { (key, emoji, label) ->
                FacilityChip(
                    emoji = emoji,
                    label = label,
                    selected = key in selectedFacilities,
                    onClick = {
                        val newFacilities = if (key in selectedFacilities) {
                            selectedFacilities - key
                        } else {
                            selectedFacilities + key
                        }
                        onFacilitiesChange(newFacilities)
                    },
                    primaryColor = primaryColor
                )
            }
        }

        // Contador de seleccionados
        if (selectedFacilities.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = primaryColor.copy(alpha = 0.08f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ ${selectedFacilities.size} instalación(es) seleccionada(s)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = primaryColor
                    ),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

/**
 * Chip individual de instalación
 */
@Composable
private fun FacilityChip(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primaryColor.copy(alpha = 0.15f),
            selectedLabelColor = primaryColor,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = primaryColor,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderWidth = 2.dp
        )
    )
}
