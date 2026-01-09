package com.llego.shared.ui.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.*
import com.llego.shared.data.upload.ImageUploadServiceFactory
import com.llego.shared.ui.components.atoms.LlegoTextField
import com.llego.shared.ui.components.atoms.LlegoButton
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
    onRegisterSuccess: (BusinessType) -> Unit,
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
    var businessType by remember { mutableStateOf(BusinessType.RESTAURANT) }
    var businessDescription by remember { mutableStateOf("") }
    var businessTagsList by remember { mutableStateOf(emptyList<String>()) }
    
    // Estados de upload de imágenes del negocio (usando ImageUploadState)
    var businessAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var businessCoverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    
    // Paths de S3 (extraídos del estado Success)
    val businessAvatarPath = (businessAvatarState as? ImageUploadState.Success)?.s3Path
    val businessCoverPath = (businessCoverState as? ImageUploadState.Success)?.s3Path

    // Estados del formulario - Sucursal
    var branchName by remember { mutableStateOf("") }
    var branchAddress by remember { mutableStateOf("") }
    var branchPhone by remember { mutableStateOf("") }
    var branchCountryCode by remember { mutableStateOf("+51") }
    var branchLatitude by remember { mutableStateOf(0.0) }
    var branchLongitude by remember { mutableStateOf(0.0) }
    var branchSchedule by remember {
        mutableStateOf<Map<String, DaySchedule>>(
            mapOf(
                "lun" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
                "mar" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
                "mie" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
                "jue" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
                "vie" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
                "sab" to DaySchedule(false, emptyList()),
                "dom" to DaySchedule(false, emptyList())
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // SECCIÓN: Información del Negocio
                Text(
                    text = "Información del Negocio",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                LlegoTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = "Nombre del Negocio *",
                    placeholder = "Ej: Restaurante La Havana"
                )

                // Selector de tipo de negocio
                Column {
                    Text(
                        text = "Tipo de Negocio *",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BusinessTypeChip(
                            type = BusinessType.RESTAURANT,
                            selected = businessType == BusinessType.RESTAURANT,
                            onClick = { businessType = BusinessType.RESTAURANT },
                            modifier = Modifier.weight(1f)
                        )
                        BusinessTypeChip(
                            type = BusinessType.MARKET,
                            selected = businessType == BusinessType.MARKET,
                            onClick = { businessType = BusinessType.MARKET },
                            modifier = Modifier.weight(1f)
                        )
                        BusinessTypeChip(
                            type = BusinessType.CANDY_STORE,
                            selected = businessType == BusinessType.CANDY_STORE,
                            onClick = { businessType = BusinessType.CANDY_STORE },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

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
                    onTagsChange = { businessTagsList = it },
                    businessType = businessType
                )

                // Imágenes del Negocio con upload a S3
                Text(
                    text = "Imágenes del Negocio (Opcional)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
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

                    ImageUploadPreview(
                        label = "Portada",
                        uploadState = businessCoverState,
                        onStateChange = { businessCoverState = it },
                        uploadFunction = { uri, token ->
                            imageUploadService.uploadBusinessCover(uri, token)
                        },
                        size = ImageUploadSize.MEDIUM,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN: Primera Sucursal
                Text(
                    text = "Primera Sucursal",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

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
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
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
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de registro
                LlegoButton(
                    text = "Registrar Negocio",
                    onClick = {
                        registeredBusinessName = businessName

                        val business = CreateBusinessInput(
                            name = businessName,
                            type = businessType.toBackendType(),
                            description = businessDescription.ifBlank { null },
                            tags = businessTagsList,
                            avatar = businessAvatarPath,
                            coverImage = businessCoverPath
                        )

                        val branch = RegisterBranchInput(
                            name = branchName,
                            address = branchAddress.ifBlank { null },
                            phone = combinePhoneNumber(branchCountryCode, branchPhone),
                            coordinates = CoordinatesInput(
                                lat = branchLatitude,
                                lng = branchLongitude
                            ),
                            schedule = branchSchedule.toBackendSchedule(),
                            avatar = branchAvatarPath,
                            coverImage = branchCoverPath,
                            deliveryRadius = branchDeliveryRadius,
                            facilities = branchFacilities
                        )

                        viewModel.registerBusiness(business, listOf(branch))
                    },
                    enabled = !uiState.isLoading &&
                            businessName.isNotBlank() &&
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
                onRegisterSuccess(businessType)
            }
        )
    }
}

@Composable
private fun BusinessTypeChip(
    type: BusinessType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = when (type) {
                    BusinessType.RESTAURANT -> "🍽️ Restaurante"
                    BusinessType.MARKET -> "🛒 Tienda"
                    BusinessType.CANDY_STORE -> "🍬 Dulcería"
                },
                style = MaterialTheme.typography.bodySmall
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primaryColor.copy(alpha = 0.15f),
            selectedLabelColor = primaryColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = primaryColor,
            selectedBorderWidth = 2.dp
        ),
        modifier = modifier
    )
}
