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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.nichos.common.ui.components.rememberImagePickerController
import com.llego.shared.data.model.BusinessType
import com.llego.shared.data.model.CreateBusinessInput
import com.llego.shared.data.model.RegisterBranchInput
import com.llego.shared.data.model.CoordinatesInput
import com.llego.shared.ui.components.atoms.LlegoTextField
import com.llego.shared.ui.components.atoms.LlegoButton

/**
 * Pantalla para registrar un nuevo negocio con su sucursal inicial
 *
 * Flujo:
 * 1. Usuario ya está autenticado (tiene JWT)
 * 2. Formulario para crear negocio (nombre, tipo, descripción, tags)
 * 3. Formulario para crear primera sucursal (nombre, dirección, teléfono, coordenadas)
 * 4. Al enviar: registerBusiness mutation
 * 5. businessId se agrega automáticamente a user.businessIds
 * 6. Redirigir a Dashboard del nicho
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

    // ImagePicker controller
    val imagePickerController = rememberImagePickerController()

    // Estados del formulario - Negocio
    var businessName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf(BusinessType.RESTAURANT) }
    var businessDescription by remember { mutableStateOf("") }
    var businessTags by remember { mutableStateOf("") }
    var businessAvatarUri by remember { mutableStateOf<String?>(null) }
    var businessCoverUri by remember { mutableStateOf<String?>(null) }

    // Estados del formulario - Sucursal
    var branchName by remember { mutableStateOf("") }
    var branchAddress by remember { mutableStateOf("") }
    var branchPhone by remember { mutableStateOf("") }
    var branchLatitude by remember { mutableStateOf("") }
    var branchLongitude by remember { mutableStateOf("") }
    var branchSchedule by remember { mutableStateOf("Lun-Vie: 9:00-18:00") }
    var branchAvatarUri by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Negocio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                // Sección: Información del Negocio
                Text(
                    text = "Información del Negocio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                LlegoTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = "Nombre del Negocio",
                    placeholder = "Ej: Restaurante La Havana"
                )

                // Selector de tipo de negocio
                Column {
                    Text(
                        text = "Tipo de Negocio",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
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
                            type = BusinessType.PHARMACY,
                            selected = businessType == BusinessType.PHARMACY,
                            onClick = { businessType = BusinessType.PHARMACY },
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

                LlegoTextField(
                    value = businessTags,
                    onValueChange = { businessTags = it },
                    label = "Tags (separados por coma)",
                    placeholder = "Ej: comida, delivery, rápido"
                )

                // Sección: Imágenes del Negocio
                Text(
                    text = "Imágenes (Opcional)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Avatar
                    OutlinedButton(
                        onClick = {
                            imagePickerController.pickImage { uri ->
                                businessAvatarUri = uri
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (businessAvatarUri != null) "Avatar ✓" else "Avatar")
                    }

                    // Botón Cover
                    OutlinedButton(
                        onClick = {
                            imagePickerController.pickImage { uri ->
                                businessCoverUri = uri
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (businessCoverUri != null) "Portada ✓" else "Portada")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección: Primera Sucursal
                Text(
                    text = "Primera Sucursal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                LlegoTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    label = "Nombre de la Sucursal",
                    placeholder = "Ej: Sucursal Centro"
                )

                LlegoTextField(
                    value = branchAddress,
                    onValueChange = { branchAddress = it },
                    label = "Dirección",
                    placeholder = "Ej: Av. Principal 123"
                )

                LlegoTextField(
                    value = branchPhone,
                    onValueChange = { branchPhone = it },
                    label = "Teléfono",
                    placeholder = "Ej: +51 999 999 999"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LlegoTextField(
                        value = branchLatitude,
                        onValueChange = { branchLatitude = it },
                        label = "Latitud",
                        placeholder = "-12.0464",
                        modifier = Modifier.weight(1f)
                    )
                    LlegoTextField(
                        value = branchLongitude,
                        onValueChange = { branchLongitude = it },
                        label = "Longitud",
                        placeholder = "-77.0428",
                        modifier = Modifier.weight(1f)
                    )
                }

                LlegoTextField(
                    value = branchSchedule,
                    onValueChange = { branchSchedule = it },
                    label = "Horario",
                    placeholder = "Ej: Lun-Vie: 9:00-18:00"
                )

                // Imagen de la Sucursal
                OutlinedButton(
                    onClick = {
                        imagePickerController.pickImage { uri ->
                            branchAvatarUri = uri
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (branchAvatarUri != null) "Foto Sucursal ✓" else "Agregar Foto (Opcional)")
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
                        val business = CreateBusinessInput(
                            name = businessName,
                            type = businessType.name.lowercase(),
                            description = businessDescription.ifBlank { null },
                            tags = businessTags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            avatar = null, // TODO: Implementar upload de imágenes
                            coverImage = null
                        )

                        val branch = RegisterBranchInput(
                            name = branchName,
                            address = branchAddress.ifBlank { null },
                            phone = branchPhone,
                            coordinates = CoordinatesInput(
                                lat = branchLatitude.toDoubleOrNull() ?: 0.0,
                                lng = branchLongitude.toDoubleOrNull() ?: 0.0
                            ),
                            schedule = mapOf("general" to branchSchedule),
                            avatar = null,
                            coverImage = null,
                            deliveryRadius = 5.0,
                            facilities = emptyList()
                        )

                        viewModel.registerBusiness(business, listOf(branch))
                    },
                    enabled = !uiState.isLoading &&
                            businessName.isNotBlank() &&
                            branchName.isNotBlank() &&
                            branchPhone.isNotBlank()
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
            onRegisterSuccess(businessType)
        }
    }
}

@Composable
private fun BusinessTypeChip(
    type: BusinessType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = when (type) {
                    BusinessType.RESTAURANT -> "🍽️ Restaurante"
                    BusinessType.MARKET -> "🛒 Tienda"
                    BusinessType.PHARMACY -> "💊 Farmacia"
                    else -> type.name
                },
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier = modifier
    )
}
