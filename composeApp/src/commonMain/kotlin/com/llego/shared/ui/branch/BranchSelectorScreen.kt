package com.llego.shared.ui.branch

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.BranchTipo
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

/**
 * Pantalla de selección de sucursal — diseño premium
 *
 * - TopAppBar normal (no LargeTopAppBar)
 * - Negocio como banner card prominente con avatar/foto real
 * - Sucursales con foto real (AsyncImage) o icono fallback
 * - Acentos de color cálidos para romper monotonía blanca
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchSelectorScreen(
    branchSelectorViewModel: BranchSelectorViewModel,
    branches: List<Branch>,
    onBranchSelected: (Branch) -> Unit,
    onAddBusiness: () -> Unit,
    onAddBranch: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    invitationViewModel: com.llego.business.invitations.ui.viewmodel.InvitationViewModel,
    authViewModel: com.llego.shared.ui.auth.AuthViewModel
) {
    val branchSelectorState by branchSelectorViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        branchSelectorViewModel.loadBusinesses()
    }

    val authUiState by authViewModel.uiState.collectAsState()
    val currentUserId = authUiState.user?.id

    val branchesByBusiness = remember(branchSelectorState.businessesWithBranches) {
        branchSelectorState.businessesWithBranches.map { it.toBusiness() to it.branches }
    }

    val canCreateBusiness = remember(branchSelectorState.businessesWithBranches) {
        branchSelectorState.businessesWithBranches.isEmpty()
    }

    val orphanBranches = remember(branches, branchSelectorState.businessesWithBranches) {
        val knownIds =
            branchSelectorState.businessesWithBranches.flatMap { it.branches }.map { it.id }
                .toSet()
        branches.filter { it.id !in knownIds }
    }

    val redeemState by invitationViewModel.redeemState.collectAsState()

    // Handle invitation redemption success
    LaunchedEffect(redeemState) {
        if (redeemState is com.llego.business.invitations.ui.viewmodel.RedeemState.Success) {
            authViewModel.reloadUserData()
            try {
                withTimeout(10000) {
                    authViewModel.currentBusiness.filterNotNull().first()
                }
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
            }
            branchSelectorViewModel.loadBusinesses()
            invitationViewModel.resetRedeemState()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestiona tus Negocios",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (branchSelectorState.isLoading) {
                LoadingState()
            } else if (branchesByBusiness.isEmpty() && orphanBranches.isEmpty()) {
                EmptyState(
                    canCreateBusiness = canCreateBusiness,
                    onAddBusiness = onAddBusiness,
                    invitationViewModel = invitationViewModel,
                    redeemState = redeemState
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Subtitle
                    item(key = "subtitle") {
                        Text(
                            text = "Selecciona la sucursal que deseas administrar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
                        )
                    }

                    // Business groups
                    branchesByBusiness.forEach { (business, businessBranches) ->
                        item(key = "business_${business.id}") {
                            BusinessSection(
                                business = business,
                                branches = businessBranches,
                                onBranchSelected = onBranchSelected,
                                onAddBranch = { onAddBranch(business.id) },
                                canAddBranch = true
                            )
                        }
                    }

                    // Orphan branches
                    if (orphanBranches.isNotEmpty()) {
                        item(key = "orphan_header") {
                            SectionLabel("Otras sucursales")
                        }
                        item(key = "orphan_branches") {
                            GroupedCard {
                                orphanBranches.forEachIndexed { idx, branch ->
                                    BranchRow(
                                        branch = branch,
                                        onClick = { onBranchSelected(branch) }
                                    )
                                    if (idx < orphanBranches.lastIndex) {
                                        ListDivider()
                                    }
                                }
                            }
                        }
                    }

                    // Actions section
                    item(key = "actions") {
                        ActionsSection(
                            canCreateBusiness = canCreateBusiness,
                            onAddBusiness = onAddBusiness
                        )
                    }

                    // Invitation code section
                    item(key = "invitation") {
                        InvitationSection(
                            invitationViewModel = invitationViewModel,
                            redeemState = redeemState
                        )
                    }

                    // Bottom spacing
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Error overlay
            branchSelectorState.error?.let { error ->
                ErrorBanner(
                    message = error,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

// ─── SECTIONS ────────────────────────────────────────────────────────────────

@Composable
private fun BusinessSection(
    business: Business,
    branches: List<Branch>,
    onBranchSelected: (Branch) -> Unit,
    onAddBranch: () -> Unit,
    canAddBranch: Boolean
) {
    var isExpanded by remember { mutableStateOf(true) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "chevron_rotation"
    )

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // ── Business banner card ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            onClick = { isExpanded = !isExpanded }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Business avatar — real photo or letter fallback
                    BusinessAvatar(
                        avatarUrl = business.avatarUrl,
                        name = business.name,
                        size = 56
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = business.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${branches.size} ${if (branches.size == 1) "sucursal" else "sucursales"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Expand/collapse chevron
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotationAngle)
                            )
                        }
                    }
                }
            }
        }

        // ── Branches list (animated) ──
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(200, delayMillis = 50)),
            exit = shrinkVertically(
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(150))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    branches.forEachIndexed { index, branch ->
                        BranchRow(
                            branch = branch,
                            onClick = { onBranchSelected(branch) }
                        )
                        if (index < branches.lastIndex || canAddBranch) {
                            ListDivider()
                        }
                    }

                    if (canAddBranch) {
                        AddBranchRow(onClick = onAddBranch)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsSection(
    canCreateBusiness: Boolean,
    onAddBusiness: () -> Unit
) {
    if (!canCreateBusiness) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("Acciones")

        GroupedCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddBusiness)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Crear nuevo negocio",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun InvitationSection(
    invitationViewModel: com.llego.business.invitations.ui.viewmodel.InvitationViewModel,
    redeemState: com.llego.business.invitations.ui.viewmodel.RedeemState
) {
    val focusManager = LocalFocusManager.current
    var codeInput by remember { mutableStateOf(TextFieldValue("")) }
    var showError by remember { mutableStateOf(false) }
    val errorMessage =
        (redeemState as? com.llego.business.invitations.ui.viewmodel.RedeemState.Error)?.message
    val isLoading =
        redeemState is com.llego.business.invitations.ui.viewmodel.RedeemState.Loading

    LaunchedEffect(errorMessage) {
        showError = errorMessage != null
    }

    LaunchedEffect(redeemState) {
        if (redeemState is com.llego.business.invitations.ui.viewmodel.RedeemState.Success) {
            codeInput = TextFieldValue("")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("Invitación")

        GroupedCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Mail,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "¿Tienes un código de invitación?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ingresa el código para unirte a un negocio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = {
                            codeInput = it.copy(text = it.text.uppercase())
                            showError = false
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "ABC123",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        },
                        singleLine = true,
                        enabled = !isLoading,
                        isError = showError,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.4f
                            ),
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (codeInput.text.isNotBlank()) {
                                    invitationViewModel.redeemInvitationCode(codeInput.text.trim())
                                }
                            }
                        ),
                        supportingText = if (showError && errorMessage != null) {
                            {
                                Text(
                                    errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else null
                    )

                    FilledTonalButton(
                        onClick = {
                            focusManager.clearFocus()
                            if (codeInput.text.isNotBlank()) {
                                invitationViewModel.redeemInvitationCode(codeInput.text.trim())
                            }
                        },
                        enabled = !isLoading && codeInput.text.isNotBlank(),
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Canjear",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── COMPONENTS ──────────────────────────────────────────────────────────────

/**
 * Avatar del negocio — carga foto real si existe, sino muestra inicial sobre fondo.
 */
