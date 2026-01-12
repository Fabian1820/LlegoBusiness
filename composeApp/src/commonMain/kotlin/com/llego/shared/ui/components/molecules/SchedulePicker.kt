package com.llego.shared.ui.components.molecules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Modelo para un rango de horario
 */
data class TimeRange(
    val start: String, // Formato: "HH:mm" (ej: "09:00")
    val end: String    // Formato: "HH:mm" (ej: "18:00")
)

/**
 * Modelo para el horario de un día
 */
data class DaySchedule(
    val isOpen: Boolean = true,
    val timeRanges: List<TimeRange> = listOf(TimeRange("09:00", "18:00"))
)

/**
 * Picker interactivo de horarios para la semana
 *
 * Permite configurar:
 * - Días abiertos/cerrados
 * - Múltiples rangos de horario por día (ej: 9-13, 16-20)
 * - Copiar horario a otros días
 *
 * El resultado se convierte a Map<String, List<String>> para el backend
 *
 * @param schedule Horario actual por día de la semana
 * @param onScheduleChange Callback cuando cambia el horario
 * @param modifier Modificador opcional
 */
@Composable
fun SchedulePicker(
    schedule: Map<String, DaySchedule>,
    onScheduleChange: (Map<String, DaySchedule>) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Días de la semana
    val daysOfWeek = listOf(
        "lun" to "Lunes",
        "mar" to "Martes",
        "mie" to "Miércoles",
        "jue" to "Jueves",
        "vie" to "Viernes",
        "sab" to "Sábado",
        "dom" to "Domingo"
    )

    var showDetailedPicker by remember { mutableStateOf(false) }
    var expandedDay by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = primaryColor
                )
                Text(
                    text = "Horario de Atención",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                )
            }

            TextButton(
                onClick = { showDetailedPicker = !showDetailedPicker }
            ) {
                Text(if (showDetailedPicker) "Modo Simple" else "Modo Avanzado")
            }
        }

        if (!showDetailedPicker) {
            // Modo simple: Un horario general
            SimpleModeSchedule(
                schedule = schedule,
                onScheduleChange = onScheduleChange,
                daysOfWeek = daysOfWeek
            )
        } else {
            // Modo avanzado: Configuración por día
            AdvancedModeSchedule(
                schedule = schedule,
                onScheduleChange = onScheduleChange,
                daysOfWeek = daysOfWeek,
                expandedDay = expandedDay,
                onExpandDay = { expandedDay = it }
            )
        }
    }
}

/**
 * Modo simple: Un horario general para toda la semana
 */
@Composable
private fun SimpleModeSchedule(
    schedule: Map<String, DaySchedule>,
    onScheduleChange: (Map<String, DaySchedule>) -> Unit,
    daysOfWeek: List<Pair<String, String>>
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Estado local para el horario general
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("18:00") }
    var selectedDays by remember {
        mutableStateOf(
            daysOfWeek.map { it.first }.filter { schedule[it]?.isOpen == true }.toSet()
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Selector de días
        Text(
            text = "Días de apertura:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            )
        )

        // Chips de días
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            daysOfWeek.forEach { (key, label) ->
                FilterChip(
                    selected = key in selectedDays,
                    onClick = {
                        selectedDays = if (key in selectedDays) {
                            selectedDays - key
                        } else {
                            selectedDays + key
                        }
                    },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor.copy(alpha = 0.15f),
                        selectedLabelColor = primaryColor
                    )
                )
            }
        }

        // Selector de horario
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it },
                label = { Text("Apertura") },
                placeholder = { Text("09:00") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "a",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = endTime,
                onValueChange = { endTime = it },
                label = { Text("Cierre") },
                placeholder = { Text("18:00") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Botón aplicar
        Button(
            onClick = {
                val newSchedule = daysOfWeek.associate { (key, _) ->
                    key to if (key in selectedDays) {
                        DaySchedule(
                            isOpen = true,
                            timeRanges = listOf(TimeRange(startTime, endTime))
                        )
                    } else {
                        DaySchedule(isOpen = false, timeRanges = emptyList())
                    }
                }
                onScheduleChange(newSchedule)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            )
        ) {
            Text("Aplicar Horario")
        }

        // Resumen visual
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = primaryColor.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Resumen:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                )
                if (selectedDays.isNotEmpty()) {
                    Text(
                        text = "${selectedDays.joinToString(", ") { it.uppercase() }}: $startTime - $endTime",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Ningún día seleccionado",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Modo avanzado: Configuración detallada por día
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
 * Card de configuración para un día específico
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
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onExpandToggle
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del día
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

            // Detalles expandibles
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

                    // Botón agregar rango (máximo 5 rangos por día)
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

                    // Mensaje informativo si alcanzó el límite
                    if (daySchedule.timeRanges.size >= 5) {
                        Text(
                            text = "✓ Máximo 5 rangos horarios por día",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = primaryColor
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Convierte el schedule de DaySchedule a formato del backend
 */
fun Map<String, DaySchedule>.toBackendSchedule(): Map<String, List<String>> {
    return this.mapValues { (_, daySchedule) ->
        if (daySchedule.isOpen) {
            daySchedule.timeRanges.map { "${it.start}-${it.end}" }
        } else {
            emptyList()
        }
    }.filterValues { it.isNotEmpty() }
}

/**
 * Convierte el schedule del backend a DaySchedule
 */
fun Map<String, List<String>>.toDaySchedule(): Map<String, DaySchedule> {
    val allDays = listOf("lun", "mar", "mie", "jue", "vie", "sab", "dom")

    return allDays.associateWith { day ->
        val ranges = this[day]
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
