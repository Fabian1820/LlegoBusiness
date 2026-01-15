package com.llego.business.branches.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CoordinatesInput
import com.llego.shared.data.model.CreateBranchInput
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.upload.ImageUploadServiceFactory
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.components.molecules.DaySchedule
import com.llego.shared.ui.components.molecules.FacilitiesSelector
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize
import com.llego.shared.ui.components.molecules.SchedulePicker
import com.llego.shared.ui.components.molecules.TimeRange
import com.llego.shared.ui.components.molecules.toBackendSchedule
import com.llego.shared.ui.components.molecules.toDaySchedule
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import com.llego.shared.ui.components.molecules.MapLocationPickerReal
import com.llego.business.shared.ui.components.BusinessLocationMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de gestion de sucursales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchesManagementScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {},
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()

    // Estado
    val branches by authViewModel.branches.collectAsState()
    val currentBranch by authViewModel.currentBranch.collectAsState()
    val currentBusiness by authViewModel.currentBusiness.collectAsState()

    var showCreateBranch by remember { mutableStateOf(false) }
    var selectedBranchId by remember { mutableStateOf<String?>(null) }
    var editingBranchId by remember { mutableStateOf<String?>(null) }
    var branchToDelete by remember { mutableStateOf<Branch?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val selectedBranch = branches.firstOrNull { it.id == selectedBranchId }
    val editingBranch = branches.firstOrNull { it.id == editingBranchId }
    val showListScreen = !showCreateBranch && editingBranch == null && selectedBranch == null

    // Mostrar mensaje temporal
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            delay(3000)
            statusMessage = null
        }
    }

    when {
        showCreateBranch -> {
            BranchCreateScreen(
                businessId = currentBusiness?.id.orEmpty(),
                onNavigateBack = { showCreateBranch = false },
                onSuccess = { newBranch ->
                    showCreateBranch = false
                    statusMessage = "OK: Sucursal '${newBranch.name}' agregada correctamente"
                },
                onError = { error -> statusMessage = "Error: $error" },
                authViewModel = authViewModel,
                onOpenMapSelection = onOpenMapSelection
            )
        }
        editingBranch != null -> {
            BranchEditScreen(
                branch = editingBranch,
                onNavigateBack = { editingBranchId = null },
                onSuccess = { updatedBranch ->
                    editingBranchId = null
                    selectedBranchId = updatedBranch.id
                    statusMessage = "OK: Sucursal '${updatedBranch.name}' actualizada"
                },
                onError = { error -> statusMessage = "Error: $error" },
                authViewModel = authViewModel
            )
        }
        selectedBranch != null -> {
            BranchDetailScreen(
                branch = selectedBranch,
                isActive = selectedBranch.id == currentBranch?.id,
                onNavigateBack = { selectedBranchId = null },
                onEdit = { editingBranchId = selectedBranch.id },
                onSetActive = {
                    authViewModel.setCurrentBranch(selectedBranch)
                    statusMessage = "OK: Sucursal '${selectedBranch.name}' seleccionada como activa"
                }
            )
        }
        else -> {
            BranchesListScreen(
                branches = branches,
                currentBranchId = currentBranch?.id,
                statusMessage = statusMessage,
                onNavigateBack = onNavigateBack,
                onAddBranch = { showCreateBranch = true },
                onOpenDetails = { selectedBranchId = it.id },
                onSetActive = { branch ->
                    coroutineScope.launch {
                        authViewModel.setCurrentBranch(branch)
                        statusMessage = "OK: Sucursal '${branch.name}' seleccionada como activa"
                    }
                },
                onEdit = { editingBranchId = it.id },
                onDelete = { branch ->
                    if (branches.size > 1) {
                        branchToDelete = branch
                    } else {
                        statusMessage = "Error: No puedes eliminar la ultima sucursal"
                    }
                },
                onLocationUpdate = { branch, lat, lng ->
                    coroutineScope.launch {
                        val input = UpdateBranchInput(
                            coordinates = CoordinatesInput(lat = lat, lng = lng)
                        )
                        when (val result = authViewModel.updateBranch(branch.id, input)) {
                            is BusinessResult.Success -> {
                                statusMessage = "OK: Ubicacion de '${branch.name}' actualizada"
                            }
                            is BusinessResult.Error -> {
                                statusMessage = "Error: ${result.message}"
                            }
                            else -> {}
                        }
                    }
                },
                onOpenMapSelection = onOpenMapSelection
            )
        }
    }

    if (showListScreen && branchToDelete != null) {
        AlertDialog(
            onDismissRequest = { branchToDelete = null },
            title = { Text("Eliminar sucursal") },
            text = {
                Text(
                    "Estas seguro de que deseas eliminar la sucursal '${branchToDelete?.name}'? " +
                        "Esta accion no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val branch = branchToDelete ?: return@TextButton
                        coroutineScope.launch {
                            when (val result = authViewModel.deleteBranch(branch.id)) {
                                is BusinessResult.Success -> {
                                    statusMessage = "OK: Sucursal '${branch.name}' eliminada"
                                }
                                is BusinessResult.Error -> {
                                    statusMessage = "Error: ${result.message}"
                                }
                                else -> {}
                            }
                            branchToDelete = null
                        }
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { branchToDelete = null }) {
                    Text("Cancelar")
                }
            },
            shape = LlegoCustomShapes.infoCard,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * Card de sucursal con informacion y acciones
 */
@Composable
fun BranchCard(
    branch: Branch,
    isActive: Boolean,
    onOpenDetails: () -> Unit,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLocationUpdate: (Double, Double) -> Unit = { _, _ -> },
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val statusColor = when (branch.status) {
        "active" -> MaterialTheme.colorScheme.tertiary
        "inactive" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = LlegoCustomShapes.infoCard,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            if (isActive) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header con nombre y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = branch.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isActive) {
                        Surface(
                            shape = LlegoShapes.small,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Activa",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Estado
                Surface(
                    shape = CircleShape,
                    color = statusColor
                ) {
                    Box(modifier = Modifier.size(10.dp))
                }
            }

            // Informacion
            branch.address?.let { address ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = branch.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            branch.deliveryRadius?.let { radius ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Radio: $radius km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isActive) {
                    TextButton(onClick = onSetActive) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Activar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Activar")
                    }
                }
                IconButton(onClick = {
                    onOpenMapSelection(
                        branch.name,
                        branch.coordinates.latitude,
                        branch.coordinates.longitude
                    ) { lat, lng ->
                        onLocationUpdate(lat, lng)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Editar ubicacion",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchCreateScreen(
    businessId: String,
    onNavigateBack: () -> Unit,
    onSuccess: (Branch) -> Unit,
    onError: (String) -> Unit,
    authViewModel: AuthViewModel,
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val imageUploadService = remember { ImageUploadServiceFactory.create() }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    // Coordenadas default: La Habana, Cuba
    var branchLatitude by remember { mutableStateOf(23.1136) }
    var branchLongitude by remember { mutableStateOf(-82.3666) }
    var deliveryRadius by remember { mutableStateOf("") }
    var managerIds by remember { mutableStateOf("") }
    var selectedTipos by remember { mutableStateOf(setOf<BranchTipo>()) }
    var branchSchedule by remember {
        mutableStateOf(
            mapOf(
                "mon" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
                "tue" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
                "wed" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
                "thu" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
                "fri" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
                "sat" to DaySchedule(false, emptyList()),
                "sun" to DaySchedule(false, emptyList())
            )
        )
    }
    var branchFacilities by remember { mutableStateOf(emptyList<String>()) }
    var branchAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var branchCoverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var isLoading by remember { mutableStateOf(false) }

    val avatarPath = (branchAvatarState as? ImageUploadState.Success)?.s3Path
    val coverPath = (branchCoverState as? ImageUploadState.Success)?.s3Path
    val isUploading = branchAvatarState is ImageUploadState.Uploading ||
        branchCoverState is ImageUploadState.Uploading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva sucursal",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            statusMessage?.let { message ->
                Card(
                    shape = LlegoCustomShapes.infoCard,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la sucursal *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefono *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Direccion") },
                modifier = Modifier.fillMaxWidth(),
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Selector de ubicacion con mapa
            MapLocationPickerReal(
                latitude = branchLatitude,
                longitude = branchLongitude,
                onLocationSelected = { lat, lng ->
                    branchLatitude = lat
                    branchLongitude = lng
                },
                onOpenMapSelection = onOpenMapSelection
            )

            OutlinedTextField(
                value = deliveryRadius,
                onValueChange = { deliveryRadius = it },
                label = { Text("Radio de entrega (km)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Text(
                text = "Tipos de servicio *",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
            BranchTipoSelector(
                selectedTipos = selectedTipos,
                onSelectionChange = { selectedTipos = it }
            )

            SchedulePicker(
                schedule = branchSchedule,
                onScheduleChange = { branchSchedule = it }
            )

            FacilitiesSelector(
                selectedFacilities = branchFacilities,
                onFacilitiesChange = { branchFacilities = it }
            )

            Text(
                text = "Imagenes (opcional)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImageUploadPreview(
                    label = "Avatar",
                    uploadState = branchAvatarState,
                    onStateChange = { branchAvatarState = it },
                    uploadFunction = { uri, token ->
                        imageUploadService.uploadBranchAvatar(uri, token)
                    },
                    size = ImageUploadSize.SMALL,
                    modifier = Modifier.weight(1f)
                )
                ImageUploadPreview(
                    label = "Portada",
                    uploadState = branchCoverState,
                    onStateChange = { branchCoverState = it },
                    uploadFunction = { uri, token ->
                        imageUploadService.uploadBranchCover(uri, token)
                    },
                    size = ImageUploadSize.SMALL,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    if (businessId.isBlank()) {
                        statusMessage = "Selecciona un negocio para continuar"
                        onError(statusMessage ?: "")
                        return@Button
                    }
                    if (name.isBlank() || phone.isBlank()) {
                        statusMessage = "Completa todos los campos requeridos"
                        onError(statusMessage ?: "")
                        return@Button
                    }
                    if (branchLatitude == 0.0 && branchLongitude == 0.0) {
                        statusMessage = "Selecciona la ubicacion en el mapa"
                        onError(statusMessage ?: "")
                        return@Button
                    }
                    if (selectedTipos.isEmpty()) {
                        statusMessage = "Selecciona al menos un tipo de sucursal"
                        onError(statusMessage ?: "")
                        return@Button
                    }
                    if (isUploading) {
                        statusMessage = "Espera a que terminen las subidas de imagen"
                        onError(statusMessage ?: "")
                        return@Button
                    }

                    coroutineScope.launch {
                        isLoading = true
                        val parsedManagerIds = parseManagerIds(managerIds)

                        val input = CreateBranchInput(
                            businessId = businessId,
                            name = name.trim(),
                            phone = phone.trim(),
                            address = address.trim().takeIf { it.isNotBlank() },
                            coordinates = CoordinatesInput(
                                lat = branchLatitude,
                                lng = branchLongitude
                            ),
                            schedule = branchSchedule.toBackendSchedule(),
                            tipos = selectedTipos.toList(),
                            managerIds = parsedManagerIds.takeIf { it.isNotEmpty() },
                            avatar = avatarPath,
                            coverImage = coverPath,
                            deliveryRadius = deliveryRadius.toDoubleOrNull(),
                            facilities = branchFacilities.takeIf { it.isNotEmpty() }
                        )

                        when (val result = authViewModel.createBranch(input)) {
                            is BusinessResult.Success -> {
                                onSuccess(result.data)
                            }
                            is BusinessResult.Error -> {
                                statusMessage = result.message
                                onError(result.message)
                            }
                            else -> {}
                        }

                        isLoading = false
                    }
                },
                enabled = !isLoading && !isUploading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear sucursal")
                }
            }
        }
    }
}

/**
 * Dialogo para editar sucursal existente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBranchDialog(
    branch: Branch,
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onSuccess: (Branch) -> Unit,
    onError: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val imageUploadService = remember { ImageUploadServiceFactory.create() }

    var name by remember { mutableStateOf(branch.name) }
    var phone by remember { mutableStateOf(branch.phone) }
    var address by remember { mutableStateOf(branch.address.orEmpty()) }
    var latitude by remember { mutableStateOf(branch.coordinates.latitude.toString()) }
    var longitude by remember { mutableStateOf(branch.coordinates.longitude.toString()) }
    var deliveryRadius by remember { mutableStateOf(branch.deliveryRadius?.toString().orEmpty()) }
    var managerIds by remember { mutableStateOf(branch.managerIds.joinToString(", ")) }
    var selectedTipos by remember { mutableStateOf(branch.tipos.toSet()) }
    var branchSchedule by remember(branch) { mutableStateOf(branch.schedule.toDaySchedule()) }
    var branchFacilities by remember(branch) { mutableStateOf(branch.facilities) }
    var status by remember { mutableStateOf(if (branch.status == "inactive") "inactive" else "active") }
    var branchAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var branchCoverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var isLoading by remember { mutableStateOf(false) }

    val avatarPath = (branchAvatarState as? ImageUploadState.Success)?.s3Path
    val coverPath = (branchCoverState as? ImageUploadState.Success)?.s3Path
    val isUploading = branchAvatarState is ImageUploadState.Uploading ||
        branchCoverState is ImageUploadState.Uploading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar sucursal") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la sucursal") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = LlegoCustomShapes.inputField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = LlegoCustomShapes.inputField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Direccion") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = LlegoCustomShapes.inputField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitud") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = LlegoCustomShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitud") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = LlegoCustomShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                OutlinedTextField(
                    value = deliveryRadius,
                    onValueChange = { deliveryRadius = it },
                    label = { Text("Radio de entrega (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = LlegoCustomShapes.inputField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Text(
                    text = "Tipos de servicio *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
                BranchTipoSelector(
                    selectedTipos = selectedTipos,
                    onSelectionChange = { selectedTipos = it }
                )

                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
                BranchStatusSelector(
                    status = status,
                    onStatusChange = { status = it }
                )

                SchedulePicker(
                    schedule = branchSchedule,
                    onScheduleChange = { branchSchedule = it }
                )

                FacilitiesSelector(
                    selectedFacilities = branchFacilities,
                    onFacilitiesChange = { branchFacilities = it }
                )

                OutlinedTextField(
                    value = managerIds,
                    onValueChange = { managerIds = it },
                    label = { Text("Manager IDs (separados por coma)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = LlegoCustomShapes.inputField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Text(
                    text = "Imagenes (opcional)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImageUploadPreview(
                        label = "Avatar",
                        uploadState = branchAvatarState,
                        onStateChange = { branchAvatarState = it },
                        uploadFunction = { uri, token ->
                            imageUploadService.uploadBranchAvatar(uri, token)
                        },
                        size = ImageUploadSize.SMALL,
                        modifier = Modifier.weight(1f)
                    )
                    ImageUploadPreview(
                        label = "Portada",
                        uploadState = branchCoverState,
                        onStateChange = { branchCoverState = it },
                        uploadFunction = { uri, token ->
                            imageUploadService.uploadBranchCover(uri, token)
                        },
                        size = ImageUploadSize.SMALL,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        onError("Completa los campos requeridos")
                        return@Button
                    }
                    if (selectedTipos.isEmpty()) {
                        onError("Selecciona al menos un tipo de sucursal")
                        return@Button
                    }
                    if (isUploading) {
                        onError("Espera a que terminen las subidas de imagen")
                        return@Button
                    }

                    val latValue = latitude.toDoubleOrNull()
                    val lngValue = longitude.toDoubleOrNull()
                    if (latValue == null || lngValue == null) {
                        onError("Coordenadas invalidas")
                        return@Button
                    }

                    coroutineScope.launch {
                        isLoading = true

                        val nameValue = name.trim()
                        val phoneValue = phone.trim()
                        val addressValue = address.trim()
                        val scheduleValue = branchSchedule.toBackendSchedule()
                        val parsedManagerIds = parseManagerIds(managerIds)
                        val deliveryValue = deliveryRadius.toDoubleOrNull()

                        val input = UpdateBranchInput(
                            name = nameValue.takeIf { it != branch.name },
                            phone = phoneValue.takeIf { it != branch.phone },
                            address = addressValue.takeIf { it != branch.address.orEmpty() },
                            coordinates = if (latValue != branch.coordinates.latitude ||
                                lngValue != branch.coordinates.longitude) {
                                CoordinatesInput(lat = latValue, lng = lngValue)
                            } else {
                                null
                            },
                            schedule = scheduleValue.takeIf { it != branch.schedule },
                            tipos = selectedTipos.toList().takeIf { it != branch.tipos },
                            status = status.takeIf { it != branch.status },
                            deliveryRadius = deliveryValue.takeIf { it != branch.deliveryRadius },
                            facilities = branchFacilities.takeIf { it != branch.facilities },
                            managerIds = parsedManagerIds.takeIf { it != branch.managerIds },
                            avatar = avatarPath,
                            coverImage = coverPath
                        )

                        when (val result = authViewModel.updateBranch(branch.id, input)) {
                            is BusinessResult.Success -> {
                                onSuccess(result.data)
                            }
                            is BusinessResult.Error -> {
                                onError(result.message)
                            }
                            else -> {}
                        }

                        isLoading = false
                    }
                },
                enabled = !isLoading && !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = LlegoCustomShapes.infoCard,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun BranchTipoSelector(
    selectedTipos: Set<BranchTipo>,
    onSelectionChange: (Set<BranchTipo>) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val options = listOf(
        BranchTipo.RESTAURANTE,
        BranchTipo.TIENDA,
        BranchTipo.DULCERIA
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { tipo ->
            val selected = tipo in selectedTipos
            val (label, color) = when (tipo) {
                BranchTipo.RESTAURANTE -> "Restaurante" to secondaryColor
                BranchTipo.TIENDA -> "Tienda" to primaryColor
                BranchTipo.DULCERIA -> "Dulceria" to tertiaryColor
            }

            FilterChip(
                selected = selected,
                onClick = {
                    val updated = if (selected) {
                        selectedTipos - tipo
                    } else {
                        selectedTipos + tipo
                    }
                    onSelectionChange(updated)
                },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.15f),
                    selectedLabelColor = color,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    selectedBorderColor = color.copy(alpha = 0.6f),
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                shape = LlegoCustomShapes.secondaryButton
            )
        }
    }
}

/**
 * Pantalla de lista de sucursales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchesListScreen(
    branches: List<Branch>,
    currentBranchId: String?,
    statusMessage: String?,
    onNavigateBack: () -> Unit,
    onAddBranch: () -> Unit,
    onOpenDetails: (Branch) -> Unit,
    onSetActive: (Branch) -> Unit,
    onEdit: (Branch) -> Unit,
    onDelete: (Branch) -> Unit,
    onLocationUpdate: (Branch, Double, Double) -> Unit = { _, _, _ -> },
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sucursales",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBranch,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar sucursal")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mensaje de estado
            statusMessage?.let { message ->
                item {
                    Card(
                        shape = LlegoCustomShapes.infoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.startsWith("OK:")) {
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            }
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (message.startsWith("OK:")) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }

            items(branches, key = { it.id }) { branch ->
                BranchCard(
                    branch = branch,
                    isActive = branch.id == currentBranchId,
                    onOpenDetails = { onOpenDetails(branch) },
                    onSetActive = { onSetActive(branch) },
                    onEdit = { onEdit(branch) },
                    onDelete = { onDelete(branch) },
                    onLocationUpdate = { lat, lng -> onLocationUpdate(branch, lat, lng) },
                    onOpenMapSelection = onOpenMapSelection
                )
            }
        }
    }
}

/**
 * Pantalla de detalle de sucursal (full-screen)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchDetailScreen(
    branch: Branch,
    isActive: Boolean,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onSetActive: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de sucursal",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con nombre y estado activo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = LlegoCustomShapes.infoCard,
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(
                    1.dp,
                    if (isActive) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = if (isActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = branch.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isActive) {
                                    Text(
                                        text = "Sucursal activa",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Estado
                        val statusColor = when (branch.status) {
                            "active" -> MaterialTheme.colorScheme.tertiary
                            "inactive" -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.secondary
                        }
                        Surface(
                            shape = LlegoShapes.small,
                            color = statusColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (branch.status == "active") "Activo" else "Inactivo",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor
                            )
                        }
                    }

                    // Boton para activar sucursal
                    if (!isActive) {
                        Button(
                            onClick = onSetActive,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = LlegoCustomShapes.primaryButton
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Establecer como activa")
                        }
                    }
                }
            }

            // Informacion de contacto
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = LlegoCustomShapes.infoCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Informacion de contacto",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    BranchDetailRow(
                        icon = Icons.Default.Phone,
                        label = "Telefono",
                        value = branch.phone
                    )

                    branch.address?.let { address ->
                        BranchDetailRow(
                            icon = Icons.Default.LocationOn,
                            label = "Direccion",
                            value = address
                        )
                    }

                    BranchDetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Coordenadas",
                        value = "${branch.coordinates.latitude}, ${branch.coordinates.longitude}"
                    )

                    branch.deliveryRadius?.let { radius ->
                        BranchDetailRow(
                            icon = Icons.Default.DeliveryDining,
                            label = "Radio de entrega",
                            value = "$radius km"
                        )
                    }
                }
            }

            // Tipos de servicio
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = LlegoCustomShapes.infoCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tipos de servicio",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        branch.tipos.forEach { tipo ->
                            val (label, color) = when (tipo) {
                                BranchTipo.RESTAURANTE -> "Restaurante" to MaterialTheme.colorScheme.secondary
                                BranchTipo.TIENDA -> "Tienda" to MaterialTheme.colorScheme.primary
                                BranchTipo.DULCERIA -> "Dulceria" to MaterialTheme.colorScheme.tertiary
                            }
                            Surface(
                                shape = LlegoShapes.small,
                                color = color.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }

            // Facilidades
            if (branch.facilities.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = LlegoCustomShapes.infoCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Facilidades",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            branch.facilities.forEach { facility ->
                                Surface(
                                    shape = LlegoShapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = facility,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Horario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = LlegoCustomShapes.infoCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Horario de atencion",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    val dayNames = mapOf(
                        "mon" to "Lunes",
                        "tue" to "Martes",
                        "wed" to "Miercoles",
                        "thu" to "Jueves",
                        "fri" to "Viernes",
                        "sat" to "Sabado",
                        "sun" to "Domingo"
                    )

                    val orderedDays = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")

                    orderedDays.forEach { dayKey ->
                        val daySchedule = branch.schedule[dayKey]
                        val dayName = dayNames[dayKey] ?: dayKey

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (daySchedule != null && daySchedule.isNotEmpty()) {
                                Text(
                                    text = daySchedule.joinToString(", ") { range ->
                                        range.replace("-", " - ")
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Cerrado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BranchDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 26.dp)
        )
    }
}

/**
 * Pantalla de edicion de sucursal (full-screen)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchEditScreen(
    branch: Branch,
    onNavigateBack: () -> Unit,
    onSuccess: (Branch) -> Unit,
    onError: (String) -> Unit,
    authViewModel: AuthViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val imageUploadService = remember { ImageUploadServiceFactory.create() }

    var name by remember { mutableStateOf(branch.name) }
    var phone by remember { mutableStateOf(branch.phone) }
    var address by remember { mutableStateOf(branch.address.orEmpty()) }
    var latitude by remember { mutableStateOf(branch.coordinates.latitude.toString()) }
    var longitude by remember { mutableStateOf(branch.coordinates.longitude.toString()) }
    var deliveryRadius by remember { mutableStateOf(branch.deliveryRadius?.toString().orEmpty()) }
    var managerIds by remember { mutableStateOf(branch.managerIds.joinToString(", ")) }
    var selectedTipos by remember { mutableStateOf(branch.tipos.toSet()) }
    var branchSchedule by remember(branch) { mutableStateOf(branch.schedule.toDaySchedule()) }
    var branchFacilities by remember(branch) { mutableStateOf(branch.facilities) }
    var status by remember { mutableStateOf(if (branch.status == "inactive") "inactive" else "active") }
    var branchAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var branchCoverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val avatarPath = (branchAvatarState as? ImageUploadState.Success)?.s3Path
    val coverPath = (branchCoverState as? ImageUploadState.Success)?.s3Path
    val isUploading = branchAvatarState is ImageUploadState.Uploading ||
        branchCoverState is ImageUploadState.Uploading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar sucursal",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            statusMessage?.let { message ->
                Card(
                    shape = LlegoCustomShapes.infoCard,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la sucursal *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefono *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Direccion") },
                modifier = Modifier.fillMaxWidth(),
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitud *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = LlegoCustomShapes.inputField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitud *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = LlegoCustomShapes.inputField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            OutlinedTextField(
                value = deliveryRadius,
                onValueChange = { deliveryRadius = it },
                label = { Text("Radio de entrega (km)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Text(
                text = "Tipos de servicio *",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
            BranchTipoSelector(
                selectedTipos = selectedTipos,
                onSelectionChange = { selectedTipos = it }
            )

            Text(
                text = "Estado",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
            BranchStatusSelector(
                status = status,
                onStatusChange = { status = it }
            )

            SchedulePicker(
                schedule = branchSchedule,
                onScheduleChange = { branchSchedule = it }
            )

            FacilitiesSelector(
                selectedFacilities = branchFacilities,
                onFacilitiesChange = { branchFacilities = it }
            )

            Text(
                text = "Imagenes (opcional)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImageUploadPreview(
                    label = "Avatar",
                    uploadState = branchAvatarState,
                    onStateChange = { branchAvatarState = it },
                    uploadFunction = { uri, token ->
                        imageUploadService.uploadBranchAvatar(uri, token)
                    },
                    size = ImageUploadSize.SMALL,
                    modifier = Modifier.weight(1f)
                )
                ImageUploadPreview(
                    label = "Portada",
                    uploadState = branchCoverState,
                    onStateChange = { branchCoverState = it },
                    uploadFunction = { uri, token ->
                        imageUploadService.uploadBranchCover(uri, token)
                    },
                    size = ImageUploadSize.SMALL,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        statusMessage = "Completa los campos requeridos"
                        onError(statusMessage ?: "")
                        return@Button
                    }
                    if (selectedTipos.isEmpty()) {
                        statusMessage = "Selecciona al menos un tipo de sucursal"
                        onError(statusMessage ?: "")
                        return@Button
                    }
                    if (isUploading) {
                        statusMessage = "Espera a que terminen las subidas de imagen"
                        onError(statusMessage ?: "")
                        return@Button
                    }

                    val latValue = latitude.toDoubleOrNull()
                    val lngValue = longitude.toDoubleOrNull()
                    if (latValue == null || lngValue == null) {
                        statusMessage = "Coordenadas invalidas"
                        onError(statusMessage ?: "")
                        return@Button
                    }

                    coroutineScope.launch {
                        isLoading = true

                        val nameValue = name.trim()
                        val phoneValue = phone.trim()
                        val addressValue = address.trim()
                        val scheduleValue = branchSchedule.toBackendSchedule()
                        val parsedManagerIds = parseManagerIds(managerIds)
                        val deliveryValue = deliveryRadius.toDoubleOrNull()

                        val input = UpdateBranchInput(
                            name = nameValue.takeIf { it != branch.name },
                            phone = phoneValue.takeIf { it != branch.phone },
                            address = addressValue.takeIf { it != branch.address.orEmpty() },
                            coordinates = if (latValue != branch.coordinates.latitude ||
                                lngValue != branch.coordinates.longitude) {
                                CoordinatesInput(lat = latValue, lng = lngValue)
                            } else {
                                null
                            },
                            schedule = scheduleValue.takeIf { it != branch.schedule },
                            tipos = selectedTipos.toList().takeIf { it != branch.tipos },
                            status = status.takeIf { it != branch.status },
                            deliveryRadius = deliveryValue.takeIf { it != branch.deliveryRadius },
                            facilities = branchFacilities.takeIf { it != branch.facilities },
                            managerIds = parsedManagerIds.takeIf { it != branch.managerIds },
                            avatar = avatarPath,
                            coverImage = coverPath
                        )

                        when (val result = authViewModel.updateBranch(branch.id, input)) {
                            is BusinessResult.Success -> {
                                onSuccess(result.data)
                            }
                            is BusinessResult.Error -> {
                                statusMessage = result.message
                                onError(result.message)
                            }
                            else -> {}
                        }

                        isLoading = false
                    }
                },
                enabled = !isLoading && !isUploading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = LlegoCustomShapes.primaryButton
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar cambios")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BranchStatusSelector(
    status: String,
    onStatusChange: (String) -> Unit
) {
    val activeSelected = status == "active"
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = activeSelected,
            onClick = { onStatusChange("active") },
            label = { Text("Activa") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = activeColor.copy(alpha = 0.15f),
                selectedLabelColor = activeColor,
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = activeSelected,
                selectedBorderColor = activeColor.copy(alpha = 0.6f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            shape = LlegoCustomShapes.secondaryButton
        )
        FilterChip(
            selected = !activeSelected,
            onClick = { onStatusChange("inactive") },
            label = { Text("Inactiva") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = inactiveColor.copy(alpha = 0.15f),
                selectedLabelColor = inactiveColor,
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = !activeSelected,
                selectedBorderColor = inactiveColor.copy(alpha = 0.6f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            shape = LlegoCustomShapes.secondaryButton
        )
    }
}

private fun parseManagerIds(value: String): List<String> {
    return value.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
