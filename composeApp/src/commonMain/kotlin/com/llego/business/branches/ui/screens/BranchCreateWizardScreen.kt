package com.llego.business.branches.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.business.branches.ui.components.BranchTipoSelector
import com.llego.business.branches.ui.components.BranchVehiclesSelector
import com.llego.business.branches.util.parseExchangeRate
import com.llego.business.branches.util.validateExchangeRateInput
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BranchVehicle
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CoordinatesInput
import com.llego.shared.data.model.CreateBranchInput
import com.llego.shared.data.model.TransferAccount
import com.llego.shared.data.model.VariantOptionDraft
import com.llego.shared.data.model.toDisplayName
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.business.TransferAccountsEditor
import com.llego.shared.ui.business.findInvalidTransferAccountItems
import com.llego.shared.ui.business.normalizeTransferAccountsInput
import com.llego.shared.ui.business.state.defaultBranchSchedule
import com.llego.shared.ui.components.molecules.MapLocationPickerReal
import com.llego.shared.ui.components.molecules.PaymentMethodSelector
import com.llego.shared.ui.components.molecules.PaymentMethodSelectorLayout
import com.llego.shared.ui.components.molecules.SchedulePicker
import com.llego.shared.ui.components.molecules.toBackendSchedule
import com.llego.shared.ui.onboarding.components.OnboardingStepLayout
import com.llego.shared.ui.onboarding.components.RequiredFieldLabel
import com.llego.shared.ui.payment.PaymentMethodsViewModel
import kotlinx.coroutines.launch

private data class VariantOptionDraftUi(
    val id: String? = null,
    val name: String = "",
    val priceAdjustment: String = "0"
)

