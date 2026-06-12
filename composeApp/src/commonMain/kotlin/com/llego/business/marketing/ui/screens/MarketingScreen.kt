package com.llego.business.marketing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.marketing.ui.components.CreativePreview
import com.llego.business.marketing.ui.components.hexColor
import com.llego.business.marketing.ui.viewmodel.MarketingViewModel
import com.llego.shared.data.model.AdCampaign
import com.llego.shared.data.model.AdPlacement
import com.llego.shared.data.model.AnimationPresets
import com.llego.shared.data.model.CreativeBackground
import com.llego.shared.data.model.CreativeBadge
import com.llego.shared.data.model.CreativeCta
import com.llego.shared.data.model.CreativeSpec
import com.llego.shared.data.model.CreativeText
import com.llego.shared.ui.components.molecules.PaymentMethodSelector
import com.llego.shared.ui.components.molecules.PaymentMethodSelectorLayout

private enum class Step { LIST, DESIGN, CHECKOUT }

private val PALETTES = listOf(
    listOf("#023133", "#0A5C3F"),
    listOf("#7C412B", "#E1C78E"),
    listOf("#1A237E", "#42A5F5"),
    listOf("#B71C1C", "#FF8A65"),
    listOf("#4A148C", "#CE93D8"),
    listOf("#004D40", "#80CBC4"),
)

