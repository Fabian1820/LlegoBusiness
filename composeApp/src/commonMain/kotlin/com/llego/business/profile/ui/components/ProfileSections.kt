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

// ============= BANNER SECTION =============

@Composable
fun BannerWithLogoSection(
    avatarUrl: String? = null,
    coverUrl: String? = null,
    onChangeAvatar: () -> Unit = {},
    onChangeCover: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        // Banner de fondo - Imagen o gradiente
        if (!coverUrl.isNullOrEmpty()) {
            // Mostrar imagen de portada desde el backend
            NetworkImage(
                url = coverUrl,
                contentDescription = "Portada del negocio",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback: gradiente por defecto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )
        }

        // Boton para cambiar portada - solo mostrar si onChangeCover no es null
        if (onChangeCover != null) {
            IconButton(
                onClick = onChangeCover,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar portada",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Logo circular
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp)
                .offset(y = 48.dp)
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                if (!avatarUrl.isNullOrEmpty()) {
                    // Mostrar avatar desde el backend
                    NetworkImage(
                        url = avatarUrl,
                        contentDescription = "Logo del negocio",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback: icono por defecto
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Boton para cambiar avatar
            IconButton(
                onClick = onChangeAvatar,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Cambiar logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(48.dp))
}

// ============= BUSINESS INFO SECTION =============

@Composable
fun BusinessInfoSection(
    business: Business?,
    onSave: (name: String, description: String, tags: List<String>) -> Unit = { _, _, _ -> }
) {
    var businessName by remember(business) { mutableStateOf(business?.name ?: "") }
    var description by remember(business) { mutableStateOf(business?.description ?: "") }
    var isEditingName by remember { mutableStateOf(false) }
    var isEditingDescription by remember { mutableStateOf(false) }

    val rating = business?.globalRating ?: 0.0
    val tags = business?.tags ?: emptyList()
    val saveChanges = {
        onSave(businessName.trim(), description.trim(), tags)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nombre y rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Nombre editable
                if (isEditingName) {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = {
                                    saveChanges()
                                    isEditingName = false
                                }) {
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    businessName = business?.name ?: ""
                                    isEditingName = false
                                }) {
                                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = LlegoCustomShapes.inputField
                    )
                } else {
                    Text(
                        text = businessName.ifEmpty { "Sin nombre" },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { isEditingName = true }
                    )
                }

            }

            // Rating badge
            Surface(
                shape = LlegoShapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = formatDouble("%.1f", rating),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Descripcion
        if (isEditingDescription) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripcion") },
                minLines = 3,
                maxLines = 5,
                trailingIcon = {
                    Row {
                        IconButton(onClick = {
                            saveChanges()
                            isEditingDescription = false
                        }) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            description = business?.description ?: ""
                            isEditingDescription = false
                        }) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = LlegoCustomShapes.inputField
            )
        } else {
            Text(
                text = description.ifEmpty { "Agregar descripcion" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (description.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.clickable { isEditingDescription = true }
            )
        }
    }
}

// ============= USER INFO SECTION =============

@Composable
fun UserInfoSection(
    user: User?,
    onSave: (String, String) -> Unit = { _, _ -> }
) {
    var userName by remember(user) { mutableStateOf(user?.name ?: "") }
    var userPhone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var isEditingName by remember { mutableStateOf(false) }
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
                onSave(userName.trim(), userPhone.trim())
                isEditingName = false
            },
            onCancelClick = { userName = user?.name ?: ""; isEditingName = false },
            icon = Icons.Default.Person
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
                onSave(userName.trim(), userPhone.trim())
                isEditingPhone = false
            },
            onCancelClick = { userPhone = user?.phone ?: ""; isEditingPhone = false },
            icon = Icons.Default.Phone,
            placeholder = "Agregar telefono"
        )
    }
}

// ============= BRANCH INFO SECTION =============

@Composable
fun BranchInfoSection(
    branch: Branch?,
    onSave: (String, String, String, Double?) -> Unit = { _, _, _, _ -> }
) {
    var branchName by remember(branch) { mutableStateOf(branch?.name ?: "") }
    var branchPhone by remember(branch) { mutableStateOf(branch?.phone ?: "") }
    var branchAddress by remember(branch) { mutableStateOf(branch?.address ?: "") }
    var deliveryRadius by remember(branch) { mutableStateOf(branch?.deliveryRadius?.toString() ?: "") }

    var isEditingName by remember { mutableStateOf(false) }
    var isEditingPhone by remember { mutableStateOf(false) }
    var isEditingAddress by remember { mutableStateOf(false) }
    var isEditingRadius by remember { mutableStateOf(false) }

    val tiposLabel = branch?.tipos
        ?.joinToString(", ") { it.toDisplayName() }
        .orEmpty()

    val saveChanges = {
        onSave(
            branchName.trim(),
            branchPhone.trim(),
            branchAddress.trim(),
            deliveryRadius.toDoubleOrNull()
        )
    }

    ProfileSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Informacion de la sucursal")
            StatusBadge(branch?.status)
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

        ReadOnlyField(
            label = "Tipos",
            value = tiposLabel,
            icon = Icons.Default.Label
        )

        EditableField(
            label = "Radio de entrega (km)",
            value = deliveryRadius,
            onValueChange = { deliveryRadius = it },
            isEditing = isEditingRadius,
            onEditClick = { isEditingRadius = true },
            onSaveClick = {
                saveChanges()
                isEditingRadius = false
            },
            onCancelClick = { deliveryRadius = branch?.deliveryRadius?.toString() ?: ""; isEditingRadius = false },
            icon = Icons.Default.DeliveryDining,
            placeholder = "Ej: 5.0"
        )
    }
}

