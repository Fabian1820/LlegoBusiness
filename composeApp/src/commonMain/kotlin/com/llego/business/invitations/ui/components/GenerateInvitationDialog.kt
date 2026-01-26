package com.llego.business.invitations.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.business.invitations.data.model.InvitationDuration
import com.llego.business.invitations.data.model.InvitationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateInvitationDialog(
    branches: List<Pair<String, String>>, // List of (id, name)
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
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text("Generar código de invitación") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type selection
                Text(
                    text = "Tipo de acceso",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == InvitationType.BRANCH,
                            onClick = { selectedType = InvitationType.BRANCH },
                            label = { Text("Sucursal específica") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedType == InvitationType.BUSINESS,
                            onClick = { selectedType = InvitationType.BUSINESS },
                            label = { Text("Negocio completo") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Text(
                        text = if (selectedType == InvitationType.BRANCH) {
                            "El usuario tendrá acceso solo a la sucursal seleccionada"
                        } else {
                            "El usuario tendrá acceso a todas las sucursales del negocio"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Branch selection (only for BRANCH type)
                if (selectedType == InvitationType.BRANCH && branches.isNotEmpty()) {
                    Text(
                        text = "Sucursal",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = showBranchDropdown,
                        onExpandedChange = { showBranchDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = branches.find { it.first == selectedBranchId }?.second ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBranchDropdown) }
                        )
                        
                        ExposedDropdownMenu(
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
                
                // Duration selection
                Text(
                    text = "Duración del acceso",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedDuration is InvitationDuration.Indefinite,
                            onClick = { selectedDuration = InvitationDuration.Indefinite },
                            label = { Text("Indefinido") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedDuration == InvitationDuration.ONE_DAY,
                            onClick = { selectedDuration = InvitationDuration.ONE_DAY },
                            label = { Text("1 día") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedDuration == InvitationDuration.ONE_WEEK,
                            onClick = { selectedDuration = InvitationDuration.ONE_WEEK },
                            label = { Text("7 días") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedDuration == InvitationDuration.ONE_MONTH,
                            onClick = { selectedDuration = InvitationDuration.ONE_MONTH },
                            label = { Text("30 días") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Custom duration
                    OutlinedTextField(
                        value = customDays,
                        onValueChange = { 
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                customDays = it
                                if (it.isNotEmpty()) {
                                    val days = it.toIntOrNull()
                                    if (days != null && days in 1..365) {
                                        selectedDuration = InvitationDuration.Days(days)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Días personalizados (1-365)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = {
                            if (customDays.isNotEmpty()) {
                                val days = customDays.toIntOrNull()
                                if (days == null || days !in 1..365) {
                                    Text("Debe ser un número entre 1 y 365")
                                }
                            }
                        },
                        isError = customDays.isNotEmpty() && (customDays.toIntOrNull() == null || customDays.toInt() !in 1..365)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val branchId = if (selectedType == InvitationType.BRANCH) selectedBranchId else null
                    val durationDays = selectedDuration.toDays()
                    onGenerate(selectedType, branchId, durationDays)
                },
                enabled = selectedType == InvitationType.BUSINESS || selectedBranchId.isNotEmpty()
            ) {
                Text("Generar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
