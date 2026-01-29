package com.llego.business.branches.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.ui.theme.LlegoCustomShapes

@Composable
fun BranchTipoSelector(
    selectedTipos: Set<BranchTipo>,
    onSelectionChange: (Set<BranchTipo>) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val options = listOf(
        BranchTipo.RESTAURANTE,
        BranchTipo.TIENDA,
        BranchTipo.DULCERIA
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { tipo ->
            val selected = tipo in selectedTipos
            val (label, color) = when (tipo) {
                BranchTipo.RESTAURANTE -> "Restaurante" to secondaryColor
                BranchTipo.TIENDA -> "Tienda" to primaryColor
                BranchTipo.DULCERIA -> "Dulceria" to tertiaryColor
            }

            FilterChip(
                selected = selected,
                onClick = {
                    val updated = if (selected) {
                        selectedTipos - tipo
                    } else {
                        selectedTipos + tipo
                    }
                    onSelectionChange(updated)
                },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.15f),
                    selectedLabelColor = color,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    selectedBorderColor = color.copy(alpha = 0.6f),
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                shape = LlegoCustomShapes.secondaryButton
            )
        }
    }
}

@Composable
fun BranchStatusSelector(
    status: String,
    onStatusChange: (String) -> Unit
) {
    val activeSelected = status == "active"
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = activeSelected,
            onClick = { onStatusChange("active") },
            label = { Text("Activa") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = activeColor.copy(alpha = 0.15f),
                selectedLabelColor = activeColor,
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = activeSelected,
                selectedBorderColor = activeColor.copy(alpha = 0.6f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            shape = LlegoCustomShapes.secondaryButton
        )
        FilterChip(
            selected = !activeSelected,
            onClick = { onStatusChange("inactive") },
            label = { Text("Inactiva") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = inactiveColor.copy(alpha = 0.15f),
                selectedLabelColor = inactiveColor,
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = !activeSelected,
                selectedBorderColor = inactiveColor.copy(alpha = 0.6f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            shape = LlegoCustomShapes.secondaryButton
        )
    }
}
