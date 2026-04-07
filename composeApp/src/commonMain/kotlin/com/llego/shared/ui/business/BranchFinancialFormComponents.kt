package com.llego.shared.ui.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.QrPayment
import com.llego.shared.data.model.TransferAccount
import com.llego.shared.data.model.TransferPhone

@Composable
fun TransferAccountsEditor(
    accounts: List<TransferAccount>,
    onAccountsChange: (List<TransferAccount>) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Cuentas para transferencias"
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = "Agrega cada cuenta con tarjeta, telefono de confirmacion y titular opcional.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        accounts.forEachIndexed { index, account ->
            key(index) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cuenta ${index + 1}",
                                style = MaterialTheme.typography.labelMedium
                            )
                            IconButton(
                                onClick = {
                                    onAccountsChange(accounts.filterIndexed { currentIndex, _ -> currentIndex != index })
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar cuenta"
                                )
                            }
                        }

                        OutlinedTextField(
                            value = account.cardNumber,
                            onValueChange = { value ->
                                val normalized = value.filter { it.isDigit() }.take(16)
                                onAccountsChange(
                                    accounts.updated(index, account.copy(cardNumber = normalized))
                                )
                            },
                            label = { Text("Numero de tarjeta (16 digitos)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = account.confirmPhone,
                            onValueChange = { value ->
                                val normalized = value.filter { it.isDigit() || it == '+' }
                                onAccountsChange(
                                    accounts.updated(index, account.copy(confirmPhone = normalized))
                                )
                            },
                            label = { Text("Telefono de confirmacion (8 digitos)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        OutlinedTextField(
                            value = account.cardHolderName.orEmpty(),
                            onValueChange = { value ->
                                onAccountsChange(
                                    accounts.updated(index, account.copy(cardHolderName = value))
                                )
                            },
                            label = { Text("Titular (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (!account.pagoQr.isNullOrBlank()) {
                            Text(
                                text = "Pago QR: ${account.pagoQr}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Activa",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Switch(
                                checked = account.isActive,
                                onCheckedChange = { checked ->
                                    onAccountsChange(
                                        accounts.updated(index, account.copy(isActive = checked))
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = {
                onAccountsChange(
                    accounts + TransferAccount(
                        cardNumber = "",
                        confirmPhone = "",
                        cardHolderName = "",
                        isActive = true
                    )
                )
            }
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Text("Agregar cuenta", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun QrPaymentsEditor(
    qrPayments: List<QrPayment>,
    onQrPaymentsChange: (List<QrPayment>) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Pagos QR"
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )

        qrPayments.forEachIndexed { index, qrPayment ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = qrPayment.value,
                        onValueChange = { value ->
                            onQrPaymentsChange(
                                qrPayments.updated(index, qrPayment.copy(value = value))
                            )
                        },
                        label = { Text("Valor QR") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            onQrPaymentsChange(qrPayments.filterIndexed { currentIndex, _ -> currentIndex != index })
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar QR"
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = {
                onQrPaymentsChange(
                    qrPayments + QrPayment(value = "", isActive = true)
                )
            }
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Text("Agregar QR", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun TransferPhonesEditor(
    phones: List<TransferPhone>,
    onPhonesChange: (List<TransferPhone>) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Telefonos de transferencia"
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )

        phones.forEachIndexed { index, transferPhone ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = transferPhone.phone,
                        onValueChange = { value ->
                            onPhonesChange(
                                phones.updated(index, transferPhone.copy(phone = value))
                            )
                        },
                        label = { Text("Telefono") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            onPhonesChange(phones.filterIndexed { currentIndex, _ -> currentIndex != index })
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar telefono"
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = {
                onPhonesChange(
                    phones + TransferPhone(phone = "", isActive = true)
                )
            }
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Text("Agregar telefono", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

private fun <T> List<T>.updated(index: Int, value: T): List<T> {
    return mapIndexed { currentIndex, currentValue ->
        if (currentIndex == index) value else currentValue
    }
}
