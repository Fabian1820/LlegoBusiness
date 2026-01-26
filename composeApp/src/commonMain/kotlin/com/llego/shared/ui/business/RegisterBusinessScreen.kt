package com.llego.shared.ui.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.*
import com.llego.shared.data.upload.ImageUploadServiceFactory
import com.llego.shared.ui.components.atoms.LlegoButton
import com.llego.shared.ui.components.atoms.LlegoButtonSize
import com.llego.shared.ui.components.atoms.LlegoTextField
import com.llego.shared.ui.components.molecules.DaySchedule
import com.llego.shared.ui.components.molecules.TimeRange
import com.llego.shared.ui.components.molecules.DeliveryRadiusPicker
import com.llego.shared.ui.components.molecules.FacilitiesSelector
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize
import com.llego.shared.ui.components.molecules.LlegoConfirmationDefaults
import com.llego.shared.ui.components.molecules.LlegoConfirmationScreen
import com.llego.shared.ui.components.molecules.MapLocationPickerReal
import com.llego.shared.ui.components.molecules.PaymentMethodSelector
import com.llego.shared.ui.components.molecules.PaymentMethodSelectorLayout
import com.llego.shared.ui.components.molecules.PhoneInput
import com.llego.shared.ui.components.molecules.SchedulePicker
import com.llego.shared.ui.components.molecules.TagsSelector
import com.llego.shared.ui.components.molecules.combinePhoneNumber
import com.llego.shared.ui.components.molecules.toBackendSchedule
import com.llego.shared.data.repositories.PaymentMethodsRepository
import com.llego.shared.data.model.PaymentMethod
import com.llego.shared.ui.theme.LlegoCustomShapes
import kotlinx.coroutines.launch

