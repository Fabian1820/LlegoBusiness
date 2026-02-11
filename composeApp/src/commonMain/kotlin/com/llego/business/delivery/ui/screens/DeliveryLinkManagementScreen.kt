package com.llego.business.delivery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.delivery.data.model.BranchDeliveryRequest
import com.llego.business.delivery.data.model.DeliveryRequestStatus
import com.llego.business.delivery.data.model.LinkedDriverSummary
import com.llego.business.delivery.data.model.toVehicleLabel
import com.llego.business.delivery.ui.viewmodel.DeliveryLinkViewModel

private const val DELIVERY_PULL_REFRESH_THRESHOLD_PX = 120f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryLinkManagementScreen(
    viewModel: DeliveryLinkViewModel,
    branchId: String,
    branchName: String,
    initialBranchUsesAppMessaging: Boolean,
    onNavigateBack: () -> Unit,
    onDeliveryModeEnabled: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var gesturePullDistance by remember { mutableStateOf(0f) }
    var showDisableDeliveryDialog by remember { mutableStateOf(false) }

    val canTriggerRefresh = !uiState.isLoading &&
        !uiState.isRefreshing &&
        uiState.actionRequestId == null &&
        !uiState.isUpdatingDeliveryMode
    val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0

    val refreshData = {
        viewModel.loadManagementData(
            branchId = branchId,
            branchUsesAppMessaging = uiState.branchUsesAppMessaging,
            isManualRefresh = true
        )
        viewModel.loadEntryPoint(
            branchId = branchId,
            branchUsesAppMessaging = uiState.branchUsesAppMessaging
        )
    }

    LaunchedEffect(branchId, initialBranchUsesAppMessaging) {
        viewModel.loadManagementData(
            branchId = branchId,
            branchUsesAppMessaging = initialBranchUsesAppMessaging
        )
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val message = uiState.errorMessage ?: uiState.successMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Choferes vinculados",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = branchName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = refreshData,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(canTriggerRefresh, isAtTop) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (!canTriggerRefresh || !isAtTop) return@detectVerticalDragGestures

                            if (dragAmount > 0f) {
                                gesturePullDistance += dragAmount
                                if (gesturePullDistance >= DELIVERY_PULL_REFRESH_THRESHOLD_PX) {
                                    refreshData()
                                    gesturePullDistance = 0f
                                }
                            } else if (dragAmount < 0f) {
                                gesturePullDistance = 0f
                            }
                        },
                        onDragEnd = { gesturePullDistance = 0f },
                        onDragCancel = { gesturePullDistance = 0f }
                    )
                }
        ) {
            if (uiState.isLoading && uiState.pendingRequests.isEmpty() && uiState.linkedDrivers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        DeliveryStatusBanner(
                            hasOwnDelivery = !uiState.branchUsesAppMessaging,
                            pendingRequests = uiState.pendingRequestCount
                        )
                    }

                    item {
                        SummaryRow(
                            pendingCount = uiState.pendingRequestCount,
                            linkedCount = uiState.linkedDrivers.size
                        )
                    }

                    item {
                        SectionTitle("Solicitudes pendientes")
                    }

                    if (uiState.pendingRequests.isEmpty()) {
                        item {
                            EmptyStateText("No hay solicitudes pendientes")
                        }
                    } else {
                        items(uiState.pendingRequests, key = { it.id }) { request ->
                            PendingRequestCard(
                                request = request,
                                isActionLoading = uiState.actionRequestId == request.id,
                                onAccept = {
                                    viewModel.respondToRequest(
                                        branchId = branchId,
                                        requestId = request.id,
                                        accept = true,
                                        onDeliveryModeEnabled = onDeliveryModeEnabled
                                    )
                                },
                                onReject = {
                                    viewModel.respondToRequest(
                                        branchId = branchId,
                                        requestId = request.id,
                                        accept = false
                                    )
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionTitle("Choferes ya vinculados")
                    }

                    if (uiState.linkedDrivers.isEmpty()) {
                        item {
                            EmptyStateText("Aun no tienes choferes vinculados")
                        }
                    } else {
                        items(uiState.linkedDrivers, key = { it.deliveryPerson.id }) { linkedDriver ->
                            LinkedDriverCard(linkedDriver = linkedDriver)
                        }
                    }

                    if (uiState.processedRequests.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            SectionTitle("Solicitudes procesadas")
                        }
                        items(uiState.processedRequests, key = { it.id }) { request ->
                            ProcessedRequestCard(request = request)
                        }
                    }

                    if (!uiState.branchUsesAppMessaging) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            DeactivateOwnDeliverySection(
                                isProcessing = uiState.isUpdatingDeliveryMode,
                                onDeactivateClick = { showDisableDeliveryDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDisableDeliveryDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDisableDeliveryDialog = false },
            title = { Text("Desactivar delivery propio") },
            text = {
                Text("Se desactivara el delivery propio y la sucursal volvera a usar mensajeria de la app. Deseas continuar?")
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showDisableDeliveryDialog = false
                        viewModel.disableOwnDeliveryForBranch(
                            branchId = branchId,
                            onDeliveryModeChanged = onDeliveryModeEnabled
                        )
                    }
                ) {
                    Text("Desactivar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDisableDeliveryDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun DeliveryStatusBanner(
    hasOwnDelivery: Boolean,
    pendingRequests: Int
) {
    val containerColor = if (hasOwnDelivery) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
    }

    val title = if (hasOwnDelivery) {
        "Delivery propio activo"
    } else {
        "Delivery de la app activo"
    }

    val description = if (hasOwnDelivery) {
        "Tus solicitudes aceptadas quedaran vinculadas a esta sucursal."
    } else if (pendingRequests > 0) {
        "Si aceptas una solicitud, se activara delivery propio automaticamente."
    } else {
        "Puedes activar delivery propio aceptando una solicitud de chofer."
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DeliveryDining,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    pendingCount: Int,
    linkedCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Pendientes",
            value = pendingCount.toString(),
            toneColor = MaterialTheme.colorScheme.tertiary
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Vinculados",
            value = linkedCount.toString(),
            toneColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    toneColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = toneColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = toneColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeactivateOwnDeliverySection(
    isProcessing: Boolean,
    onDeactivateClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Desactivar delivery propio",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Al desactivarlo, la sucursal vuelve a usar mensajeria de la app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onDeactivateClick,
                enabled = !isProcessing,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Desactivar ahora")
                }
            }
        }
    }
}

@Composable
private fun PendingRequestCard(
    request: BranchDeliveryRequest,
    isActionLoading: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            DriverIdentity(request = request)

            request.message?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$note\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isActionLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isActionLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rechazar")
                    }
                }

                Button(
                    onClick = onAccept,
                    enabled = !isActionLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isActionLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkedDriverCard(
    linkedDriver: LinkedDriverSummary
) {
    Card(
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DriverMiniIdentity(linkedDriver)
                OnlineStatusChip(isOnline = linkedDriver.deliveryPerson.isOnline)
            }
        }
    }
}