@Composable
private fun BusinessAvatar(
    avatarUrl: String?,
    name: String,
    size: Int
) {
    val sizeDp = size.dp
    val hasPhoto = !avatarUrl.isNullOrBlank()

    Surface(
        modifier = Modifier.size(sizeDp),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.2f),
        shadowElevation = 0.dp
    ) {
        if (hasPhoto) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Avatar de sucursal — carga foto real si existe, sino muestra ícono de tipo.
 */
@Composable
private fun BranchAvatar(
    branch: Branch
) {
    val hasPhoto = !branch.avatarUrl.isNullOrBlank()
    val typeColor = branchTypeColor(branch.tipos.firstOrNull())

    Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (hasPhoto) Color.Transparent else typeColor.copy(alpha = 0.1f),
        shadowElevation = 0.dp
    ) {
        if (hasPhoto) {
            AsyncImage(
                model = branch.avatarUrl,
                contentDescription = branch.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = branchTypeIcon(branch.tipos.firstOrNull()),
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Card agrupada — fondo surface, bordes suaves
 */
@Composable
private fun GroupedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Column(content = content)
    }
}

/**
 * Fila de sucursal — foto real o ícono, info compacta, indicador de estado
 */
@Composable
private fun BranchRow(
    branch: Branch,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Branch avatar — real photo or type icon
        BranchAvatar(branch = branch)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = branch.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Type chips + address
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show type chips
                branch.tipos.take(2).forEach { tipo ->
                    BranchTypeChip(tipo = tipo)
                }
            }

            // Address line
            if (!branch.address.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = branch.address,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (branch.status == "active") Color(0xFF34C759)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Seleccionar",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Chip visual de tipo de negocio con color
 */
@Composable
private fun BranchTypeChip(tipo: BranchTipo) {
    val color = branchTypeColor(tipo)
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = branchTypeLabel(tipo),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = color
        )
    }
}

/**
 * Fila para agregar sucursal
 */
@Composable
private fun AddBranchRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AddBusiness,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = "Agregar sucursal",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Divider fino entre elementos de lista
 */
@Composable
private fun ListDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 78.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    )
}

