package com.llego.business.branches.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.business.branches.ui.viewmodel.BranchesManagementViewModel
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.BusinessWithBranches
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CoordinatesInput
import com.llego.shared.data.model.CreateBranchInput
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.components.molecules.DaySchedule
import com.llego.shared.ui.components.molecules.FacilitiesSelector
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize
import com.llego.shared.ui.components.molecules.PaymentMethodSelector
import com.llego.shared.ui.components.molecules.SchedulePicker
import com.llego.shared.ui.components.molecules.TimeRange
import com.llego.shared.ui.components.molecules.toBackendSchedule
import com.llego.shared.ui.components.molecules.toDaySchedule
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import com.llego.shared.ui.components.molecules.MapLocationPickerReal
import com.llego.business.shared.ui.components.BusinessLocationMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de gestion de sucursales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchesManagementScreen(
    authViewModel: AuthViewModel,
    branchesManagementViewModel: BranchesManagementViewModel,
    onNavigateBack: () -> Unit = {},
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    val branchesManagementState by branchesManagementViewModel.uiState.collectAsState()

    // Estado
    val branches by authViewModel.branches.collectAsState()
    val currentBranch by authViewModel.currentBranch.collectAsState()
    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val currentUserId = authUiState.user?.id

    var createBranchBusinessId by remember { mutableStateOf<String?>(null) }
    var selectedBranchId by remember { mutableStateOf<String?>(null) }
    var editingBranchId by remember { mutableStateOf<String?>(null) }
    var branchToDelete by remember { mutableStateOf<Branch?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val fallbackBusinesses = remember(currentBusiness, branches, branchesManagementState.businessesWithBranches) {
        if (branchesManagementState.businessesWithBranches.isNotEmpty()) {
            branchesManagementState.businessesWithBranches
        } else if (currentBusiness != null && branches.isNotEmpty()) {
            listOf(
                BusinessWithBranches(
                    id = currentBusiness!!.id,
                    name = currentBusiness!!.name,
                    ownerId = currentBusiness!!.ownerId,
                    globalRating = currentBusiness!!.globalRating,
                    avatar = currentBusiness!!.avatar,
                    description = currentBusiness!!.description,
                    socialMedia = currentBusiness!!.socialMedia,
                    tags = currentBusiness!!.tags,
                    isActive = currentBusiness!!.isActive,
                    createdAt = currentBusiness!!.createdAt,
                    avatarUrl = currentBusiness!!.avatarUrl,
                    branches = branches
                )
            )
        } else {
            emptyList()
        }
    }

    val allBranches = remember(fallbackBusinesses, branches) {
        if (fallbackBusinesses.isNotEmpty()) {
            fallbackBusinesses.flatMap { it.branches }
        } else {
            branches
        }
    }

    val orphanBranches = remember(branches, fallbackBusinesses) {
        val knownIds = fallbackBusinesses.flatMap { it.branches }.map { it.id }.toSet()
        branches.filter { it.id !in knownIds }
    }

    val selectedBranch = allBranches.firstOrNull { it.id == selectedBranchId }
    val editingBranch = allBranches.firstOrNull { it.id == editingBranchId }
    val showListScreen = createBranchBusinessId == null && editingBranch == null && selectedBranch == null

    fun reloadBusinesses() {
        branchesManagementViewModel.loadBusinesses()
    }

    LaunchedEffect(Unit) {
        reloadBusinesses()
    }

    // Mostrar mensaje temporal
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            delay(3000)
            statusMessage = null
        }
    }

    when {
        createBranchBusinessId != null -> {
            BranchCreateScreen(
                businessId = createBranchBusinessId.orEmpty(),
                onNavigateBack = { createBranchBusinessId = null },
                onSuccess = { newBranch ->
                    createBranchBusinessId = null
                    statusMessage = "OK: Sucursal '${newBranch.name}' agregada correctamente"
                    reloadBusinesses()
                },
                onError = { error -> statusMessage = "Error: $error" },
                authViewModel = authViewModel,
                onOpenMapSelection = onOpenMapSelection
            )
        }
        editingBranch != null -> {
            BranchEditScreen(
                branch = editingBranch,
                onNavigateBack = { editingBranchId = null },
                onSuccess = { updatedBranch ->
                    editingBranchId = null
                    selectedBranchId = updatedBranch.id
                    statusMessage = "OK: Sucursal '${updatedBranch.name}' actualizada"
                    reloadBusinesses()
                },
                onError = { error -> statusMessage = "Error: $error" },
                authViewModel = authViewModel
            )
        }
        selectedBranch != null -> {
            BranchDetailScreen(
                branch = selectedBranch,
                isActive = selectedBranch.id == currentBranch?.id,
                onNavigateBack = { selectedBranchId = null },
                onEdit = { editingBranchId = selectedBranch.id },
                onSetActive = {
                    authViewModel.setCurrentBranch(selectedBranch)
                    statusMessage = "OK: Sucursal '${selectedBranch.name}' seleccionada como activa"
                }
            )
        }
        else -> {
            BranchesListScreen(
                businessesWithBranches = fallbackBusinesses,
                orphanBranches = orphanBranches,
                currentBranchId = currentBranch?.id,
                statusMessage = statusMessage,
                isLoadingBusinesses = branchesManagementState.isLoading,
                errorMessage = branchesManagementState.error,
                onNavigateBack = onNavigateBack,
                onAddBranch = { businessId -> createBranchBusinessId = businessId },
                onOpenDetails = { selectedBranchId = it.id },
                onSetActive = { branch ->
                    coroutineScope.launch {
                        authViewModel.setCurrentBranch(branch)
                        statusMessage = "OK: Sucursal '${branch.name}' seleccionada como activa"
                    }
                },
                onEdit = { editingBranchId = it.id },
                onDelete = { branch ->
                    if (allBranches.size > 1) {
                        branchToDelete = branch
                    } else {
                        statusMessage = "Error: No puedes eliminar la ultima sucursal"
                    }
                },
                onLocationUpdate = { branch, lat, lng ->
                    coroutineScope.launch {
                        val input = UpdateBranchInput(
                            coordinates = CoordinatesInput(lat = lat, lng = lng)
                        )
                        when (val result = authViewModel.updateBranch(branch.id, input)) {
                            is BusinessResult.Success -> {
                                statusMessage = "OK: Ubicacion de '${branch.name}' actualizada"
                                reloadBusinesses()
                            }
                            is BusinessResult.Error -> {
                                statusMessage = "Error: ${result.message}"
                            }
                            else -> {}
                        }
                    }
                },
                onOpenMapSelection = onOpenMapSelection,
                currentUserId = currentUserId
            )
        }
    }

    if (showListScreen && branchToDelete != null) {
        AlertDialog(
            onDismissRequest = { branchToDelete = null },
            title = { Text("Eliminar sucursal") },
            text = {
                Text(
                    "Estas seguro de que deseas eliminar la sucursal '${branchToDelete?.name}'? " +
                        "Esta accion no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val branch = branchToDelete ?: return@TextButton
                        coroutineScope.launch {
                            when (val result = authViewModel.deleteBranch(branch.id)) {
                                is BusinessResult.Success -> {
                                    statusMessage = "OK: Sucursal '${branch.name}' eliminada"
                                    reloadBusinesses()
                                }
                                is BusinessResult.Error -> {
                                    statusMessage = "Error: ${result.message}"
                                }
                                else -> {}
                            }
                            branchToDelete = null
                        }
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { branchToDelete = null }) {
                    Text("Cancelar")
                }
            },
            shape = LlegoCustomShapes.infoCard,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

