package com.llego.shared.ui.components.molecules

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.SupportedCurrency
import com.llego.shared.ui.theme.LlegoCustomShapes

/**
 * Selector de moneda para productos.
 * 
 * Permite seleccionar una moneda de la lista de monedas soportadas
 * usando un dropdown de Material3.
 *
 * @param selectedCurrency Código de la moneda seleccionada (ej: "USD")
 * @param onCurrencySelected Callback cuando se selecciona una moneda
 * @param modifier Modificador opcional
 * @param label Etiqueta del campo (por defecto "Moneda")
 * @param enabled Si el selector está habilitado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Moneda",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Obtener la moneda seleccionada actual
    val currentCurrency = SupportedCurrency.fromCode(selectedCurrency) ?: SupportedCurrency.USD
    
    // Colores del tema
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "${currentCurrency.symbol} ${currentCurrency.displayName}",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = LlegoCustomShapes.inputField,
            colors = textFieldColors,
            singleLine = true
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SupportedCurrency.entries.forEach { currency ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currency.symbol,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = currency.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = currency.code,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    onClick = {
                        onCurrencySelected(currency.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