@Composable
private fun ProcessedRequestCard(
    request: BranchDeliveryRequest
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.deliveryPerson?.name ?: "Chofer",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    request.deliveryPerson?.phone?.let { phone ->
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(request.status)
            }
        }
    }
}

@Composable
private fun DriverIdentity(request: BranchDeliveryRequest) {
    val driver = request.deliveryPerson
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = driver?.name ?: "Chofer",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = driver?.phone ?: "Sin telefono",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFFB300)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${driver?.rating ?: 0.0} (${driver?.totalDeliveries ?: 0} entregas)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            driver?.vehicleType?.let { vehicleType ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Vehiculo: ${vehicleType.toVehicleLabel()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        StatusChip(request.status)
    }
}

@Composable
private fun RowScope.DriverMiniIdentity(linkedDriver: LinkedDriverSummary) {
    val driver = linkedDriver.deliveryPerson
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = driver.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = driver.phone,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${driver.vehicleType.toVehicleLabel()} - ${driver.totalDeliveries} entregas",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OnlineStatusChip(isOnline: Boolean) {
    val color = if (isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(color = color, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOnline) "En linea" else "Sin conexion",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun StatusChip(status: DeliveryRequestStatus) {
    val (containerColor, textColor) = when (status) {
        DeliveryRequestStatus.PENDING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f) to MaterialTheme.colorScheme.tertiary
        DeliveryRequestStatus.ACCEPTED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) to MaterialTheme.colorScheme.primary
        DeliveryRequestStatus.REJECTED -> MaterialTheme.colorScheme.error.copy(alpha = 0.14f) to MaterialTheme.colorScheme.error
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = status.toLabel(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
}

@Composable
private fun EmptyStateText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
