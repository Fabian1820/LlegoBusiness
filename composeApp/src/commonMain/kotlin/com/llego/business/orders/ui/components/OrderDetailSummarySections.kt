package com.llego.business.orders.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderComboSelectedOption
import com.llego.business.orders.data.model.OrderComboSelection
import com.llego.business.orders.data.model.OrderItem
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.shared.ui.components.NetworkImage
import com.llego.shared.data.network.ServerClock
import com.llego.shared.utils.formatDouble
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

@Composable
fun OrderStatusSection(order: Order) {
    val statusColor = order.status.getColor()
    val statusIcon = when (order.status) {
        OrderStatus.AWAITING_DELIVERY_ACCEPTANCE -> Icons.Default.LocalShipping
        OrderStatus.PENDING_PAYMENT             -> Icons.Default.HourglassEmpty
        OrderStatus.PAYMENT_IN_PROGRESS         -> Icons.Default.Timer
        OrderStatus.PENDING_ACCEPTANCE          -> Icons.Default.HourglassEmpty
        OrderStatus.MODIFIED_BY_STORE           -> Icons.Default.Edit
        OrderStatus.REJECTED_BY_STORE           -> Icons.Default.Cancel
        OrderStatus.ACCEPTED                    -> Icons.Default.CheckCircle
        OrderStatus.PREPARING                   -> Icons.Default.Restaurant
        OrderStatus.READY_FOR_PICKUP            -> Icons.Default.Done
        OrderStatus.ON_THE_WAY                  -> Icons.Default.LocalShipping
        OrderStatus.DELIVERED                   -> Icons.Default.CheckCircle
        OrderStatus.CANCELLED                   -> Icons.Default.Cancel
    }
    val isPickup = order.isPickupOrder()
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Fila principal: ícono + estado + tiempo estimado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Estado del pedido",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = order.status.getDisplayName(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }

                order.estimatedMinutesRemaining?.let { minutes ->
                    if (minutes > 0) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$minutes min",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Chips secundarios: tipo de entrega + countdown
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Chip tipo de entrega
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPickup) Icons.Default.Restaurant else Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isPickup) "Recogida en tienda" else "Delivery",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Chip countdown (si aplica)
                if (countdownText != null) {
                    val isExpired = countdownText == "Vencido"
                    val chipColor = if (isExpired) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = chipColor.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = chipColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isExpired) "Vencido" else "Límite: $countdownText",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = chipColor
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
            if (item.isCombo) {
                ComboOrderItemCard(item = item)
            } else {
                ProductOrderItemCard(item = item)
            }
        }
    }
}

@Composable
private fun ProductOrderItemCard(item: OrderItem) {
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f, fill = false)
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
                    if (item.isShowcase) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Showcase",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                if (!item.discountType.isNullOrBlank() && (item.discountValue ?: 0.0) > 0.0) {
                    Text(
                        text = "Descuento ${item.discountType}: -${formatDouble("%.2f", item.discountValue ?: 0.0)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
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

@Composable
private fun ComboOrderItemCard(item: OrderItem) {
    var expanded by remember { mutableStateOf(false) }

    val hasDetails = item.comboSelections.isNotEmpty() || item.hasGift

    // Resumen compacto de las selecciones (una línea)
    val selectionSummary = remember(item.comboSelections) {
        item.comboSelections.joinToString(" · ") { slot ->
            slot.selectedOptions.joinToString(", ") { it.name }
        }
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Fila principal: imagen · nombre · precio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                val imageUrl = item.imageUrl?.takeIf { it.isNotBlank() }
                    ?: item.previewProducts.firstOrNull()?.imageUrl?.takeIf { it.isNotBlank() }
                if (!imageUrl.isNullOrBlank()) {
                    NetworkImage(
                        url = imageUrl,
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
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Badges en una línea
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Combo",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                        if (item.hasGift) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
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
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Text(
                        text = "Cantidad: ${item.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    item.requestDescription?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!item.discountType.isNullOrBlank() && (item.discountValue ?: 0.0) > 0.0) {
                        Text(
                            text = "Desc. ${item.discountType}: -${formatDouble("%.2f", item.discountValue ?: 0.0)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "$${formatDouble("%.2f", item.lineTotal)}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Toggle expandir/colapsar (solo si hay detalles que mostrar)
            if (hasDetails) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Ocultar detalles" else "Ver detalles del combo",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Detalles expandidos
                if (expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item.comboSelections.forEach { slot ->
                            ComboSlotDetail(slot = slot)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComboSlotDetail(slot: OrderComboSelection) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            Text(
                text = slot.slotName,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
        }
        slot.selectedOptions.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${option.quantity}x ${option.name}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                    if (option.modifiers.isNotEmpty()) {
                        Text(
                            text = option.modifiers.joinToString(", ") { it.name },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (option.priceAdjustment != 0.0) {
                    val sign = if (option.priceAdjustment > 0) "+" else ""
                    Text(
                        text = "$sign${formatDouble("%.2f", option.priceAdjustment)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                text = formatAmount(order.subtotal, order.currency),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (order.deliveryFee > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Envio", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatAmount(order.deliveryFee, order.currency),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (order.serviceCharge > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cargo de servicio", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatAmount(order.serviceCharge, order.currency),
                    style = MaterialTheme.typography.bodyLarge
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
                    text = "-${formatAmount(discount.amount, order.currency)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Método de pago",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = order.paymentMethodDisplayName(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            val statusColor = order.paymentStatus.getColor()
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = order.paymentStatus.getDisplayName(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = statusColor
                )
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
                text = formatAmount(order.total, order.currency),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
@OptIn(ExperimentalTime::class)
internal fun rememberDeadlineCountdownText(deadlineAt: String?, enabled: Boolean): String? {
    if (!enabled || deadlineAt.isNullOrBlank()) return null

    val deadlineEpochSeconds = remember(deadlineAt) { parseDeadlineToEpochSeconds(deadlineAt) }
        ?: return null

    var nowEpochSeconds by remember(deadlineAt) {
        mutableStateOf(ServerClock.nowMs() / 1000)
    }

    LaunchedEffect(deadlineAt, enabled) {
        if (!enabled) return@LaunchedEffect
        while (true) {
            nowEpochSeconds = ServerClock.nowMs() / 1000
            delay(1000)
        }
    }

    val remainingSeconds = deadlineEpochSeconds - nowEpochSeconds
    return if (remainingSeconds <= 0L) "Vencido" else formatCountdown(remainingSeconds)
}

@OptIn(ExperimentalTime::class)
private fun parseDeadlineToEpochSeconds(rawDeadline: String): Long? {
    val value = rawDeadline.trim()
    if (value.isBlank()) return null

    val candidates = buildList {
        add(value)
        if (!value.endsWith("Z") && !value.contains("+")) {
            add("${value}Z")
        }
    }

    candidates.forEach { candidate ->
        runCatching { Instant.parse(candidate) }
            .getOrNull()
            ?.let { parsed -> return parsed.toEpochMilliseconds() / 1000 }
    }

    return null
}

private fun formatAmount(amount: Double, currency: String): String {
    val formatted = "%.2f".format(amount)
    val parts = formatted.split(".")
    val intPart = parts[0]
    val decPart = if (parts.size > 1) parts[1] else "00"
    val spaced = intPart.reversed().chunked(3).joinToString(" ").reversed()
    return "$spaced.$decPart $currency"
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
