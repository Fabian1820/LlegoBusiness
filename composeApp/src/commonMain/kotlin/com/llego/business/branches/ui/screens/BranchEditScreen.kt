package com.llego.business.branches.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.business.branches.ui.components.BranchStatusSelector
import com.llego.business.branches.ui.components.BranchTipoSelector
import com.llego.business.branches.ui.components.BranchVehiclesSelector
import com.llego.shared.ui.payment.PaymentMethodsViewModel
import com.llego.business.branches.util.parseManagerIds
import com.llego.shared.data.model.*
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.components.molecules.*
import com.llego.shared.ui.upload.ImageUploadViewModel
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import kotlinx.coroutines.launch


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
    val imageUploadViewModel = remember { ImageUploadViewModel() }
    val paymentMethodsViewModel = remember { PaymentMethodsViewModel() }
    val paymentMethodsUiState by paymentMethodsViewModel.uiState.collectAsState()

    var name by remember { mutableStateOf(branch.name) }
    var phone by remember { mutableStateOf(branch.phone) }
    var address by remember { mutableStateOf(branch.address.orEmpty()) }
    var latitude by remember { mutableStateOf(branch.coordinates.latitude.toString()) }
    var longitude by remember { mutableStateOf(branch.coordinates.longitude.toString()) }
    var deliveryRadius by remember { mutableStateOf(branch.deliveryRadius?.toString().orEmpty()) }
    var managerIds by remember { mutableStateOf(branch.managerIds.joinToString(", ")) }
    var selectedTipos by remember { mutableStateOf(branch.tipos.toSet()) }
    var useAppMessaging by remember { mutableStateOf(branch.useAppMessaging) }
    var selectedVehicles by remember { mutableStateOf(branch.vehicles.toSet()) }
    var branchSchedule by remember(branch) { mutableStateOf(branch.schedule.toDaySchedule()) }
    var branchFacilities by remember(branch) { mutableStateOf(branch.facilities) }
    var status by remember { mutableStateOf(if (branch.status == "inactive") "inactive" else "active") }
    var branchAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var branchCoverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Payment methods state
    var selectedPaymentMethodIds by remember(branch) { mutableStateOf(branch.paymentMethodIds) }

    // Load payment methods on screen open
    LaunchedEffect(branch.id) {
        paymentMethodsViewModel.loadPaymentMethods()
    }

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

            Text(
                text = "Mensajeria con clientes",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (useAppMessaging) {
                            "Mensajeria de la app activada"
                        } else {
                            "Mensajeria externa"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (useAppMessaging) {
                            "Los mensajes de pedidos y clientes se gestionan desde la app."
                        } else {
                            "La sucursal gestiona la comunicacion por su propio canal."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = useAppMessaging,
                    onCheckedChange = { useAppMessaging = it }
                )
            }

            Text(
                text = "Vehiculos de delivery",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
            BranchVehiclesSelector(
                selectedVehicles = selectedVehicles,
                onSelectionChange = { selectedVehicles = it }
            )

            SchedulePicker(
                schedule = branchSchedule,
                onScheduleChange = { branchSchedule = it }
            )

            FacilitiesSelector(
                selectedFacilities = branchFacilities,
                onFacilitiesChange = { branchFacilities = it }
            )

            PaymentMethodSelector(
                availablePaymentMethods = paymentMethodsUiState.methods,
                selectedPaymentMethodIds = selectedPaymentMethodIds,
                onSelectionChange = { selectedPaymentMethodIds = it },
                isLoading = paymentMethodsUiState.isLoading
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
                    uploadFunction = imageUploadViewModel::uploadBranchAvatar,
                    size = ImageUploadSize.SMALL,
                    modifier = Modifier.weight(1f)
                )
                ImageUploadPreview(
                    label = "Portada",
                    uploadState = branchCoverState,
                    onStateChange = { branchCoverState = it },
                    uploadFunction = imageUploadViewModel::uploadBranchCover,
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
                    if (selectedPaymentMethodIds.isEmpty()) {
                        statusMessage = "Selecciona al menos un metodo de pago"
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
                            useAppMessaging = useAppMessaging.takeIf { it != branch.useAppMessaging },
                            vehicles = selectedVehicles.toList().takeIf { it.toSet() != branch.vehicles.toSet() },
                            paymentMethodIds = selectedPaymentMethodIds.takeIf { it != branch.paymentMethodIds },
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

