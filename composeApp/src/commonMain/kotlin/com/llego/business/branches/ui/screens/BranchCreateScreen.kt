package com.llego.business.branches.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.llego.business.branches.ui.components.BranchTipoSelector
import com.llego.business.branches.ui.components.BranchVehiclesSelector
import com.llego.business.branches.util.parseExchangeRate
import com.llego.shared.ui.payment.PaymentMethodsViewModel
import com.llego.business.branches.util.parseManagerIds
import com.llego.business.branches.util.validateExchangeRateInput
import com.llego.shared.data.model.*
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.business.parseQrPaymentsInput
import com.llego.shared.ui.business.parseTransferAccountsInput
import com.llego.shared.ui.business.parseTransferPhonesInput
import com.llego.shared.ui.components.molecules.*
import com.llego.shared.ui.upload.ImageUploadViewModel
import com.llego.shared.ui.theme.LlegoCustomShapes
import kotlinx.coroutines.launch

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
    val imageUploadViewModel = remember { ImageUploadViewModel() }
    val paymentMethodsViewModel = remember { PaymentMethodsViewModel() }
    val paymentMethodsUiState by paymentMethodsViewModel.uiState.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var facebook by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var accountsInput by remember { mutableStateOf("") }
    var qrPaymentsInput by remember { mutableStateOf("") }
    var transferPhonesInput by remember { mutableStateOf("") }
    // Coordenadas default: La Habana, Cuba
    var branchLatitude by remember { mutableStateOf(23.1136) }
    var branchLongitude by remember { mutableStateOf(-82.3666) }
    var managerIds by remember { mutableStateOf("") }
    var selectedTipos by remember { mutableStateOf(setOf<BranchTipo>()) }
    var useAppMessaging by remember { mutableStateOf(true) }
    var selectedVehicles by remember { mutableStateOf(emptySet<BranchVehicle>()) }
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
    var branchAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var branchCoverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var isLoading by remember { mutableStateOf(false) }

    // Payment methods state
    var selectedPaymentMethodIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var exchangeRateInput by remember { mutableStateOf("") }

    // Load payment methods on screen open
    LaunchedEffect(Unit) {
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
            Text(
                text = "Cuentas: una por linea en formato numero|titular|banco",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = accountsInput,
                onValueChange = { accountsInput = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Text(
                text = "Pagos QR: uno por linea",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = qrPaymentsInput,
                onValueChange = { qrPaymentsInput = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Text(
                text = "Telefonos de transferencia: uno por linea",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = transferPhonesInput,
                onValueChange = { transferPhonesInput = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
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

            Text(
                text = "Tipos de servicio *",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
            BranchTipoSelector(
                selectedTipos = selectedTipos,
                onSelectionChange = { selectedTipos = it }
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
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    if (selectedPaymentMethodIds.isEmpty()) {
                        statusMessage = "Selecciona al menos un metodo de pago"
                        onError(statusMessage ?: "")
                        return@Button
                    }
                    validateExchangeRateInput(exchangeRateInput)?.let { error ->
                        statusMessage = error
                        onError(error)
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
                            useAppMessaging = useAppMessaging,
                            vehicles = selectedVehicles.toList(),
                            paymentMethodIds = selectedPaymentMethodIds,
                            managerIds = parsedManagerIds.takeIf { it.isNotEmpty() },
                            avatar = avatarPath,
                            coverImage = coverPath,
                            socialMedia = buildBranchSocialMediaMap(
                                instagram = instagram,
                                facebook = facebook,
                                whatsapp = whatsapp
                            ),
                            exchangeRate = parseExchangeRate(exchangeRateInput),
                            accounts = parseTransferAccountsInput(accountsInput).takeIf { it.isNotEmpty() },
                            qrPayments = parseQrPaymentsInput(qrPaymentsInput).takeIf { it.isNotEmpty() },
                            phones = parseTransferPhonesInput(transferPhonesInput).takeIf { it.isNotEmpty() }
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

internal fun buildBranchSocialMediaMap(
    instagram: String,
    facebook: String,
    whatsapp: String
): Map<String, String>? {
    val socialMedia = mutableMapOf<String, String>()
    val instagramValue = instagram.trim()
    val facebookValue = facebook.trim()
    val whatsappValue = whatsapp.trim()
    if (instagramValue.isNotBlank()) socialMedia["instagram"] = instagramValue
    if (facebookValue.isNotBlank()) socialMedia["facebook"] = facebookValue
    if (whatsappValue.isNotBlank()) socialMedia["whatsapp"] = whatsappValue
    return socialMedia.takeIf { it.isNotEmpty() }
}
