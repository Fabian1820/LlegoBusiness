package com.llego.business.profile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.profile.ui.components.BannerWithLogoSection
import com.llego.business.profile.ui.components.BranchFacilitiesSection
import com.llego.business.profile.ui.components.BranchInfoSection
import com.llego.business.profile.ui.components.BranchScheduleSection
import com.llego.business.profile.ui.components.BusinessInfoSection
import com.llego.business.profile.ui.components.BusinessTagsSection
import com.llego.business.profile.ui.components.LocationMapSection
import com.llego.business.profile.ui.components.LogoutDialog
import com.llego.business.profile.ui.components.ShareDialog
import com.llego.business.profile.ui.components.SocialLinksSection
import com.llego.business.profile.ui.components.UserInfoSection
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.model.UpdateBusinessInput
import com.llego.shared.data.repositories.BusinessRepository
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.theme.LlegoCustomShapes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de perfil del negocio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
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
                    Text(
                        text = "Perfil del negocio",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar sesion",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner y logo
            item {
                BannerWithLogoSection(
                    avatarUrl = currentBusiness?.avatarUrl,
                    coverUrl = null // Business ya no tiene cover
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = LlegoCustomShapes.infoCard
                    ) {
                        Text(
                            text = saveMessage ?: "",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Informacion del negocio
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
                                        saveMessage = "OK: Negocio actualizado correctamente"
                                    }
                                    is BusinessResult.Error -> {
                                        saveMessage = "Error: ${result.message}"
                                    }
                                    else -> {}
                                }

                                isSaving = false
                            }
                        }
                    }
                )
            }

            // Informacion del propietario
            item {
                UserInfoSection(
                    user = currentUser,
                    onSave = { name, phone ->
                        // TODO: Implement user update via AuthRepository
                        saveMessage = "La actualizacion de usuario estara disponible pronto"
                    }
                )
            }

            // Gestion de sucursales (solo si tiene multiples)
            if (branches.size > 1) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable { onNavigateToBranches() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = LlegoCustomShapes.infoCard,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
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
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "Gestionar sucursales",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${branches.size} sucursales registradas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Informacion de la sucursal
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
                                        saveMessage = "OK: Sucursal actualizada correctamente"
                                    }
                                    is BusinessResult.Error -> {
                                        saveMessage = "Error: ${result.message}"
                                    }
                                    else -> {}
                                }

                                isSaving = false
                            }
                        }
                    }
                )
            }

            // Redes sociales
            item {
                SocialLinksSection(socialMedia = currentBusiness?.socialMedia)
            }

            // Horarios
            item {
                BranchScheduleSection(branch = currentBranch)
            }

            // Mapa de ubicacion
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

        // Dialogos
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
