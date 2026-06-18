@file:OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@file:Suppress("OPT_IN_USAGE")

package com.llego.business.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BranchTemporaryStatus
import com.llego.shared.data.model.BusinessResult
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Chip compacto que muestra el estado actual de la sucursal (abierto/cerrado/
 * pedidos en pausa) y al tocarlo abre un BottomSheet con controles rápidos.
 */
@Composable
fun BranchStatusChip(
    branch: Branch,
    modifier: Modifier = Modifier,
    onSetAcceptingOrders: suspend (Boolean) -> BusinessResult<Branch>,
    onSetDailyOverride: suspend (
        date: String,
        temporallyClosed: Boolean,
        temporallyOpen: Boolean,
        openTime: String?,
        closeTime: String?
    ) -> BusinessResult<Branch>,
    onClearDailyOverride: suspend () -> BusinessResult<Branch>
) {
    val today = remember { LocalDate.todayIso() }
    val status = remember(branch, today) { branchStatusOf(branch, today) }
    var showSheet by remember { mutableStateOf(false) }

    val (label, bg, fg) = when {
        !branch.acceptingOrders -> Triple("Pedidos en pausa", Color(0xFFFEE2E2), Color(0xFF7F1D1D))
        status.isOpen -> Triple(
            buildString {
                append("Abierto")
                status.hoursText?.let { append(" · ").append(it) }
            },
            Color(0xFFDCFCE7),
            Color(0xFF166534)
        )
        else -> Triple("Cerrado", Color(0xFFFEE2E2), Color(0xFF7F1D1D))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { showSheet = true }
            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (!branch.acceptingOrders) {
            Icon(
                imageVector = Icons.Default.PauseCircle,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(14.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(fg, shape = CircleShape)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(16.dp)
        )
    }

    if (showSheet) {
        BranchStatusBottomSheet(
            branch = branch,
            today = today,
            status = status,
            onDismiss = { showSheet = false },
            onSetAcceptingOrders = onSetAcceptingOrders,
            onSetDailyOverride = onSetDailyOverride,
            onClearDailyOverride = onClearDailyOverride
        )
    }
}

private data class BranchStatus(
    val isOpen: Boolean,
    val hoursText: String?,
    val overrideActive: Boolean,
    val overrideOpenTime: String?,
    val overrideCloseTime: String?,
    val temporallyOpen: Boolean,
    val temporallyClosed: Boolean
)

private fun branchStatusOf(branch: Branch, today: String): BranchStatus {
    val ts = branch.temporaryStatus
    val overrideAppliesToday = ts?.date == today
    val hasOverrideHours = overrideAppliesToday &&
        !ts?.openTime.isNullOrBlank() &&
        !ts?.closeTime.isNullOrBlank()

    val effectiveOpen = when {
        overrideAppliesToday && ts.temporallyClosed -> false
        overrideAppliesToday && ts.temporallyOpen -> true
        else -> true // si no hay override, asumimos abierto (UI muestra horario semanal)
    }

    val hours = when {
        hasOverrideHours -> "${ts!!.openTime}–${ts.closeTime}"
        else -> null
    }

    return BranchStatus(
        isOpen = effectiveOpen,
        hoursText = hours,
        overrideActive = overrideAppliesToday,
        overrideOpenTime = ts?.openTime?.takeIf { overrideAppliesToday },
        overrideCloseTime = ts?.closeTime?.takeIf { overrideAppliesToday },
        temporallyOpen = (ts?.temporallyOpen == true) && overrideAppliesToday,
        temporallyClosed = (ts?.temporallyClosed == true) && overrideAppliesToday
    )
}

@Composable
private fun BranchStatusBottomSheet(
    branch: Branch,
    today: String,
    status: BranchStatus,
    onDismiss: () -> Unit,
    onSetAcceptingOrders: suspend (Boolean) -> BusinessResult<Branch>,
    onSetDailyOverride: suspend (
        date: String,
        temporallyClosed: Boolean,
        temporallyOpen: Boolean,
        openTime: String?,
        closeTime: String?
    ) -> BusinessResult<Branch>,
    onClearDailyOverride: suspend () -> BusinessResult<Branch>
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var customOpen by remember(branch) { mutableStateOf(status.overrideOpenTime ?: "") }
    var customClose by remember(branch) { mutableStateOf(status.overrideCloseTime ?: "") }
    var showCustomHours by remember(branch) {
        mutableStateOf(status.overrideOpenTime != null || status.overrideCloseTime != null)
    }
    var pending by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Estado de hoy · ${branch.name}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            // === ABIERTO / CERRADO HOY ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Abierto hoy",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        if (status.isOpen) "La sucursal está visible para clientes" else "La sucursal aparece cerrada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = status.isOpen,
                    enabled = !pending,
                    onCheckedChange = { isOpen ->
                        pending = true
                        feedback = null
                        scope.launch {
                            val result = onSetDailyOverride(
                                today,
                                /* temporallyClosed = */ !isOpen,
                                /* temporallyOpen = */ isOpen,
                                if (showCustomHours) customOpen.takeIf { it.isNotBlank() } else null,
                                if (showCustomHours) customClose.takeIf { it.isNotBlank() } else null
                            )
                            pending = false
                            if (result is BusinessResult.Error) feedback = result.message
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            }

            // === HORARIO SOLO POR HOY ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Cambiar horario solo hoy",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "No afecta el horario semanal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showCustomHours,
                    enabled = !pending,
                    onCheckedChange = { showCustomHours = it }
                )
            }

            if (showCustomHours) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customOpen,
                        onValueChange = { customOpen = it.take(5) },
                        label = { Text("Abre") },
                        placeholder = { Text("09:00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customClose,
                        onValueChange = { customClose = it.take(5) },
                        label = { Text("Cierra") },
                        placeholder = { Text("22:00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Button(
                    onClick = {
                        pending = true
                        feedback = null
                        scope.launch {
                            val result = onSetDailyOverride(
                                today,
                                status.temporallyClosed,
                                status.temporallyOpen || !status.temporallyClosed,
                                customOpen.takeIf { it.isNotBlank() },
                                customClose.takeIf { it.isNotBlank() }
                            )
                            pending = false
                            if (result is BusinessResult.Error) feedback = result.message
                        }
                    },
                    enabled = !pending && customOpen.isNotBlank() && customClose.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        "Aplicar horario de hoy",
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            if (status.overrideActive) {
                OutlinedButton(
                    onClick = {
                        pending = true
                        feedback = null
                        scope.launch {
                            val result = onClearDailyOverride()
                            pending = false
                            if (result is BusinessResult.Success) showCustomHours = false
                            if (result is BusinessResult.Error) feedback = result.message
                        }
                    },
                    enabled = !pending,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quitar override de hoy")
                }
            }

            // === PAUSAR PEDIDOS ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Pausar nuevos pedidos",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        if (!branch.acceptingOrders) "Los clientes no pueden hacer pedidos" else "Los clientes pueden hacer pedidos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = !branch.acceptingOrders,
                    enabled = !pending,
                    onCheckedChange = { pause ->
                        pending = true
                        feedback = null
                        scope.launch {
                            val result = onSetAcceptingOrders(!pause)
                            pending = false
                            if (result is BusinessResult.Error) feedback = result.message
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFEF4444),
                        checkedTrackColor = Color(0xFFEF4444).copy(alpha = 0.3f)
                    )
                )
            }

            feedback?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun LocalDate.Companion.todayIso(): String {
    val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val month = now.monthNumber.toString().padStart(2, '0')
    val day = now.dayOfMonth.toString().padStart(2, '0')
    return "${now.year}-$month-$day"
}

