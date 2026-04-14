@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.llego.business.orders.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderItem
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.shared.ui.components.NetworkImage
import com.llego.shared.utils.formatDouble
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlin.time.Clock

@Composable
fun OrderStatusSection(order: Order) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (order.status) {
                    OrderStatus.AWAITING_DELIVERY_ACCEPTANCE -> Icons.Default.LocalShipping
                    OrderStatus.PENDING_PAYMENT -> Icons.Default.HourglassEmpty
                    OrderStatus.PAYMENT_IN_PROGRESS -> Icons.Default.Timer
                    OrderStatus.PENDING_ACCEPTANCE -> Icons.Default.HourglassEmpty
                    OrderStatus.MODIFIED_BY_STORE -> Icons.Default.Edit
                    OrderStatus.REJECTED_BY_STORE -> Icons.Default.Cancel
                    OrderStatus.ACCEPTED -> Icons.Default.CheckCircle
                    OrderStatus.PREPARING -> Icons.Default.Restaurant
                    OrderStatus.READY_FOR_PICKUP -> Icons.Default.Done
                    OrderStatus.ON_THE_WAY -> Icons.Default.LocalShipping
                    OrderStatus.DELIVERED -> Icons.Default.CheckCircle
                    OrderStatus.CANCELLED -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = order.status.getColor(),
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estado actual",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = order.status.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = order.status.getColor()
                )
                val showDeadline = order.status in listOf(
                    OrderStatus.PENDING_ACCEPTANCE,
                    OrderStatus.MODIFIED_BY_STORE,
                    OrderStatus.REJECTED_BY_STORE,
                    OrderStatus.AWAITING_DELIVERY_ACCEPTANCE,
                    OrderStatus.PENDING_PAYMENT,
                    OrderStatus.PAYMENT_IN_PROGRESS
                )
                val countdownText = rememberDeadlineCountdownText(
                    deadlineAt = order.deadlineAt,
                    enabled = showDeadline
                )
                if (countdownText != null) {
                    Text(
                        text = "Limite: $countdownText",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (countdownText == "Vencido") MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            order.estimatedMinutesRemaining?.let { minutes ->
                if (minutes > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$minutes min",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemsSection(items: List<OrderItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Items del pedido",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        items.forEach { item ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val itemImageUrl = item.imageUrl?.takeIf { it.isNotBlank() }
                        ?: item.previewProducts.firstOrNull()?.imageUrl?.takeIf { it.isNotBlank() }
                    if (!itemImageUrl.isNullOrBlank()) {
                        NetworkImage(
                            url = itemImageUrl,
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            if (item.wasModifiedByStore) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Modificado",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (!item.itemType.equals("PRODUCT", ignoreCase = true)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = item.itemType.uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (item.hasGift) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Incluye regalo",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Cantidad: ${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        item.requestDescription?.takeIf { it.isNotBlank() }?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (item.comboSelections.isNotEmpty()) {
                            item.comboSelections.forEach { slot ->
                                Text(
                                    text = slot.slotName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                slot.selectedOptions.forEach { option ->
                                    val modifiers = option.modifiers.joinToString { it.name }
                                    val optionText = buildString {
                                        append("- ${option.quantity}x ${option.name}")
                                        if (option.priceAdjustment != 0.0) {
                                            append(" (+${formatDouble("%.2f", option.priceAdjustment)})")
                                        }
                                        if (modifiers.isNotBlank()) {
                                            append(" [$modifiers]")
                                        }
                                    }
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (!item.discountType.isNullOrBlank() && (item.discountValue ?: 0.0) > 0.0) {
                            Text(
                                text = "Descuento ${item.discountType}: ${formatDouble("%.2f", item.discountValue ?: 0.0)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = if (item.isShowcase && item.lineTotal <= 0.0) {
                            "Por confirmar"
                        } else {
                            "$${formatDouble("%.2f", item.lineTotal)}"
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentSummarySection(order: Order) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Resumen de pago",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${order.currency} ${formatDouble("%.2f", order.subtotal)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (order.deliveryFee > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Envio", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${order.currency} ${formatDouble("%.2f", order.deliveryFee)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Modalidad", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (order.isPickupOrder()) "Recogida en tienda" else "Delivery",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        order.discounts.forEach { discount ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = discount.title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = discount.type.getDisplayName(),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "-${order.currency} ${formatDouble("%.2f", discount.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Metodo de pago", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = order.paymentMethodDisplayNameWithCurrency(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = order.paymentStatus.getColor().copy(alpha = 0.1f)
                ) {
                    Text(
                        text = order.paymentStatus.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = order.paymentStatus.getColor()
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${order.currency} ${formatDouble("%.2f", order.total)}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
internal fun rememberDeadlineCountdownText(deadlineAt: String?, enabled: Boolean): String? {
    if (!enabled || deadlineAt.isNullOrBlank()) return null

    val deadlineEpochSeconds = remember(deadlineAt) { parseDeadlineToEpochSeconds(deadlineAt) }
        ?: return null

    var nowEpochSeconds by remember(deadlineAt) {
        mutableStateOf(Clock.System.now().toEpochMilliseconds() / 1000L)
    }

    LaunchedEffect(deadlineAt, enabled) {
        if (!enabled) return@LaunchedEffect
        while (true) {
            nowEpochSeconds = Clock.System.now().toEpochMilliseconds() / 1000L
            delay(1000)
        }
    }

    val remainingSeconds = deadlineEpochSeconds - nowEpochSeconds
    return if (remainingSeconds <= 0L) "Vencido" else formatCountdown(remainingSeconds)
}

private fun parseDeadlineToEpochSeconds(rawDeadline: String): Long? {
    val value = rawDeadline.trim()
    if (value.isBlank()) return null

    val candidates = if (!value.endsWith("Z") && !value.contains("+")) {
        listOf(value, "${value}Z")
    } else {
        listOf(value)
    }

    for (candidate in candidates) {
        try {
            return Instant.parse(candidate).toEpochMilliseconds() / 1000L
        } catch (_: Exception) {
            // Keep trying with the next candidate format.
        }
    }

    return null
}

private fun formatCountdown(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val mm = minutes.toString().padStart(2, '0')
    val ss = seconds.toString().padStart(2, '0')

    return if (hours > 0) {
        val hh = hours.toString().padStart(2, '0')
        "$hh:$mm:$ss restantes"
    } else {
        "$mm:$ss restantes"
    }
}
