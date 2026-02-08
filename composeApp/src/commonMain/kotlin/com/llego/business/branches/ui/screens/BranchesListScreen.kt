package com.llego.business.branches.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.branches.ui.components.BranchCard
import com.llego.business.branches.ui.components.BusinessBranchesGroupCard
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BusinessWithBranches
import com.llego.shared.ui.theme.LlegoCustomShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchesListScreen(
    businessesWithBranches: List<BusinessWithBranches>,
    orphanBranches: List<Branch>,
    currentBranchId: String?,
    statusMessage: String?,
    isLoadingBusinesses: Boolean,
    errorMessage: String?,
    onNavigateBack: () -> Unit,
    onAddBranch: (String) -> Unit,
    onOpenDetails: (Branch) -> Unit,
    onSetActive: (Branch) -> Unit,
    onEdit: (Branch) -> Unit,
    onDelete: (Branch) -> Unit,
    onLocationUpdate: (Branch, Double, Double) -> Unit = { _, _, _ -> },
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit = { _, _, _, _ -> },
    currentUserId: String? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sucursales",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            statusMessage?.let { message ->
                item {
                    Card(
                        shape = LlegoCustomShapes.infoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.startsWith("OK:")) {
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            }
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (message.startsWith("OK:")) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }

            if (isLoadingBusinesses) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            errorMessage?.let { error ->
                item {
                    Card(
                        shape = LlegoCustomShapes.infoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            businessesWithBranches.forEach { business ->
                item(key = "business_${business.id}") {
                    BusinessBranchesGroupCard(
                        business = business,
                        branches = business.branches,
                        currentBranchId = currentBranchId,
                        onOpenDetails = onOpenDetails,
                        onSetActive = onSetActive,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onLocationUpdate = onLocationUpdate,
                        onOpenMapSelection = onOpenMapSelection,
                        onAddBranch = { onAddBranch(business.id) },
                        canAddBranch = true, // Todos pueden agregar sucursales
                        canEditBranch = true, // Todos pueden editar
                        canDeleteBranch = true // Todos pueden eliminar
                    )
                }
            }

            if (orphanBranches.isNotEmpty()) {
                item(key = "orphan_header") {
                    Text(
                        text = "Otras sucursales",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(orphanBranches, key = { it.id }) { branch ->
                    BranchCard(
                        branch = branch,
                        isActive = branch.id == currentBranchId,
                        onOpenDetails = { onOpenDetails(branch) },
                        onSetActive = { onSetActive(branch) },
                        onEdit = { onEdit(branch) },
                        onDelete = { onDelete(branch) },
                        onLocationUpdate = { lat, lng -> onLocationUpdate(branch, lat, lng) },
                        onOpenMapSelection = onOpenMapSelection,
                        canEdit = true, // Allow editing for orphan branches
                        canDelete = false // Don't allow deletion without business context
                    )
                }
            }

            if (!isLoadingBusinesses && businessesWithBranches.isEmpty() && orphanBranches.isEmpty()) {
                item {
                    Text(
                        text = "No hay sucursales disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
