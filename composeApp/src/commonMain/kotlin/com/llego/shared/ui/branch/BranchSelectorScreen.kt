package com.llego.shared.ui.branch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.ui.theme.LlegoCustomShapes
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

/**
 * Pantalla mejorada de selección de sucursal
 * MEJORAS:
 * - Agrupa sucursales por negocio
 * - Permite agregar nuevo negocio
 * - Permite agregar sucursal a negocio existente
 * - Código de invitación integrado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchSelectorScreen(
    branches: List<Branch>,
    onBranchSelected: (Branch) -> Unit,
    onAddBusiness: () -> Unit,
    onAddBranch: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    invitationViewModel: com.llego.business.invitations.ui.viewmodel.InvitationViewModel,
    authViewModel: com.llego.shared.ui.auth.AuthViewModel
) {
    val branchSelectorViewModel = remember { BranchSelectorViewModel() }
    val branchSelectorState by branchSelectorViewModel.uiState.collectAsState()

    // Cargar negocios del usuario
    LaunchedEffect(Unit) {
        branchSelectorViewModel.loadBusinesses()
    }

    // Obtener usuario actual para verificar permisos
    val authUiState by authViewModel.uiState.collectAsState()
    val currentUserId = authUiState.user?.id

    // Agrupar sucursales por negocio
    val branchesByBusiness = remember(branchSelectorState.businessesWithBranches) {
        val grouped = branchSelectorState.businessesWithBranches.map { it.toBusiness() to it.branches }
        grouped
    }

    // Verificar si el usuario es propietario de al menos un negocio
    // Solo propietarios y nuevos usuarios pueden crear negocios
    val canCreateBusiness = remember(currentUserId, branchSelectorState.businessesWithBranches) {
        val userId = currentUserId
        if (userId == null) {
            false // Usuario no autenticado
        } else {
            // Puede crear si no tiene negocios (nuevo usuario) o si es propietario de al menos uno
            val isOwnerOfAnyBusiness = branchSelectorState.businessesWithBranches.any { it.ownerId == userId }
            val hasNoBusinesses = branchSelectorState.businessesWithBranches.isEmpty()
            isOwnerOfAnyBusiness || hasNoBusinesses
        }
    }

    val orphanBranches = remember(branches, branchSelectorState.businessesWithBranches) {
        val knownBranchIds = branchSelectorState.businessesWithBranches.flatMap { it.branches }.map { it.id }.toSet()
        branches.filter { it.id !in knownBranchIds }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestiona tus Negocios",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Subtítulo
                Text(
                    text = "Selecciona la sucursal que deseas administrar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Contenido principal
                if (branchSelectorState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Mostrar negocios con sus sucursales agrupadas
                        branchesByBusiness.forEach { (business, businessBranches) ->
                            item(key = "business_${business.id}") {
                                val isOwner = currentUserId != null && currentUserId == business.ownerId

                                BusinessGroupCard(
                                    business = business,
                                    branches = businessBranches,
                                    onBranchSelected = onBranchSelected,
                                    onAddBranch = { onAddBranch(business.id) },
                                    canAddBranch = isOwner
                                )
                            }
                        }

                        // Mostrar sucursales huérfanas (sin negocio identificado)
                        orphanBranches.forEach { branch ->
                            item(key = "orphan_branch_${branch.id}") {
                                BranchCard(
                                    branch = branch,
                                    onClick = { onBranchSelected(branch) }
                                )
                            }
                        }

                        // Botón para agregar nuevo negocio (solo propietarios o nuevos usuarios)
                        if (canCreateBusiness) {
                            item(key = "add_business_button") {
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedCard(
                                    onClick = onAddBusiness,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = LlegoCustomShapes.productCard,
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        width = 2.dp
                                    ),
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Crear Nuevo Negocio",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Código de invitación
                        item(key = "invitation_code") {
                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "¿Tienes un código de invitación?",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val redeemState by invitationViewModel.redeemState.collectAsState()

                            com.llego.business.invitations.ui.components.InvitationCodeInput(
                                isLoading = redeemState is com.llego.business.invitations.ui.viewmodel.RedeemState.Loading,
                                errorMessage = (redeemState as? com.llego.business.invitations.ui.viewmodel.RedeemState.Error)?.message,
                                onRedeemCode = { code ->
                                    invitationViewModel.redeemInvitationCode(code)
                                }
                            )

                            // Handle invitation redemption success
                            LaunchedEffect(redeemState) {
                                if (redeemState is com.llego.business.invitations.ui.viewmodel.RedeemState.Success) {
                                    println("DEBUG BranchSelectorScreen: Invitación aceptada, recargando datos...")
                                    
                                    // Reload user data to get updated businessIds and branchIds
                                    authViewModel.reloadUserData()
                                    
                                    // Esperar a que se cargue al menos un negocio (máximo 10 segundos)
                                    try {
                                        withTimeout(10000) {
                                            println("DEBUG BranchSelectorScreen: Esperando a que se cargue un negocio...")
                                            authViewModel.currentBusiness
                                                .filterNotNull()
                                                .first()
                                            
                                            println("DEBUG BranchSelectorScreen: Negocio cargado")
                                        }
                                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                                        println("DEBUG BranchSelectorScreen: Timeout esperando negocios")
                                    }

                                    // Recargar negocios en el selector
                                    branchSelectorViewModel.loadBusinesses()

                                    // Reset invitation state
                                    invitationViewModel.resetRedeemState()
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card que muestra un negocio con sus sucursales agrupadas
 */
