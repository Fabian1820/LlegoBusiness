package com.llego.business.profile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.shared.ui.components.NetworkImage
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.User
import com.llego.shared.data.model.toDisplayName
import com.llego.shared.utils.formatDouble
import com.llego.shared.ui.components.molecules.SchedulePicker
import com.llego.shared.ui.components.molecules.toBackendSchedule
import com.llego.shared.ui.components.molecules.toDaySchedule
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes

// ============= BRANCH SCHEDULE SECTION =============

@Composable
fun BranchScheduleSection(
    branch: Branch?,
    onSave: (Map<String, List<String>>) -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    val backendSchedule = branch?.schedule ?: emptyMap()
    var editableSchedule by remember(branch) { mutableStateOf(backendSchedule.toDaySchedule()) }

    val dayNames = listOf(
        "mon" to "Lunes",
        "tue" to "Martes",
        "wed" to "Miercoles",
        "thu" to "Jueves",
        "fri" to "Viernes",
        "sat" to "Sabado",
        "sun" to "Domingo"
    )

    ProfileSectionCard {
        SectionHeader(
            title = "Horario de atención",
            sectionIcon = Icons.Default.Schedule,
            isEditing = isEditing,
            onEditClick = {
                if (isEditing) {
                    onSave(editableSchedule.toBackendSchedule())
                } else {
                    editableSchedule = backendSchedule.toDaySchedule()
                }
                isEditing = !isEditing
            }
        )

        if (isEditing) {
            SchedulePicker(
                schedule = editableSchedule,
                onScheduleChange = { editableSchedule = it }
            )
        } else {
            val scheduleForDisplay = backendSchedule.toDaySchedule()
            if (backendSchedule.isEmpty()) {
                Text(
                    text = "Sin horarios configurados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column {
                    dayNames.forEach { (key, label) ->
                        val daySchedule = scheduleForDisplay[key]
                        val hoursText = if (daySchedule == null || !daySchedule.isOpen) {
                            "Cerrado"
                        } else {
                            daySchedule.timeRanges.joinToString(", ") { "${it.start}-${it.end}" }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = hoursText,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (hoursText == "Cerrado") {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
