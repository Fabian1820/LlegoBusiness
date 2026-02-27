package com.llego.shared.ui.branch

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel
import com.llego.business.invitations.ui.viewmodel.RedeemState
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import com.llego.shared.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import androidx.compose.foundation.Image
import llegobusiness.composeapp.generated.resources.Res
import llegobusiness.composeapp.generated.resources.iconbussines
import org.jetbrains.compose.resources.painterResource

@Composable
fun BranchSelectorScreen(
    branchSelectorViewModel: BranchSelectorViewModel,
    branches: List<Branch>,
    onBranchSelected: (Branch) -> Unit,
    onEditBusiness: ((Business) -> Unit)? = null,
    onAddBusiness: () -> Unit,
    onAddBranch: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    invitationViewModel: InvitationViewModel,
    authViewModel: AuthViewModel
) {
    val branchSelectorState by branchSelectorViewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val redeemState by invitationViewModel.redeemState.collectAsState()

    var searchEnabled by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val currentUserId = authUiState.user?.id
    val branchesByBusiness = remember(branchSelectorState.businessesWithBranches) {
        branchSelectorState.businessesWithBranches.map { it.toBusiness() to it.branches }
    }
    val normalizedQuery = remember(searchQuery) { searchQuery.trim().lowercase() }
    val visibleBusinesses = remember(branchesByBusiness, normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            branchesByBusiness
        } else {
            branchesByBusiness.mapNotNull { (business, businessBranches) ->
                val businessMatches = business.name.contains(normalizedQuery, ignoreCase = true)
                val visibleBranches = if (businessMatches) {
                    businessBranches
                } else {
                    businessBranches.filter { it.name.contains(normalizedQuery, ignoreCase = true) }
                }

                if (businessMatches || visibleBranches.isNotEmpty()) {
                    business to visibleBranches
                } else {
                    null
                }
            }
        }
    }
    val canCreateBusiness = remember(currentUserId, branchSelectorState.businessesWithBranches) {
        currentUserId?.let { userId ->
            val isOwnerOfAny = branchSelectorState.businessesWithBranches.any { it.ownerId == userId }
            isOwnerOfAny || branchSelectorState.businessesWithBranches.isEmpty()
        } ?: false
    }

    LaunchedEffect(currentUserId) {
        branchSelectorViewModel.onAuthUserChanged(currentUserId)
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
        topBar = {
            BranchSelectorTopBar(
                searchEnabled = searchEnabled,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onOpenSearch = { searchEnabled = true },
                onCloseSearch = {
                    searchEnabled = false
                    searchQuery = ""
                },
                onRequestLogout = { showLogoutDialog = true }
            )
        },
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
        PullToRefreshBox(
            isRefreshing = branchSelectorState.isRefreshing,
            onRefresh = { branchSelectorViewModel.refreshBusinesses() },
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
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        if (visibleBusinesses.isEmpty()) {
                            item(key = "empty_search") {
                                Text(
                                    text = "No encontramos negocios ni sucursales con \"$searchQuery\"",
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                                )
                            }
                        } else {
                            visibleBusinesses.forEach { (business, businessBranches) ->
                                item(key = "business_${business.id}") {
                                    val isOwner = currentUserId != null && currentUserId == business.ownerId
                                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                                        BusinessSection(
                                            business = business,
                                            branches = businessBranches,
                                            onBranchSelected = onBranchSelected,
                                            onEditBusiness = if (isOwner) onEditBusiness else null,
                                            onAddBranch = { onAddBranch(business.id) },
                                            canAddBranch = isOwner
                                        )
                                    }
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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        branchSelectorViewModel.clearState()
                        onLogout()
                    }
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BranchSelectorTopBar(
    searchEnabled: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onRequestLogout: () -> Unit
) {
    Surface(shadowElevation = 2.dp, color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = searchEnabled,
            transitionSpec = {
                slideIntoContainer(
                    towards = if (targetState) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    },
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(180)) togetherWith
                    slideOutOfContainer(
                        towards = if (targetState) {
                            AnimatedContentTransitionScope.SlideDirection.Left
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.Right
                        },
                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(150))
            },
            label = "branch_selector_topbar_transition"
        ) { isSearchMode ->
            if (isSearchMode) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Busca por negocio o sucursal")
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    },
                    actions = {
                        IconButton(onClick = onCloseSearch) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar búsqueda"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(Res.drawable.iconbussines),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
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
                    },
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                IconButton(onClick = onOpenSearch) {
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
                                IconButton(onClick = onRequestLogout) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Cerrar sesión",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    }
}
