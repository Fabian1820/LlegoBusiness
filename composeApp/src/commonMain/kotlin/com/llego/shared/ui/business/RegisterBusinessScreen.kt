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
    authViewModel: com.llego.shared.ui.auth.AuthViewModel,
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
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

    // Estados del formulario - Múltiples Negocios
    var nextBusinessId by remember { mutableStateOf(2) }
    var nextBranchId by remember { mutableStateOf(2) }
    val businessForms = remember {
        mutableStateListOf(defaultBusinessFormState(1, 1))
    }

    fun updateBusiness(index: Int, update: (BusinessFormState) -> BusinessFormState) {
        businessForms[index] = update(businessForms[index])
    }

    fun updateBranch(businessIndex: Int, branchIndex: Int, update: (BranchFormState) -> BranchFormState) {
        val business = businessForms[businessIndex]
        val updatedBranches = business.branches.toMutableList()
        updatedBranches[branchIndex] = update(updatedBranches[branchIndex])
        businessForms[businessIndex] = business.copy(branches = updatedBranches)
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
                businessForms.forEachIndexed { businessIndex, business ->
                    val isMultipleBusinesses = businessForms.size > 1
                    val businessTitle = if (isMultipleBusinesses) {
                        "Negocio ${businessIndex + 1}"
                    } else {
                        "Informacion del Negocio"
                    }

                    Text(
                        text = businessTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    LlegoTextField(
                        value = business.name,
                        onValueChange = { value ->
                            updateBusiness(businessIndex) { current -> current.copy(name = value) }
                        },
                        label = "Nombre del Negocio *",
                        placeholder = "Ej: Mi negocio"
                    )

                    LlegoTextField(
                        value = business.description,
                        onValueChange = { value ->
                            updateBusiness(businessIndex) { current -> current.copy(description = value) }
                        },
                        label = "Descripcion",
                        placeholder = "Describe tu negocio",
                        singleLine = false
                    )

                    TagsSelector(
                        selectedTags = business.tags,
                        onTagsChange = { tags ->
                            updateBusiness(businessIndex) { current -> current.copy(tags = tags) }
                        }
                    )

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
                            uploadState = business.avatarState,
                            onStateChange = { state ->
                                updateBusiness(businessIndex) { current -> current.copy(avatarState = state) }
                            },
                            uploadFunction = { uri, token ->
                                imageUploadService.uploadBusinessAvatar(uri, token)
                            },
                            size = ImageUploadSize.MEDIUM,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Sucursales",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    business.branches.forEachIndexed { branchIndex, branch ->
                        key(branch.id) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Sucursal ${branchIndex + 1}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )

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
                                                updateBranch(businessIndex, branchIndex) { current -> current.copy(selectedTipos = updated) }
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
                                                updateBranch(businessIndex, branchIndex) { current -> current.copy(selectedTipos = updated) }
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
                                                updateBranch(businessIndex, branchIndex) { current -> current.copy(selectedTipos = updated) }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                LlegoTextField(
                                    value = branch.name,
                                    onValueChange = { value ->
                                        updateBranch(businessIndex, branchIndex) { current -> current.copy(name = value) }
                                    },
                                    label = "Nombre de la Sucursal *",
                                    placeholder = "Ej: Sucursal Centro"
                                )

                                LlegoTextField(
                                    value = branch.address,
                                    onValueChange = { value ->
                                        updateBranch(businessIndex, branchIndex) { current -> current.copy(address = value) }
                                    },
                                    label = "Direccion",
                                    placeholder = "Ej: Av. Principal 123"
                                )

                                PhoneInput(
                                    phoneNumber = branch.phone,
                                    countryCode = branch.countryCode,
                                    onPhoneChange = { value ->
                                        updateBranch(businessIndex, branchIndex) { current -> current.copy(phone = value) }
                                    },
                                    onCountryCodeChange = { value ->
                                        updateBranch(businessIndex, branchIndex) { current -> current.copy(countryCode = value) }
                                    }
                                )

                                MapLocationPickerReal(
                                    latitude = branch.latitude,
                                    longitude = branch.longitude,
                                    onLocationSelected = { lat, lng ->
                                        updateBranch(businessIndex, branchIndex) { current ->
                                            current.copy(latitude = lat, longitude = lng)
                                        }
                                    },
                                    onOpenMapSelection = onOpenMapSelection
                                )

                                SchedulePicker(
                                    schedule = branch.schedule,
                                    onScheduleChange = { schedule ->
                                        updateBranch(businessIndex, branchIndex) { current -> current.copy(schedule = schedule) }
                                    }
                                )

                                DeliveryRadiusPicker(
                                    radiusKm = branch.deliveryRadius,
                                    onRadiusChange = { radius ->
                                        updateBranch(businessIndex, branchIndex) { current -> current.copy(deliveryRadius = radius) }
                                    }
                                )

                                FacilitiesSelector(
                                    selectedFacilities = branch.facilities,
                                    onFacilitiesChange = { facilities ->
                                        updateBranch(businessIndex, branchIndex) { current -> current.copy(facilities = facilities) }
                                    }
                                )

                                PaymentMethodSelector(
                                    availablePaymentMethods = availablePaymentMethods,
                                    selectedPaymentMethodIds = branch.selectedPaymentMethodIds,
                                    onSelectionChange = { selection ->
                                        updateBranch(businessIndex, branchIndex) { current ->
                                            current.copy(selectedPaymentMethodIds = selection)
                                        }
                                    },
                                    isLoading = isLoadingPaymentMethods,
                                    layout = PaymentMethodSelectorLayout.FLOW
                                )

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
                                            updateBranch(businessIndex, branchIndex) { current -> current.copy(avatarState = state) }
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
                                            updateBranch(businessIndex, branchIndex) { current -> current.copy(coverState = state) }
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

                        if (branchIndex < business.branches.lastIndex) {
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
                            val updatedBranches = business.branches + defaultBranchFormState(nextBranchId)
                            nextBranchId += 1
                            updateBusiness(businessIndex) { current -> current.copy(branches = updatedBranches) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = LlegoCustomShapes.secondaryButton
                    ) {
                        Text("Agregar sucursal")
                    }

                    if (businessIndex < businessForms.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                OutlinedButton(
                    onClick = {
                        val newBusiness = defaultBusinessFormState(nextBusinessId, nextBranchId)
                        nextBusinessId += 1
                        nextBranchId += 1
                        businessForms.add(newBusiness)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = LlegoCustomShapes.secondaryButton
                ) {
                    Text("Agregar negocio")
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
                
                // Handle invitation redemption success
                LaunchedEffect(redeemState) {
                    if (redeemState is com.llego.business.invitations.ui.viewmodel.RedeemState.Success) {
                        println("RegisterBusinessScreen: Invitación redimida exitosamente, recargando datos del usuario...")

                        // Reload user data to get updated businessIds and branchIds
                        authViewModel.reloadUserData()

                        // Navigate to success (will handle branch selection if needed)
                        onRegisterSuccess()

                        // Reset state
                        invitationViewModel.resetRedeemState()
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Bot?n de registro
                val businessesValid = businessForms.all { business ->
                    business.name.isNotBlank() &&
                        business.branches.all { branch ->
                            branch.name.isNotBlank() &&
                                branch.phone.isNotBlank() &&
                                branch.selectedTipos.isNotEmpty() &&
                                branch.selectedPaymentMethodIds.isNotEmpty() &&
                                branch.latitude != 0.0 &&
                                branch.longitude != 0.0
                        }
                }
                val isMultipleBusinesses = businessForms.size > 1

                LlegoButton(
                    text = if (isMultipleBusinesses) "Registrar Negocios" else "Registrar Negocio",
                    modifier = Modifier.fillMaxWidth(),
                    size = LlegoButtonSize.LARGE,
                    onClick = {
                        registeredBusinessName = if (isMultipleBusinesses) {
                            "tus negocios"
                        } else {
                            businessForms.firstOrNull()?.name.orEmpty()
                        }

                        val businessesInput = businessForms.map { business ->
                            val businessInput = CreateBusinessInput(
                                name = business.name,
                                description = business.description.ifBlank { null },
                                tags = business.tags,
                                avatar = (business.avatarState as? ImageUploadState.Success)?.s3Path
                            )
                            val branchesInput = business.branches.map { branch ->
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
                            businessInput to branchesInput
                        }

                        if (isMultipleBusinesses) {
                            viewModel.registerMultipleBusinesses(businessesInput)
                        } else {
                            val (businessInput, branchesInput) = businessesInput.first()
                            viewModel.registerBusiness(businessInput, branchesInput)
                        }
                    },
                    enabled = !uiState.isLoading && businessesValid
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

private data class BusinessFormState(
    val id: Int,
    val name: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val avatarState: ImageUploadState = ImageUploadState.Idle,
    val branches: List<BranchFormState> = listOf(defaultBranchFormState(1))
)

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

private fun defaultBusinessFormState(id: Int, firstBranchId: Int): BusinessFormState {
    return BusinessFormState(
        id = id,
        branches = listOf(defaultBranchFormState(firstBranchId))
    )
}

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
