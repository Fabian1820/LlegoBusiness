package com.llego.shared.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.data.model.*
import com.llego.shared.ui.business.RegisterBusinessViewModel
import com.llego.shared.ui.business.components.BranchTipoChip
import com.llego.shared.ui.business.state.defaultBranchSchedule
import com.llego.shared.ui.components.atoms.LlegoTextField
import com.llego.shared.ui.components.molecules.*
import com.llego.shared.ui.onboarding.components.*
import com.llego.shared.ui.payment.PaymentMethodsViewModel
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.theme.LlegoAccent
import com.llego.shared.ui.theme.LlegoPrimary
import com.llego.shared.ui.theme.LlegoSecondary
import kotlinx.coroutines.delay
import kotlin.math.roundToLong

/**
 * Wizard de creacion de negocio paso a paso para nuevos usuarios.
 *
 * Divide el formulario de registro (que antes era una sola pantalla larga)
 * en 7 pasos claros y guiados, con 1-3 campos por pantalla:
 *
 *   Paso 1 → Nombre y descripcion del negocio
 *   Paso 2 → Nombre de la sucursal y tipos de servicio
 *   Paso 3 → Telefono y direccion
 *   Paso 4 → Ubicacion en el mapa
 *   Paso 5 → Horario de atencion
 *   Paso 6 → Metodos de pago
 *   Paso 7 → Resumen y creacion
 *
 * Diseno Apple-inspired: transiciones con slide + fade entre pasos,
 * barra de progreso animada, tarjetas de informacion y tipografia limpia.
 */

