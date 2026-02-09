package com.llego.business.invitations.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.delivery.ui.screens.DeliveryLinkManagementScreen
import com.llego.business.delivery.ui.viewmodel.DeliveryLinkViewModel
import com.llego.business.invitations.data.model.GenerateInvitationInput
import com.llego.business.invitations.ui.components.BusinessAccessListItem
import com.llego.business.invitations.ui.components.GenerateInvitationDialog
import com.llego.business.invitations.ui.components.InvitationCodeDisplay
import com.llego.business.invitations.ui.components.InvitationListItem
import com.llego.business.invitations.ui.viewmodel.BusinessAccessListState
import com.llego.business.invitations.ui.viewmodel.InvitationListState
import com.llego.business.invitations.ui.viewmodel.InvitationUiState
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel
import com.llego.shared.data.model.Branch

enum class InvitationTab {
    CODES,
    ACCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationDashboard(
    viewModel: InvitationViewModel,
    deliveryLinkViewModel: DeliveryLinkViewModel,
    businessId: String,
    businessName: String,
    branches: List<Pair<String, String>>,
    allBranches: List<Branch> = emptyList(),
    currentBranchId: String? = null,
    currentBranchName: String? = null,
    currentBranchUsesAppMessaging: Boolean = true,
    onBranchDeliveryModeUpdated: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isOwner: Boolean = true
) {
    var selectedTab by remember { mutableStateOf(InvitationTab.CODES) }
    var showGenerateDialog by remember { mutableStateOf(false) }
    var showActiveOnly by remember { mutableStateOf(true) }
    var showDeliveryManagement by remember { mutableStateOf(false) }
    var deliveryManagementBranchId by remember { mutableStateOf<String?>(null) }

    val generateState by viewModel.generateState.collectAsState()
    val invitationsState by viewModel.invitationsState.collectAsState()
    val businessAccessState by viewModel.businessAccessState.collectAsState()
    val deliveryUiState by deliveryLinkViewModel.uiState.collectAsState()

    LaunchedEffect(selectedTab, showActiveOnly) {
        when (selectedTab) {
            InvitationTab.CODES -> viewModel.loadInvitations(businessId, showActiveOnly)
            InvitationTab.ACCESS -> viewModel.loadBusinessAccess(businessId)
        }
    }

    LaunchedEffect(allBranches, currentBranchId, currentBranchUsesAppMessaging) {
        if (allBranches.isNotEmpty()) {
            deliveryLinkViewModel.loadEntryPointForBranches(
                branches = allBranches,
                currentBranchId = currentBranchId
            )
        } else {
            deliveryLinkViewModel.loadEntryPoint(
                branchId = currentBranchId,
                branchUsesAppMessaging = currentBranchUsesAppMessaging
            )
        }
    }

    LaunchedEffect(currentBranchId) {
        showDeliveryManagement = false
        deliveryManagementBranchId = null
    }

    val hasOwnDeliveryInBusiness = allBranches.any { !it.useAppMessaging }
    val canOpenDeliveryManagement = allBranches.isNotEmpty() &&
        (hasOwnDeliveryInBusiness || deliveryUiState.hasPendingRequests || deliveryUiState.entryPointQueryFailed)

    val managementBranch = allBranches.firstOrNull { it.id == deliveryManagementBranchId }
    if (showDeliveryManagement && managementBranch != null) {
        DeliveryLinkManagementScreen(
            viewModel = deliveryLinkViewModel,
            branchId = managementBranch.id,
            branchName = managementBranch.name,
            initialBranchUsesAppMessaging = managementBranch.useAppMessaging,
            onNavigateBack = { showDeliveryManagement = false },
            onDeliveryModeEnabled = onBranchDeliveryModeUpdated
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gestion de accesos",
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
                    if (canOpenDeliveryManagement) {
                        IconButton(
                            onClick = {
                                val preferredBranch = resolveDeliveryManagementBranch(
                                    allBranches = allBranches,
                                    currentBranchId = currentBranchId,
                                    pendingBranchIds = deliveryUiState.pendingRequestBranchIds,
                                    suggestedBranchId = deliveryUiState.suggestedEntryBranchId
                                )
                                if (preferredBranch != null) {
                                    deliveryManagementBranchId = preferredBranch.id
                                    showDeliveryManagement = true
                                }
                            }
                        ) {
                            BadgedBox(
                                badge = {
                                    if (deliveryUiState.pendingRequestCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error
                                        ) {
                                            Text(
                                                text = deliveryUiState.pendingRequestCount.toString(),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeliveryDining,
                                    contentDescription = "Gestionar choferes",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (selectedTab == InvitationTab.CODES) {
                        FilterChip(
                            selected = showActiveOnly,
                            onClick = { showActiveOnly = !showActiveOnly },
                            label = { Text(if (showActiveOnly) "Activos" else "Todos") },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == InvitationTab.CODES) {
                ExtendedFloatingActionButton(
                    onClick = { showGenerateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Generar codigo")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generar")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (generateState is InvitationUiState.Success && selectedTab == InvitationTab.CODES) {
                InvitationCodeDisplay(
                    invitation = (generateState as InvitationUiState.Success).invitation,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = selectedTab == InvitationTab.CODES,
                    onClick = { selectedTab = InvitationTab.CODES },
                    text = { Text("Codigos") }
                )
                Tab(
                    selected = selectedTab == InvitationTab.ACCESS,
                    onClick = { selectedTab = InvitationTab.ACCESS },
                    text = { Text("Usuarios") }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    InvitationTab.CODES -> {
                        InvitationCodesContent(
                            state = invitationsState,
                            showActiveOnly = showActiveOnly,
                            onRevokeInvitation = { invitationId ->
                                viewModel.revokeInvitation(invitationId, businessId)
                            },
                            onRetry = {
                                viewModel.loadInvitations(businessId, showActiveOnly)
                            }
                        )
                    }

                    InvitationTab.ACCESS -> {
                        BusinessAccessContent(
                            state = businessAccessState,
                            onRevokeAccess = { _ ->
                                // TODO: Implement revoke BusinessAccess mutation
                            },
                            onRetry = {
                                viewModel.loadBusinessAccess(businessId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showGenerateDialog) {
        GenerateInvitationDialog(
            branches = branches,
            businessId = businessId,
            businessName = businessName,
            onDismiss = {
                showGenerateDialog = false
                viewModel.resetGenerateState()
            },
            onGenerate = { type, branchId, durationDays ->
                val input = GenerateInvitationInput(
                    invitationType = type,
                    businessId = businessId,
                    branchId = branchId,
                    accessDurationDays = durationDays
                )
                viewModel.generateInvitationCode(input)
                showGenerateDialog = false
            }
        )
    }

    if (generateState is InvitationUiState.Error) {
        LaunchedEffect(generateState) {
            kotlinx.coroutines.delay(3000)
            viewModel.resetGenerateState()
        }
    }

    LaunchedEffect(generateState) {
        if (generateState is InvitationUiState.Success) {
            viewModel.loadInvitations(businessId, showActiveOnly)
        }
    }
}

private fun resolveDeliveryManagementBranch(
    allBranches: List<Branch>,
    currentBranchId: String?,
    pendingBranchIds: Set<String>,
    suggestedBranchId: String?
): Branch? {
    val current = allBranches.firstOrNull { it.id == currentBranchId }
    if (current != null && (!current.useAppMessaging || current.id in pendingBranchIds)) {
        return current
    }

    if (!suggestedBranchId.isNullOrBlank()) {
        allBranches.firstOrNull { it.id == suggestedBranchId }?.let { return it }
    }

    allBranches.firstOrNull { it.id in pendingBranchIds }?.let { return it }
    allBranches.firstOrNull { !it.useAppMessaging }?.let { return it }
    return current ?: allBranches.firstOrNull()
}

@Composable
private fun InvitationCodesContent(
    state: InvitationListState,
    showActiveOnly: Boolean,
    onRevokeInvitation: ((String) -> Unit)?,
    onRetry: () -> Unit
) {
    when (state) {
        is InvitationListState.Idle -> {}
        is InvitationListState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is InvitationListState.Success -> {
            if (state.invitations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (showActiveOnly) {
                                "No hay codigos activos"
                            } else {
                                "No hay codigos de invitacion"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Toca el boton + para generar uno",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.invitations) { invitation ->
                        InvitationListItem(
                            invitation = invitation,
                            onRevoke = onRevokeInvitation
                        )
                    }
                }
            }
        }

        is InvitationListState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Error al cargar invitaciones",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onRetry) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
private fun BusinessAccessContent(
    state: BusinessAccessListState,
    onRevokeAccess: ((String) -> Unit)?,
    onRetry: () -> Unit
) {
    when (state) {
        is BusinessAccessListState.Idle -> {}
        is BusinessAccessListState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is BusinessAccessListState.Success -> {
            if (state.accesses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No hay usuarios con acceso",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Genera un codigo tipo 'Negocio completo' para compartir",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.accesses) { access ->
                        BusinessAccessListItem(
                            access = access,
                            onRevoke = onRevokeAccess
                        )
                    }
                }
            }
        }

        is BusinessAccessListState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Error al cargar usuarios",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onRetry) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}
