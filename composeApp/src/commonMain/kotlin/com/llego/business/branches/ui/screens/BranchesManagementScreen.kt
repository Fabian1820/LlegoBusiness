package com.llego.business.branches.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.*
import com.llego.shared.data.repositories.BusinessRepository
import com.llego.shared.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de Gestión de Sucursales
 *
 * Permite:
 * - Ver todas las sucursales
 * - Seleccionar sucursal activa
 * - Editar sucursales existentes
 * - Agregar nuevas sucursales
 * - Eliminar sucursales (si hay más de una)
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
            kotlinx.coroutines.delay(3000)
            statusMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Gestión de Sucursales",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBranchDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Sucursal",
                    tint = Color.White
                )
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mensaje de estado
            if (statusMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Header
            item {
                Text(
                    text = "Selecciona la sucursal activa y gestiona tus ubicaciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
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
                            statusMessage = "✓ Sucursal '${branch.name}' seleccionada como activa"
                        }
                    },
                    onEdit = { branchToEdit = branch },
                    onDelete = {
                        if (branches.size > 1) {
                            branchToDelete = branch
                        } else {
                            statusMessage = "✗ No puedes eliminar la última sucursal"
                        }
                    }
                )
            }

            // Empty state
            if (branches.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No hay sucursales registradas",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "Agrega tu primera sucursal",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        // Diálogos
        if (showAddBranchDialog) {
            AddBranchDialog(
                businessId = currentBusiness?.id ?: "",
                businessRepository = businessRepository,
                onDismiss = { showAddBranchDialog = false },
                onSuccess = { newBranch ->
                    showAddBranchDialog = false
                    statusMessage = "✓ Sucursal '${newBranch.name}' agregada correctamente"
                    coroutineScope.launch {
                        businessRepository.getBranches(currentBusiness?.id)
                    }
                },
                onError = { error ->
                    statusMessage = "✗ Error: $error"
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
                    statusMessage = "✓ Sucursal '${updatedBranch.name}' actualizada"
                },
                onError = { error ->
                    statusMessage = "✗ Error: $error"
                }
            )
        }

        if (branchToDelete != null) {
            AlertDialog(
                onDismissRequest = { branchToDelete = null },
                title = { Text("Eliminar Sucursal") },
                text = {
                    Text("¿Estás seguro de que deseas eliminar la sucursal '${branchToDelete?.name}'? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // TODO: Implement delete branch mutation
                            statusMessage = "La eliminación de sucursales estará disponible pronto"
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
                }
            )
        }
    }
}

/**
 * Card de sucursal con información y acciones
 */
@Composable
fun BranchCard(
    branch: Branch,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 2.dp
        ),
        border = if (isActive) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
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
                            Color.Gray
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = branch.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            Color.Black
                        }
                    )
                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "ACTIVA",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                // Estado
                Surface(
                    shape = CircleShape,
                    color = when (branch.status) {
                        "active" -> Color(0xFF4CAF50)
                        "inactive" -> Color.Gray
                        else -> Color(0xFFFFC107)
                    }
                ) {
                    Box(modifier = Modifier.size(12.dp))
                }
            }

            // Información
            branch.address?.let { address ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
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
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = branch.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
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
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Radio: $radius km",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
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
 * Diálogo para agregar nueva sucursal
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
        title = { Text("Agregar Nueva Sucursal") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la sucursal *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
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
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitud *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = deliveryRadius,
                    onValueChange = { deliveryRadius = it },
                    label = { Text("Radio de entrega (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
                            tipos = listOf(com.llego.shared.data.model.BranchTipo.RESTAURANTE), // Por defecto RESTAURANTE
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
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
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
        }
    )
}

/**
 * Diálogo para editar sucursal existente
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
        title = { Text("Editar Sucursal") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la sucursal") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deliveryRadius,
                    onValueChange = { deliveryRadius = it },
                    label = { Text("Radio de entrega (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
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
        }
    )
}
