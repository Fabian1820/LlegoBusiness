package com.llego.business.orders.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.ui.viewmodel.DateRangeFilter
import com.llego.shared.ui.theme.LlegoCustomShapes

/**
 * Filtros estilo iOS 16 - Dos selectores lado a lado que despliegan opciones animadas
 * Actualizado para usar OrderStatus del nuevo modelo
 * Requirements: 9.1, 9.5
 */
@Composable
fun iOSStyleFilters(
    selectedDateRange: DateRangeFilter,
    onDateRangeSelected: (DateRangeFilter) -> Unit,
    selectedStatus: OrderStatus?,
    onStatusSelected: (OrderStatus) -> Unit,
    onStatusCleared: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStatusPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        iOSStylePicker(
            label = "Fecha",
            selectedValue = selectedDateRange.displayName,
            isExpanded = showDatePicker,
            onToggle = { showDatePicker = !showDatePicker },
            modifier = Modifier.weight(1f)
        ) {
            DateRangeFilter.values().filter { it != DateRangeFilter.CUSTOM }.forEach { range ->
                iOSStylePickerOption(
                    text = range.displayName,
                    isSelected = selectedDateRange == range,
                    onClick = {
                        onDateRangeSelected(range)
                        showDatePicker = false
                    }
                )
            }
        }

        iOSStylePicker(
            label = "Estado",
            selectedValue = selectedStatus?.getDisplayName() ?: "Todos",
            isExpanded = showStatusPicker,
            onToggle = { showStatusPicker = !showStatusPicker },
            modifier = Modifier.weight(1f)
        ) {
            val businessStatuses = listOf(
                OrderStatus.AWAITING_DELIVERY_ACCEPTANCE,
                OrderStatus.PENDING_PAYMENT,
                OrderStatus.PAYMENT_IN_PROGRESS,
                OrderStatus.PENDING_ACCEPTANCE,
                OrderStatus.MODIFIED_BY_STORE,
                OrderStatus.REJECTED_BY_STORE,
                OrderStatus.ACCEPTED,
                OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.ON_THE_WAY,
                OrderStatus.DELIVERED,
                OrderStatus.CANCELLED
            )
            iOSStylePickerOption(
                text = "Todos",
                isSelected = selectedStatus == null,
                onClick = {
                    onStatusCleared()
                    showStatusPicker = false
                }
            )
            businessStatuses.forEach { status ->
                iOSStylePickerOption(
                    text = status.getDisplayName(),
                    isSelected = selectedStatus == status,
                    onClick = {
                        onStatusSelected(status)
                        showStatusPicker = false
                    }
                )
            }
        }
    }
}

/**
 * Selector estilo iOS 16 con animacion de despliegue - Sin ripple para evitar delay visual
 */
@Composable
fun iOSStylePicker(
    label: String,
    selectedValue: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (isExpanded) 4.dp else 1.dp,
        animationSpec = tween(150),
        label = "picker_elevation"
    )

    BoxWithConstraints(modifier = modifier) {
        val pickerWidth = maxWidth

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onToggle,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = LlegoCustomShapes.secondaryButton,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = elevation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedValue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                if (isExpanded) onToggle()
            },
            modifier = Modifier.width(pickerWidth),
            offset = DpOffset(x = 0.dp, y = 4.dp),
            shape = LlegoCustomShapes.secondaryButton,
            containerColor = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Opcion individual del picker estilo iOS
 */
@Composable
fun iOSStylePickerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    if (!isSelected) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 14.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
            thickness = 0.5.dp
        )
    }
}
