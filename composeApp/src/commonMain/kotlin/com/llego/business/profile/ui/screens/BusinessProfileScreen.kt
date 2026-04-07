package com.llego.business.profile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.app.PlatformBackHandler
import com.llego.business.shared.ui.components.rememberImagePickerController
import com.llego.business.branches.ui.components.BranchVehiclesSelector
import com.llego.business.branches.util.parseExchangeRate
import com.llego.business.branches.util.validateExchangeRateInput
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.business.profile.ui.components.BannerWithLogoSection
import com.llego.business.profile.ui.components.BranchInfoSection
import com.llego.business.profile.ui.components.BranchScheduleSection
import com.llego.business.profile.ui.components.LocationMapSection
import com.llego.business.profile.ui.components.ProfileSaveMessageCard
import com.llego.business.profile.ui.components.ProfileSectionCard
import com.llego.business.profile.ui.components.SectionHeader
import com.llego.business.profile.ui.components.SocialLinksSection
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CoordinatesInput
import com.llego.shared.data.model.ImageUploadResult
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.TransferAccount
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.model.VariantList
import com.llego.shared.data.model.VariantOptionDraft
import com.llego.shared.data.model.avatarLargeUrl
import com.llego.shared.data.model.coverBestUrl
import com.llego.shared.data.model.extractFilename
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.business.TransferAccountsEditor
import com.llego.shared.ui.business.findInvalidTransferAccountItems
import com.llego.shared.ui.business.normalizeTransferAccountsInput
import com.llego.shared.ui.components.molecules.PaymentMethodSelector
import com.llego.shared.ui.components.molecules.PaymentMethodSelectorLayout
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.upload.ImageUploadViewModel
import com.llego.shared.ui.payment.PaymentMethodsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class VariantOptionEditorState(
    val id: String? = null,
    val name: String = "",
    val priceAdjustment: String = "0"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    onNavigateBack: () -> Unit = {},
    onOpenMapSelection: ((String, Double, Double, (Double, Double) -> Unit) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val currentBranch by authViewModel.currentBranch.collectAsState()
    val imageUploadViewModel = remember { ImageUploadViewModel() }
    val imagePickerController = rememberImagePickerController()
    val paymentMethodsViewModel = remember { PaymentMethodsViewModel() }
    val paymentMethodsUiState by paymentMethodsViewModel.uiState.collectAsState()
    val variantListsUiState by productViewModel.variantListsState.collectAsState()

    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var variantMessage by remember { mutableStateOf<String?>(null) }
    var avatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var coverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }

    var name by remember(currentBranch?.id) { mutableStateOf(currentBranch?.name.orEmpty()) }
    var phone by remember(currentBranch?.id) { mutableStateOf(currentBranch?.phone.orEmpty()) }
    var address by remember(currentBranch?.id) { mutableStateOf(currentBranch?.address.orEmpty()) }
    var latitude by remember(currentBranch?.id) { mutableStateOf(currentBranch?.coordinates?.latitude ?: 0.0) }
    var longitude by remember(currentBranch?.id) { mutableStateOf(currentBranch?.coordinates?.longitude ?: 0.0) }
    var branchSchedule by remember(currentBranch?.id) { mutableStateOf(currentBranch?.schedule ?: emptyMap()) }
    var selectedTipos by remember(currentBranch?.id) { mutableStateOf(currentBranch?.tipos?.toSet() ?: emptySet()) }
    var selectedPaymentMethodIds by remember(currentBranch?.id) {
        mutableStateOf(
            currentBranch?.paymentMethodIds ?: emptyList()
        )
    }
    var useAppMessaging by remember(currentBranch?.id) { mutableStateOf(currentBranch?.useAppMessaging ?: true) }
    var selectedVehicles by remember(currentBranch?.id) {
        mutableStateOf(
            currentBranch?.vehicles?.toSet() ?: emptySet()
        )
    }
    var socialMedia by remember(currentBranch?.id) { mutableStateOf(currentBranch?.socialMedia ?: emptyMap()) }
    var transferAccounts by remember(currentBranch?.id) { mutableStateOf(currentBranch?.accounts ?: emptyList()) }
    var isActive by remember(currentBranch?.id) { mutableStateOf(currentBranch?.isActive ?: true) }
    var exchangeRateInput by remember(currentBranch?.id) {
        mutableStateOf(currentBranch?.exchangeRate?.toString().orEmpty())
    }

    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    var showVariantEditor by remember { mutableStateOf(false) }
    var editingVariantList by remember { mutableStateOf<VariantList?>(null) }
    var variantName by remember { mutableStateOf("") }
    var variantDescription by remember { mutableStateOf("") }
    var variantOptions by remember { mutableStateOf(listOf(VariantOptionEditorState())) }
    var variantOperationLoading by remember { mutableStateOf(false) }

    LaunchedEffect(currentBranch?.id) {
        if (currentBranch != null) {
            paymentMethodsViewModel.loadPaymentMethods()
            productViewModel.loadVariantLists(currentBranch!!.id, force = true)
        }
    }

    LaunchedEffect(saveMessage) {
        if (!saveMessage.isNullOrBlank()) {
            delay(3000)
            saveMessage = null
        }
    }

    LaunchedEffect(variantMessage, showVariantEditor) {
        if (!variantMessage.isNullOrBlank() && !showVariantEditor) {
            delay(3000)
            if (!showVariantEditor) {
                variantMessage = null
            }
        }
    }

    val avatarPath = (avatarState as? ImageUploadState.Success)?.s3Path
    val coverPath = (coverState as? ImageUploadState.Success)?.s3Path
    val isUploading = avatarState is ImageUploadState.Uploading || coverState is ImageUploadState.Uploading
    val coverPreviewUrl = when (val state = coverState) {
        is ImageUploadState.Selected -> state.localUri
        is ImageUploadState.Uploading -> state.localUri
        is ImageUploadState.Success -> state.localUri
        is ImageUploadState.Error -> state.localUri
        ImageUploadState.Idle -> null
    }
    val previewExchangeRate = parseExchangeRate(exchangeRateInput) ?: currentBranch?.exchangeRate

    val branchPreview = currentBranch?.copy(
        name = name,
        phone = phone,
        address = address.ifBlank { null },
        schedule = branchSchedule,
        socialMedia = socialMedia.ifEmpty { null },
        tipos = selectedTipos.toList(),
        useAppMessaging = useAppMessaging,
        vehicles = selectedVehicles.toList(),
        paymentMethodIds = selectedPaymentMethodIds,
        isActive = isActive,
        exchangeRate = previewExchangeRate
    )

    fun getUnsavedChanges(): List<String> {
        val branch = currentBranch ?: return emptyList()
        val changes = mutableListOf<String>()
        if (name.trim() != branch.name) changes.add("Nombre")
        if (phone.trim() != branch.phone) changes.add("Teléfono")
        if (address.trim() != (branch.address ?: "")) changes.add("Dirección")
        if (latitude != branch.coordinates.latitude || longitude != branch.coordinates.longitude) changes.add("Ubicación")
        if (branchSchedule != branch.schedule) changes.add("Horario")
        if (selectedTipos != branch.tipos.toSet()) changes.add("Tipos de servicio")
        if (selectedPaymentMethodIds.toSet() != branch.paymentMethodIds.toSet()) changes.add("Métodos de pago")
        if (useAppMessaging != branch.useAppMessaging) changes.add("Mensajería de la app")
        if (selectedVehicles != branch.vehicles.toSet()) changes.add("Vehículos de delivery")
        if (isActive != branch.isActive) changes.add("Estado de sucursal")
        val currentSocial = socialMedia.mapValues { it.value.trim() }.filterValues { it.isNotEmpty() }
        val branchSocial = (branch.socialMedia ?: emptyMap()).mapValues { it.value.trim() }.filterValues { it.isNotEmpty() }
        if (currentSocial != branchSocial) changes.add("Redes sociales")
        if (normalizeTransferAccountsInput(transferAccounts, branch.accounts) != branch.accounts) {
            changes.add("Cuentas para transferencias")
        }
        if (parseExchangeRate(exchangeRateInput) != branch.exchangeRate) changes.add("Tasa de cambio")
        if (avatarPath != null) changes.add("Avatar")
        if (coverPath != null) changes.add("Portada")
        return changes
    }

    val handleBack: () -> Unit = {
        val changes = getUnsavedChanges()
        if (changes.isEmpty()) {
            onNavigateBack()
        } else {
            showUnsavedChangesDialog = true
        }
    }

    PlatformBackHandler(
        enabled = !showVariantEditor &&
            !showUnsavedChangesDialog
    ) {
        handleBack()
    }

    fun pickAndUploadImage(
        isCover: Boolean,
        uploadFunction: suspend (String) -> ImageUploadResult
    ) {
        imagePickerController.pickImage { selectedUri ->
            val filename = selectedUri.extractFilename()

            if (isCover) {
                coverState = ImageUploadState.Selected(selectedUri, filename)
            } else {
                avatarState = ImageUploadState.Selected(selectedUri, filename)
            }

            coroutineScope.launch {
                if (isCover) {
                    coverState = ImageUploadState.Uploading(selectedUri, filename)
                } else {
                    avatarState = ImageUploadState.Uploading(selectedUri, filename)
                }

                try {
                    when (val result = uploadFunction(selectedUri)) {
                        is ImageUploadResult.Success -> {
                            val successState = ImageUploadState.Success(
                                localUri = selectedUri,
                                s3Path = result.response.imagePath,
                                filename = filename
                            )
                            if (isCover) {
                                coverState = successState
                                saveMessage = "Portada lista. Guarda para aplicar cambios."
                            } else {
                                avatarState = successState
                                saveMessage = "Avatar listo. Guarda para aplicar cambios."
                            }
                        }

                        is ImageUploadResult.Error -> {
                            val errorState = ImageUploadState.Error(
                                localUri = selectedUri,
                                message = result.message,
                                filename = filename
                            )
                            if (isCover) {
                                coverState = errorState
                            } else {
                                avatarState = errorState
                            }
                            saveMessage = result.message
                        }

                        is ImageUploadResult.Loading -> Unit
                    }
                } catch (e: Exception) {
                    val message = e.message ?: "No se pudo subir la imagen"
                    val errorState = ImageUploadState.Error(
                        localUri = selectedUri,
                        message = message,
                        filename = filename
                    )
                    if (isCover) {
                        coverState = errorState
                    } else {
                        avatarState = errorState
                    }
                    saveMessage = message
                }
            }
        }
    }

    fun saveBranchChanges(navigateBackOnSuccess: Boolean = false) {
        val branch = currentBranch ?: return
        if (isSaving) return

        if (name.isBlank() || phone.isBlank()) {
            saveMessage = "Completa nombre y telefono"
            return
        }
        if (selectedTipos.isEmpty()) {
            saveMessage = "Selecciona al menos un tipo de sucursal"
            return
        }
        if (selectedPaymentMethodIds.isEmpty()) {
            saveMessage = "Selecciona al menos un metodo de pago"
            return
        }
        if (!useAppMessaging && selectedVehicles.isEmpty()) {
            saveMessage = "Si usas delivery propio debes seleccionar vehiculos"
            return
        }
        if (isUploading) {
            saveMessage = "Espera a que terminen las subidas de imagen"
            return
        }
        validateExchangeRateInput(exchangeRateInput)?.let { error ->
            saveMessage = error
            return
        }
        val invalidAccounts = findInvalidTransferAccountItems(transferAccounts)
        if (invalidAccounts.isNotEmpty()) {
            saveMessage = "Revisa las cuentas #${invalidAccounts.joinToString(", ")}: tarjeta (16 dígitos) y teléfono de confirmación (8 dígitos) son obligatorios."
            return
        }

        val socialMediaValue = socialMedia
            .mapValues { it.value.trim() }
            .filterValues { it.isNotEmpty() }

        val accountsValue = normalizeTransferAccountsInput(transferAccounts, branch.accounts)
        val exchangeRateValue = parseExchangeRate(exchangeRateInput)

        val input = UpdateBranchInput(
            name = name.trim().takeIf { it != branch.name },
            phone = phone.trim().takeIf { it != branch.phone },
            address = address.trim().takeIf { it != branch.address.orEmpty() },
            coordinates = if (
                latitude != branch.coordinates.latitude ||
                longitude != branch.coordinates.longitude
            ) {
                CoordinatesInput(lat = latitude, lng = longitude)
            } else {
                null
            },
            schedule = branchSchedule.takeIf { it != branch.schedule },
            tipos = selectedTipos.toList().takeIf { it.toSet() != branch.tipos.toSet() },
            paymentMethodIds = selectedPaymentMethodIds.takeIf { it.toSet() != branch.paymentMethodIds.toSet() },
            useAppMessaging = useAppMessaging.takeIf { it != branch.useAppMessaging },
            vehicles = selectedVehicles.toList().takeIf { it.toSet() != branch.vehicles.toSet() },
            isActive = isActive.takeIf { it != branch.isActive },
            socialMedia = socialMediaValue.takeIf { it != (branch.socialMedia ?: emptyMap<String, String>()) },
            accounts = accountsValue.takeIf { it != branch.accounts },
            exchangeRate = exchangeRateValue.takeIf { it != branch.exchangeRate },
            avatar = avatarPath,
            coverImage = coverPath
        )

        coroutineScope.launch {
            isSaving = true
            when (val result = authViewModel.updateBranch(branch.id, input)) {
                is BusinessResult.Success -> {
                    val updated = result.data
                    name = updated.name
                    phone = updated.phone
                    address = updated.address.orEmpty()
                    latitude = updated.coordinates.latitude
                    longitude = updated.coordinates.longitude
                    branchSchedule = updated.schedule
                    selectedTipos = updated.tipos.toSet()
                    selectedPaymentMethodIds = updated.paymentMethodIds
                    useAppMessaging = updated.useAppMessaging
                    selectedVehicles = updated.vehicles.toSet()
                    socialMedia = updated.socialMedia ?: emptyMap()
                    transferAccounts = updated.accounts
                    isActive = updated.isActive
                    exchangeRateInput = updated.exchangeRate?.toString().orEmpty()
                    authViewModel.setCurrentBranch(updated)
                    authViewModel.reloadUserData()
                    saveMessage = "Perfil de sucursal actualizado"
                    avatarState = ImageUploadState.Idle
                    coverState = ImageUploadState.Idle
                    if (navigateBackOnSuccess) {
                        onNavigateBack()
                    }
                }

                is BusinessResult.Error -> {
                    saveMessage = result.message
                }

                else -> {}
            }
            isSaving = false
        }
    }

    fun openVariantEditor(variantList: VariantList?) {
        variantMessage = null
        editingVariantList = variantList
        variantName = variantList?.name.orEmpty()
        variantDescription = variantList?.description.orEmpty()
        variantOptions = if (variantList != null) {
            variantList.options.map {
                VariantOptionEditorState(
                    id = it.id,
                    name = it.name,
                    priceAdjustment = it.priceAdjustment.toString()
                )
            }
        } else {
            listOf(VariantOptionEditorState())
        }
        showVariantEditor = true
    }

    fun validateVariantEditor(): String? {
        if (variantName.isBlank()) return "El nombre de la lista es obligatorio."
        if (variantOptions.isEmpty()) return "Debes agregar al menos una opcion."
        if (variantOptions.any { it.name.isBlank() || it.priceAdjustment.toDoubleOrNull() == null }) {
            return "Cada opcion necesita nombre y ajuste de precio valido."
        }
        return null
    }

    fun saveVariantList() {
        val branch = currentBranch ?: return
        val validationError = validateVariantEditor()
        if (validationError != null) {
            variantMessage = validationError
            return
        }

        val normalizedOptions = variantOptions.map {
            VariantOptionDraft(
                id = it.id,
                name = it.name.trim(),
                priceAdjustment = it.priceAdjustment.toDoubleOrNull() ?: 0.0
            )
        }

        coroutineScope.launch {
            variantOperationLoading = true
            variantMessage = null
            val result = if (editingVariantList == null) {
                productViewModel.createVariantList(
                    branchId = branch.id,
                    name = variantName.trim(),
                    description = variantDescription.trim().ifBlank { null },
                    options = normalizedOptions
                )
            } else {
                productViewModel.updateVariantList(
                    branchId = branch.id,
                    variantListId = editingVariantList!!.id,
                    name = variantName.trim(),
                    description = variantDescription.trim().ifBlank { null },
                    options = normalizedOptions
                )
            }

            result.onSuccess {
                variantMessage = if (editingVariantList == null) {
                    "Lista de variantes creada."
                } else {
                    "Lista de variantes actualizada."
                }
                showVariantEditor = false
            }.onFailure { throwable ->
                val errorMessage = throwable.message ?: "No se pudo guardar la lista de variantes."
                variantMessage = errorMessage
            }

            variantOperationLoading = false
        }
    }

    fun deleteVariantList(variantListId: String) {
        val branch = currentBranch ?: return
        coroutineScope.launch {
            variantOperationLoading = true
            productViewModel.deleteVariantList(branch.id, variantListId)
                .onSuccess {
                    variantMessage = "Lista de variantes eliminada."
                }
                .onFailure {
                    variantMessage = it.message ?: "No se pudo eliminar la lista."
                }
            variantOperationLoading = false
        }
    }

    if (currentBranch == null) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Volver",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "No hay sucursal seleccionada",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = {
                    if (!isSaving && !isUploading) {
                        saveBranchChanges()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
                Text(
                    text = "Guardar cambios",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(top = paddingValues.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 88.dp)
        ) {
            item {
                BannerWithLogoSection(
                    avatarUrl = branchPreview?.avatarLargeUrl(),
                    coverUrl = branchPreview?.coverBestUrl(),
                    coverPreviewUrl = coverPreviewUrl,
                    branchName = branchPreview?.name,
                    onChangeAvatar = {
                        pickAndUploadImage(
                            isCover = false,
                            uploadFunction = imageUploadViewModel::uploadBranchAvatar
                        )
                    },
                    onChangeCover = {
                        pickAndUploadImage(
                            isCover = true,
                            uploadFunction = imageUploadViewModel::uploadBranchCover
                        )
                    },
                    onNavigateBack = handleBack
                )
            }

            if (saveMessage != null) {
                item {
                    Box(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
                        ProfileSaveMessageCard(
                            message = saveMessage ?: ""
                        )
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    BranchInfoSection(
                        branch = branchPreview,
                        isActive = isActive,
                        onStatusChange = { isActive = it },
                        selectedTipos = selectedTipos,
                        onTiposChange = { selectedTipos = it },
                        onSave = { branchName, branchPhone, branchAddress ->
                            name = branchName
                            phone = branchPhone
                            address = branchAddress
                        }
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    LocationMapSection(
                        branch = branchPreview,
                        onLocationSave = { lat, lng ->
                            latitude = lat
                            longitude = lng
                        },
                        onOpenMapSelection = onOpenMapSelection
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    BranchScheduleSection(
                        branch = branchPreview,
                        onSave = { updatedSchedule ->
                            branchSchedule = updatedSchedule
                        }
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    SocialLinksSection(
                        socialMedia = branchPreview?.socialMedia,
                        onSave = { updatedSocial ->
                            socialMedia = updatedSocial
                        }
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    ProfileSectionCard {
                        SectionHeader(
                            title = "Listas de Variantes",
                            sectionIcon = Icons.Default.Store
                        )

                    Text(
                        text = "Crea listas como Tamaños o Extras y asignalas luego a productos de esta sucursal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { openVariantEditor(null) },
                            enabled = !variantOperationLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                            Text("Nueva lista")
                        }

                        if (variantListsUiState.isLoading || variantOperationLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(4.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    if (!variantMessage.isNullOrBlank()) {
                        Text(
                            text = variantMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!variantListsUiState.error.isNullOrBlank()) {
                        Text(
                            text = variantListsUiState.error.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (variantListsUiState.variantLists.isEmpty()) {
                        Text(
                            text = "Aun no hay listas de variantes creadas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            variantListsUiState.variantLists.forEach { variantList ->
                                ProfileSectionCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = variantList.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            variantList.description?.takeIf { it.isNotBlank() }?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = variantList.options.joinToString(", ") { option ->
                                                    "${option.name} (${option.priceAdjustment})"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row {
                                            IconButton(
                                                onClick = { openVariantEditor(variantList) },
                                                enabled = !variantOperationLoading
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Editar lista"
                                                )
                                            }
                                            IconButton(
                                                onClick = { deleteVariantList(variantList.id) },
                                                enabled = !variantOperationLoading
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Eliminar lista",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }

            item {
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    ProfileSectionCard {
                    SectionHeader(
                        title = "Operación",
                        sectionIcon = Icons.Default.Build
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = exchangeRateInput,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() }) {
                                    exchangeRateInput = value
                                }
                            },
                            label = { Text("Tasa de cambio USD -> CUP (opcional)") },
                            placeholder = { Text("Ej: 120") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = validateExchangeRateInput(exchangeRateInput) != null,
                            supportingText = {
                                Text(
                                    validateExchangeRateInput(exchangeRateInput)
                                        ?: "Si la configuras, se usara para 1 USD = X CUP"
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = LlegoCustomShapes.inputField
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Métodos de pago",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PaymentMethodSelector(
                            availablePaymentMethods = paymentMethodsUiState.methods,
                            selectedPaymentMethodIds = selectedPaymentMethodIds,
                            onSelectionChange = { selectedPaymentMethodIds = it },
                            isLoading = paymentMethodsUiState.isLoading,
                            layout = PaymentMethodSelectorLayout.FLOW
                        )
                    }

                    TransferAccountsEditor(
                        accounts = transferAccounts,
                        onAccountsChange = { transferAccounts = it },
                        title = "Cuentas para transferencias"
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
                                text = "Mensajería de la app",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = if (useAppMessaging) {
                                    "Clientes escriben por chat de la app"
                                } else {
                                    "La sucursal gestiona su propio canal"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useAppMessaging,
                            onCheckedChange = { useAppMessaging = it }
                        )
                    }

                    if (!useAppMessaging) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Vehículos de delivery",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            BranchVehiclesSelector(
                                selectedVehicles = selectedVehicles,
                                onSelectionChange = { selectedVehicles = it }
                            )
                        }
                    }
                }
                }
            }
        }
    }

    if (showVariantEditor) {
        AlertDialog(
            onDismissRequest = {
                if (!variantOperationLoading) {
                    variantMessage = null
                    showVariantEditor = false
                }
            },
            title = {
                Text(if (editingVariantList == null) "Nueva lista de variantes" else "Editar lista de variantes")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!variantMessage.isNullOrBlank()) {
                        Text(
                            text = variantMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    OutlinedTextField(
                        value = variantName,
                        onValueChange = { variantName = it },
                        label = { Text("Nombre *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    OutlinedTextField(
                        value = variantDescription,
                        onValueChange = { variantDescription = it },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Text(
                        text = "Opciones",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )

                    variantOptions.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = option.name,
                                onValueChange = { value ->
                                    variantOptions = variantOptions.mapIndexed { optionIndex, item ->
                                        if (optionIndex == index) item.copy(name = value) else item
                                    }
                                },
                                label = { Text("Nombre") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = option.priceAdjustment,
                                onValueChange = { value ->
                                    if (value.isEmpty() || value.toDoubleOrNull() != null) {
                                        variantOptions = variantOptions.mapIndexed { optionIndex, item ->
                                            if (optionIndex == index) item.copy(priceAdjustment = value) else item
                                        }
                                    }
                                },
                                label = { Text("Ajuste") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            IconButton(
                                onClick = {
                                    variantOptions = variantOptions.filterIndexed { optionIndex, _ ->
                                        optionIndex != index
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar opción"
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = { variantOptions = variantOptions + VariantOptionEditorState() }
                    ) {
                        Text("Agregar opción")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { saveVariantList() },
                    enabled = !variantOperationLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (variantOperationLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        variantMessage = null
                        showVariantEditor = false
                    },
                    enabled = !variantOperationLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showUnsavedChangesDialog) {
        val unsavedChanges = getUnsavedChanges()
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = {
                Text(
                    text = "Cambios sin guardar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tienes cambios en:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    unsavedChanges.forEach { change ->
                        Text(
                            text = "• $change",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "¿Desea guardar los cambios?",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedChangesDialog = false
                        saveBranchChanges(navigateBackOnSuccess = true)
                    },
                    enabled = !isSaving && !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Sí, guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("No, ir atrás")
                }
            }
        )
    }
}
