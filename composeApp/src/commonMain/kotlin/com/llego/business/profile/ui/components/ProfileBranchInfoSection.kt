package com.llego.business.profile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.shared.ui.components.NetworkImage
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.User
import com.llego.shared.data.model.toDisplayName
import com.llego.shared.utils.formatDouble
import com.llego.shared.ui.components.molecules.SchedulePicker
import com.llego.shared.ui.components.molecules.toBackendSchedule
import com.llego.shared.ui.components.molecules.toDaySchedule
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes

// ============= BRANCH INFO SECTION =============

@Composable
fun BranchInfoSection(
    branch: Branch?,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Informacion de la sucursal")
            StatusBadge(branch?.isActive)
        }

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
    }
}
