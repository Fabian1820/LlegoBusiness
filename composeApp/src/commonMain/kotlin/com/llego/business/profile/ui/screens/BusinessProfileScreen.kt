package com.llego.business.profile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.llego.business.branches.ui.components.BranchStatusSelector
import com.llego.business.branches.ui.components.BranchTipoSelector
import com.llego.business.branches.ui.components.BranchVehiclesSelector
import com.llego.business.profile.ui.components.BannerWithLogoSection
import com.llego.business.profile.ui.components.BranchInfoSection
import com.llego.business.profile.ui.components.BranchScheduleSection
import com.llego.business.profile.ui.components.ImageUploadDialog
import com.llego.business.profile.ui.components.LocationMapSection
import com.llego.business.profile.ui.components.ProfileSaveMessageCard
import com.llego.business.profile.ui.components.ProfileSectionCard
import com.llego.business.profile.ui.components.SectionHeader
import com.llego.business.profile.ui.components.SocialLinksSection
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CoordinatesInput
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.business.formatQrPaymentsInput
import com.llego.shared.ui.business.formatTransferAccountsInput
import com.llego.shared.ui.business.formatTransferPhonesInput
import com.llego.shared.ui.business.parseQrPaymentsInput
import com.llego.shared.ui.business.parseTransferAccountsInput
import com.llego.shared.ui.business.parseTransferPhonesInput
import com.llego.shared.ui.components.molecules.PaymentMethodSelector
import com.llego.shared.ui.components.molecules.PaymentMethodSelectorLayout
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.upload.ImageUploadViewModel
import com.llego.shared.ui.payment.PaymentMethodsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val currentBranch by authViewModel.currentBranch.collectAsState()
    val imageUploadViewModel = remember { ImageUploadViewModel() }
    val paymentMethodsViewModel = remember { PaymentMethodsViewModel() }
    val paymentMethodsUiState by paymentMethodsViewModel.uiState.collectAsState()

    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showCoverDialog by remember { mutableStateOf(false) }
    var avatarState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }
    var coverState by remember { mutableStateOf<ImageUploadState>(ImageUploadState.Idle) }

    var name by remember(currentBranch?.id) { mutableStateOf(currentBranch?.name.orEmpty()) }
    var phone by remember(currentBranch?.id) { mutableStateOf(currentBranch?.phone.orEmpty()) }
    var address by remember(currentBranch?.id) { mutableStateOf(currentBranch?.address.orEmpty()) }
    var latitude by remember(currentBranch?.id) { mutableStateOf(currentBranch?.coordinates?.latitude ?: 0.0) }
    var longitude by remember(currentBranch?.id) { mutableStateOf(currentBranch?.coordinates?.longitude ?: 0.0) }
    var branchSchedule by remember(currentBranch?.id) { mutableStateOf(currentBranch?.schedule ?: emptyMap()) }
    var selectedTipos by remember(currentBranch?.id) { mutableStateOf(currentBranch?.tipos?.toSet() ?: emptySet()) }
    var selectedPaymentMethodIds by remember(currentBranch?.id) { mutableStateOf(currentBranch?.paymentMethodIds ?: emptyList()) }
    var useAppMessaging by remember(currentBranch?.id) { mutableStateOf(currentBranch?.useAppMessaging ?: true) }
    var selectedVehicles by remember(currentBranch?.id) { mutableStateOf(currentBranch?.vehicles?.toSet() ?: emptySet()) }
    var socialMedia by remember(currentBranch?.id) { mutableStateOf(currentBranch?.socialMedia ?: emptyMap()) }
    var accountsInput by remember(currentBranch?.id) { mutableStateOf(formatTransferAccountsInput(currentBranch?.accounts ?: emptyList())) }
    var qrPaymentsInput by remember(currentBranch?.id) { mutableStateOf(formatQrPaymentsInput(currentBranch?.qrPayments ?: emptyList())) }
    var transferPhonesInput by remember(currentBranch?.id) { mutableStateOf(formatTransferPhonesInput(currentBranch?.phones ?: emptyList())) }
    var isActive by remember(currentBranch?.id) { mutableStateOf(currentBranch?.isActive ?: true) }

    LaunchedEffect(currentBranch?.id) {
        if (currentBranch != null) {
            paymentMethodsViewModel.loadPaymentMethods()
        }
    }

    LaunchedEffect(saveMessage) {
        if (!saveMessage.isNullOrBlank()) {
            delay(3000)
            saveMessage = null
        }
    }

    val avatarPath = (avatarState as? ImageUploadState.Success)?.s3Path
    val coverPath = (coverState as? ImageUploadState.Success)?.s3Path
    val isUploading = avatarState is ImageUploadState.Uploading || coverState is ImageUploadState.Uploading

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
        isActive = isActive
    )

    fun saveBranchChanges() {
        val branch = currentBranch ?: return

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

        val socialMediaValue = socialMedia
            .mapValues { it.value.trim() }
            .filterValues { it.isNotEmpty() }

        val accountsValue = parseTransferAccountsInput(accountsInput)
        val qrPaymentsValue = parseQrPaymentsInput(qrPaymentsInput)
        val transferPhonesValue = parseTransferPhonesInput(transferPhonesInput)

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
            tipos = selectedTipos.toList().takeIf { it != branch.tipos },
            paymentMethodIds = selectedPaymentMethodIds.takeIf { it != branch.paymentMethodIds },
            useAppMessaging = useAppMessaging.takeIf { it != branch.useAppMessaging },
            vehicles = selectedVehicles.toList().takeIf { it.toSet() != branch.vehicles.toSet() },
            isActive = isActive.takeIf { it != branch.isActive },
            socialMedia = socialMediaValue.takeIf { it != (branch.socialMedia ?: emptyMap<String, String>()) },
            accounts = accountsValue.takeIf { it != branch.accounts },
            qrPayments = qrPaymentsValue.takeIf { it != branch.qrPayments },
            phones = transferPhonesValue.takeIf { it != branch.phones },
            avatar = avatarPath,
            coverImage = coverPath
        )

        coroutineScope.launch {
            isSaving = true
            when (val result = authViewModel.updateBranch(branch.id, input)) {
                is BusinessResult.Success -> {
                    val updated = result.data
                    authViewModel.setCurrentBranch(updated)
                    authViewModel.reloadUserData()
                    saveMessage = "Perfil de sucursal actualizado"
                    avatarState = ImageUploadState.Idle
                    coverState = ImageUploadState.Idle
                }

                is BusinessResult.Error -> {
                    saveMessage = result.message
                }

                else -> {}
            }
            isSaving = false
        }
    }

    if (currentBranch == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Perfil de sucursal",
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
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil de sucursal",
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
                    TextButton(
                        onClick = { saveBranchChanges() },
                        enabled = !isSaving && !isUploading
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 4.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Text("Guardar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
            item {
                BannerWithLogoSection(
                    avatarUrl = branchPreview?.avatarUrl,
                    coverUrl = branchPreview?.coverUrl,
                    onChangeAvatar = { showAvatarDialog = true },
                    onChangeCover = { showCoverDialog = true }
                )
            }

            if (saveMessage != null) {
                item {
                    ProfileSaveMessageCard(
                        message = saveMessage ?: "",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            item {
                BranchInfoSection(
                    branch = branchPreview,
                    onSave = { branchName, branchPhone, branchAddress ->
                        name = branchName
                        phone = branchPhone
                        address = branchAddress
                    }
                )
            }

            item {
                ProfileSectionCard {
                    SectionHeader(title = "Estado y billetera")

                    Text(
                        text = "Estado de sucursal",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    BranchStatusSelector(
                        isActive = isActive,
                        onStatusChange = { isActive = it }
                    )

                    Text(
                        text = "Billetera (solo lectura)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "Local: ${currentBranch!!.wallet.local} | USD: ${currentBranch!!.wallet.usd}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Estado: ${currentBranch!!.walletStatus}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                LocationMapSection(
                    branch = branchPreview,
                    onLocationSave = { lat, lng ->
                        latitude = lat
                        longitude = lng
                    }
                )
            }

            item {
                BranchScheduleSection(
                    branch = branchPreview,
                    onSave = { updatedSchedule ->
                        branchSchedule = updatedSchedule
                    }
                )
            }

            item {
                SocialLinksSection(
                    socialMedia = branchPreview?.socialMedia,
                    onSave = { updatedSocial ->
                        socialMedia = updatedSocial
                    }
                )
            }

            item {
                ProfileSectionCard {
                    SectionHeader(title = "Operacion")

                    Text(
                        text = "Tipos de servicio",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    BranchTipoSelector(
                        selectedTipos = selectedTipos,
                        onSelectionChange = { selectedTipos = it }
                    )

                    PaymentMethodSelector(
                        availablePaymentMethods = paymentMethodsUiState.methods,
                        selectedPaymentMethodIds = selectedPaymentMethodIds,
                        onSelectionChange = { selectedPaymentMethodIds = it },
                        isLoading = paymentMethodsUiState.isLoading,
                        layout = PaymentMethodSelectorLayout.FLOW
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
                                text = "Mensajeria de la app",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = if (useAppMessaging) {
                                    "Clientes escriben por chat de la app"
                                } else {
                                    "La sucursal gestiona su propio canal"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useAppMessaging,
                            onCheckedChange = { useAppMessaging = it }
                        )
                    }

                    if (!useAppMessaging) {
                        Text(
                            text = "Vehiculos de delivery",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                        )
                        BranchVehiclesSelector(
                            selectedVehicles = selectedVehicles,
                            onSelectionChange = { selectedVehicles = it }
                        )
                    }
                }
            }

            item {
                ProfileSectionCard {
                    SectionHeader(title = "Cobros")

                    Text(
                        text = "Cuentas para transferencias",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "Una por linea: numero|titular|banco",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = accountsInput,
                        onValueChange = { accountsInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = LlegoCustomShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Text(
                        text = "Pagos QR (uno por linea)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    OutlinedTextField(
                        value = qrPaymentsInput,
                        onValueChange = { qrPaymentsInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = LlegoCustomShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Text(
                        text = "Telefonos de transferencia (uno por linea)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    OutlinedTextField(
                        value = transferPhonesInput,
                        onValueChange = { transferPhonesInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = LlegoCustomShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }
        }
    }

    if (showAvatarDialog) {
        ImageUploadDialog(
            title = "Avatar de la sucursal",
            label = "Avatar",
            uploadState = avatarState,
            onStateChange = { state ->
                avatarState = state
                if (state is ImageUploadState.Success) {
                    saveMessage = "Avatar listo. Guarda para aplicar cambios."
                    showAvatarDialog = false
                }
            },
            uploadFunction = imageUploadViewModel::uploadBranchAvatar,
            onDismiss = { showAvatarDialog = false }
        )
    }

    if (showCoverDialog) {
        ImageUploadDialog(
            title = "Portada de la sucursal",
            label = "Portada",
            uploadState = coverState,
            onStateChange = { state ->
                coverState = state
                if (state is ImageUploadState.Success) {
                    saveMessage = "Portada lista. Guarda para aplicar cambios."
                    showCoverDialog = false
                }
            },
            uploadFunction = imageUploadViewModel::uploadBranchCover,
            onDismiss = { showCoverDialog = false }
        )
    }
}
