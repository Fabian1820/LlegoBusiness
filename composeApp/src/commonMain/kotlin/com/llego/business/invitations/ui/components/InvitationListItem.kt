@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.llego.business.invitations.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.invitations.data.model.AccessStatus
import com.llego.business.invitations.data.model.Invitation
import com.llego.business.invitations.data.model.InvitationStatus
import com.llego.business.invitations.data.model.InvitationType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun InvitationListItem(
    invitation: Invitation,
    onRevoke: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    var showRevokeDialog by remember { mutableStateOf(false) }
    val canRevoke = onRevoke != null &&
        invitation.status != InvitationStatus.REVOKED &&
        (invitation.status == InvitationStatus.PENDING || invitation.accessStatus == AccessStatus.ACTIVE)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (invitation.status) {
                InvitationStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                InvitationStatus.USED -> Color(0xFFFFF1EC)
                InvitationStatus.REVOKED -> Color(0xFFFFE6DE)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = invitation.code,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = when (invitation.invitationType) {
                        InvitationType.BRANCH -> "Sucursal: ${invitation.branch?.name ?: "N/A"}"
                        InvitationType.BUSINESS -> "Negocio completo"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (invitation.accessDurationDays != null) {
                        "Duracion: ${invitation.accessDurationDays} dias"
                    } else {
                        "Duracion: Indefinida"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(invitation.status)
                    if (invitation.status == InvitationStatus.USED && invitation.accessStatus == AccessStatus.ACTIVE) {
                        AccessChip("Acceso activo")
                    }

                    if (invitation.status == InvitationStatus.USED && invitation.redeemer != null) {
                        Text(
                            text = "por ${invitation.redeemer.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Creado: ${formatDate(invitation.createdAt.toString())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canRevoke) {
                IconButton(onClick = { showRevokeDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Revocar invitacion",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            title = { Text("Revocar invitacion") },
            text = {
                Text(
                    if (invitation.status == InvitationStatus.USED && invitation.accessStatus == AccessStatus.ACTIVE) {
                        "Se revocara este codigo y tambien el acceso activo otorgado. Esta accion no se puede deshacer."
                    } else {
                        "Se revocara este codigo de invitacion. Esta accion no se puede deshacer."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRevoke?.invoke(invitation.id)
                        showRevokeDialog = false
                    }
                ) {
                    Text("Revocar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun StatusChip(status: InvitationStatus) {
    val (text, color) = when (status) {
        InvitationStatus.PENDING -> "Pendiente" to MaterialTheme.colorScheme.primary
        InvitationStatus.USED -> "Usado" to MaterialTheme.colorScheme.tertiary
        InvitationStatus.REVOKED -> "Revocado" to MaterialTheme.colorScheme.error
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun AccessChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatDate(isoString: String): String {
    return try {
        val instant = kotlinx.datetime.Instant.parse(isoString)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year}"
    } catch (e: Exception) {
        isoString
    }
}
