package com.llego.business.profile.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
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
import com.llego.business.profile.ui.components.BusinessInfoSection
import com.llego.business.profile.ui.components.BusinessTagsSection
import com.llego.business.profile.ui.components.LogoutDialog
import com.llego.business.profile.ui.components.ShareDialog
import com.llego.business.profile.ui.components.SocialLinksSection
import com.llego.business.profile.ui.components.UserInfoSection
import com.llego.shared.data.model.AuthResult
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.ImageUploadResult
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.model.UpdateBusinessInput
import com.llego.shared.data.model.UpdateUserInput
import com.llego.shared.data.upload.ImageUploadServiceFactory
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize
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
    onNavigateBack: () -> Unit = {},
    onNavigateToBranches: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var showShareDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val imageUploadService = remember { ImageUploadServiceFactory.create() }

    var showBranchAvatarDialog by remember { mutableStateOf(false) }
    var showBranchCoverDialog by remember { mutableStateOf(false) }
    var branchAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var branchCoverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }

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

    val branchAvatarUrl = currentBranch?.avatarUrl ?: currentBusiness?.avatarUrl
    val branchCoverUrl = currentBranch?.coverUrl
    val canEditBranch = currentBranch != null

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
                    avatarUrl = branchAvatarUrl,
                    coverUrl = branchCoverUrl,
                    onChangeAvatar = {
                        if (canEditBranch) {
                            branchAvatarState = ImageUploadState.Idle
                            showBranchAvatarDialog = true
                        } else {
                            saveMessage = "Selecciona una sucursal para editar imagenes"
                        }
                    },
                    onChangeCover = if (canEditBranch) {
                        {
                            branchCoverState = ImageUploadState.Idle
                            showBranchCoverDialog = true
                        }
                    } else {
                        null
                    }
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

                                when (val result = authViewModel.updateBusiness(business.id, input)) {
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
                    onSave = { name, username, phone ->
                        currentUser?.let { user ->
                            coroutineScope.launch {
                                isSaving = true
                                saveMessage = "Guardando cambios del usuario..."

                                val normalizedUsername = username.trim().removePrefix("@")
                                val input = UpdateUserInput(
                                    name = name.takeIf { it != user.name },
                                    username = normalizedUsername.takeIf {
                                        it.isNotBlank() && it != user.username
                                    },
                                    phone = phone.takeIf { it != user.phone }
                                )

                                when (val result = authViewModel.updateUser(input)) {
                                    is AuthResult.Success -> {
                                        saveMessage = "OK: Usuario actualizado correctamente"
                                    }
                                    is AuthResult.Error -> {
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
                SocialLinksSection(
                    socialMedia = currentBusiness?.socialMedia,
                    onSave = { socialMedia ->
                        currentBusiness?.let { business ->
                            coroutineScope.launch {
                                isSaving = true
                                saveMessage = "Guardando redes sociales..."

                                val input = UpdateBusinessInput(
                                    socialMedia = socialMedia.takeIf { it != business.socialMedia }
                                )

                                when (val result = authViewModel.updateBusiness(business.id, input)) {
                                    is BusinessResult.Success -> {
                                        saveMessage = "OK: Redes sociales actualizadas"
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

            // Tags
            item {
                BusinessTagsSection(
                    business = currentBusiness,
                    onSave = { tags ->
                        currentBusiness?.let { business ->
                            coroutineScope.launch {
                                isSaving = true
                                saveMessage = "Guardando etiquetas..."

                                val input = UpdateBusinessInput(
                                    tags = tags.takeIf { it != business.tags }
                                )

                                when (val result = authViewModel.updateBusiness(business.id, input)) {
                                    is BusinessResult.Success -> {
                                        saveMessage = "OK: Etiquetas actualizadas"
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

            // Cerrar sesion
            item {
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    ),
                    shape = LlegoCustomShapes.secondaryButton
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar sesion"
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Cerrar sesion",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        if (showBranchAvatarDialog) {
            ImageUploadDialog(
                title = "Avatar de la sucursal",
                label = "Avatar",
                uploadState = branchAvatarState,
                onStateChange = { state ->
                    branchAvatarState = state
                    if (state is ImageUploadState.Success) {
                        currentBranch?.let { branch ->
                            coroutineScope.launch {
                                isSaving = true
                                saveMessage = "Guardando avatar de la sucursal..."

                                when (val result = authViewModel.updateBranch(
                                    branch.id,
                                    UpdateBranchInput(avatar = state.s3Path)
                                )) {
                                    is BusinessResult.Success -> {
                                        saveMessage = "OK: Avatar de sucursal actualizado"
                                    }
                                    is BusinessResult.Error -> {
                                        saveMessage = "Error: ${result.message}"
                                    }
                                    else -> {}
                                }

                                isSaving = false
                                showBranchAvatarDialog = false
                                branchAvatarState = ImageUploadState.Idle
                            }
                        }
                    }
                },
                uploadFunction = { uri, token -> imageUploadService.uploadBranchAvatar(uri, token) },
                onDismiss = {
                    showBranchAvatarDialog = false
                    branchAvatarState = ImageUploadState.Idle
                }
            )
        }

        if (showBranchCoverDialog) {
            ImageUploadDialog(
                title = "Portada de la sucursal",
                label = "Portada",
                uploadState = branchCoverState,
                size = ImageUploadSize.LARGE,
                onStateChange = { state ->
                    branchCoverState = state
                    if (state is ImageUploadState.Success) {
                        currentBranch?.let { branch ->
                            coroutineScope.launch {
                                isSaving = true
                                saveMessage = "Guardando portada de la sucursal..."

                                when (val result = authViewModel.updateBranch(
                                    branch.id,
                                    UpdateBranchInput(coverImage = state.s3Path)
                                )) {
                                    is BusinessResult.Success -> {
                                        saveMessage = "OK: Portada de sucursal actualizada"
                                    }
                                    is BusinessResult.Error -> {
                                        saveMessage = "Error: ${result.message}"
                                    }
                                    else -> {}
                                }

                                isSaving = false
                                showBranchCoverDialog = false
                                branchCoverState = ImageUploadState.Idle
                            }
                        }
                    }
                },
                uploadFunction = { uri, token -> imageUploadService.uploadBranchCover(uri, token) },
                onDismiss = {
                    showBranchCoverDialog = false
                    branchCoverState = ImageUploadState.Idle
                }
            )
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

@Composable
private fun ImageUploadDialog(
    title: String,
    label: String,
    uploadState: ImageUploadState,
    onStateChange: (ImageUploadState) -> Unit,
    uploadFunction: suspend (filePath: String, token: String?) -> ImageUploadResult,
    onDismiss: () -> Unit,
    size: ImageUploadSize = ImageUploadSize.MEDIUM
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            ImageUploadPreview(
                label = label,
                uploadState = uploadState,
                onStateChange = onStateChange,
                uploadFunction = uploadFunction,
                size = size,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
