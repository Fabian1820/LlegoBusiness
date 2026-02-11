package com.llego.shared.ui.branch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.model.UpdateBusinessInput
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize
import com.llego.shared.ui.components.molecules.TagsSelector
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.upload.ImageUploadViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessEditScreen(
    business: Business,
    branches: List<Branch>,
    onNavigateBack: () -> Unit,
    onBusinessUpdated: (Business) -> Unit,
    onDataChanged: () -> Unit,
    onError: (String) -> Unit,
    authViewModel: AuthViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val imageUploadViewModel = remember { ImageUploadViewModel() }

    var originalBusiness by remember(business.id) { mutableStateOf(business) }
    var name by remember(business.id) { mutableStateOf(business.name) }
    var description by remember(business.id) { mutableStateOf(business.description.orEmpty()) }
    var selectedTags by remember(business.id) { mutableStateOf(business.tags) }
    var isActive by remember(business.id) { mutableStateOf(business.isActive) }
    var avatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }

    var localBranches by remember(business.id, branches) {
        mutableStateOf(branches.sortedBy { it.name.lowercase() })
    }
    var branchIdsInProgress by remember { mutableStateOf(setOf<String>()) }
    var branchPendingDeactivate by remember { mutableStateOf<Branch?>(null) }
    var branchPendingDelete by remember { mutableStateOf<Branch?>(null) }
    var businessPendingDelete by remember { mutableStateOf(false) }
    var isDeletingBusiness by remember { mutableStateOf(false) }

    var isSavingBusiness by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val avatarPath = (avatarState as? ImageUploadState.Success)?.s3Path
    val isUploadingAvatar = avatarState is ImageUploadState.Uploading

    fun saveBusiness() {
        val nameValue = name.trim()
        val descriptionValue = description.trim()
        val tagsValue = selectedTags

        if (nameValue.isBlank()) {
            statusMessage = "El nombre del negocio es obligatorio"
            onError(statusMessage ?: "")
            return
        }
        if (isUploadingAvatar) {
            statusMessage = "Espera a que termine la subida del avatar"
            onError(statusMessage ?: "")
            return
        }

        val input = UpdateBusinessInput(
            name = nameValue.takeIf { it != originalBusiness.name },
            description = descriptionValue.takeIf { it != originalBusiness.description.orEmpty() },
            tags = tagsValue.takeIf { it != originalBusiness.tags },
            isActive = isActive.takeIf { it != originalBusiness.isActive },
            avatar = avatarPath
        )

        if (input.name == null &&
            input.description == null &&
            input.tags == null &&
            input.isActive == null &&
            input.avatar == null
        ) {
            statusMessage = "No hay cambios para guardar"
            return
        }

        coroutineScope.launch {
            isSavingBusiness = true
            when (val result = authViewModel.updateBusiness(originalBusiness.id, input)) {
                is BusinessResult.Success -> {
                    val updated = result.data
                    originalBusiness = updated
                    name = updated.name
                    description = updated.description.orEmpty()
                    selectedTags = updated.tags
                    isActive = updated.isActive
                    avatarState = ImageUploadState.Idle
                    statusMessage = "Negocio actualizado correctamente"
                    onBusinessUpdated(updated)
                    onDataChanged()
                }

                is BusinessResult.Error -> {
                    statusMessage = result.message
                    onError(result.message)
                }

                else -> {}
            }
            isSavingBusiness = false
        }
    }

    fun setBranchInProgress(branchId: String, inProgress: Boolean) {
        branchIdsInProgress = if (inProgress) {
            branchIdsInProgress + branchId
        } else {
            branchIdsInProgress - branchId
        }
    }

    fun deactivateBranch(target: Branch) {
        if (!target.isActive || target.id in branchIdsInProgress) return
        coroutineScope.launch {
            setBranchInProgress(target.id, true)
            when (val result = authViewModel.updateBranch(target.id, UpdateBranchInput(isActive = false))) {
                is BusinessResult.Success -> {
                    val updatedBranch = result.data
                    localBranches = localBranches.map { if (it.id == updatedBranch.id) updatedBranch else it }
                    statusMessage = "Sucursal desactivada"
                    onDataChanged()
                }

                is BusinessResult.Error -> {
                    statusMessage = result.message
                    onError(result.message)
                }

                else -> {}
            }
            setBranchInProgress(target.id, false)
        }
    }

    fun deleteBranch(target: Branch) {
        if (target.id in branchIdsInProgress) return
        coroutineScope.launch {
            setBranchInProgress(target.id, true)
            when (val result = authViewModel.deleteBranch(target.id)) {
                is BusinessResult.Success -> {
                    if (result.data) {
                        localBranches = localBranches.filterNot { it.id == target.id }
                        if (authViewModel.getCurrentBranchId() == target.id) {
                            authViewModel.clearCurrentBranch()
                        }
                        statusMessage = "Sucursal eliminada"
                        onDataChanged()
                    } else {
                        statusMessage = "No se pudo eliminar la sucursal"
                        onError(statusMessage ?: "")
                    }
                }

                is BusinessResult.Error -> {
                    statusMessage = result.message
                    onError(result.message)
                }

                else -> {}
            }
            setBranchInProgress(target.id, false)
        }
    }

    fun deleteBusinessCompletely() {
        if (isDeletingBusiness) return

        coroutineScope.launch {
            isDeletingBusiness = true
            statusMessage = "Eliminando sucursales del negocio..."

            val branchesSnapshot = localBranches
            for (branch in branchesSnapshot) {
                when (val branchResult = authViewModel.deleteBranch(branch.id)) {
                    is BusinessResult.Success -> {
                        if (!branchResult.data) {
                            statusMessage = "No se pudo eliminar la sucursal ${branch.name}"
                            onError(statusMessage ?: "")
                            isDeletingBusiness = false
                            return@launch
                        }
                    }

                    is BusinessResult.Error -> {
                        statusMessage = branchResult.message
                        onError(branchResult.message)
                        isDeletingBusiness = false
                        return@launch
                    }

                    else -> Unit
                }
            }

            statusMessage = "Desactivando negocio..."
            when (val businessResult = authViewModel.updateBusiness(
                businessId = originalBusiness.id,
                input = UpdateBusinessInput(isActive = false)
            )) {
                is BusinessResult.Success -> {
                    statusMessage = "Negocio eliminado: sucursales borradas y negocio desactivado."
                    if (authViewModel.getCurrentBusinessId() == originalBusiness.id) {
                        authViewModel.clearCurrentBranch()
                    }
                    onDataChanged()
                    onNavigateBack()
                }

                is BusinessResult.Error -> {
                    statusMessage = businessResult.message
                    onError(businessResult.message)
                }

                else -> Unit
            }

            isDeletingBusiness = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar negocio",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { saveBusiness() },
                        enabled = !isSavingBusiness && !isUploadingAvatar
                    ) {
                        if (isSavingBusiness) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Guardar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            statusMessage?.let { message ->
                Card(
                    shape = LlegoCustomShapes.infoCard,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Datos del negocio",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BusinessAvatar(
                    avatarUrl = originalBusiness.avatarUrl,
                    name = name.ifBlank { originalBusiness.name },
                    size = 56
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Calificacion global: ${originalBusiness.globalRating}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                }
            }

            ImageUploadPreview(
                label = "Avatar del negocio",
                uploadState = avatarState,
                onStateChange = { avatarState = it },
                uploadFunction = imageUploadViewModel::uploadBusinessAvatar,
                size = ImageUploadSize.MEDIUM,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del negocio *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripcion") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = LlegoCustomShapes.inputField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            TagsSelector(
                selectedTags = selectedTags,
                onTagsChange = { selectedTags = it },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Negocio activo",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = if (isActive) "Visible para clientes" else "Negocio pausado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sucursales",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Gestiona el estado o elimina sucursales de este negocio",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (localBranches.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = LlegoCustomShapes.infoCard,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Este negocio no tiene sucursales registradas",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                localBranches.forEach { branch ->
                    val inProgress = branch.id in branchIdsInProgress
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = LlegoCustomShapes.infoCard,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = branch.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (branch.isActive) "Activa" else "Inactiva",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (branch.isActive) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                            }

                            IconButton(
                                onClick = { branchPendingDeactivate = branch },
                                enabled = branch.isActive && !inProgress
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = "Desactivar sucursal",
                                    tint = if (branch.isActive) Color(0xFFE65100) else MaterialTheme.colorScheme.outline
                                )
                            }

                            IconButton(
                                onClick = { branchPendingDelete = branch },
                                enabled = !inProgress
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar sucursal",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Zona de peligro",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Esta accion elimina todas las sucursales y desactiva definitivamente este negocio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { businessPendingDelete = true },
                enabled = !isDeletingBusiness,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = LlegoCustomShapes.secondaryButton
            ) {
                if (isDeletingBusiness) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Eliminar negocio completo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    branchPendingDeactivate?.let { target ->
        AlertDialog(
            onDismissRequest = { branchPendingDeactivate = null },
            title = { Text("Desactivar sucursal") },
            text = { Text("Se desactivara la sucursal \"${target.name}\". Deseas continuar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        branchPendingDeactivate = null
                        deactivateBranch(target)
                    }
                ) {
                    Text("Desactivar", color = Color(0xFFE65100))
                }
            },
            dismissButton = {
                TextButton(onClick = { branchPendingDeactivate = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    branchPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { branchPendingDelete = null },
            title = { Text("Eliminar sucursal") },
            text = { Text("Esta accion no se puede deshacer. Eliminar \"${target.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        branchPendingDelete = null
                        deleteBranch(target)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { branchPendingDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (businessPendingDelete) {
        AlertDialog(
            onDismissRequest = { businessPendingDelete = false },
            title = { Text("Eliminar negocio completo") },
            text = {
                Text(
                    "Se eliminaran todas las sucursales de \"${originalBusiness.name}\" y el negocio quedara desactivado. Esta accion no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        businessPendingDelete = false
                        deleteBusinessCompletely()
                    }
                ) {
                    Text("Eliminar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { businessPendingDelete = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
