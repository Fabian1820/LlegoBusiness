package com.llego.shared.ui.components.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Input de teléfono con selector de código de país
 *
 * @param phoneNumber Número de teléfono sin código de país
 * @param countryCode Código de país seleccionado (ej: "+51")
 * @param onPhoneChange Callback cuando cambia el número
 * @param onCountryCodeChange Callback cuando cambia el código de país
 * @param modifier Modificador opcional
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneInput(
    phoneNumber: String,
    countryCode: String,
    onPhoneChange: (String) -> Unit,
    onCountryCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var showCountryPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneChange,
            label = { Text("Teléfono") },
            placeholder = { Text("999 999 999") },
            leadingIcon = {
                Row(
                    modifier = Modifier
                        .clickable { showCountryPicker = true }
                        .padding(start = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = countryCode,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Seleccionar código",
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown de códigos de país
        if (showCountryPicker) {
            CountryCodePicker(
                onCountrySelected = { code ->
                    onCountryCodeChange(code)
                    showCountryPicker = false
                },
                onDismiss = { showCountryPicker = false }
            )
        }
    }
}

/**
 * Picker de códigos de país
 */
@Composable
private fun CountryCodePicker(
    onCountrySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val countryCodes = listOf(
        "+1" to "Estados Unidos / Canadá",
        "+52" to "México",
        "+51" to "Perú",
        "+54" to "Argentina",
        "+56" to "Chile",
        "+57" to "Colombia",
        "+58" to "Venezuela",
        "+591" to "Bolivia",
        "+593" to "Ecuador",
        "+595" to "Paraguay",
        "+598" to "Uruguay",
        "+34" to "España",
        "+53" to "Cuba",
        "+507" to "Panamá",
        "+506" to "Costa Rica"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar código de país") },
        text = {
            Column {
                countryCodes.forEach { (code, country) ->
                    TextButton(
                        onClick = { onCountrySelected(code) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(code, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                country,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Combina el código de país y el número en formato completo
 */
fun combinePhoneNumber(countryCode: String, phoneNumber: String): String {
    return "$countryCode $phoneNumber"
}

/**
 * Separa un número completo en código de país y número
 */
fun separatePhoneNumber(fullNumber: String): Pair<String, String> {
    val trimmed = fullNumber.trim()
    val codes = listOf("+591", "+593", "+595", "+598", "+507", "+506", "+53", "+52", "+51", "+54", "+56", "+57", "+58", "+34", "+1")

    for (code in codes) {
        if (trimmed.startsWith(code)) {
            return Pair(code, trimmed.removePrefix(code).trim())
        }
    }

    return Pair("+51", trimmed) // Default a Perú
}
