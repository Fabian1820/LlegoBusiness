package com.llego.business.wallet.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.business.wallet.data.model.*
import com.llego.business.wallet.ui.viewmodel.WalletViewModel
import com.llego.business.wallet.ui.viewmodel.WalletUiState
import com.llego.business.wallet.util.formatToTwoDecimals
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    var recipientAccount by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var isTransferring by remember { mutableStateOf(false) }
    var transferError by remember { mutableStateOf<String?>(null) }

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
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = "Transferir fondos",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Envia dinero a otra cuenta Llego de forma instantanea",
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
                            color = MaterialTheme.colorScheme.primary
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
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
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
                        text = "Las transferencias entre cuentas Llego son instantaneas y sin comision.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Error message
            transferError?.let { error ->
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Boton
            Button(
                onClick = {
                    scope.launch {
                        isTransferring = true
                        transferError = null

                        // Validaciones
                        if (recipientAccount.isBlank()) {
                            transferError = "Ingresa un destinatario"
                            isTransferring = false
                            return@launch
                        }

                        val amount = transferAmount.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            transferError = "Monto invalido"
                            isTransferring = false
                            return@launch
                        }

                        if (amount > balance) {
                            transferError = "Saldo insuficiente"
                            isTransferring = false
                            return@launch
                        }

                        // Determinar tipo de bÃºsqueda: email, username o ID
                        val isEmail = recipientAccount.contains("@") && recipientAccount.contains(".")
                        val isUsername = recipientAccount.startsWith("@") ||
                                       (!isEmail && recipientAccount.all { it.isLetterOrDigit() || it == '_' })

                        val currencyStr = when (selectedCurrency) {
                            com.llego.business.wallet.data.model.WalletCurrency.USD -> "usd"
                            com.llego.business.wallet.data.model.WalletCurrency.CUP -> "local"
                        }

                        // Ejecutar transferencia
                        val result = viewModel.transferMoney(
                            toOwnerId = if (!isEmail && !isUsername) recipientAccount else null,
                            toOwnerEmail = if (isEmail) recipientAccount else null,
                            toOwnerUsername = if (isUsername) recipientAccount.removePrefix("@") else null,
                            amount = amount,
                            currency = currencyStr
                        )

                        result.fold(
                            onSuccess = {
                                // Ã‰xito - cerrar sheet
                                onDismiss()
                            },
                            onFailure = { error ->
                                transferError = error.message ?: "Error en la transferencia"
                            }
                        )

                        isTransferring = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isTransferring && recipientAccount.isNotBlank() && transferAmount.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                if (isTransferring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Transferir",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
