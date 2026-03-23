package com.llego.business.profile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.branches.ui.components.BranchStatusSelector
import com.llego.business.branches.ui.components.BranchTipoSelector
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BranchTipo

// ============= BRANCH INFO SECTION =============

@Composable
fun BranchInfoSection(
    branch: Branch?,
    isActive: Boolean = branch?.isActive ?: false,
    onStatusChange: (Boolean) -> Unit = {},
    selectedTipos: Set<BranchTipo> = emptySet(),
    onTiposChange: (Set<BranchTipo>) -> Unit = {},
    onSave: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var branchName by remember(branch) { mutableStateOf(branch?.name ?: "") }
    var branchPhone by remember(branch) { mutableStateOf(branch?.phone ?: "") }
    var branchAddress by remember(branch) { mutableStateOf(branch?.address ?: "") }

    var isEditingName by remember { mutableStateOf(false) }
    var isEditingPhone by remember { mutableStateOf(false) }
    var isEditingAddress by remember { mutableStateOf(false) }

    val saveChanges = {
        onSave(
            branchName.trim(),
            branchPhone.trim(),
            branchAddress.trim()
        )
    }

    ProfileSectionCard {
        SectionHeader(
            title = "Información de la sucursal",
            sectionIcon = Icons.Default.Store
        )

        Text(
            text = "Estado de sucursal",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BranchStatusSelector(
            isActive = isActive,
            onStatusChange = onStatusChange
        )

        EditableField(
            label = "Nombre",
            value = branchName,
            onValueChange = { branchName = it },
            isEditing = isEditingName,
            onEditClick = { isEditingName = true },
            onSaveClick = {
                saveChanges()
                isEditingName = false
            },
            onCancelClick = { branchName = branch?.name ?: ""; isEditingName = false },
            icon = Icons.Default.Store
        )

        EditableField(
            label = "Telefono",
            value = branchPhone,
            onValueChange = { branchPhone = it },
            isEditing = isEditingPhone,
            onEditClick = { isEditingPhone = true },
            onSaveClick = {
                saveChanges()
                isEditingPhone = false
            },
            onCancelClick = { branchPhone = branch?.phone ?: ""; isEditingPhone = false },
            icon = Icons.Default.Phone
        )

        EditableField(
            label = "Direccion",
            value = branchAddress,
            onValueChange = { branchAddress = it },
            isEditing = isEditingAddress,
            onEditClick = { isEditingAddress = true },
            onSaveClick = {
                saveChanges()
                isEditingAddress = false
            },
            onCancelClick = { branchAddress = branch?.address ?: ""; isEditingAddress = false },
            icon = Icons.Default.LocationOn,
            placeholder = "Agregar direccion"
        )

        Text(
            text = "Tipos de servicio",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BranchTipoSelector(
            selectedTipos = selectedTipos,
            onSelectionChange = onTiposChange
        )
    }
}
