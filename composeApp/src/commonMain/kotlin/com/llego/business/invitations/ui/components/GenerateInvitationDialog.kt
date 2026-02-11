package com.llego.business.invitations.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.business.invitations.data.model.InvitationDuration
import com.llego.business.invitations.data.model.InvitationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateInvitationDialog(
    branches: List<Pair<String, String>>,
    businessId: String,
    businessName: String,
    onDismiss: () -> Unit,
    onGenerate: (InvitationType, String?, Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(InvitationType.BRANCH) }
    var selectedBranchId by remember { mutableStateOf(branches.firstOrNull()?.first ?: "") }
    var selectedDuration by remember { mutableStateOf<InvitationDuration>(InvitationDuration.Indefinite) }
    var customDays by remember { mutableStateOf("") }
    var showBranchDropdown by remember { mutableStateOf(false) }

    val customDaysValue = customDays.toIntOrNull()
    val customDaysInvalid = customDays.isNotBlank() && (customDaysValue == null || customDaysValue !in 1..365)
    val canGenerate = (selectedType == InvitationType.BUSINESS || selectedBranchId.isNotBlank()) && !customDaysInvalid

    val accessSummary = when (selectedType) {
        InvitationType.BRANCH -> {
            val branchName = branches.firstOrNull { it.first == selectedBranchId }?.second ?: "Sucursal no seleccionada"
            "Acceso para sucursal: $branchName"
        }
        InvitationType.BUSINESS -> "Acceso para todo el negocio"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Nuevo codigo de invitacion",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = businessName.ifBlank { businessId },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tipo de acceso",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedType == InvitationType.BRANCH,
                                onClick = { selectedType = InvitationType.BRANCH },
                                label = { Text("Sucursal") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            FilterChip(
                                selected = selectedType == InvitationType.BUSINESS,
                                onClick = { selectedType = InvitationType.BUSINESS },
                                label = { Text("Negocio") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (selectedType == InvitationType.BRANCH) {
                            ExposedDropdownMenuBox(
                                expanded = showBranchDropdown,
                                onExpandedChange = { showBranchDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = branches.firstOrNull { it.first == selectedBranchId }?.second ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Sucursal") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBranchDropdown)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                DropdownMenu(
                                    expanded = showBranchDropdown,
                                    onDismissRequest = { showBranchDropdown = false }
                                ) {
                                    branches.forEach { (id, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                selectedBranchId = id
                                                showBranchDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Duracion del acceso",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedDuration is InvitationDuration.Indefinite,
                                onClick = {
                                    selectedDuration = InvitationDuration.Indefinite
                                    customDays = ""
                                },
                                label = { Text("Indefinido") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedDuration == InvitationDuration.ONE_DAY,
                                onClick = {
                                    selectedDuration = InvitationDuration.ONE_DAY
                                    customDays = ""
                                },
                                label = { Text("1 dia") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedDuration == InvitationDuration.ONE_WEEK,
                                onClick = {
                                    selectedDuration = InvitationDuration.ONE_WEEK
                                    customDays = ""
                                },
                                label = { Text("7 dias") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedDuration == InvitationDuration.ONE_MONTH,
                                onClick = {
                                    selectedDuration = InvitationDuration.ONE_MONTH
                                    customDays = ""
                                },
                                label = { Text("30 dias") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = customDays,
                            onValueChange = {
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    customDays = it
                                    val days = it.toIntOrNull()
                                    if (days != null && days in 1..365) {
                                        selectedDuration = InvitationDuration.Days(days)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Dias personalizados (1-365)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = customDaysInvalid,
                            supportingText = {
                                if (customDaysInvalid) {
                                    Text("Introduce un valor valido entre 1 y 365")
                                }
                            }
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Resumen",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = accessSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Vigencia: ${selectedDurationLabel(selectedDuration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val branchId = if (selectedType == InvitationType.BRANCH) selectedBranchId else null
                    val durationDays = selectedDuration.toDays()
                    onGenerate(selectedType, branchId, durationDays)
                },
                enabled = canGenerate
            ) {
                Text("Generar codigo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun selectedDurationLabel(duration: InvitationDuration): String = when (duration) {
    is InvitationDuration.Indefinite -> "Indefinida"
    is InvitationDuration.Days -> "${duration.days} dias"
}
