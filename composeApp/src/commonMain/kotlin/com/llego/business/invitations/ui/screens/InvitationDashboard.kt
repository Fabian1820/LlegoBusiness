package com.llego.business.invitations.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llego.business.invitations.data.model.GenerateInvitationInput
import com.llego.business.invitations.data.model.InvitationType
import com.llego.business.invitations.ui.components.BusinessAccessListItem
import com.llego.business.invitations.ui.components.GenerateInvitationDialog
import com.llego.business.invitations.ui.components.InvitationCodeDisplay
import com.llego.business.invitations.ui.components.InvitationListItem
import com.llego.business.invitations.ui.viewmodel.BusinessAccessListState
import com.llego.business.invitations.ui.viewmodel.InvitationListState
import com.llego.business.invitations.ui.viewmodel.InvitationUiState
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel

enum class InvitationTab {
    CODES,
    ACCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationDashboard(
    viewModel: InvitationViewModel,
    businessId: String,
    businessName: String,
    branches: List<Pair<String, String>>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isOwner: Boolean = true
) {
    var selectedTab by remember { mutableStateOf(InvitationTab.CODES) }
    var showGenerateDialog by remember { mutableStateOf(false) }
    var showActiveOnly by remember { mutableStateOf(true) }

    val generateState by viewModel.generateState.collectAsState()
    val invitationsState by viewModel.invitationsState.collectAsState()
    val businessAccessState by viewModel.businessAccessState.collectAsState()

    LaunchedEffect(selectedTab, showActiveOnly) {
        when (selectedTab) {
            InvitationTab.CODES -> {
                viewModel.loadInvitations(businessId, showActiveOnly)
            }
            InvitationTab.ACCESS -> {
                viewModel.loadBusinessAccess(businessId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Accesos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (selectedTab == InvitationTab.CODES && isOwner) {
                        FilterChip(
                            selected = showActiveOnly,
                            onClick = { showActiveOnly = !showActiveOnly },
                            label = { Text(if (showActiveOnly) "Activos" else "Todos") },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == InvitationTab.CODES && isOwner) {
                FloatingActionButton(
                    onClick = { showGenerateDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Generar código")
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == InvitationTab.CODES,
                    onClick = { selectedTab = InvitationTab.CODES },
                    text = { Text("Códigos") }
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
                            onRevokeInvitation = if (isOwner) {
                                { invitationId ->
                                    viewModel.revokeInvitation(invitationId, businessId)
                                }
                            } else null,
                            onRetry = {
                                viewModel.loadInvitations(businessId, showActiveOnly)
                            }
                        )
                    }
                    InvitationTab.ACCESS -> {
                        BusinessAccessContent(
                            state = businessAccessState,
                            onRevokeAccess = if (isOwner) {
                                { accessId ->
                                    // TODO: Implement revoke BusinessAccess mutation
                                }
                            } else null,
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
                            text = if (showActiveOnly) "No hay códigos activos" else "No hay códigos de invitación",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Toca el botón + para generar uno",
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
                            text = "Genera un código tipo 'Negocio completo' para compartir",
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
