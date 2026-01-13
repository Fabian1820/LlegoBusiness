package com.llego.shared.ui.components.molecules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * Selector interactivo de tags/etiquetas para el negocio
 *
 * Muestra tags sugeridos gen?ricos y permite agregar tags personalizados.
 * Los tags seleccionados se muestran como chips removibles.
 *
 * @param selectedTags Lista de tags seleccionados
 * @param onTagsChange Callback cuando cambian los tags
 * @param modifier Modificador opcional
 */
@Composable
fun TagsSelector(
    selectedTags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    var customTag by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Tags sugeridos gen?ricos
    val suggestedTags = getSuggestedTags()

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
                imageVector = Icons.Default.Label,
                contentDescription = null,
                tint = primaryColor
            )
            Text(
                text = "Etiquetas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            )
        }

        Text(
            text = "Las etiquetas ayudan a los clientes a encontrar tu negocio",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )

        // Tags seleccionados (si hay)
        if (selectedTags.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = primaryColor.copy(alpha = 0.08f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tags seleccionados:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedTags.forEach { tag ->
                            SelectedTagChip(
                                tag = tag,
                                onRemove = {
                                    onTagsChange(selectedTags - tag)
                                },
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
            }
        }

        // Tags sugeridos
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tags sugeridos:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedTags.forEach { tag ->
                    val isSelected = tag in selectedTags
                    SuggestedTagChip(
                        tag = tag,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) {
                                onTagsChange(selectedTags - tag)
                            } else {
                                onTagsChange(selectedTags + tag)
                            }
                        },
                        primaryColor = primaryColor
                    )
                }
            }
        }

        // Input para tag personalizado
        if (showCustomInput) {
            OutlinedTextField(
                value = customTag,
                onValueChange = { customTag = it },
                label = { Text("Tag personalizado") },
                placeholder = { Text("Ej: vegano, sin gluten, 24h") },
                singleLine = true,
                trailingIcon = {
                    Row {
                        if (customTag.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    if (customTag.isNotBlank() && customTag !in selectedTags) {
                                        onTagsChange(selectedTags + customTag.trim().lowercase())
                                        customTag = ""
                                        keyboardController?.hide()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Agregar")
                            }
                        }
                        IconButton(
                            onClick = {
                                showCustomInput = false
                                customTag = ""
                                keyboardController?.hide()
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (customTag.isNotBlank() && customTag !in selectedTags) {
                            onTagsChange(selectedTags + customTag.trim().lowercase())
                            customTag = ""
                        }
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedButton(
                onClick = { showCustomInput = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar tag personalizado")
            }
        }

        // L?mite de tags
        if (selectedTags.size >= 10) {
            Text(
                text = "?? M?ximo 10 tags alcanzado",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}

/**
 * Chip de tag sugerido (seleccionable)
 */
@Composable
private fun SuggestedTagChip(
    tag: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = tag,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primaryColor.copy(alpha = 0.15f),
            selectedLabelColor = primaryColor,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            selectedBorderColor = primaryColor,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderWidth = 2.dp
        )
    )
}

/**
 * Chip de tag seleccionado (removible)
 */
@Composable
private fun SelectedTagChip(
    tag: String,
    onRemove: () -> Unit,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    AssistChip(
        onClick = onRemove,
        label = {
            Text(
                text = tag,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Quitar",
                modifier = Modifier.size(18.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = primaryColor,
            labelColor = androidx.compose.ui.graphics.Color.White,
            trailingIconContentColor = androidx.compose.ui.graphics.Color.White
        )
    )
}

/**
 * Obtiene tags sugeridos gen?ricos
 */
private fun getSuggestedTags(): List<String> {
    return listOf(
        "delivery",
        "24 horas",
        "para llevar",
        "familiar",
        "premium",
        "econ?mico",
        "r?pido",
        "local",
        "recomendado",
        "nuevo",
        "popular",
        "promociones"
    )
}
