package com.llego.business.wallet.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.business.wallet.data.model.*
import com.llego.business.wallet.util.formatToTwoDecimals
import com.llego.shared.ui.theme.*

/**
 * Card de balance de wallet con diseño visual atractivo
 */
@Composable
fun WalletBalanceCard(
    currency: WalletCurrency,
    balance: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(300)
    )

    val gradientColors = when (currency) {
        WalletCurrency.USD -> listOf(LlegoPrimary, LlegoPrimary.copy(alpha = 0.85f))
        WalletCurrency.CUP -> listOf(LlegoTertiary, LlegoTertiary.copy(alpha = 0.82f))
    }

    Card(
        modifier = modifier
            .height(220.dp)
            .clickable(onClick = onClick)
            .shadow(
                elevation = if (isSelected) 20.dp else 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = gradientColors[0].copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors
                    )
                )
        ) {
            // Decoración circular superior derecha
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 110.dp, y = (-90).dp)
                    .background(
                        color = when (currency) {
                            WalletCurrency.USD -> Color.White.copy(alpha = 0.12f)
                            WalletCurrency.CUP -> LlegoSecondary.copy(alpha = 0.28f)
                        },
                        shape = CircleShape
                    )
            )

            // Decoración circular inferior izquierda
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = (-90).dp, y = 110.dp)
                    .background(
                        color = when (currency) {
                            WalletCurrency.USD -> LlegoSecondary.copy(alpha = 0.18f)
                            WalletCurrency.CUP -> LlegoAccent.copy(alpha = 0.18f)
                        },
                        shape = CircleShape
                    )
            )

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Llego Wallet Business",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                        Text(
                            text = when (currency) {
                                WalletCurrency.USD -> "Balance disponible"
                                WalletCurrency.CUP -> "Saldo en CUP"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = LlegoSecondary
                    )
                }

                // Balance
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = currency.symbol,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = balance.formatToTwoDecimals(),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = currency.code,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                }

                // Decoración de puntos
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(6) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = Color.White.copy(
                                        alpha = if (index % 2 == 0) 0.35f else 0.25f
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Botón de acción rápida con icono y texto
 */
@Composable
fun WalletActionButton(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = iconColor
                    )
                }

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

/**
 * Card de información con icono y texto
 */
@Composable
fun WalletInfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    iconColor: Color = LlegoPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
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
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = iconColor
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Chip selector de moneda
 */
@Composable
fun CurrencyChip(
    currency: WalletCurrency,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        when (currency) {
            WalletCurrency.USD -> LlegoPrimary
            WalletCurrency.CUP -> LlegoTertiary
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = "Cuenta ${currency.code}",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = textColor
        )
    }
}

/**
 * Item de transacción en la lista
 */
@Composable
fun TransactionItem(
    transaction: WalletTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icono según tipo de transacción
                val (icon, iconColor) = when (transaction.type) {
                    TransactionType.INCOME -> Icons.Default.Add to LlegoSuccess
                    TransactionType.WITHDRAWAL -> Icons.Default.Remove to LlegoWarning
                    TransactionType.TRANSFER -> Icons.Default.SwapHoriz to LlegoPrimary
                    TransactionType.REFUND -> Icons.Default.Undo to LlegoError
                    TransactionType.FEE -> Icons.Default.Receipt to LlegoOnSurfaceVariant
                    TransactionType.ADJUSTMENT -> Icons.Default.Edit to LlegoSecondary
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = iconColor
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = transaction.type.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        // Badge de estado
                        if (transaction.status != TransactionStatus.COMPLETED) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (transaction.status) {
                                    TransactionStatus.PENDING -> LlegoWarning.copy(alpha = 0.15f)
                                    TransactionStatus.PROCESSING -> LlegoInfo.copy(alpha = 0.15f)
                                    TransactionStatus.FAILED -> LlegoError.copy(alpha = 0.15f)
                                    TransactionStatus.CANCELLED -> LlegoOnSurfaceVariant.copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                }
                            ) {
                                Text(
                                    text = transaction.status.displayName,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (transaction.status) {
                                        TransactionStatus.PENDING -> LlegoWarning
                                        TransactionStatus.PROCESSING -> LlegoInfo
                                        TransactionStatus.FAILED -> LlegoError
                                        TransactionStatus.CANCELLED -> LlegoOnSurfaceVariant
                                        else -> LlegoPrimary
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Monto
            Text(
                text = "${transaction.currency.symbol}${kotlin.math.abs(transaction.amount).formatToTwoDecimals()}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.amount >= 0) {
                        LlegoSuccess
                    } else {
                        LlegoError
                    }
                )
            )
        }
    }
}

/**
 * Card de resumen de ganancias
 */
@Composable
fun EarningsSummaryCard(
    summary: EarningsSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = when (summary.period) {
                    "today" -> "Ingresos de hoy"
                    "week" -> "Ingresos de esta semana"
                    "month" -> "Ingresos de este mes"
                    else -> "Ingresos"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            // Ingresos netos destacados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ingresos netos",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${summary.currency.symbol}${summary.netEarnings.formatToTwoDecimals()}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = LlegoSuccess
                    )
                )
            }

            Divider()

            // Detalles
            SummaryRow("Ingresos totales", summary.totalIncome, summary.currency, true)
            SummaryRow("Comisiones", summary.totalFees, summary.currency, false)
            SummaryRow("Retiros", summary.totalWithdrawals, summary.currency, false)

            // Cantidad de transacciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Transacciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${summary.transactionCount}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    amount: Double,
    currency: WalletCurrency,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "${if (!isPositive && amount > 0) "-" else ""}${currency.symbol}${amount.formatToTwoDecimals()}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (isPositive) LlegoSuccess else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
