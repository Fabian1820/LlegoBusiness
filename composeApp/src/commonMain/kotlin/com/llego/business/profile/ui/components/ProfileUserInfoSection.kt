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

// ============= USER INFO SECTION =============

@Composable
fun UserInfoSection(
    user: User?,
    onSave: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var userName by remember(user) { mutableStateOf(user?.name ?: "") }
    var userUsername by remember(user) { mutableStateOf(user?.username ?: "") }
    var userPhone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var isEditingName by remember { mutableStateOf(false) }
    var isEditingUsername by remember { mutableStateOf(false) }
    var isEditingPhone by remember { mutableStateOf(false) }

    ProfileSectionCard {
        SectionHeader(title = "Informacion del propietario")

        EditableField(
            label = "Nombre",
            value = userName,
            onValueChange = { userName = it },
            isEditing = isEditingName,
            onEditClick = { isEditingName = true },
            onSaveClick = {
                onSave(userName.trim(), userUsername.trim(), userPhone.trim())
                isEditingName = false
            },
            onCancelClick = { userName = user?.name ?: ""; isEditingName = false },
            icon = Icons.Default.Person
        )

        EditableField(
            label = "Nombre de usuario",
            value = userUsername,
            onValueChange = { userUsername = it },
            isEditing = isEditingUsername,
            onEditClick = { isEditingUsername = true },
            onSaveClick = {
                onSave(userName.trim(), userUsername.trim(), userPhone.trim())
                isEditingUsername = false
            },
            onCancelClick = { userUsername = user?.username ?: ""; isEditingUsername = false },
            icon = Icons.Default.AlternateEmail,
            placeholder = "@usuario"
        )

        ReadOnlyField(
            label = "Email",
            value = user?.email ?: "",
            icon = Icons.Default.Email
        )

        EditableField(
            label = "Telefono",
            value = userPhone,
            onValueChange = { userPhone = it },
            isEditing = isEditingPhone,
            onEditClick = { isEditingPhone = true },
            onSaveClick = {
                onSave(userName.trim(), userUsername.trim(), userPhone.trim())
                isEditingPhone = false
            },
            onCancelClick = { userPhone = user?.phone ?: ""; isEditingPhone = false },
            icon = Icons.Default.Phone,
            placeholder = "Agregar telefono"
        )
    }
}

