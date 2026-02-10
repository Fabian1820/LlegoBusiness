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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CoordinatesInput
import com.llego.shared.data.model.CreateBranchInput
import com.llego.shared.data.model.toDisplayName
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.business.state.defaultBranchSchedule
import com.llego.shared.ui.components.molecules.MapLocationPickerReal
import com.llego.shared.ui.components.molecules.PaymentMethodSelector
import com.llego.shared.ui.components.molecules.PaymentMethodSelectorLayout
import com.llego.shared.ui.components.molecules.SchedulePicker
import com.llego.shared.ui.components.molecules.toBackendSchedule
import com.llego.shared.ui.onboarding.components.OnboardingStepLayout
import com.llego.shared.ui.onboarding.components.RequiredFieldLabel
import com.llego.shared.ui.onboarding.components.StepProgressBar
import com.llego.shared.ui.payment.PaymentMethodsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchCreateWizardScreen(
    businessId: String,
    onNavigateBack: () -> Unit,
    onSuccess: (Branch) -> Unit,
    onError: (String) -> Unit,
    authViewModel: AuthViewModel,
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
    var latitude by remember { mutableStateOf(23.1136) }
    var longitude by remember { mutableStateOf(-82.3666) }
    var selectedTipos by remember { mutableStateOf(setOf<BranchTipo>()) }
    var schedule by remember { mutableStateOf(defaultBranchSchedule()) }
    var selectedPaymentMethodIds by remember { mutableStateOf(emptyList<String>()) }

    val totalSteps = 4
    val isLastStep = currentStep == totalSteps - 1

    LaunchedEffect(Unit) {
        paymentMethodsViewModel.loadPaymentMethods()
    }

    fun validationErrorForStep(step: Int): String? {
        return when (step) {
            0 -> when {
                name.isBlank() -> "El nombre de la sucursal es obligatorio."
                phone.isBlank() -> "El teléfono de la sucursal es obligatorio."
                else -> null
            }

            1 -> if (latitude == 0.0 && longitude == 0.0) {
                "Selecciona la ubicación de la sucursal en el mapa."
            } else {
                null
            }

            2 -> when {
                selectedTipos.isEmpty() -> "Selecciona al menos un tipo de sucursal."
                selectedPaymentMethodIds.isEmpty() -> "Selecciona al menos un método de pago."
                schedule.values.none { it.isOpen } -> "Debes configurar al menos un día abierto."
                else -> null
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
        val input = CreateBranchInput(
            businessId = businessId,
            name = name.trim(),
            coordinates = CoordinatesInput(lat = latitude, lng = longitude),
            phone = phone.trim(),
            schedule = schedule.toBackendSchedule(),
            tipos = selectedTipos.toList(),
            paymentMethodIds = selectedPaymentMethodIds,
            address = address.trim().ifBlank { null }
        )

        coroutineScope.launch {
            when (val result = authViewModel.createBranch(input)) {
                is BusinessResult.Success -> onSuccess(result.data)
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
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StepProgressBar(currentStep = step, totalSteps = totalSteps)

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
                        title = "Datos básicos",
                        subtitle = "Define la información principal de la nueva sucursal."
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
                        RequiredFieldLabel("Teléfono")
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
                            text = "Dirección (opcional)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            placeholder = { Text("Calle, número, referencia") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    1 -> OnboardingStepLayout(
                        stepIcon = Icons.Default.LocationOn,
                        title = "Ubicación",
                        subtitle = "Selecciona en el mapa dónde opera esta sucursal."
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
                        title = "Operación",
                        subtitle = "Configura tipo de servicio, horario y métodos de pago."
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
                    }

                    else -> {
                        val selectedPaymentMethodNames = paymentMethodsUiState.methods
                            .filter { it.id in selectedPaymentMethodIds }
                            .joinToString(", ") { it.toDisplayName() }

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
                                    ReviewRow("Negocio", "Seleccionado automáticamente")
                                    ReviewRow("Nombre", name)
                                    ReviewRow("Teléfono", phone)
                                    if (address.isNotBlank()) {
                                        ReviewRow("Dirección", address)
                                    }
                                    ReviewRow("Tipos", selectedTipos.joinToString(", ") { it.toDisplayName() })
                                    ReviewRow("Horario", "${schedule.values.count { it.isOpen }} días abiertos")
                                    ReviewRow(
                                        "Pagos",
                                        selectedPaymentMethodNames.ifBlank { "Sin métodos seleccionados" }
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