// ============= BUSINESS TAGS SECTION =============

@Composable
fun BusinessTagsSection(
    business: Business?,
    onSave: (List<String>) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var currentTags by remember(business) { mutableStateOf(business?.tags ?: emptyList()) }
    var newTag by remember { mutableStateOf("") }

    ProfileSectionCard {
        SectionHeader(
            title = "Etiquetas del negocio",
            isEditing = isEditing,
            onEditClick = {
                if (isEditing) {
                    onSave(currentTags)
                } else {
                    currentTags = business?.tags ?: emptyList()
                }
                isEditing = !isEditing
            }
        )

        if (currentTags.isEmpty() && !isEditing) {
            Text(
                text = "Sin etiquetas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(currentTags) { tag ->
                    TagChip(
                        text = tag,
                        onRemove = if (isEditing) {{
                            currentTags = currentTags - tag
                        }} else null
                    )
                }
            }
        }

        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nueva etiqueta") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = LlegoCustomShapes.inputField
                )
                IconButton(
                    onClick = {
                        val tag = newTag.trim()
                        if (tag.isNotEmpty() && !currentTags.contains(tag)) {
                            currentTags = currentTags + tag
                            newTag = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, "Agregar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ============= BRANCH FACILITIES SECTION =============

@Composable
fun BranchFacilitiesSection(
    branch: Branch?,
    onSave: (List<String>) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var currentFacilities by remember(branch) { mutableStateOf(branch?.facilities ?: emptyList()) }

    val facilityOptions = listOf(
        "parking" to "Estacionamiento",
        "wifi" to "WiFi gratis",
        "ac" to "Aire acondicionado",
        "wheelchair" to "Acceso sillas de ruedas",
        "terrace" to "Terraza",
        "kids_area" to "Zona para ninos",
        "pet_friendly" to "Pet friendly",
        "takeout" to "Para llevar",
        "card_payment" to "Pago con tarjeta",
        "delivery" to "Delivery propio"
    )

    ProfileSectionCard {
        SectionHeader(
            title = "Instalaciones",
            isEditing = isEditing,
            onEditClick = {
                if (isEditing) {
                    onSave(currentFacilities)
                } else {
                    currentFacilities = branch?.facilities ?: emptyList()
                }
                isEditing = !isEditing
            }
        )

        if (isEditing) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                facilityOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = currentFacilities.contains(key),
                        onClick = {
                            currentFacilities = if (currentFacilities.contains(key)) {
                                currentFacilities - key
                            } else {
                                currentFacilities + key
                            }
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = currentFacilities.contains(key),
                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        } else {
            if (currentFacilities.isEmpty()) {
                Text(
                    text = "Sin instalaciones especificadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(currentFacilities) { facility ->
                        val label = facilityOptions.firstOrNull { it.first == facility }?.second ?: facility
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(label) },
                            enabled = false,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = false,
                                selected = false,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ============= BRANCH SCHEDULE SECTION =============

@Composable
fun BranchScheduleSection(
    branch: Branch?,
    onSave: (Map<String, List<String>>) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    val backendSchedule = branch?.schedule ?: emptyMap()
    var editableSchedule by remember(branch) { mutableStateOf(backendSchedule.toDaySchedule()) }

    val dayNames = listOf(
        "mon" to "Lunes",
        "tue" to "Martes",
        "wed" to "Miercoles",
        "thu" to "Jueves",
        "fri" to "Viernes",
        "sat" to "Sabado",
        "sun" to "Domingo"
    )

    ProfileSectionCard {
        SectionHeader(
            title = "Horarios de atencion",
            isEditing = isEditing,
            onEditClick = {
                if (isEditing) {
                    onSave(editableSchedule.toBackendSchedule())
                } else {
                    editableSchedule = backendSchedule.toDaySchedule()
                }
                isEditing = !isEditing
            }
        )

        if (isEditing) {
            SchedulePicker(
                schedule = editableSchedule,
                onScheduleChange = { editableSchedule = it }
            )
        } else {
            val scheduleForDisplay = backendSchedule.toDaySchedule()
            if (backendSchedule.isEmpty()) {
                Text(
                    text = "Sin horarios configurados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    dayNames.forEach { (key, label) ->
                        val daySchedule = scheduleForDisplay[key]
                        val hoursText = if (daySchedule == null || !daySchedule.isOpen) {
                            "Cerrado"
                        } else {
                            daySchedule.timeRanges.joinToString(", ") { "${it.start}-${it.end}" }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = hoursText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hoursText == "Cerrado") {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
