package com.llego.shared.ui.branch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel
import com.llego.business.invitations.ui.viewmodel.RedeemState
import com.llego.shared.data.model.Branch
import com.llego.shared.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

@Composable
fun BranchSelectorScreen(
    branchSelectorViewModel: BranchSelectorViewModel,
    branches: List<Branch>,
    onBranchSelected: (Branch) -> Unit,
    onEditBranch: ((Branch) -> Unit)? = null,
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
        floatingActionButton = {
            if (canCreateBusiness) {
                FloatingActionButton(
                    onClick = onAddBusiness,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar negocio",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                branchSelectorState.isLoading -> LoadingState()
                branchesByBusiness.isEmpty() -> {
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
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Header: logo + title + action buttons
                        item(key = "header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Business,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Mis Negocios",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 20.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        IconButton(onClick = { /* search */ }) {
                                            Icon(
                                                imageVector = Icons.Outlined.Search,
                                                contentDescription = "Buscar",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        IconButton(onClick = onNavigateBack) {
                                            Icon(
                                                imageVector = Icons.Outlined.Menu,
                                                contentDescription = "Menú",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section header: "Tus negocios"
                        item(key = "section_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tus negocios",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Business cards
                        branchesByBusiness.forEach { (business, businessBranches) ->
                            item(key = "business_${business.id}") {
                                val isOwner = currentUserId != null && currentUserId == business.ownerId
                                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                                    BusinessSection(
                                        business = business,
                                        branches = businessBranches,
                                        onBranchSelected = onBranchSelected,
                                        onEditBranch = onEditBranch,
                                        onAddBranch = { onAddBranch(business.id) },
                                        canAddBranch = isOwner
                                    )
                                }
                            }
                        }

                        item(key = "invitation") {
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                                InvitationSection(
                                    invitationViewModel = invitationViewModel,
                                    redeemState = redeemState
                                )
                            }
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
