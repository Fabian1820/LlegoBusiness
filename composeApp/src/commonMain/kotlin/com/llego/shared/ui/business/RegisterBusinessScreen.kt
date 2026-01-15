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
import com.llego.shared.ui.components.molecules.PhoneInput
import com.llego.shared.ui.components.molecules.SchedulePicker
import com.llego.shared.ui.components.molecules.TagsSelector
import com.llego.shared.ui.components.molecules.combinePhoneNumber
import com.llego.shared.ui.components.molecules.toBackendSchedule
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val imageUploadService = remember { ImageUploadServiceFactory.create() }

    // Estados de confirmación
    var showSuccessConfirmation by remember { mutableStateOf(false) }
    var registeredBusinessName by remember { mutableStateOf("") }

    // Estados del formulario - Negocio
    var businessName by remember { mutableStateOf("") }
    var businessDescription by remember { mutableStateOf("") }
    var businessTagsList by remember { mutableStateOf(emptyList<String>()) }

    // Estado para tipos de sucursal seleccionados (nuevo)
    var selectedBranchTipos by remember { mutableStateOf(setOf<BranchTipo>()) }
    
    // Estados de upload de imágenes del negocio (usando ImageUploadState)
    var businessAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    
    // Paths de S3 (extraídos del estado Success)
    val businessAvatarPath = (businessAvatarState as? ImageUploadState.Success)?.s3Path

    // Estados del formulario - Sucursal
    var branchName by remember { mutableStateOf("") }
    var branchAddress by remember { mutableStateOf("") }
    var branchPhone by remember { mutableStateOf("") }
    var branchCountryCode by remember { mutableStateOf("+51") }
    // Coordenadas default: La Habana, Cuba
    var branchLatitude by remember { mutableStateOf(23.1136) }
    var branchLongitude by remember { mutableStateOf(-82.3666) }
    var branchSchedule by remember {
        mutableStateOf<Map<String, DaySchedule>>(
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
    var branchDeliveryRadius by remember { mutableStateOf(5.0) }
    var branchFacilities by remember { mutableStateOf(emptyList<String>()) }
    
    // Estados de upload de imágenes de sucursal (usando ImageUploadState)
    var branchAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var branchCoverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    
    // Paths de S3 (extraídos del estado Success)
    val branchAvatarPath = (branchAvatarState as? ImageUploadState.Success)?.s3Path
    val branchCoverPath = (branchCoverState as? ImageUploadState.Success)?.s3Path

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
                // SECCIÓN: Información del Negocio
                Text(
                    text = "Información del Negocio",
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
                    label = "Descripción",
                    placeholder = "Describe tu negocio",
                    singleLine = false
                )

                // Tags selector mejorado
                TagsSelector(
                    selectedTags = businessTagsList,
                    onTagsChange = { businessTagsList = it }
                )

                // Imágenes del Negocio con upload a S3
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

                // SECCIÓN: Primera Sucursal
                Text(
                    text = "Primera Sucursal",
                    style = MaterialTheme.typography.titleMedium.copy(
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
                        text = "Selecciona uno o más tipos de servicio que ofrece esta sucursal",
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
                            selected = selectedBranchTipos.contains(BranchTipo.RESTAURANTE),
                            onClick = {
                                selectedBranchTipos = if (selectedBranchTipos.contains(BranchTipo.RESTAURANTE)) {
                                    selectedBranchTipos - BranchTipo.RESTAURANTE
                                } else {
                                    selectedBranchTipos + BranchTipo.RESTAURANTE
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BranchTipoChip(
                            tipo = BranchTipo.TIENDA,
                            selected = selectedBranchTipos.contains(BranchTipo.TIENDA),
                            onClick = {
                                selectedBranchTipos = if (selectedBranchTipos.contains(BranchTipo.TIENDA)) {
                                    selectedBranchTipos - BranchTipo.TIENDA
                                } else {
                                    selectedBranchTipos + BranchTipo.TIENDA
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BranchTipoChip(
                            tipo = BranchTipo.DULCERIA,
                            selected = selectedBranchTipos.contains(BranchTipo.DULCERIA),
                            onClick = {
                                selectedBranchTipos = if (selectedBranchTipos.contains(BranchTipo.DULCERIA)) {
                                    selectedBranchTipos - BranchTipo.DULCERIA
                                } else {
                                    selectedBranchTipos + BranchTipo.DULCERIA
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                LlegoTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    label = "Nombre de la Sucursal *",
                    placeholder = "Ej: Sucursal Centro"
                )

                LlegoTextField(
                    value = branchAddress,
                    onValueChange = { branchAddress = it },
                    label = "Dirección",
                    placeholder = "Ej: Av. Principal 123"
                )

                // PhoneInput con código de país
                PhoneInput(
                    phoneNumber = branchPhone,
                    countryCode = branchCountryCode,
                    onPhoneChange = { branchPhone = it },
                    onCountryCodeChange = { branchCountryCode = it }
                )

                // MapLocationPicker con Google Maps REAL
                MapLocationPickerReal(
                    latitude = branchLatitude,
                    longitude = branchLongitude,
                    onLocationSelected = { lat, lng ->
                        branchLatitude = lat
                        branchLongitude = lng
                    }
                )

                // SchedulePicker interactivo
                SchedulePicker(
                    schedule = branchSchedule,
                    onScheduleChange = { branchSchedule = it }
                )

                // DeliveryRadiusPicker
                DeliveryRadiusPicker(
                    radiusKm = branchDeliveryRadius,
                    onRadiusChange = { branchDeliveryRadius = it }
                )

                // FacilitiesSelector
                FacilitiesSelector(
                    selectedFacilities = branchFacilities,
                    onFacilitiesChange = { branchFacilities = it }
                )

                // Imágenes de la Sucursal
                Text(
                    text = "Imágenes de la Sucursal (Opcional)",
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
                        uploadState = branchAvatarState,
                        onStateChange = { branchAvatarState = it },
                        uploadFunction = { uri, token ->
                            imageUploadService.uploadBranchAvatar(uri, token)
                        },
                        size = ImageUploadSize.MEDIUM,
                        modifier = Modifier.weight(1f)
                    )

                    ImageUploadPreview(
                        label = "Portada",
                        uploadState = branchCoverState,
                        onStateChange = { branchCoverState = it },
                        uploadFunction = { uri, token ->
                            imageUploadService.uploadBranchCover(uri, token)
                        },
                        size = ImageUploadSize.MEDIUM,
                        modifier = Modifier.weight(1f)
                    )
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

                // Botón de registro
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

                        // El usuario selecciona el tipo de sucursal directamente, no hay conversión automática
                        val branch = RegisterBranchInput(
                            name = branchName,
                            address = branchAddress.ifBlank { null },
                            phone = combinePhoneNumber(branchCountryCode, branchPhone),
                            coordinates = CoordinatesInput(
                                lat = branchLatitude,
                                lng = branchLongitude
                            ),
                            schedule = branchSchedule.toBackendSchedule(),
                            tipos = selectedBranchTipos.toList(), // Usar los tipos seleccionados por el usuario
                            avatar = branchAvatarPath,
                            coverImage = branchCoverPath,
                            deliveryRadius = branchDeliveryRadius,
                            facilities = branchFacilities
                        )

                        viewModel.registerBusiness(business, listOf(branch))
                    },
                    enabled = !uiState.isLoading &&
                            businessName.isNotBlank() &&
                            selectedBranchTipos.isNotEmpty() && // Validar que se haya seleccionado al menos un tipo
                            branchName.isNotBlank() &&
                            branchPhone.isNotBlank() &&
                            branchLatitude != 0.0 &&
                            branchLongitude != 0.0
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
