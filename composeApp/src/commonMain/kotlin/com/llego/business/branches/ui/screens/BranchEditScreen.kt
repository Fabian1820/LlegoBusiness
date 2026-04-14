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
import com.llego.business.branches.util.parseExchangeRate
import com.llego.shared.ui.payment.PaymentMethodsViewModel
import com.llego.business.branches.util.parseManagerIds
import com.llego.business.branches.util.validateExchangeRateInput
import com.llego.shared.data.model.*
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.business.TransferAccountsEditor
import com.llego.shared.ui.business.findInvalidTransferAccountItems
import com.llego.shared.ui.business.normalizeTransferAccountsInput
import com.llego.shared.ui.components.molecules.*
import com.llego.shared.ui.upload.ImageUploadViewModel
import com.llego.shared.ui.theme.LlegoCustomShapes
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
    var instagram by remember(branch) { mutableStateOf(branch.socialMedia?.get("instagram").orEmpty()) }
    var facebook by remember(branch) { mutableStateOf(branch.socialMedia?.get("facebook").orEmpty()) }
    var whatsapp by remember(branch) { mutableStateOf(branch.socialMedia?.get("whatsapp").orEmpty()) }
    var transferAccounts by remember(branch) { mutableStateOf(branch.accounts) }
    var latitude by remember { mutableStateOf(branch.coordinates.latitude.toString()) }
    var longitude by remember { mutableStateOf(branch.coordinates.longitude.toString()) }
    var managerIds by remember { mutableStateOf(branch.managerIds.joinToString(", ")) }
    var selectedTipos by remember { mutableStateOf(branch.tipos.toSet()) }
    var useAppMessaging by remember { mutableStateOf(branch.useAppMessaging) }
    var catalogOnly by remember { mutableStateOf(branch.catalogOnly) }
    var selectedVehicles by remember { mutableStateOf(branch.vehicles.toSet()) }
    var branchSchedule by remember(branch) { mutableStateOf(branch.schedule.toDaySchedule()) }
    var isActive by remember { mutableStateOf(branch.isActive) }
    var exchangeRateInput by remember(branch) { mutableStateOf(branch.exchangeRate?.toString().orEmpty()) }
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

    fun saveBranchChanges() {
        if (isLoading) return

        if (name.isBlank() || phone.isBlank()) {
            statusMessage = "Completa los campos requeridos"
            onError(statusMessage ?: "")
            return
        }
        if (selectedTipos.isEmpty()) {
            statusMessage = "Selecciona al menos un tipo de sucursal"
            onError(statusMessage ?: "")
            return
        }
        if (selectedPaymentMethodIds.isEmpty()) {
            statusMessage = "Selecciona al menos un metodo de pago"
            onError(statusMessage ?: "")
            return
        }
        if (!useAppMessaging && selectedVehicles.isEmpty()) {
            statusMessage = "Para delivery propio debes seleccionar al menos un vehiculo"
            onError(statusMessage ?: "")
            return
        }
        validateExchangeRateInput(exchangeRateInput)?.let { error ->
            statusMessage = error
            onError(error)
            return
        }
        if (isUploading) {
            statusMessage = "Espera a que terminen las subidas de imagen"
            onError(statusMessage ?: "")
            return
        }
        val invalidAccounts = findInvalidTransferAccountItems(transferAccounts)
        if (invalidAccounts.isNotEmpty()) {
            statusMessage = "Revisa las cuentas #${invalidAccounts.joinToString(", ")}: tarjeta (16 dígitos) y teléfono de confirmación (8 dígitos) son obligatorios."
            onError(statusMessage ?: "")
            return
        }

        val latValue = latitude.toDoubleOrNull()
        val lngValue = longitude.toDoubleOrNull()
        if (latValue == null || lngValue == null) {
            statusMessage = "Coordenadas invalidas"
            onError(statusMessage ?: "")
            return
        }

        coroutineScope.launch {
            isLoading = true

            val nameValue = name.trim()
            val phoneValue = phone.trim()
            val addressValue = address.trim()
            val socialMediaValue = buildBranchSocialMediaMap(
                instagram = instagram,
                facebook = facebook,
                whatsapp = whatsapp
            ) ?: emptyMap()
            val accountsValue = normalizeTransferAccountsInput(transferAccounts, branch.accounts)
            val scheduleValue = branchSchedule.toBackendSchedule()
            val parsedManagerIds = parseManagerIds(managerIds)
            val exchangeRateValue = parseExchangeRate(exchangeRateInput)

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
                isActive = isActive.takeIf { it != branch.isActive },
                socialMedia = socialMediaValue.takeIf { it != (branch.socialMedia ?: emptyMap<String, String>()) },
                accounts = accountsValue.takeIf { it != branch.accounts },
                exchangeRate = exchangeRateValue.takeIf { it != branch.exchangeRate },
                managerIds = parsedManagerIds.takeIf { it != branch.managerIds },
                avatar = avatarPath,
                coverImage = coverPath,
                catalogOnly = catalogOnly.takeIf { it != branch.catalogOnly }
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
    }

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
                actions = {
                    TextButton(
                        onClick = { saveBranchChanges() },
                        enabled = !isLoading && !isUploading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Guardar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
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

            Text(
                text = "Redes sociales (opcional)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
            OutlinedTextField(
                value = instagram,
                onValueChange = { instagram = it },
                label = { Text("Instagram") },
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
                value = facebook,
                onValueChange = { facebook = it },
                label = { Text("Facebook") },
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
                value = whatsapp,
                onValueChange = { whatsapp = it },
                label = { Text("WhatsApp") },
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
                text = "Cobros por transferencia (opcional)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
            TransferAccountsEditor(
                accounts = transferAccounts,
                onAccountsChange = { transferAccounts = it }
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
                isActive = isActive,
                onStatusChange = { isActive = it }
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

            if (!useAppMessaging) {
                Text(
                    text = "Vehiculos de delivery",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
                BranchVehiclesSelector(
                    selectedVehicles = selectedVehicles,
                    onSelectionChange = { selectedVehicles = it }
                )
            }

            Text(
                text = "Modo catalogo",
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
                        text = "Modo solo catalogo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Los clientes podran ver tus productos pero no podran hacer pedidos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = catalogOnly,
                    onCheckedChange = { catalogOnly = it }
                )
            }

            SchedulePicker(
                schedule = branchSchedule,
                onScheduleChange = { branchSchedule = it }
            )

            PaymentMethodSelector(
                availablePaymentMethods = paymentMethodsUiState.methods,
                selectedPaymentMethodIds = selectedPaymentMethodIds,
                onSelectionChange = { selectedPaymentMethodIds = it },
                isLoading = paymentMethodsUiState.isLoading
            )

            OutlinedTextField(
                value = exchangeRateInput,
                onValueChange = { value ->
                    if (value.all { it.isDigit() }) {
                        exchangeRateInput = value
                    }
                },
                label = { Text("Tasa de cambio USD -> CUP (opcional)") },
                placeholder = { Text("Ej: 120") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = LlegoCustomShapes.inputField,
                isError = validateExchangeRateInput(exchangeRateInput) != null,
                supportingText = {
                    Text(
                        validateExchangeRateInput(exchangeRateInput)
                            ?: "Si la configuras, se usara para 1 USD = X CUP"
                    )
                },
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
