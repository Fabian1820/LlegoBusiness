package com.llego.shared.ui.components.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.PaymentMethod
import com.llego.shared.ui.theme.LlegoCustomShapes

enum class PaymentMethodSelectorLayout {
    HORIZONTAL,
    FLOW
}

/**
 * Selector de métodos de pago para sucursales
 * Permite seleccionar múltiples métodos de pago de una lista disponible
 */
@Composable
fun PaymentMethodSelector(
    availablePaymentMethods: List<PaymentMethod>,
    selectedPaymentMethodIds: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    layout: PaymentMethodSelectorLayout = PaymentMethodSelectorLayout.HORIZONTAL
) {
    val availablePaymentMethodIds = remember(availablePaymentMethods) {
        availablePaymentMethods.map { it.id }.toSet()
    }
    val visibleSelectedPaymentMethodIds = remember(selectedPaymentMethodIds, availablePaymentMethodIds) {
        selectedPaymentMethodIds.filter { it in availablePaymentMethodIds }
    }

    LaunchedEffect(selectedPaymentMethodIds, visibleSelectedPaymentMethodIds) {
        if (selectedPaymentMethodIds != visibleSelectedPaymentMethodIds) {
            onSelectionChange(visibleSelectedPaymentMethodIds)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Métodos de Pago *",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Selecciona los métodos de pago que acepta esta sucursal",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(16.dp)
                    )
                }
            }
            !errorMessage.isNullOrBlank() -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (onRetry != null) {
                        OutlinedButton(
                            onClick = onRetry,
                            enabled = enabled
                        ) {
                            Text("Reintentar carga")
                        }
                    }
                }
            }
            availablePaymentMethods.isEmpty() -> {
                Text(
                    text = "No hay métodos de pago disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                when (layout) {
                    PaymentMethodSelectorLayout.HORIZONTAL -> {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(availablePaymentMethods) { paymentMethod ->
                                PaymentMethodChip(
                                    paymentMethod = paymentMethod,
                                    isSelected = visibleSelectedPaymentMethodIds.contains(paymentMethod.id),
                                    onClick = {
                                        val newSelection = if (visibleSelectedPaymentMethodIds.contains(paymentMethod.id)) {
                                            visibleSelectedPaymentMethodIds - paymentMethod.id
                                        } else {
                                            visibleSelectedPaymentMethodIds + paymentMethod.id
                                        }
                                        onSelectionChange(newSelection)
                                    },
                                    enabled = enabled
                                )
                            }
                        }
                    }
                    PaymentMethodSelectorLayout.FLOW -> {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availablePaymentMethods.forEach { paymentMethod ->
                                PaymentMethodChip(
                                    paymentMethod = paymentMethod,
                                    isSelected = visibleSelectedPaymentMethodIds.contains(paymentMethod.id),
                                    onClick = {
                                        val newSelection = if (visibleSelectedPaymentMethodIds.contains(paymentMethod.id)) {
                                            visibleSelectedPaymentMethodIds - paymentMethod.id
                                        } else {
                                            visibleSelectedPaymentMethodIds + paymentMethod.id
                                        }
                                        onSelectionChange(newSelection)
                                    },
                                    enabled = enabled
                                )
                            }
                        }
                    }
                }
            }
        }

        if (
            visibleSelectedPaymentMethodIds.isEmpty() &&
            !isLoading &&
            errorMessage.isNullOrBlank() &&
            availablePaymentMethods.isNotEmpty()
        ) {
            Text(
                text = "Debes seleccionar al menos un método de pago",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Chip individual para un método de pago
 */
@Composable
private fun PaymentMethodChip(
    paymentMethod: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = paymentMethod.name,
                style = MaterialTheme.typography.labelMedium
            )
        },
        enabled = enabled,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primaryColor.copy(alpha = 0.12f),
            selectedLabelColor = primaryColor,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            selectedBorderColor = primaryColor.copy(alpha = 0.5f),
            selectedBorderWidth = 1.dp,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        shape = LlegoCustomShapes.secondaryButton,
        modifier = modifier
    )
}
