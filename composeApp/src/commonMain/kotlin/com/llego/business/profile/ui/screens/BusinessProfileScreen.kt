package com.llego.business.profile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.profile.ui.components.BannerWithLogoSection
import com.llego.business.profile.ui.components.BusinessInfoSection
import com.llego.business.profile.ui.components.BusinessTagsSection
import com.llego.business.profile.ui.components.ImageUploadDialog
import com.llego.business.profile.ui.components.InvitationsCard
import com.llego.business.profile.ui.components.ProfileSaveMessageCard
import com.llego.business.profile.ui.components.ShareDialog
import com.llego.business.profile.ui.components.SocialLinksSection
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.model.UpdateBusinessInput
import com.llego.shared.ui.components.molecules.ImageUploadSize
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.upload.ImageUploadViewModel
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
    onNavigateToInvitations: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var showShareDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val imageUploadViewModel = remember { ImageUploadViewModel() }

    var showBranchAvatarDialog by remember { mutableStateOf(false) }
    var showBranchCoverDialog by remember { mutableStateOf(false) }
    var branchAvatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var branchCoverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }

    // Datos del negocio y sucursal
    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    val currentBranch by authViewModel.currentBranch.collectAsState()

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
                    ProfileSaveMessageCard(
                        message = saveMessage ?: "",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
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

            // Codigos de invitacion (siempre visible)
            item {
                InvitationsCard(
                    onClick = onNavigateToInvitations,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
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
                uploadFunction = imageUploadViewModel::uploadBranchAvatar,
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
                uploadFunction = imageUploadViewModel::uploadBranchCover,
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

    }
}