/**
 * Label de sección
 */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            fontSize = 13.sp
        ),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

// ─── STATES ──────────────────────────────────────────────────────────────────

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Text(
                text = "Cargando negocios...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun EmptyState(
    canCreateBusiness: Boolean,
    onAddBusiness: () -> Unit,
    invitationViewModel: com.llego.business.invitations.ui.viewmodel.InvitationViewModel,
    redeemState: com.llego.business.invitations.ui.viewmodel.RedeemState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Illustration
        item {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                tonalElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Comienza tu negocio",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Crea tu primer negocio o usa un código de\ninvitación para unirte a uno existente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        // Create business CTA
        if (canCreateBusiness) {
            item {
                Button(
                    onClick = onAddBusiness,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Crear mi negocio",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Invitation in empty state
        item {
            InvitationSection(
                invitationViewModel = invitationViewModel,
                redeemState = redeemState
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─── HELPERS ─────────────────────────────────────────────────────────────────

@Composable
private fun branchTypeColor(tipo: BranchTipo?): Color {
    return when (tipo) {
        BranchTipo.RESTAURANTE -> MaterialTheme.colorScheme.secondary
        BranchTipo.TIENDA -> MaterialTheme.colorScheme.primary
        BranchTipo.DULCERIA -> MaterialTheme.colorScheme.tertiary
        null -> MaterialTheme.colorScheme.primary
    }
}

private fun branchTypeIcon(tipo: BranchTipo?): ImageVector {
    return when (tipo) {
        BranchTipo.RESTAURANTE -> Icons.Outlined.Restaurant
        BranchTipo.TIENDA -> Icons.Outlined.Storefront
        BranchTipo.DULCERIA -> Icons.Outlined.Cake
        null -> Icons.Outlined.Store
    }
}

private fun branchTypeLabel(tipo: BranchTipo): String {
    return when (tipo) {
        BranchTipo.RESTAURANTE -> "Restaurante"
        BranchTipo.TIENDA -> "Tienda"
        BranchTipo.DULCERIA -> "Dulcería"
    }
}