private data class VariantListDraftUi(
    val name: String = "",
    val description: String = "",
    val options: List<VariantOptionDraftUi> = listOf(VariantOptionDraftUi())
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchCreateWizardScreen(
    businessId: String,
    onNavigateBack: () -> Unit,
    onSuccess: (Branch) -> Unit,
    onError: (String) -> Unit,
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val paymentMethodsViewModel = remember { PaymentMethodsViewModel() }
    val paymentMethodsUiState by paymentMethodsViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var facebook by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(23.1136) }
    var longitude by remember { mutableStateOf(-82.3666) }
    var selectedTipos by remember { mutableStateOf(setOf<BranchTipo>()) }
    var useAppMessaging by remember { mutableStateOf(true) }
    var selectedVehicles by remember { mutableStateOf(setOf<BranchVehicle>()) }
    var transferAccounts by remember { mutableStateOf(emptyList<TransferAccount>()) }
    var schedule by remember { mutableStateOf(defaultBranchSchedule()) }
    var selectedPaymentMethodIds by remember { mutableStateOf(emptyList<String>()) }
    var exchangeRateInput by remember { mutableStateOf("") }
    var catalogOnly by remember { mutableStateOf(false) }
    var variantLists by remember { mutableStateOf(listOf<VariantListDraftUi>()) }

    val totalSteps = 5
    val isLastStep = currentStep == totalSteps - 1

    LaunchedEffect(Unit) {
        paymentMethodsViewModel.loadPaymentMethods()
    }

    fun validationErrorForStep(step: Int): String? {
        return when (step) {
            0 -> when {
                name.isBlank() -> "El nombre de la sucursal es obligatorio."
                phone.isBlank() -> "El telefono de la sucursal es obligatorio."
                else -> null
            }

            1 -> if (latitude == 0.0 && longitude == 0.0) {
                "Selecciona la ubicacion de la sucursal en el mapa."
            } else {
                null
            }

            2 -> when {
                selectedTipos.isEmpty() -> "Selecciona al menos un tipo de sucursal."
                selectedPaymentMethodIds.isEmpty() -> "Selecciona al menos un metodo de pago."
                schedule.values.none { it.isOpen } -> "Debes configurar al menos un dia abierto."
                !useAppMessaging && selectedVehicles.isEmpty() -> "Selecciona al menos un vehiculo para delivery propio."
                validateExchangeRateInput(exchangeRateInput) != null -> validateExchangeRateInput(exchangeRateInput)
                findInvalidTransferAccountItems(transferAccounts).isNotEmpty() ->
                    "Revisa las cuentas de transferencia: tarjeta (16 dígitos) y teléfono de confirmación (8 dígitos) son obligatorios."
                else -> null
            }

            3 -> {
                val invalidList = variantLists.firstOrNull { draft ->
                    draft.name.isBlank() ||
                        draft.options.isEmpty() ||
                        draft.options.any { option ->
                            option.name.isBlank() || option.priceAdjustment.toDoubleOrNull() == null
                        }
                }
                if (invalidList != null) {
                    "Revisa las listas de variantes: nombre y opciones con precio valido son obligatorios."
                } else {
                    null
                }
            }

            else -> null
        }
    }

    fun submitBranch() {
        if (businessId.isBlank()) {
            statusMessage = "No se pudo identificar el negocio para esta sucursal."
            onError(statusMessage ?: "")
            return
        }

        isLoading = true
        val exchangeRateValue = parseExchangeRate(exchangeRateInput)
        val input = CreateBranchInput(
            businessId = businessId,
            name = name.trim(),
            coordinates = CoordinatesInput(lat = latitude, lng = longitude),
            phone = phone.trim(),
            schedule = schedule.toBackendSchedule(),
            tipos = selectedTipos.toList(),
            paymentMethodIds = selectedPaymentMethodIds,
            address = address.trim().ifBlank { null },
            socialMedia = buildBranchSocialMediaMap(
                instagram = instagram,
                facebook = facebook,
                whatsapp = whatsapp
            ),
            useAppMessaging = useAppMessaging,
            vehicles = selectedVehicles.toList(),
            exchangeRate = exchangeRateValue,
            accounts = normalizeTransferAccountsInput(transferAccounts).takeIf { it.isNotEmpty() },
            catalogOnly = catalogOnly
        )

        coroutineScope.launch {
            when (val result = authViewModel.createBranch(input)) {
                is BusinessResult.Success -> {
                    val createdBranch = result.data
                    val variantErrors = mutableListOf<String>()
                    variantLists.forEach { draft ->
                        val normalizedName = draft.name.trim()
                        if (normalizedName.isBlank()) return@forEach

                        val normalizedOptions = draft.options.mapNotNull { option ->
                            val optionName = option.name.trim()
                            val optionPrice = option.priceAdjustment.toDoubleOrNull()
                            if (optionName.isBlank() || optionPrice == null) {
                                null
                            } else {
                                VariantOptionDraft(
                                    id = option.id,
                                    name = optionName,
                                    priceAdjustment = optionPrice
                                )
                            }
                        }

                        if (normalizedOptions.isEmpty()) {
                            return@forEach
                        }

                        val createResult = productViewModel.createVariantList(
                            branchId = createdBranch.id,
                            name = normalizedName,
                            description = draft.description.trim().ifBlank { null },
                            options = normalizedOptions
                        )

                        if (createResult.isFailure) {
                            variantErrors += normalizedName
                        }
                    }

                    if (variantErrors.isNotEmpty()) {
                        statusMessage = "Sucursal creada, pero no se pudieron crear estas listas: ${
                            variantErrors.joinToString(", ")
                        }"
                        onError(
                            statusMessage ?: "No se pudieron crear algunas listas de variantes."
                        )
                    }

                    onSuccess(createdBranch)
                }
                is BusinessResult.Error -> {
                    statusMessage = result.message
                    onError(result.message)
                }

                else -> Unit
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva sucursal",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentStep == 0) onNavigateBack() else currentStep--
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val error = validationErrorForStep(currentStep)
                            if (error != null) {
                                statusMessage = error
                                onError(error)
                                return@TextButton
                            }

                            statusMessage = null
                            if (isLastStep) {
                                submitBranch()
                            } else {
                                currentStep++
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = if (isLastStep) "Crear" else "Siguiente",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
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
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                val direction = if (targetState > initialState) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                }
                slideIntoContainer(
                    towards = direction,
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(220)) togetherWith
                    slideOutOfContainer(
                        towards = direction,
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(180))
            },
            label = "branch_wizard_step_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                statusMessage?.let { message ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                when (step) {
                    0 -> OnboardingStepLayout(
                        stepIcon = Icons.Default.Business,
                        title = "Datos basicos",
                        subtitle = "Define la informacion principal de la nueva sucursal."
                    ) {
                        RequiredFieldLabel("Nombre de la sucursal")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Ej: Sucursal Centro") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        RequiredFieldLabel("Telefono")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            placeholder = { Text("Ej: +53 55555555") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Direccion (opcional)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            placeholder = { Text("Calle, numero, referencia") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Redes sociales (opcional)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = instagram,
                            onValueChange = { instagram = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Instagram") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = facebook,
                            onValueChange = { facebook = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Facebook") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("WhatsApp") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Modo solo catálogo",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Los clientes ven productos pero no pueden hacer pedidos.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = catalogOnly,
                                onCheckedChange = { catalogOnly = it }
                            )
                        }
                    }

                    1 -> OnboardingStepLayout(
                        stepIcon = Icons.Default.LocationOn,
                        title = "Ubicacion",
                        subtitle = "Selecciona en el mapa donde opera esta sucursal."
                    ) {
                        MapLocationPickerReal(
                            latitude = latitude,
                            longitude = longitude,
                            onLocationSelected = { lat, lng ->
                                latitude = lat
                                longitude = lng
                            },
                            onOpenMapSelection = onOpenMapSelection
                        )
                    }

                    2 -> OnboardingStepLayout(
                        stepIcon = Icons.Default.Schedule,
                        title = "Operacion",
                        subtitle = "Configura tipo de servicio, horario y metodos de pago."
                    ) {
                        RequiredFieldLabel("Tipos de servicio")
                        Spacer(modifier = Modifier.height(8.dp))
                        BranchTipoSelector(
                            selectedTipos = selectedTipos,
                            onSelectionChange = { selectedTipos = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        SchedulePicker(
                            schedule = schedule,
                            onScheduleChange = { schedule = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        PaymentMethodSelector(
                            availablePaymentMethods = paymentMethodsUiState.methods,
                            selectedPaymentMethodIds = selectedPaymentMethodIds,
                            onSelectionChange = { selectedPaymentMethodIds = it },
                            isLoading = paymentMethodsUiState.isLoading,
                            errorMessage = paymentMethodsUiState.error,
                            onRetry = { paymentMethodsViewModel.loadPaymentMethods() },
                            layout = PaymentMethodSelectorLayout.FLOW
                        )

                        Spacer(modifier = Modifier.height(16.dp))
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
                            shape = RoundedCornerShape(12.dp),
                            isError = validateExchangeRateInput(exchangeRateInput) != null,
                            supportingText = {
                                Text(
                                    validateExchangeRateInput(exchangeRateInput)
                                        ?: "Si la configuras, se usara para 1 USD = X CUP"
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Mensajeria de la app",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (useAppMessaging) "Activa" else "Externa",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = useAppMessaging,
                                onCheckedChange = { useAppMessaging = it }
                            )
                        }

                        if (!useAppMessaging) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Vehiculos de entrega",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            BranchVehiclesSelector(
                                selectedVehicles = selectedVehicles,
                                onSelectionChange = { selectedVehicles = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cobros por transferencia (opcional)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        TransferAccountsEditor(
                            accounts = transferAccounts,
                            onAccountsChange = { transferAccounts = it }
                        )
                    }

                    3 -> OnboardingStepLayout(
                        stepIcon = Icons.Default.Tune,
                        title = "Listas de Variantes",
                        subtitle = "Opcional: crea listas reutilizables (tamano, extras, ingredientes) para asignarlas luego a productos."
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Cada lista se usa en muchos productos de esta sucursal. Ejemplo: una lista \"Tamanos\" con Pequeno/Mediano/Grande.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Button(
                            onClick = {
                                variantLists = variantLists + VariantListDraftUi()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PostAdd,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar lista")
                        }

                        if (variantLists.isEmpty()) {
                            Text(
                                text = "Puedes omitir este paso y administrar las listas despues desde el perfil de sucursal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            variantLists.forEachIndexed { listIndex, listDraft ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Lista ${listIndex + 1}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            IconButton(
                                                onClick = {
                                                    variantLists = variantLists.filterIndexed { index, _ ->
                                                        index != listIndex
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Eliminar lista",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        OutlinedTextField(
                                            value = listDraft.name,
                                            onValueChange = { value ->
                                                variantLists = variantLists.mapIndexed { index, item ->
                                                    if (index == listIndex) item.copy(name = value) else item
                                                }
                                            },
                                            label = { Text("Nombre de la lista *") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = listDraft.description,
                                            onValueChange = { value ->
                                                variantLists = variantLists.mapIndexed { index, item ->
                                                    if (index == listIndex) item.copy(description = value) else item
                                                }
                                            },
                                            label = { Text("Descripcion (opcional)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 2,
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Text(
                                            text = "Opciones",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                        )

                                        listDraft.options.forEachIndexed { optionIndex, option ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = option.name,
                                                    onValueChange = { value ->
                                                        variantLists = variantLists.mapIndexed { index, item ->
                                                            if (index != listIndex) {
                                                                item
                                                            } else {
                                                                item.copy(
                                                                    options = item.options.mapIndexed { optIndex, optItem ->
                                                                        if (optIndex == optionIndex) {
                                                                            optItem.copy(name = value)
                                                                        } else {
                                                                            optItem
                                                                        }
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    },
                                                    label = { Text("Nombre *") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                OutlinedTextField(
                                                    value = option.priceAdjustment,
                                                    onValueChange = { value ->
                                                        if (value.isEmpty() || value.toDoubleOrNull() != null) {
                                                            variantLists = variantLists.mapIndexed { index, item ->
                                                                if (index != listIndex) {
                                                                    item
                                                                } else {
                                                                    item.copy(
                                                                        options = item.options.mapIndexed { optIndex, optItem ->
                                                                            if (optIndex == optionIndex) {
                                                                                optItem.copy(priceAdjustment = value)
                                                                            } else {
                                                                                optItem
                                                                            }
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    },
                                                    label = { Text("Ajuste") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        variantLists = variantLists.mapIndexed { index, item ->
                                                            if (index == listIndex) {
                                                                item.copy(
                                                                    options = item.options.filterIndexed { idx, _ ->
                                                                        idx != optionIndex
                                                                    }
                                                                )
                                                            } else {
                                                                item
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar opcion"
                                                    )
                                                }
                                            }
                                        }

                                        TextButton(
                                            onClick = {
                                                variantLists = variantLists.mapIndexed { index, item ->
                                                    if (index == listIndex) {
                                                        item.copy(options = item.options + VariantOptionDraftUi())
                                                    } else {
                                                        item
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Agregar opcion")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        val selectedPaymentMethodNames = paymentMethodsUiState.methods
                            .filter { it.id in selectedPaymentMethodIds }
                            .joinToString(", ") { it.name }
                        val socialMedia = buildBranchSocialMediaMap(
                            instagram = instagram,
                            facebook = facebook,
                            whatsapp = whatsapp
                        )

                        OnboardingStepLayout(
                            stepIcon = Icons.Default.CheckCircle,
                            title = "Resumen",
                            subtitle = "Revisa los datos antes de crear la sucursal."
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ReviewRow("Negocio", "Seleccionado automaticamente")
                                    ReviewRow("Nombre", name)
                                    ReviewRow("Telefono", phone)
                                    if (address.isNotBlank()) {
                                        ReviewRow("Direccion", address)
                                    }
                                    socialMedia?.forEach { (key, value) ->
                                        ReviewRow(
                                            key.replaceFirstChar { it.uppercase() },
                                            value
                                        )
                                    }
                                    ReviewRow("Tipos", selectedTipos.joinToString(", ") { it.toDisplayName() })
                                    ReviewRow("Mensajeria", if (useAppMessaging) "App" else "Externa")
                                    if (!useAppMessaging) {
                                        ReviewRow("Vehiculos", selectedVehicles.joinToString(", ") { it.toDisplayName() })
                                    }
                                    ReviewRow("Horario", "${schedule.values.count { it.isOpen }} dias abiertos")
                                    ReviewRow(
                                        "Pagos",
                                        selectedPaymentMethodNames.ifBlank { "Sin metodos seleccionados" }
                                    )
                                    if (exchangeRateInput.isNotBlank()) {
                                        ReviewRow("Tasa de cambio", "1 USD = ${exchangeRateInput.trim()} CUP")
                                    }
                                    ReviewRow("Listas de variantes", variantLists.size.toString())
                                    ReviewRow("Cuentas", normalizeTransferAccountsInput(transferAccounts).size.toString())
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
