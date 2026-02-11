package com.llego.shared.ui.components.molecules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

/**
 * Modelo para un rango de horario.
 */
@Serializable
data class TimeRange(
    val start: String,
    val end: String
)

/**
 * Modelo para el horario de un dia.
 */
@Serializable
data class DaySchedule(
    val isOpen: Boolean = true,
    val timeRanges: List<TimeRange> = listOf(TimeRange("09:00", "18:00"))
)

/**
 * Picker interactivo de horarios (solo modo avanzado).
 */
@Composable
fun SchedulePicker(
    schedule: Map<String, DaySchedule>,
    onScheduleChange: (Map<String, DaySchedule>) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true
) {
    val daysOfWeek = listOf(
        "mon" to "Lunes",
        "tue" to "Martes",
        "wed" to "Miercoles",
        "thu" to "Jueves",
        "fri" to "Viernes",
        "sat" to "Sabado",
        "sun" to "Domingo"
    )

    var expandedDay by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Horario de Atencion",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        AdvancedModeSchedule(
            schedule = schedule,
            onScheduleChange = onScheduleChange,
            daysOfWeek = daysOfWeek,
            expandedDay = expandedDay,
            onExpandDay = { expandedDay = it }
        )
    }
}

/**
 * Modo avanzado: configuracion detallada por dia.
 */
@Composable
private fun AdvancedModeSchedule(
    schedule: Map<String, DaySchedule>,
    onScheduleChange: (Map<String, DaySchedule>) -> Unit,
    daysOfWeek: List<Pair<String, String>>,
    expandedDay: String?,
    onExpandDay: (String?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        daysOfWeek.forEach { (key, label) ->
            DayScheduleCard(
                dayKey = key,
                dayLabel = label,
                daySchedule = schedule[key] ?: DaySchedule(isOpen = false),
                isExpanded = expandedDay == key,
                onExpandToggle = { onExpandDay(if (expandedDay == key) null else key) },
                onScheduleChange = { newDaySchedule ->
                    val newSchedule = schedule.toMutableMap()
                    newSchedule[key] = newDaySchedule
                    onScheduleChange(newSchedule)
                }
            )
        }
    }
}

/**
 * Card de configuracion para un dia especifico.
 */
@Composable
private fun DayScheduleCard(
    dayKey: String,
    dayLabel: String,
    daySchedule: DaySchedule,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onScheduleChange: (DaySchedule) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onExpandToggle
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (daySchedule.isOpen) {
                            daySchedule.timeRanges.joinToString(", ") { "${it.start}-${it.end}" }
                        } else {
                            "Cerrado"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }

                Switch(
                    checked = daySchedule.isOpen,
                    onCheckedChange = { isOpen ->
                        onScheduleChange(
                            if (isOpen) {
                                DaySchedule(isOpen = true, timeRanges = listOf(TimeRange("09:00", "18:00")))
                            } else {
                                DaySchedule(isOpen = false, timeRanges = emptyList())
                            }
                        )
                    }
                )
            }

            AnimatedVisibility(visible = isExpanded && daySchedule.isOpen) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    daySchedule.timeRanges.forEachIndexed { index, range ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = range.start,
                                onValueChange = { newStart ->
                                    val newRanges = daySchedule.timeRanges.toMutableList()
                                    newRanges[index] = range.copy(start = newStart)
                                    onScheduleChange(daySchedule.copy(timeRanges = newRanges))
                                },
                                label = { Text("Inicio") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            Text("a")

                            OutlinedTextField(
                                value = range.end,
                                onValueChange = { newEnd ->
                                    val newRanges = daySchedule.timeRanges.toMutableList()
                                    newRanges[index] = range.copy(end = newEnd)
                                    onScheduleChange(daySchedule.copy(timeRanges = newRanges))
                                },
                                label = { Text("Fin") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            if (daySchedule.timeRanges.size > 1) {
                                IconButton(
                                    onClick = {
                                        val newRanges = daySchedule.timeRanges.toMutableList()
                                        newRanges.removeAt(index)
                                        onScheduleChange(daySchedule.copy(timeRanges = newRanges))
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }

                    if (daySchedule.timeRanges.size < 5) {
                        OutlinedButton(
                            onClick = {
                                val newRanges = daySchedule.timeRanges + TimeRange("16:00", "20:00")
                                onScheduleChange(daySchedule.copy(timeRanges = newRanges))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agregar otro horario")
                        }
                    }

                    if (daySchedule.timeRanges.size >= 5) {
                        Text(
                            text = "Maximo 5 rangos horarios por dia",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Convierte el schedule de DaySchedule a formato del backend.
 */
fun Map<String, DaySchedule>.toBackendSchedule(): Map<String, List<String>> {
    val normalized = normalizeScheduleKeys()
    return normalized.mapValues { (_, daySchedule) ->
        if (daySchedule.isOpen) {
            daySchedule.timeRanges.map { "${it.start}-${it.end}" }
        } else {
            emptyList()
        }
    }.filterValues { it.isNotEmpty() }
}

/**
 * Convierte el schedule del backend a DaySchedule.
 */
fun Map<String, List<String>>.toDaySchedule(): Map<String, DaySchedule> {
    val normalized = mutableMapOf<String, List<String>>()
    forEach { (key, value) ->
        normalized[normalizeDayKey(key.lowercase())] = value
    }

    val allDays = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")

    return allDays.associateWith { day ->
        val ranges = normalized[day]
        if (ranges.isNullOrEmpty()) {
            DaySchedule(isOpen = false, timeRanges = emptyList())
        } else {
            DaySchedule(
                isOpen = true,
                timeRanges = ranges.map { rangeString ->
                    val parts = rangeString.split("-")
                    TimeRange(parts.getOrNull(0) ?: "09:00", parts.getOrNull(1) ?: "18:00")
                }
            )
        }
    }
}

private fun Map<String, DaySchedule>.normalizeScheduleKeys(): Map<String, DaySchedule> {
    if (isEmpty()) {
        return this
    }

    val normalized = mutableMapOf<String, DaySchedule>()
    forEach { (key, value) ->
        normalized[normalizeDayKey(key.lowercase())] = value
    }
    return normalized
}

private fun normalizeDayKey(key: String): String {
    return when (key) {
        "lun" -> "mon"
        "mar" -> "tue"
        "mie" -> "wed"
        "jue" -> "thu"
        "vie" -> "fri"
        "sab" -> "sat"
        "dom" -> "sun"
        else -> key
    }
}
