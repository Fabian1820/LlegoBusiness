package com.llego.business.branches.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CoordinatesInput
import com.llego.shared.data.model.CreateBranchInput
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.repositories.BusinessRepository
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de gestion de sucursales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchesManagementScreen(
    authViewModel: AuthViewModel,
    businessRepository: BusinessRepository,
    onNavigateBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    // Estado
    val branches by authViewModel.branches.collectAsState()
    val currentBranch by authViewModel.currentBranch.collectAsState()
    val currentBusiness by authViewModel.currentBusiness.collectAsState()

    var showAddBranchDialog by remember { mutableStateOf(false) }
    var branchToEdit by remember { mutableStateOf<Branch?>(null) }
    var branchToDelete by remember { mutableStateOf<Branch?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Mostrar mensaje temporal
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            delay(3000)
            statusMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sucursales",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBranchDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar sucursal"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mensaje de estado
            if (statusMessage != null) {
                item {
                    Card(
                        shape = LlegoCustomShapes.infoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Header
            item {
                Text(
                    text = "Selecciona la sucursal activa y gestiona tus ubicaciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Lista de sucursales
            items(branches) { branch ->
                BranchCard(
                    branch = branch,
                    isActive = branch.id == currentBranch?.id,
                    onSelect = {
                        coroutineScope.launch {
                            authViewModel.setCurrentBranch(branch)
                            statusMessage = "OK: Sucursal '${branch.name}' seleccionada como activa"
                        }
                    },
                    onEdit = { branchToEdit = branch },
                    onDelete = {
                        if (branches.size > 1) {
                            branchToDelete = branch
                        } else {
                            statusMessage = "Error: No puedes eliminar la ultima sucursal"
                        }
                    }
                )
            }

            // Empty state
            if (branches.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = LlegoCustomShapes.infoCard,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No hay sucursales registradas",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Agrega tu primera sucursal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Dialogos
        if (showAddBranchDialog) {
            AddBranchDialog(
                businessId = currentBusiness?.id ?: "",
                businessRepository = businessRepository,
                onDismiss = { showAddBranchDialog = false },
                onSuccess = { newBranch ->
                    showAddBranchDialog = false
                    statusMessage = "OK: Sucursal '${newBranch.name}' agregada correctamente"
                    coroutineScope.launch {
                        businessRepository.getBranches(currentBusiness?.id)
                    }
                },
                onError = { error ->
                    statusMessage = "Error: $error"
                }
            )
        }

        if (branchToEdit != null) {
            EditBranchDialog(
                branch = branchToEdit!!,
                businessRepository = businessRepository,
                onDismiss = { branchToEdit = null },
                onSuccess = { updatedBranch ->
                    branchToEdit = null
                    statusMessage = "OK: Sucursal '${updatedBranch.name}' actualizada"
                },
                onError = { error ->
                    statusMessage = "Error: $error"
                }
            )
        }

        if (branchToDelete != null) {
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
                            // TODO: Implement delete branch mutation
                            statusMessage = "La eliminacion de sucursales estara disponible pronto"
                            branchToDelete = null
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
}

/**
 * Card de sucursal con informacion y acciones
 */
@Composable
fun BranchCard(
    branch: Branch,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (branch.status) {
        "active" -> MaterialTheme.colorScheme.tertiary
        "inactive" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
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

/**
 * Dialogo para agregar nueva sucursal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBranchDialog(
    businessId: String,
    businessRepository: BusinessRepository,
    onDismiss: () -> Unit,
    onSuccess: (Branch) -> Unit,
    onError: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var deliveryRadius by remember { mutableStateOf("5.0") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar nueva sucursal") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                    shape = LlegoCustomShapes.inputField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank() || latitude.isBlank() || longitude.isBlank()) {
                        onError("Completa todos los campos requeridos")
                        return@Button
                    }

                    coroutineScope.launch {
                        isLoading = true

                        val input = CreateBranchInput(
                            businessId = businessId,
                            name = name,
                            phone = phone,
                            address = address.takeIf { it.isNotBlank() },
                            coordinates = CoordinatesInput(
                                lat = latitude.toDoubleOrNull() ?: 0.0,
                                lng = longitude.toDoubleOrNull() ?: 0.0
                            ),
                            schedule = mapOf(
                                "mon" to listOf("09:00-18:00"),
                                "tue" to listOf("09:00-18:00"),
                                "wed" to listOf("09:00-18:00"),
                                "thu" to listOf("09:00-18:00"),
                                "fri" to listOf("09:00-18:00"),
                                "sat" to listOf("10:00-14:00"),
                                "sun" to emptyList() // closed
                            ),
                            tipos = listOf(BranchTipo.RESTAURANTE), // Por defecto RESTAURANTE
                            deliveryRadius = deliveryRadius.toDoubleOrNull()
                        )

                        when (val result = businessRepository.createBranch(input)) {
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
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Agregar")
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

/**
 * Dialogo para editar sucursal existente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBranchDialog(
    branch: Branch,
    businessRepository: BusinessRepository,
    onDismiss: () -> Unit,
    onSuccess: (Branch) -> Unit,
    onError: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(branch.name) }
    var phone by remember { mutableStateOf(branch.phone) }
    var address by remember { mutableStateOf(branch.address ?: "") }
    var deliveryRadius by remember { mutableStateOf(branch.deliveryRadius?.toString() ?: "5.0") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar sucursal") },
        text = {
            Column(
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

                OutlinedTextField(
                    value = deliveryRadius,
                    onValueChange = { deliveryRadius = it },
                    label = { Text("Radio de entrega (km)") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true

                        val input = UpdateBranchInput(
                            name = name.takeIf { it != branch.name },
                            phone = phone.takeIf { it != branch.phone },
                            address = address.takeIf { it != branch.address },
                            deliveryRadius = deliveryRadius.toDoubleOrNull()
                                .takeIf { it != branch.deliveryRadius }
                        )

                        when (val result = businessRepository.updateBranch(branch.id, input)) {
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
                enabled = !isLoading,
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