/**
 * Pantalla COMPLETA para registrar negocio con TODAS las integraciones:
 * ✅ Google Maps real
 * ✅ Upload de imágenes a S3
 * ✅ PhoneInput con código de país
 * ✅ SchedulePicker interactivo
 * ✅ DeliveryRadiusPicker
 * ✅ FacilitiesSelector
 * ✅ TagsSelector mejorado
 * ✅ Pantalla de confirmación animada
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBusinessScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterBusinessViewModel,
    modifier: Modifier = Modifier,
    invitationViewModel: com.llego.business.invitations.ui.viewmodel.InvitationViewModel,
    authViewModel: com.llego.shared.ui.auth.AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val imageUploadService = remember { ImageUploadServiceFactory.create() }
    val paymentMethodsRepository = remember { PaymentMethodsRepository() }

    // Estados de confirmación
    var showSuccessConfirmation by remember { mutableStateOf(false) }
    var registeredBusinessName by remember { mutableStateOf("") }

    // Estados de métodos de pago
    var availablePaymentMethods by remember { mutableStateOf<List<PaymentMethod>>(emptyList()) }
    var isLoadingPaymentMethods by remember { mutableStateOf(false) }

    // Cargar métodos de pago al iniciar
    LaunchedEffect(Unit) {
        isLoadingPaymentMethods = true
        paymentMethodsRepository.getPaymentMethods().onSuccess { methods ->
            availablePaymentMethods = methods
        }.onFailure {
            // Handle error silently or show a message
        }
        isLoadingPaymentMethods = false
    }

    // Estados del formulario - Negocio
    var businessName by remember { mutableStateOf("") }
    var businessDescription by remember { mutableStateOf("") }
    var businessTagsList by remember { mutableStateOf(emptyList<String>()) }

    // Estados de upload de imágenes del negocio (usando ImageUploadState)
    var businessAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    
    // Paths de S3 (extraídos del estado Success)
    val businessAvatarPath = (businessAvatarState as? ImageUploadState.Success)?.s3Path

    // Estados del formulario - Sucursales
    var nextBranchId by remember { mutableStateOf(2) }
    val branchForms = remember {
        mutableStateListOf(defaultBranchFormState(1))
    }

    fun updateBranch(index: Int, update: (BranchFormState) -> BranchFormState) {
        branchForms[index] = update(branchForms[index])
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Registrar Negocio",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECCION: Informacion del Negocio
                Text(
                    text = "Informacion del Negocio",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                LlegoTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = "Nombre del Negocio *",
                    placeholder = "Ej: Mi negocio"
                )

                LlegoTextField(
                    value = businessDescription,
                    onValueChange = { businessDescription = it },
                    label = "Descripcion",
                    placeholder = "Describe tu negocio",
                    singleLine = false
                )

                // Tags selector mejorado
                TagsSelector(
                    selectedTags = businessTagsList,
                    onTagsChange = { businessTagsList = it }
                )

                // Imagen del Negocio con upload a S3
                Text(
                    text = "Imagen del Negocio (Opcional)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImageUploadPreview(
                        label = "Avatar",
                        uploadState = businessAvatarState,
                        onStateChange = { businessAvatarState = it },
                        uploadFunction = { uri, token ->
                            imageUploadService.uploadBusinessAvatar(uri, token)
                        },
                        size = ImageUploadSize.MEDIUM,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SECCION: Sucursales
                Text(
                    text = "Sucursales",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                branchForms.forEachIndexed { index, branch ->
                    key(branch.id) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Sucursal ${index + 1}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            // Selector de tipos de sucursal *
                            Column {
                                Text(
                                    text = "Tipos de Servicio *",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Selecciona uno o mas tipos de servicio que ofrece esta sucursal",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BranchTipoChip(
                                        tipo = BranchTipo.RESTAURANTE,
                                        selected = branch.selectedTipos.contains(BranchTipo.RESTAURANTE),
                                        onClick = {
                                            val updated = if (branch.selectedTipos.contains(BranchTipo.RESTAURANTE)) {
                                                branch.selectedTipos - BranchTipo.RESTAURANTE
                                            } else {
                                                branch.selectedTipos + BranchTipo.RESTAURANTE
                                            }
                                            updateBranch(index) { current -> current.copy(selectedTipos = updated) }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    BranchTipoChip(
                                        tipo = BranchTipo.TIENDA,
                                        selected = branch.selectedTipos.contains(BranchTipo.TIENDA),
                                        onClick = {
                                            val updated = if (branch.selectedTipos.contains(BranchTipo.TIENDA)) {
                                                branch.selectedTipos - BranchTipo.TIENDA
                                            } else {
                                                branch.selectedTipos + BranchTipo.TIENDA
                                            }
                                            updateBranch(index) { current -> current.copy(selectedTipos = updated) }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    BranchTipoChip(
                                        tipo = BranchTipo.DULCERIA,
                                        selected = branch.selectedTipos.contains(BranchTipo.DULCERIA),
                                        onClick = {
                                            val updated = if (branch.selectedTipos.contains(BranchTipo.DULCERIA)) {
                                                branch.selectedTipos - BranchTipo.DULCERIA
                                            } else {
                                                branch.selectedTipos + BranchTipo.DULCERIA
                                            }
                                            updateBranch(index) { current -> current.copy(selectedTipos = updated) }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            LlegoTextField(
                                value = branch.name,
                                onValueChange = { value ->
                                    updateBranch(index) { current -> current.copy(name = value) }
                                },
                                label = "Nombre de la Sucursal *",
                                placeholder = "Ej: Sucursal Centro"
                            )

                            LlegoTextField(
                                value = branch.address,
                                onValueChange = { value ->
                                    updateBranch(index) { current -> current.copy(address = value) }
                                },
                                label = "Direccion",
                                placeholder = "Ej: Av. Principal 123"
                            )

                            // PhoneInput con codigo de pais
                            PhoneInput(
                                phoneNumber = branch.phone,
                                countryCode = branch.countryCode,
                                onPhoneChange = { value ->
                                    updateBranch(index) { current -> current.copy(phone = value) }
                                },
                                onCountryCodeChange = { value ->
                                    updateBranch(index) { current -> current.copy(countryCode = value) }
                                }
                            )

                            // MapLocationPicker con Google Maps REAL
                            MapLocationPickerReal(
                                latitude = branch.latitude,
                                longitude = branch.longitude,
                                onLocationSelected = { lat, lng ->
                                    updateBranch(index) { current ->
                                        current.copy(latitude = lat, longitude = lng)
                                    }
                                }
                            )

                            // SchedulePicker interactivo
                            SchedulePicker(
                                schedule = branch.schedule,
                                onScheduleChange = { schedule ->
                                    updateBranch(index) { current -> current.copy(schedule = schedule) }
                                }
                            )

                            // DeliveryRadiusPicker
                            DeliveryRadiusPicker(
                                radiusKm = branch.deliveryRadius,
                                onRadiusChange = { radius ->
                                    updateBranch(index) { current -> current.copy(deliveryRadius = radius) }
                                }
                            )

                            // FacilitiesSelector
                            FacilitiesSelector(
                                selectedFacilities = branch.facilities,
                                onFacilitiesChange = { facilities ->
                                    updateBranch(index) { current -> current.copy(facilities = facilities) }
                                }
                            )

                            // PaymentMethodSelector
                            PaymentMethodSelector(
                                availablePaymentMethods = availablePaymentMethods,
                                selectedPaymentMethodIds = branch.selectedPaymentMethodIds,
                                onSelectionChange = { selection ->
                                    updateBranch(index) { current ->
                                        current.copy(selectedPaymentMethodIds = selection)
                                    }
                                },
                                isLoading = isLoadingPaymentMethods,
                                layout = PaymentMethodSelectorLayout.FLOW
                            )

                            // Imagenes de la Sucursal
                            Text(
                                text = "Imagenes de la Sucursal (Opcional)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ImageUploadPreview(
                                    label = "Avatar",
                                    uploadState = branch.avatarState,
                                    onStateChange = { state ->
                                        updateBranch(index) { current -> current.copy(avatarState = state) }
                                    },
                                    uploadFunction = { uri, token ->
                                        imageUploadService.uploadBranchAvatar(uri, token)
                                    },
                                    size = ImageUploadSize.MEDIUM,
                                    modifier = Modifier.weight(1f)
                                )

                                ImageUploadPreview(
                                    label = "Portada",
                                    uploadState = branch.coverState,
                                    onStateChange = { state ->
                                        updateBranch(index) { current -> current.copy(coverState = state) }
                                    },
                                    uploadFunction = { uri, token ->
                                        imageUploadService.uploadBranchCover(uri, token)
                                    },
                                    size = ImageUploadSize.MEDIUM,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (index < branchForms.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                OutlinedButton(
                    onClick = {
                        branchForms.add(defaultBranchFormState(nextBranchId))
                        nextBranchId += 1
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = LlegoCustomShapes.secondaryButton
                ) {
                    Text("Agregar sucursal")
                }

                // Mensaje de error
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Codigo de invitacion
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                
                val redeemState by invitationViewModel.redeemState.collectAsState()
                
                com.llego.business.invitations.ui.components.InvitationCodeInput(
                    isLoading = redeemState is com.llego.business.invitations.ui.viewmodel.RedeemState.Loading,
                    errorMessage = (redeemState as? com.llego.business.invitations.ui.viewmodel.RedeemState.Error)?.message,
                    onRedeemCode = { code ->
                        invitationViewModel.redeemInvitationCode(code)
                    }
                )
                
                // Show success message when code is redeemed
                LaunchedEffect(redeemState) {
                    if (redeemState is com.llego.business.invitations.ui.viewmodel.RedeemState.Success) {
                        // Optionally show a success message or reload user data
                        invitationViewModel.resetRedeemState()
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Botón de registro
                val branchesValid = branchForms.all { branch ->
                    branch.name.isNotBlank() &&
                        branch.phone.isNotBlank() &&
                        branch.selectedTipos.isNotEmpty() &&
                        branch.selectedPaymentMethodIds.isNotEmpty() &&
                        branch.latitude != 0.0 &&
                        branch.longitude != 0.0
                }

                LlegoButton(
                    text = "Registrar Negocio",
                    modifier = Modifier.fillMaxWidth(),
                    size = LlegoButtonSize.LARGE,
                    onClick = {
                        registeredBusinessName = businessName

                        val business = CreateBusinessInput(
                            name = businessName,
                            description = businessDescription.ifBlank { null },
                            tags = businessTagsList,
                            avatar = businessAvatarPath
                        )
                        val branches = branchForms.map { branch ->
                            RegisterBranchInput(
                                name = branch.name,
                                address = branch.address.ifBlank { null },
                                phone = combinePhoneNumber(branch.countryCode, branch.phone),
                                coordinates = CoordinatesInput(
                                    lat = branch.latitude,
                                    lng = branch.longitude
                                ),
                                schedule = branch.schedule.toBackendSchedule(),
                                tipos = branch.selectedTipos.toList(),
                                paymentMethodIds = branch.selectedPaymentMethodIds,
                                avatar = (branch.avatarState as? ImageUploadState.Success)?.s3Path,
                                coverImage = (branch.coverState as? ImageUploadState.Success)?.s3Path,
                                deliveryRadius = branch.deliveryRadius,
                                facilities = branch.facilities
                            )
                        }

                        viewModel.registerBusiness(business, branches)
                    },
                    enabled = !uiState.isLoading &&
                            businessName.isNotBlank() &&
                            branchesValid
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Observar éxito de registro
    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            showSuccessConfirmation = true
        }
    }

    // Mostrar pantalla de confirmación
    if (showSuccessConfirmation) {
        LlegoConfirmationScreen(
            config = LlegoConfirmationDefaults.businessCreated(registeredBusinessName),
            onDismiss = {
                showSuccessConfirmation = false
                viewModel.resetState()
                onRegisterSuccess()
            }
        )
    }
}

/**
 * Chip para seleccionar tipo de sucursal (BranchTipo)
 * Soporta selección múltiple
 */