@Composable
private fun BusinessGroupCard(
    business: Business,
    branches: List<Branch>,
    onBranchSelected: (Branch) -> Unit,
    onAddBranch: () -> Unit,
    modifier: Modifier = Modifier,
    canAddBranch: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.productCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header del negocio (clickeable para expandir/colapsar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del negocio
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = business.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${branches.size} ${if (branches.size == 1) "sucursal" else "sucursales"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Icono expandir/colapsar
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Lista de sucursales (cuando está expandido)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                branches.forEach { branch ->
                    BranchCard(
                        branch = branch,
                        onClick = { onBranchSelected(branch) },
                        isCompact = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Botón para agregar sucursal a este negocio (solo propietarios)
                if (canAddBranch) {
                    OutlinedButton(
                        onClick = onAddBranch,
                        modifier = Modifier.fillMaxWidth(),
                        shape = LlegoCustomShapes.secondaryButton
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar sucursal")
                    }
                }
            }
        }
    }
}

@Composable
private fun BranchCard(
    branch: Branch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.productCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompact) 0.dp else 1.dp,
            pressedElevation = 4.dp
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 12.dp else 16.dp)
        ) {
            // Nombre de la sucursal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = branch.name,
                    style = if (isCompact) {
                        MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    } else {
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Tipos de negocio
            if (branch.tipos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    branch.tipos.take(3).forEach { tipo ->
                        BranchTypeChip(tipo = tipo, isCompact = isCompact)
                    }
                }
            }

            // Dirección
            if (!branch.address.isNullOrBlank() && !isCompact) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = branch.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Teléfono
            if (!isCompact) {
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = branch.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BranchTypeChip(
    tipo: BranchTipo,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val (label, color) = when (tipo) {
        BranchTipo.RESTAURANTE -> "Restaurante" to MaterialTheme.colorScheme.secondary
        BranchTipo.TIENDA -> "Tienda" to MaterialTheme.colorScheme.primary
        BranchTipo.DULCERIA -> "Dulcería" to MaterialTheme.colorScheme.tertiary
    }

    Surface(
        modifier = modifier,
        shape = LlegoCustomShapes.secondaryButton,
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = if (isCompact) 8.dp else 10.dp,
                vertical = if (isCompact) 3.dp else 4.dp
            ),
            style = if (isCompact) {
                MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
            } else {
                MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
            },
            color = color
        )
    }
}
