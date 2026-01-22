package com.llego.business.wallet.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llego.business.wallet.data.model.WalletCurrency
import com.llego.business.wallet.ui.components.*
import com.llego.business.wallet.ui.viewmodel.WalletViewModel
import com.llego.business.wallet.ui.viewmodel.WalletUiState
import com.llego.shared.ui.theme.*

/**
 * Pantalla principal de Wallet para negocios
 * Muestra el balance, transacciones y opciones de retiro
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateBack: () -> Unit,
    branchId: String?,
    viewModel: WalletViewModel = viewModel { WalletViewModel(branchId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val earningsSummary by viewModel.earningsSummary.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val showWithdrawalSheet by viewModel.showWithdrawalSheet.collectAsState()
    val showHistorySheet by viewModel.showHistorySheet.collectAsState()
    val showTransferSheet by viewModel.showTransferSheet.collectAsState()
    val showReportsSheet by viewModel.showReportsSheet.collectAsState()

    var animateContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateContent = true
    }

    LaunchedEffect(branchId) {
        viewModel.setBranchId(branchId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val paddingValues = PaddingValues(0.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Fondo con gradiente usando colores del theme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )

            // Contenido principal
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                // Balance Cards
                item {
                    AnimatedVisibility(
                        visible = animateContent,
                        enter = fadeIn(tween(550, delayMillis = 50)) +
                                slideInVertically(tween(550, delayMillis = 50)) { it / 4 }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Cards de balance por moneda
                            WalletCurrency.entries.forEach { currency ->
                                WalletBalanceCard(
                                    currency = currency,
                                    balance = wallet?.balances?.get(currency) ?: 0.0,
                                    isSelected = selectedCurrency == currency,
                                    onClick = { viewModel.selectCurrency(currency) }
                                )
                            }

                            // Chips selectores
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                            ) {
                                WalletCurrency.entries.forEach { currency ->
                                    CurrencyChip(
                                        currency = currency,
                                        isSelected = selectedCurrency == currency,
                                        onClick = { viewModel.selectCurrency(currency) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Acciones rapidas
                item {
                    AnimatedVisibility(
                        visible = animateContent,
                        enter = fadeIn(tween(550, delayMillis = 150)) +
                                slideInVertically(tween(550, delayMillis = 150)) { it / 4 }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Acciones rapidas",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                Surface(
                                    shape = LlegoShapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = selectedCurrency.code,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Primera fila de acciones - Colores personalizados
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                WalletActionButton(
                                    icon = Icons.Default.AccountBalance,
                                    label = "Retirar",
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    onClick = { viewModel.showWithdrawalSheet() },
                                    modifier = Modifier.weight(1f)
                                )

                                WalletActionButton(
                                    icon = Icons.Default.SwapHoriz,
                                    label = "Transferir",
                                    iconColor = MaterialTheme.colorScheme.secondary,
                                    onClick = { viewModel.showTransferSheet() },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Segunda fila de acciones - Colores personalizados
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                WalletActionButton(
                                    icon = Icons.Default.History,
                                    label = "Historial",
                                    iconColor = MaterialTheme.colorScheme.tertiary,
                                    onClick = { viewModel.showHistorySheet() },
                                    modifier = Modifier.weight(1f)
                                )

                                WalletActionButton(
                                    icon = Icons.Default.Assessment,
                                    label = "Reportes",
                                    iconColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                    onClick = { viewModel.showReportsSheet() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Resumen de ganancias
                item {
                    AnimatedVisibility(
                        visible = animateContent,
                        enter = fadeIn(tween(550, delayMillis = 200)) +
                                slideInVertically(tween(550, delayMillis = 200)) { it / 4 }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            earningsSummary["today"]?.let { summary ->
                                EarningsSummaryCard(summary = summary)
                            }
                        }
                    }
                }

                // Transacciones recientes
                item {
                    AnimatedVisibility(
                        visible = animateContent && transactions.isNotEmpty(),
                        enter = fadeIn(tween(550, delayMillis = 250)) +
                                slideInVertically(tween(550, delayMillis = 250)) { it / 4 }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Transacciones recientes",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                TextButton(onClick = { viewModel.showHistorySheet() }) {
                                    Text("Ver todas")
                                }
                            }
                        }
                    }
                }

                // Lista de transacciones
                items(transactions.take(5)) { transaction ->
                    AnimatedVisibility(
                        visible = animateContent,
                        enter = fadeIn(tween(400)) + expandVertically()
                    ) {
                        TransactionItem(
                            transaction = transaction,
                            onClick = { /* TODO: Ver detalles */ }
                        )
                    }
                }

                // Cards informativos
                item {
                    AnimatedVisibility(
                        visible = animateContent,
                        enter = fadeIn(tween(550, delayMillis = 250)) +
                                slideInVertically(tween(550, delayMillis = 250)) { it / 4 }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            WalletInfoCard(
                                icon = Icons.Default.Security,
                                title = "Transacciones seguras",
                                description = "Proteccion bancaria nivel 256-bit",
                                iconColor = MaterialTheme.colorScheme.primary
                            )

                            WalletInfoCard(
                                icon = Icons.Default.Speed,
                                title = "Retiros rapidos",
                                description = "Procesa tus retiros en 24-48 horas",
                                iconColor = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Espaciado final reducido
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Mensaje de exito flotante
            successMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Card(
                        shape = LlegoCustomShapes.infoCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            // Indicador de carga
            if (uiState is WalletUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    // Sheets/Modales
    // Sheet de retiro
    if (showWithdrawalSheet) {
        WithdrawalSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.hideWithdrawalSheet() }
        )
    }

    // Sheet de historial
    if (showHistorySheet) {
        TransactionHistorySheet(
            transactions = transactions,
            onDismiss = { viewModel.hideHistorySheet() }
        )
    }

    // Sheet de transferencia
    if (showTransferSheet) {
        TransferSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.hideTransferSheet() }
        )
    }

    // Sheet de reportes
    if (showReportsSheet) {
        ReportsSheet(
            earningsSummary = earningsSummary,
            selectedCurrency = selectedCurrency,
            onDismiss = { viewModel.hideReportsSheet() }
        )
    }

    // Snackbar de error
    if (uiState is WalletUiState.Error) {
        val errorMessage = (uiState as WalletUiState.Error).message
        LaunchedEffect(errorMessage) {
            // TODO: Mostrar snackbar con el mensaje
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
}
