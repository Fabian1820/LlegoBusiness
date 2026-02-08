package com.llego.shared.ui.business.components

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.ui.theme.LlegoCustomShapes

/**
 * Chip para seleccionar tipo de sucursal (BranchTipo)
 * Soporta seleccion multiple
 */
@Composable
fun BranchTipoChip(
    tipo: BranchTipo,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val label = when (tipo) {
        BranchTipo.RESTAURANTE -> "Restaurante"
        BranchTipo.TIENDA -> "Tienda"
        BranchTipo.DULCERIA -> "Dulceria"
        BranchTipo.CAFE -> "Cafe"
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primaryColor.copy(alpha = 0.12f),
            selectedLabelColor = primaryColor,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            selectedBorderColor = primaryColor.copy(alpha = 0.5f),
            selectedBorderWidth = 1.dp
        ),
        shape = LlegoCustomShapes.secondaryButton,
        modifier = modifier
    )
}