@Composable
private fun BranchTipoChip(
    tipo: BranchTipo,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val label = when (tipo) {
        BranchTipo.RESTAURANTE -> "Restaurante"
        BranchTipo.TIENDA -> "Tienda"
        BranchTipo.DULCERIA -> "Dulcería"
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primaryColor.copy(alpha = 0.12f),
            selectedLabelColor = primaryColor,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            selectedBorderColor = primaryColor.copy(alpha = 0.5f),
            selectedBorderWidth = 1.dp
        ),
        shape = LlegoCustomShapes.secondaryButton,
        modifier = modifier
    )
}

private data class BranchFormState(
    val id: Int,
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val countryCode: String = "+51",
    val latitude: Double = 23.1136,
    val longitude: Double = -82.3666,
    val schedule: Map<String, DaySchedule> = defaultBranchSchedule(),
    val deliveryRadius: Double = 5.0,
    val facilities: List<String> = emptyList(),
    val selectedTipos: Set<BranchTipo> = emptySet(),
    val selectedPaymentMethodIds: List<String> = emptyList(),
    val avatarState: ImageUploadState = ImageUploadState.Idle,
    val coverState: ImageUploadState = ImageUploadState.Idle
)

private fun defaultBranchFormState(id: Int): BranchFormState {
    return BranchFormState(id = id, schedule = defaultBranchSchedule())
}

private fun defaultBranchSchedule(): Map<String, DaySchedule> {
    return mapOf(
        "mon" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
        "tue" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
        "wed" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
        "thu" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
        "fri" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
        "sat" to DaySchedule(false, emptyList()),
        "sun" to DaySchedule(false, emptyList())
    )
}
