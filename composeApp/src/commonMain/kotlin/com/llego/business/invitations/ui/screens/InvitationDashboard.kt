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
import com.llego.business.invitations.ui.components.GenerateInvitationDialog
import com.llego.business.invitations.ui.components.InvitationCodeDisplay
import com.llego.business.invitations.ui.components.InvitationListItem
import com.llego.business.invitations.ui.viewmodel.InvitationListState
import com.llego.business.invitations.ui.viewmodel.InvitationUiState
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationDashboard(
    viewModel: InvitationViewModel,
    businessId: String,
    businessName: String,
    branches: List<Pair<String, String>>, // List of (id, name)
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGenerateDialog by remember { mutableStateOf(false) }
    var showActiveOnly by remember { mutableStateOf(true) }
    
    val generateState by viewModel.generateState.collectAsState()
    val invitationsState by viewModel.invitationsState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadInvitations(businessId, showActiveOnly)
    }
    
    LaunchedEffect(showActiveOnly) {
        viewModel.loadInvitations(businessId, showActiveOnly)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Códigos de invitación") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    FilterChip(
                        selected = showActiveOnly,
                        onClick = { showActiveOnly = !showActiveOnly },
                        label = { Text(if (showActiveOnly) "Activos" else "Todos") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showGenerateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Generar código")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show generated code at the top if just generated
            if (generateState is InvitationUiState.Success) {
                InvitationCodeDisplay(
                    invitation = (generateState as InvitationUiState.Success).invitation,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            
            // Show invitations list
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (val state = invitationsState) {
                    is InvitationListState.Idle -> {
                        // Initial state
                    }
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
                                            "No hay códigos activos"
                                        } else {
                                            "No hay códigos de invitación"
                                        },
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
                                        onRevoke = { invitationId ->
                                            viewModel.revokeInvitation(invitationId, businessId)
                                        }
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
                                Button(
                                    onClick = { viewModel.loadInvitations(businessId, showActiveOnly) }
                                ) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Generate dialog
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
                println("InvitationDashboard: Generando código - tipo=$type, branchId=$branchId, días=$durationDays")
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
    
    // Show snackbar for generate errors
    if (generateState is InvitationUiState.Error) {
        LaunchedEffect(generateState) {
            // Show error message
            kotlinx.coroutines.delay(3000)
            viewModel.resetGenerateState()
        }
    }
    
    // Reload list after successful generation
    LaunchedEffect(generateState) {
        println("InvitationDashboard: Estado de generación cambió - $generateState")
        if (generateState is InvitationUiState.Success) {
            println("InvitationDashboard: Recargando invitaciones después de generación exitosa")
            viewModel.loadInvitations(businessId, showActiveOnly)
        }
    }
}