@Composable
fun MarketingScreen(
    viewModel: MarketingViewModel,
    businessId: String,
    branchId: String?,
    onNavigateBack: () -> Unit
) {
    val campaigns by viewModel.campaigns.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    var step by remember { mutableStateOf(Step.LIST) }

    // Diseño
    var placement by remember { mutableStateOf(AdPlacement.DESTACADO) }
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var badgeText by remember { mutableStateOf("") }
    var paletteIndex by remember { mutableStateOf(0) }
    var animPreset by remember { mutableStateOf("pulse") }

    // Checkout
    var durationDays by remember { mutableStateOf(7) }
    var selectedPaymentMethodId by remember { mutableStateOf<String?>(null) }

    val paymentMethods by viewModel.paymentMethods.collectAsState()

    fun buildCreative(): CreativeSpec = CreativeSpec(
        aspectRatio = "wide",
        animationPreset = animPreset,
        background = CreativeBackground(
            type = "gradient", colors = PALETTES[paletteIndex], angle = 45
        ),
        texts = buildList {
            if (subtitle.isNotBlank()) {
                add(CreativeText("eyebrow", subtitle, "#FFFFFFDD", "sm", "medium"))
            }
            val mainTitle = title.ifBlank { name }
            if (mainTitle.isNotBlank()) {
                add(CreativeText("title", mainTitle, "#FFFFFF", "lg", "bold"))
            }
        },
        badge = if (badgeText.isNotBlank()) {
            CreativeBadge(badgeText, if (placement == AdPlacement.OFERTA) "flash" else "new")
        } else null,
        cta = CreativeCta("Ver tienda")
    )

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                when (step) {
                    Step.LIST -> onNavigateBack()
                    Step.DESIGN -> step = Step.LIST
                    Step.CHECKOUT -> step = Step.DESIGN
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Text(
                text = when (step) {
                    Step.LIST -> "Promociones"
                    Step.DESIGN -> "Diseña tu promoción"
                    Step.CHECKOUT -> "Publicar y pagar"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        when (step) {
            Step.LIST -> ListContent(
                campaigns = campaigns,
                isLoading = isLoading,
                onCreate = {
                    name = ""; title = ""; subtitle = ""; badgeText = ""
                    placement = AdPlacement.DESTACADO; paletteIndex = 0; animPreset = "pulse"
                    step = Step.DESIGN
                }
            )

            Step.DESIGN -> DesignContent(
                creative = buildCreative(),
                placement = placement, onPlacement = { placement = it },
                name = name, onName = { name = it },
                title = title, onTitle = { title = it },
                subtitle = subtitle, onSubtitle = { subtitle = it },
                badgeText = badgeText, onBadge = { badgeText = it },
                paletteIndex = paletteIndex, onPalette = { paletteIndex = it },
                animPreset = animPreset, onAnim = { animPreset = it },
                onContinue = {
                    durationDays = viewModel.durationsFor(placement).firstOrNull() ?: 7
                    step = Step.CHECKOUT
                }
            )

            Step.CHECKOUT -> CheckoutContent(
                creative = buildCreative(),
                placement = placement,
                durations = viewModel.durationsFor(placement),
                durationDays = durationDays, onDuration = { durationDays = it },
                priceLabel = viewModel.priceFor(placement, durationDays)?.let {
                    "${it.price} ${it.currency.uppercase()}"
                } ?: "—",
                paymentMethods = paymentMethods,
                paymentLoading = isLoading,
                paymentError = null,
                selectedPaymentMethodId = selectedPaymentMethodId,
                onPaymentSelected = { selectedPaymentMethodId = it },
                isSubmitting = isSubmitting,
                error = error,
                canPay = branchId != null && selectedPaymentMethodId != null && name.isNotBlank(),
                onPay = {
                    val branch = branchId ?: return@CheckoutContent
                    val pm = selectedPaymentMethodId ?: return@CheckoutContent
                    viewModel.createAndPurchase(
                        businessId = businessId,
                        branchId = branch,
                        name = name.ifBlank { title },
                        placement = placement,
                        durationDays = durationDays,
                        creative = buildCreative(),
                        paymentMethodId = pm
                    ) { ok -> if (ok) step = Step.LIST }
                }
            )
        }
    }
}

@Composable
private fun ListContent(
    campaigns: List<AdCampaign>,
    isLoading: Boolean,
    onCreate: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Crear promoción")
        }
        Spacer(Modifier.height(16.dp))

        if (isLoading && campaigns.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (campaigns.isEmpty()) {
            Text(
                "Aún no tienes promociones. Crea una para aparecer en el feed.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            campaigns.forEach { c ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        CreativePreview(c.creative, Modifier.fillMaxWidth().height(120.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(c.name, fontWeight = FontWeight.Bold)
                        Row(
                            Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusChip(c.status)
                            Text(
                                "${c.impressions} vistas · ${c.clicks} clics",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        c.rejectionReason?.let {
                            Text(
                                "Rechazada: $it",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesignContent(
    creative: CreativeSpec,
    placement: String, onPlacement: (String) -> Unit,
    name: String, onName: (String) -> Unit,
    title: String, onTitle: (String) -> Unit,
    subtitle: String, onSubtitle: (String) -> Unit,
    badgeText: String, onBadge: (String) -> Unit,
    paletteIndex: Int, onPalette: (Int) -> Unit,
    animPreset: String, onAnim: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // Preview en vivo
        CreativePreview(creative, Modifier.fillMaxWidth().height(170.dp))
        Spacer(Modifier.height(16.dp))

        SectionLabel("Tipo")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip("Negocio Destacado", placement == AdPlacement.DESTACADO) {
                onPlacement(AdPlacement.DESTACADO)
            }
            ChoiceChip("Oferta", placement == AdPlacement.OFERTA) {
                onPlacement(AdPlacement.OFERTA)
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = name, onValueChange = onName,
            label = { Text("Nombre interno") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = title, onValueChange = onTitle,
            label = { Text("Título (ej. 2x1 en pizzas)") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = subtitle, onValueChange = onSubtitle,
            label = { Text("Subtítulo (ej. Solo hoy)") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = badgeText, onValueChange = onBadge,
            label = { Text("Badge (opcional, ej. OFERTA)") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        SectionLabel("Color")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PALETTES.forEachIndexed { i, palette ->
                Box(
                    Modifier.size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(hexColor(palette[0]))
                        .clickable { onPalette(i) }
                        .padding(2.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)).background(hexColor(palette[1])))
                    if (i == paletteIndex) {
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.0f))
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        SectionLabel("Animación")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AnimationPresets.all.take(3).forEach { p ->
                ChoiceChip(AnimationPresets.label(p), animPreset == p, Modifier.weight(1f)) { onAnim(p) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AnimationPresets.all.drop(3).forEach { p ->
                ChoiceChip(AnimationPresets.label(p), animPreset == p, Modifier.weight(1f)) { onAnim(p) }
            }
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            enabled = name.isNotBlank() || title.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Continuar") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CheckoutContent(
    creative: CreativeSpec,
    placement: String,
    durations: List<Int>,
    durationDays: Int, onDuration: (Int) -> Unit,
    priceLabel: String,
    paymentMethods: List<com.llego.shared.data.model.PaymentMethod>,
    paymentLoading: Boolean,
    paymentError: String?,
    selectedPaymentMethodId: String?,
    onPaymentSelected: (String?) -> Unit,
    isSubmitting: Boolean,
    error: String?,
    canPay: Boolean,
    onPay: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        CreativePreview(creative, Modifier.fillMaxWidth().height(150.dp))
        Spacer(Modifier.height(16.dp))

        SectionLabel("Duración")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            durations.forEach { d ->
                ChoiceChip("$d días", durationDays == d) { onDuration(d) }
            }
        }
        Spacer(Modifier.height(16.dp))

        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total a pagar", fontWeight = FontWeight.Medium)
                Text(priceLabel, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))

        SectionLabel("Método de pago")
        PaymentMethodSelector(
            availablePaymentMethods = paymentMethods,
            selectedPaymentMethodIds = selectedPaymentMethodId?.let { listOf(it) } ?: emptyList(),
            onSelectionChange = { ids -> onPaymentSelected(ids.lastOrNull()) },
            isLoading = paymentLoading,
            errorMessage = paymentError,
            layout = PaymentMethodSelectorLayout.HORIZONTAL
        )
        Spacer(Modifier.height(16.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = onPay,
            enabled = canPay && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp
                )
            } else {
                Text("Pagar y publicar")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, color) = when (status) {
        "active" -> "Activa" to Color(0xFF43A047)
        "pending_review" -> "En revisión" to Color(0xFFFB8C00)
        "pending_payment" -> "Pago pendiente" to Color(0xFFFB8C00)
        "draft" -> "Borrador" to Color(0xFF90A4AE)
        "rejected" -> "Rechazada" to Color(0xFFE53935)
        "paused" -> "Pausada" to Color(0xFF90A4AE)
        "ended" -> "Finalizada" to Color(0xFF90A4AE)
        else -> status to Color(0xFF90A4AE)
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f)) {
        Text(
            label, color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
