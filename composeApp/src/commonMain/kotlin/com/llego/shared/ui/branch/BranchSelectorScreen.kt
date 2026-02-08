package com.llego.shared.ui.branch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel
import com.llego.business.invitations.ui.viewmodel.RedeemState
import com.llego.shared.data.model.Branch
import com.llego.shared.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

/**
 * Pantalla de selección de sucursal.
 *
 * Este archivo mantiene solo el flujo principal de estado y navegación.
 * Las secciones visuales están divididas en archivos dedicados.
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
    invitationViewModel: InvitationViewModel,
    authViewModel: AuthViewModel
) {
    val branchSelectorState by branchSelectorViewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val redeemState by invitationViewModel.redeemState.collectAsState()

    val currentUserId = authUiState.user?.id
    val branchesByBusiness = remember(branchSelectorState.businessesWithBranches) {
        branchSelectorState.businessesWithBranches.map { it.toBusiness() to it.branches }
    }
    val canCreateBusiness = remember(currentUserId, branchSelectorState.businessesWithBranches) {
        currentUserId?.let { userId ->
            val isOwnerOfAny = branchSelectorState.businessesWithBranches.any { it.ownerId == userId }
            isOwnerOfAny || branchSelectorState.businessesWithBranches.isEmpty()
        } ?: false
    }
    val orphanBranches = remember(branches, branchSelectorState.businessesWithBranches) {
        val knownIds = branchSelectorState.businessesWithBranches
            .flatMap { it.branches }
            .map { it.id }
            .toSet()
        branches.filter { it.id !in knownIds }
    }

    LaunchedEffect(Unit) {
        branchSelectorViewModel.loadBusinesses()
    }

    LaunchedEffect(redeemState) {
        if (redeemState is RedeemState.Success) {
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
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            when {
                branchSelectorState.isLoading -> LoadingState()
                branchesByBusiness.isEmpty() && orphanBranches.isEmpty() -> {
                    EmptyState(
                        canCreateBusiness = canCreateBusiness,
                        onAddBusiness = onAddBusiness,
                        invitationViewModel = invitationViewModel,
                        redeemState = redeemState
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item(key = "subtitle") {
                            Text(
                                text = "Selecciona la sucursal que deseas administrar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
                            )
                        }

                        branchesByBusiness.forEach { (business, businessBranches) ->
                            item(key = "business_${business.id}") {
                                val isOwner = currentUserId != null && currentUserId == business.ownerId
                                BusinessSection(
                                    business = business,
                                    branches = businessBranches,
                                    onBranchSelected = onBranchSelected,
                                    onAddBranch = { onAddBranch(business.id) },
                                    canAddBranch = isOwner
                                )
                            }
                        }

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

                        item(key = "actions") {
                            ActionsSection(
                                canCreateBusiness = canCreateBusiness,
                                onAddBusiness = onAddBusiness
                            )
                        }

                        item(key = "invitation") {
                            InvitationSection(
                                invitationViewModel = invitationViewModel,
                                redeemState = redeemState
                            )
                        }

                        item(key = "bottom_spacer") {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }

            branchSelectorState.error?.let { error ->
                ErrorBanner(
                    message = error,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
