package com.llego.business.branches.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.business.branches.ui.components.BranchCard
import com.llego.business.branches.ui.components.BusinessBranchesGroupCard
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BusinessWithBranches

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
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (businessesWithBranches.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        businessesWithBranches.firstOrNull()?.let { onAddBranch(it.id) }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar sucursal",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header: logo icon + "Mis Negocios" + action buttons
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
                                    contentDescription = "Volver",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Status message
            statusMessage?.let { message ->
                item(key = "status_message") {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.startsWith("OK:")) {
                                    Color(0xFFE8F5E9)
                                } else {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                }
                            )
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = if (message.startsWith("OK:")) {
                                    Color(0xFF2E7D32)
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
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
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (isLoadingBusinesses) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            errorMessage?.let { error ->
                item(key = "error_msg") {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Business cards
            businessesWithBranches.forEach { business ->
                item(key = "business_${business.id}") {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
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
                            canAddBranch = true,
                            canEditBranch = true,
                            canDeleteBranch = true
                        )
                    }
                }
            }

            if (orphanBranches.isNotEmpty()) {
                item(key = "orphan_header") {
                    Text(
                        text = "Otras sucursales",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(orphanBranches, key = { it.id }) { branch ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        BranchCard(
                            branch = branch,
                            isActive = branch.id == currentBranchId,
                            onOpenDetails = { onOpenDetails(branch) },
                            onSetActive = { onSetActive(branch) },
                            onEdit = { onEdit(branch) },
                            onDelete = { onDelete(branch) },
                            onLocationUpdate = { lat, lng -> onLocationUpdate(branch, lat, lng) },
                            onOpenMapSelection = onOpenMapSelection,
                            canEdit = true,
                            canDelete = false
                        )
                    }
                }
            }

            if (!isLoadingBusinesses && businessesWithBranches.isEmpty() && orphanBranches.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = "No hay sucursales disponibles",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