private const val TOTAL_STEPS = 7

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWizardScreen(
    registerBusinessViewModel: RegisterBusinessViewModel,
    authViewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val registerUiState by registerBusinessViewModel.uiState.collectAsState()

    // ── Form State ──────────────────────────────────────────
    var currentStep by remember { mutableStateOf(0) }

    // Step 1: Business basics
    var businessName by remember { mutableStateOf("") }
    var businessDescription by remember { mutableStateOf("") }

    // Step 2: Branch basics
    var branchName by remember { mutableStateOf("") }
    var selectedTipos by remember { mutableStateOf(setOf<BranchTipo>()) }

    // Step 3: Contact
    var countryCode by remember { mutableStateOf("+53") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Step 4: Location
    var latitude by remember { mutableStateOf(23.1136) }
    var longitude by remember { mutableStateOf(-82.3666) }
    var locationSelected by remember { mutableStateOf(false) }

    // Step 5: Schedule
    var schedule by remember { mutableStateOf(defaultBranchSchedule()) }

    // Step 6: Payment methods
    val paymentMethodsViewModel = remember { PaymentMethodsViewModel() }
    val paymentMethodsUiState by paymentMethodsViewModel.uiState.collectAsState()
    var selectedPaymentMethodIds by remember { mutableStateOf(listOf<String>()) }

    // Load payment methods when reaching step 6
    LaunchedEffect(currentStep) {
        if (currentStep == 5 && paymentMethodsUiState.methods.isEmpty() && !paymentMethodsUiState.isLoading) {
            paymentMethodsViewModel.loadPaymentMethods()
        }
    }

    // ── Validation ──────────────────────────────────────────
    val canAdvanceFromStep: (Int) -> Boolean = { step ->
        when (step) {
            0 -> businessName.isNotBlank()
            1 -> branchName.isNotBlank() && selectedTipos.isNotEmpty()
            2 -> phoneNumber.isNotBlank()
            3 -> locationSelected
            4 -> schedule.values.any { it.isOpen }
            5 -> selectedPaymentMethodIds.isNotEmpty()
            6 -> true // review step
            else -> false
        }
    }

    // ── Success handling ────────────────────────────────────
    var showSuccessAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(registerUiState.isRegistered) {
        if (registerUiState.isRegistered) {
            showSuccessAnimation = true
            delay(2200)
            authViewModel.reloadUserData()
            delay(600)
            onSuccess()
        }
    }

    // ── Success Overlay ─────────────────────────────────────
    if (showSuccessAnimation) {
        SuccessOverlay(businessName = businessName)
        return
    }

    // ── Main UI ─────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 0) {
                            currentStep--
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 24.dp)
        ) {
            // ── Progress Bar ────────────────────────
            StepProgressBar(
                currentStep = currentStep,
                totalSteps = TOTAL_STEPS
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Step Content ────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
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
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        ) + fadeIn(
                            animationSpec = tween(280)
                        ) togetherWith slideOutOfContainer(
                            towards = direction,
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        ) + fadeOut(
                            animationSpec = tween(200)
                        )
                    },
                    label = "wizard_step_transition"
                ) { step ->
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        when (step) {
                            0 -> StepBusinessBasics(
                                businessName = businessName,
                                onBusinessNameChange = { businessName = it },
                                businessDescription = businessDescription,
                                onBusinessDescriptionChange = { businessDescription = it }
                            )
                            1 -> StepBranchBasics(
                                branchName = branchName,
                                onBranchNameChange = { branchName = it },
                                selectedTipos = selectedTipos,
                                onTiposChange = { selectedTipos = it }
                            )
                            2 -> StepContact(
                                countryCode = countryCode,
                                onCountryCodeChange = { countryCode = it },
                                phoneNumber = phoneNumber,
                                onPhoneChange = { phoneNumber = it },
                                address = address,
                                onAddressChange = { address = it }
                            )
                            3 -> StepLocation(
                                latitude = latitude,
                                longitude = longitude,
                                locationSelected = locationSelected,
                                onOpenMap = {
                                    onOpenMapSelection(
                                        "Ubicacion de tu sucursal",
                                        latitude,
                                        longitude
                                    ) { lat, lng ->
                                        latitude = lat
                                        longitude = lng
                                        locationSelected = true
                                    }
                                }
                            )
                            4 -> StepSchedule(
                                schedule = schedule,
                                onScheduleChange = { schedule = it }
                            )
                            5 -> StepPaymentMethods(
                                paymentMethods = paymentMethodsUiState.methods,
                                selectedIds = selectedPaymentMethodIds,
                                onSelectionChange = { selectedPaymentMethodIds = it },
                                isLoading = paymentMethodsUiState.isLoading,
                                error = paymentMethodsUiState.error,
                                onRetry = { paymentMethodsViewModel.loadPaymentMethods() }
                            )
                            6 -> StepReview(
                                businessName = businessName,
                                businessDescription = businessDescription,
                                branchName = branchName,
                                selectedTipos = selectedTipos,
                                phone = combinePhoneNumber(countryCode, phoneNumber),
                                address = address,
                                locationSelected = locationSelected,
                                schedule = schedule,
                                paymentMethods = paymentMethodsUiState.methods,
                                selectedPaymentMethodIds = selectedPaymentMethodIds,
                                error = registerUiState.error
                            )
                        }

                        // Bottom spacer so content doesn't hide behind nav bar
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // ── Navigation Buttons ──────────────────
            OnboardingNavigationBar(
                onBack = {
                    if (currentStep > 0) currentStep--
                    else onBack()
                },
                onNext = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (currentStep < TOTAL_STEPS - 1) {
                        currentStep++
                    } else {
                        // Submit
                        submitRegistration(
                            registerBusinessViewModel = registerBusinessViewModel,
                            businessName = businessName.trim(),
                            businessDescription = businessDescription.trim(),
                            branchName = branchName.trim(),
                            selectedTipos = selectedTipos,
                            countryCode = countryCode,
                            phoneNumber = phoneNumber.trim(),
                            address = address.trim(),
                            latitude = latitude,
                            longitude = longitude,
                            schedule = schedule,
                            selectedPaymentMethodIds = selectedPaymentMethodIds
                        )
                    }
                },
                canAdvance = canAdvanceFromStep(currentStep),
                isFirstStep = currentStep == 0,
                isLastStep = currentStep == TOTAL_STEPS - 1,
                isLoading = registerUiState.isLoading,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

// ═════════════════════════════════════════════════
//  STEP 1 — Business Name & Description
// ═════════════════════════════════════════════════

@Composable
private fun StepBusinessBasics(
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    businessDescription: String,
    onBusinessDescriptionChange: (String) -> Unit
) {
    OnboardingStepLayout(
        stepIcon = Icons.Default.Storefront,
        title = "Nombre de tu negocio",
        subtitle = "Escribe el nombre con el que los clientes conoceran tu negocio en Llego."
    ) {
        RequiredFieldLabel("Nombre del negocio")
        Spacer(modifier = Modifier.height(8.dp))
        LlegoTextField(
            value = businessName,
            onValueChange = onBusinessNameChange,
            label = "",
            placeholder = "Ej: Cafeteria El Buen Sabor",
            singleLine = true,
            isError = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        OptionalFieldLabel("Descripcion")
        Spacer(modifier = Modifier.height(8.dp))
        LlegoTextField(
            value = businessDescription,
            onValueChange = onBusinessDescriptionChange,
            label = "",
            placeholder = "Cuentale a tus clientes de que trata tu negocio...",
            singleLine = false,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tip card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = LlegoAccent.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = LlegoPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Un buen nombre y una descripcion clara ayudan a que los clientes te encuentren mas facil.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════
//  STEP 2 — Branch Name & Service Types
// ═════════════════════════════════════════════════

@Composable
private fun StepBranchBasics(
    branchName: String,
    onBranchNameChange: (String) -> Unit,
    selectedTipos: Set<BranchTipo>,
    onTiposChange: (Set<BranchTipo>) -> Unit
) {
    OnboardingStepLayout(
        stepIcon = Icons.Default.Store,
        title = "Tu primera sucursal",
        subtitle = "Cada sucursal es un punto de venta donde los clientes pueden hacer pedidos."
    ) {
        RequiredFieldLabel("Nombre de la sucursal")
        Spacer(modifier = Modifier.height(8.dp))
        LlegoTextField(
            value = branchName,
            onValueChange = onBranchNameChange,
            label = "",
            placeholder = "Ej: Sucursal Centro",
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        RequiredFieldLabel("Tipos de servicio")
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Selecciona uno o mas tipos que describan tu sucursal",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BranchTipoChip(
                tipo = BranchTipo.RESTAURANTE,
                selected = BranchTipo.RESTAURANTE in selectedTipos,
                onClick = {
                    onTiposChange(
                        if (BranchTipo.RESTAURANTE in selectedTipos) selectedTipos - BranchTipo.RESTAURANTE
                        else selectedTipos + BranchTipo.RESTAURANTE
                    )
                },
                modifier = Modifier.weight(1f)
            )
            BranchTipoChip(
                tipo = BranchTipo.TIENDA,
                selected = BranchTipo.TIENDA in selectedTipos,
                onClick = {
                    onTiposChange(
                        if (BranchTipo.TIENDA in selectedTipos) selectedTipos - BranchTipo.TIENDA
                        else selectedTipos + BranchTipo.TIENDA
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BranchTipoChip(
                tipo = BranchTipo.DULCERIA,
                selected = BranchTipo.DULCERIA in selectedTipos,
                onClick = {
                    onTiposChange(
                        if (BranchTipo.DULCERIA in selectedTipos) selectedTipos - BranchTipo.DULCERIA
                        else selectedTipos + BranchTipo.DULCERIA
                    )
                },
                modifier = Modifier.weight(1f)
            )
            BranchTipoChip(
                tipo = BranchTipo.CAFE,
                selected = BranchTipo.CAFE in selectedTipos,
                onClick = {
                    onTiposChange(
                        if (BranchTipo.CAFE in selectedTipos) selectedTipos - BranchTipo.CAFE
                        else selectedTipos + BranchTipo.CAFE
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ═════════════════════════════════════════════════
//  STEP 3 — Phone & Address
// ═════════════════════════════════════════════════

@Composable
private fun StepContact(
    countryCode: String,
    onCountryCodeChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit
) {
    OnboardingStepLayout(
        stepIcon = Icons.Default.ContactPhone,
        title = "Contacto de la sucursal",
        subtitle = "Los clientes usaran esta informacion para comunicarse contigo y encontrar tu negocio."
    ) {
        RequiredFieldLabel("Telefono")
        Spacer(modifier = Modifier.height(8.dp))
        PhoneInput(
            phoneNumber = phoneNumber,
            countryCode = countryCode,
            onPhoneChange = onPhoneChange,
            onCountryCodeChange = onCountryCodeChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        OptionalFieldLabel("Direccion")
        Spacer(modifier = Modifier.height(8.dp))
        LlegoTextField(
            value = address,
            onValueChange = onAddressChange,
            label = "",
            placeholder = "Ej: Calle 23 esquina L, Vedado",
            singleLine = false,
            maxLines = 2
        )
    }
}

// ═════════════════════════════════════════════════
//  STEP 4 — Location (Map)
// ═════════════════════════════════════════════════

@Composable
private fun StepLocation(
    latitude: Double,
    longitude: Double,
    locationSelected: Boolean,
    onOpenMap: () -> Unit
) {
    OnboardingStepLayout(
        stepIcon = Icons.Default.LocationOn,
        title = "Ubicacion en el mapa",
        subtitle = "Marca la ubicacion exacta de tu sucursal para que los clientes y repartidores te encuentren."
    ) {
        RequiredFieldLabel("Coordenadas")
        Spacer(modifier = Modifier.height(12.dp))

        // Location card / button
        Surface(
            onClick = onOpenMap,
            shape = RoundedCornerShape(16.dp),
            color = if (locationSelected) {
                LlegoAccent.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            },
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (locationSelected) LlegoPrimary.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (locationSelected) Icons.Default.CheckCircle else Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (locationSelected) LlegoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (locationSelected) {
                    Text(
                        text = "Ubicacion seleccionada",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = LlegoPrimary
                        )
                    )
                    Text(
                        text = "Lat: ${formatCoordinate(latitude, 4)}, Lng: ${formatCoordinate(longitude, 4)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca para cambiar",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = LlegoPrimary.copy(alpha = 0.6f)
                        )
                    )
                } else {
                    Text(
                        text = "Toca para seleccionar ubicacion",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Se abrira el mapa para que marques el punto exacto",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (!locationSelected) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Debes seleccionar una ubicacion para continuar",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }
        }
    }
}

private fun formatCoordinate(value: Double, decimals: Int): String {
    if (decimals <= 0) return value.roundToLong().toString()

    val multiplier = (1..decimals).fold(1.0) { acc, _ -> acc * 10.0 }
    val rounded = (value * multiplier).roundToLong() / multiplier
    val text = rounded.toString()

    return if (text.contains(".")) {
        val parts = text.split(".")
        val integerPart = parts[0]
        val decimalPart = parts.getOrElse(1) { "" }.padEnd(decimals, '0').take(decimals)
        "$integerPart.$decimalPart"
    } else {
        "$text.${"0".repeat(decimals)}"
    }
}

// ═════════════════════════════════════════════════
//  STEP 5 — Schedule
// ═════════════════════════════════════════════════

@Composable
private fun StepSchedule(
    schedule: Map<String, DaySchedule>,
    onScheduleChange: (Map<String, DaySchedule>) -> Unit
) {
    OnboardingStepLayout(
        stepIcon = Icons.Default.Schedule,
        title = "Horario de atencion",
        subtitle = "Define en que dias y horarios tu sucursal estara disponible para recibir pedidos."
    ) {
        RequiredFieldLabel("Horario semanal")
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Al menos un dia debe estar abierto",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        SchedulePicker(
            schedule = schedule,
            onScheduleChange = onScheduleChange,
            showHeader = false
        )
    }
}

// ═════════════════════════════════════════════════
//  STEP 6 — Payment Methods
// ═════════════════════════════════════════════════

@Composable
private fun StepPaymentMethods(
    paymentMethods: List<PaymentMethod>,
    selectedIds: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    OnboardingStepLayout(
        stepIcon = Icons.Default.Payment,
        title = "Metodos de pago",
        subtitle = "Selecciona los metodos de pago que aceptara tu sucursal. Podras cambiarlos despues."
    ) {
        PaymentMethodSelector(
            availablePaymentMethods = paymentMethods,
            selectedPaymentMethodIds = selectedIds,
            onSelectionChange = onSelectionChange,
            isLoading = isLoading,
            errorMessage = error,
            onRetry = onRetry,
            layout = PaymentMethodSelectorLayout.FLOW
        )
    }
}

// ═════════════════════════════════════════════════
//  STEP 7 — Review & Create
// ═════════════════════════════════════════════════

@Composable
private fun StepReview(
    businessName: String,
    businessDescription: String,
    branchName: String,
    selectedTipos: Set<BranchTipo>,
    phone: String,
    address: String,
    locationSelected: Boolean,
    schedule: Map<String, DaySchedule>,
    paymentMethods: List<PaymentMethod>,
    selectedPaymentMethodIds: List<String>,
    error: String?
) {
    OnboardingStepLayout(
        stepIcon = Icons.Default.RocketLaunch,
        title = "Todo listo para crear",
        subtitle = "Revisa la informacion de tu negocio antes de continuar. Podras editar todo despues."
    ) {
        // Error banner
        if (error != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Business Card
        ReviewSection(title = "Negocio") {
            ReviewItem(label = "Nombre", value = businessName)
            if (businessDescription.isNotBlank()) {
                ReviewItem(label = "Descripcion", value = businessDescription)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Branch Card
        ReviewSection(title = "Sucursal") {
            ReviewItem(label = "Nombre", value = branchName)
            ReviewItem(
                label = "Tipo",
                value = selectedTipos.joinToString(", ") { tipo ->
                    when (tipo) {
                        BranchTipo.RESTAURANTE -> "Restaurante"
                        BranchTipo.TIENDA -> "Tienda"
                        BranchTipo.DULCERIA -> "Dulceria"
                        BranchTipo.CAFE -> "Cafe"
                    }
                }
            )
            ReviewItem(label = "Telefono", value = phone)
            if (address.isNotBlank()) {
                ReviewItem(label = "Direccion", value = address)
            }
            ReviewItem(
                label = "Ubicacion",
                value = if (locationSelected) "Seleccionada" else "No seleccionada"
            )

            val openDays = schedule.count { (_, ds) -> ds.isOpen }
            ReviewItem(label = "Horario", value = "$openDays dias abiertos")

            val selectedMethodNames = paymentMethods
                .filter { it.id in selectedPaymentMethodIds }
                .joinToString(", ") { it.toDisplayName() }
            if (selectedMethodNames.isNotBlank()) {
                ReviewItem(label = "Pagos", value = selectedMethodNames)
            }
        }
    }
}

// ═════════════════════════════════════════════════
//  Review Helper Components
// ═════════════════════════════════════════════════

@Composable
private fun ReviewSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = LlegoPrimary
                )
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            content()
        }
    }
}

@Composable
private fun ReviewItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(0.65f),
            textAlign = TextAlign.End
        )
    }
}

// ═════════════════════════════════════════════════
//  Success Overlay
// ═════════════════════════════════════════════════

@Composable
private fun SuccessOverlay(businessName: String) {
    var visible by remember { mutableStateOf(false) }
    var iconVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(400)
        iconVisible = true
    }

    val bgAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "success_bg_alpha"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (iconVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success_icon_scale"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (iconVisible) 1f else 0f,
        animationSpec = tween(600, delayMillis = 300),
        label = "success_text_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LlegoPrimary,
                        Color(0xFF034547)
                    )
                )
            )
            .graphicsLayer { alpha = bgAlpha },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Check icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(LlegoAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(LlegoAccent.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }
            }

            // Texts
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            ) {
                Text(
                    text = "Negocio creado",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = businessName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = LlegoSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Preparando tu espacio de trabajo...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = LlegoAccent,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════
//  Submit Registration
// ═════════════════════════════════════════════════

private fun submitRegistration(
    registerBusinessViewModel: RegisterBusinessViewModel,
    businessName: String,
    businessDescription: String,
    branchName: String,
    selectedTipos: Set<BranchTipo>,
    countryCode: String,
    phoneNumber: String,
    address: String,
    latitude: Double,
    longitude: Double,
    schedule: Map<String, DaySchedule>,
    selectedPaymentMethodIds: List<String>
) {
    val businessInput = CreateBusinessInput(
        name = businessName,
        description = businessDescription.ifBlank { null }
    )

    val branchInput = RegisterBranchInput(
        name = branchName,
        coordinates = CoordinatesInput(lat = latitude, lng = longitude),
        phone = combinePhoneNumber(countryCode, phoneNumber),
        schedule = schedule.toBackendSchedule(),
        tipos = selectedTipos.toList(),
        paymentMethodIds = selectedPaymentMethodIds,
        address = address.ifBlank { null }
    )

    registerBusinessViewModel.registerBusiness(
        business = businessInput,
        branches = listOf(branchInput)
    )
}
