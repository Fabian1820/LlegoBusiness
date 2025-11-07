package com.llego.nichos.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.nichos.common.data.model.*
import com.llego.nichos.common.ui.viewmodel.WalletViewModel
import com.llego.nichos.common.ui.viewmodel.WalletUiState
import com.llego.nichos.common.utils.formatToTwoDecimals
import com.llego.shared.ui.theme.*

/**
 * Sheet modal para solicitar retiros
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalSheet(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val withdrawalAmount by viewModel.withdrawalAmount.collectAsState()
    val withdrawalMethod by viewModel.withdrawalMethod.collectAsState()
    val accountDetails by viewModel.accountDetails.collectAsState()
    val balance = viewModel.getBalance(selectedCurrency)

    val isLoading = uiState is WalletUiState.Loading
    val error = (uiState as? WalletUiState.Error)?.message

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f) // 3/4 de la pantalla
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = when (selectedCurrency) {
                                WalletCurrency.USD -> LlegoPrimary.copy(alpha = 0.15f)
                                WalletCurrency.CUP -> LlegoTertiary.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = when (selectedCurrency) {
                            WalletCurrency.USD -> LlegoPrimary
                            WalletCurrency.CUP -> LlegoTertiary
                        }
                    )
                }

                Text(
                    text = "Solicitar retiro",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Retira tu saldo de forma segura a tu cuenta bancaria o método de pago preferido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            // Balance disponible
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo disponible en ${selectedCurrency.code}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${selectedCurrency.symbol}${balance.formatToTwoDecimals()}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Monto a retirar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Monto a retirar",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                OutlinedTextField(
                    value = withdrawalAmount,
                    onValueChange = { viewModel.updateWithdrawalAmount(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Text(
                            text = selectedCurrency.symbol,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Botones de monto rápido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100.0, 250.0, 500.0, balance).forEach { amount ->
                        OutlinedButton(
                            onClick = {
                                viewModel.updateWithdrawalAmount(
                                    amount.formatToTwoDecimals()
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (amount == balance) "Todo" else "${amount.toInt()}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Método de retiro
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Método de retiro",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                WithdrawalMethod.entries.forEach { method ->
                    WithdrawalMethodOption(
                        method = method,
                        isSelected = withdrawalMethod == method,
                        onClick = { viewModel.updateWithdrawalMethod(method) }
                    )
                }
            }

            // Detalles de la cuenta
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when (withdrawalMethod) {
                        WithdrawalMethod.BANK_TRANSFER -> "Número de cuenta bancaria"
                        WithdrawalMethod.MOBILE_PAYMENT -> "Número de teléfono"
                        WithdrawalMethod.CASH_PICKUP -> "Nombre completo"
                        WithdrawalMethod.INTERNAL_TRANSFER -> "Usuario Llego"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                OutlinedTextField(
                    value = accountDetails,
                    onValueChange = { viewModel.updateAccountDetails(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            when (withdrawalMethod) {
                                WithdrawalMethod.BANK_TRANSFER -> "Ej: 1234567890"
                                WithdrawalMethod.MOBILE_PAYMENT -> "Ej: +53 5 123 4567"
                                WithdrawalMethod.CASH_PICKUP -> "Ej: Juan Pérez"
                                WithdrawalMethod.INTERNAL_TRANSFER -> "Ej: usuario123"
                            }
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Mensaje de error
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Botón de confirmar
            Button(
                onClick = { viewModel.requestWithdrawal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && withdrawalAmount.isNotBlank() && accountDetails.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedCurrency) {
                        WalletCurrency.USD -> LlegoPrimary
                        WalletCurrency.CUP -> LlegoTertiary
                    }
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Solicitar retiro",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // Información adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Los retiros se procesan en un plazo de 24-48 horas hábiles. Se te notificará cuando el dinero esté disponible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Opción de método de retiro
 */
@Composable
private fun WithdrawalMethodOption(
    method: WithdrawalMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                LlegoPrimary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(LlegoPrimary),
                width = 2.dp
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (method) {
                        WithdrawalMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                        WithdrawalMethod.MOBILE_PAYMENT -> Icons.Default.PhoneAndroid
                        WithdrawalMethod.CASH_PICKUP -> Icons.Default.Payments
                        WithdrawalMethod.INTERNAL_TRANSFER -> Icons.Default.SwapHoriz
                    },
                    contentDescription = null,
                    tint = if (isSelected) LlegoPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isSelected) LlegoPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = LlegoPrimary
                )
            }
        }
    }
}

/**
 * Sheet de historial de transacciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistorySheet(
    transactions: List<WalletTransaction>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f) // 3/4 de la pantalla
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Historial de transacciones",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                // Filtros (TODO: Implementar funcionalidad)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { /* TODO */ },
                        label = { Text("Todas") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO */ },
                        label = { Text("Ingresos") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO */ },
                        label = { Text("Retiros") }
                    )
                }
            }

            // Lista de transacciones scrolleable
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "No hay transacciones",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { /* TODO: Ver detalles */ }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sheet de transferencia entre cuentas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSheet(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val balance = viewModel.getBalance(selectedCurrency)

    var recipientAccount by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = LlegoSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = LlegoSecondary
                    )
                }

                Text(
                    text = "Transferir fondos",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Envía dinero a otra cuenta Llego de forma instantánea",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Balance disponible
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo disponible",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${selectedCurrency.symbol}${balance.formatToTwoDecimals()}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LlegoPrimary
                        )
                    )
                }
            }

            // Cuenta destino
            OutlinedTextField(
                value = recipientAccount,
                onValueChange = { recipientAccount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cuenta Llego destino") },
                placeholder = { Text("usuario@llego") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Monto
            OutlinedTextField(
                value = transferAmount,
                onValueChange = { transferAmount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monto a transferir") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Text(
                        text = selectedCurrency.symbol,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp)
            )

            // Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LlegoPrimary.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = LlegoPrimary
                    )
                    Text(
                        text = "Las transferencias entre cuentas Llego son instantáneas y sin comisión.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Botón
            Button(
                onClick = { /* TODO: Implementar transferencia */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = recipientAccount.isNotBlank() && transferAmount.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LlegoSecondary
                )
            ) {
                Text(
                    text = "Transferir",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = LlegoPrimary
                )
            }
        }
    }
}

/**
 * Sheet de reportes y estadísticas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsSheet(
    earningsSummary: Map<String, com.llego.nichos.common.data.model.EarningsSummary>,
    selectedCurrency: com.llego.nichos.common.data.model.WalletCurrency,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reportes",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = selectedCurrency.code,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Text(
                    text = "Resumen de tus ingresos y transacciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Reportes scrolleables
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Resumen de hoy
                earningsSummary["today"]?.let { summary ->
                    item {
                        com.llego.nichos.common.ui.components.EarningsSummaryCard(summary = summary)
                    }
                }

                // Resumen de la semana
                earningsSummary["week"]?.let { summary ->
                    item {
                        com.llego.nichos.common.ui.components.EarningsSummaryCard(summary = summary)
                    }
                }

                // Resumen del mes
                earningsSummary["month"]?.let { summary ->
                    item {
                        com.llego.nichos.common.ui.components.EarningsSummaryCard(summary = summary)
                    }
                }

                // Card de acciones
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = LlegoPrimary.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = LlegoPrimary
                                )
                                Text(
                                    text = "Exportar reporte",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Text(
                                text = "Descarga un reporte detallado de tus transacciones en formato PDF o Excel.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = LlegoPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("PDF")
                                }

                                OutlinedButton(
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = LlegoPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Excel")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
