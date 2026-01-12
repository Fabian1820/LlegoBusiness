package com.llego.nichos.restaurant.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.nichos.restaurant.ui.components.profile.*
import com.llego.shared.data.repositories.BusinessRepository
import com.llego.shared.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de Perfil del Restaurante
 * 
 * Muestra y permite editar:
 * - Información del negocio (nombre, descripción, categoría)
 * - Información del propietario (nombre, email, teléfono)
 * - Información de la sucursal (dirección, teléfono, horarios)
 * - Redes sociales
 * - Ubicación en mapa
 * - Tags y facilidades
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfileScreen(
    authViewModel: AuthViewModel,
    businessRepository: BusinessRepository,
    onNavigateBack: () -> Unit = {},
    onNavigateToBranches: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var showShareDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    // Datos del usuario, negocio y sucursal
    val authUiState by authViewModel.uiState.collectAsState()
    val currentUser = authUiState.user
    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    val currentBranch by authViewModel.currentBranch.collectAsState()
    val branches by authViewModel.branches.collectAsState()

    // Mostrar mensaje de guardado
    LaunchedEffect(saveMessage) {
        saveMessage?.let {
            delay(3000)
            saveMessage = null
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
                            text = "Mi Perfil",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
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
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner y Logo
            item {
                BannerWithLogoSection(
                    avatarUrl = currentBusiness?.avatarUrl,
                    coverUrl = currentBusiness?.coverUrl
                )
            }

            // Mensaje de guardado
            if (saveMessage != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = saveMessage ?: "",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Información del Negocio
            item {
                BusinessInfoSection(
                    business = currentBusiness,
                    branch = currentBranch,
                    onSave = { name, description, tags ->
                        currentBusiness?.let { business ->
                            coroutineScope.launch {
                                isSaving = true
                                saveMessage = "Guardando cambios del negocio..."

                                val input = UpdateBusinessInput(
                                    name = name.takeIf { it != business.name },
                                    description = description.takeIf { it != business.description },
                                    tags = tags.takeIf { it != business.tags }
                                )

                                when (val result = businessRepository.updateBusiness(business.id, input)) {
                                    is BusinessResult.Success -> {
                                        saveMessage = "✓ Negocio actualizado correctamente"
                                    }
                                    is BusinessResult.Error -> {
                                        saveMessage = "✗ Error: ${result.message}"
                                    }
                                    else -> {}
                                }

                                isSaving = false
                            }
                        }
                    }
                )
            }

            // Información del Propietario
            item {
                UserInfoSection(
                    user = currentUser,
                    onSave = { name, phone ->
                        // TODO: Implement user update via AuthRepository
                        saveMessage = "La actualización de usuario estará disponible pronto"
                    }
                )
            }

            // Gestión de Sucursales (solo si tiene múltiples)
            if (branches.size > 1) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable { onNavigateToBranches() },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Gestionar Sucursales",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${branches.size} sucursales registradas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }

            // Información de la Sucursal
            item {
                BranchInfoSection(
                    branch = currentBranch,
                    onSave = { name, phone, address, deliveryRadius ->
                        currentBranch?.let { branch ->
                            coroutineScope.launch {
                                isSaving = true
                                saveMessage = "Guardando datos de la sucursal..."

                                val input = UpdateBranchInput(
                                    name = name.takeIf { it != branch.name },
                                    phone = phone.takeIf { it != branch.phone },
                                    address = address.takeIf { it != branch.address },
                                    deliveryRadius = deliveryRadius.takeIf { it != branch.deliveryRadius }
                                )

                                when (val result = businessRepository.updateBranch(branch.id, input)) {
                                    is BusinessResult.Success -> {
                                        saveMessage = "✓ Sucursal actualizada correctamente"
                                    }
                                    is BusinessResult.Error -> {
                                        saveMessage = "✗ Error: ${result.message}"
                                    }
                                    else -> {}
                                }

                                isSaving = false
                            }
                        }
                    }
                )
            }

            // Redes Sociales
            item {
                SocialLinksSection(socialMedia = currentBusiness?.socialMedia)
            }

            // Horarios
            item {
                BranchScheduleSection(branch = currentBranch)
            }

            // Mapa de Ubicación
            item {
                LocationMapSection(branch = currentBranch)
            }

            // Tags
            item {
                BusinessTagsSection(business = currentBusiness)
            }

            // Facilidades
            item {
                BranchFacilitiesSection(branch = currentBranch)
            }
        }

        // Diálogos
        if (showShareDialog) {
            ShareDialog(onDismiss = { showShareDialog = false })
        }

        if (showLogoutDialog) {
            LogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    authViewModel.logout()
                    showLogoutDialog = false
                }
            )
        }
    }
}
