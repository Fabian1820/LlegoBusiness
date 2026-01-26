package com.llego.business.invitations.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.invitations.data.model.Invitation
import com.llego.business.invitations.data.model.InvitationStatus
import com.llego.business.invitations.data.model.InvitationType
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun InvitationListItem(
    invitation: Invitation,
    onRevoke: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRevokeDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (invitation.status) {
                InvitationStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                InvitationStatus.USED -> MaterialTheme.colorScheme.tertiaryContainer
                InvitationStatus.REVOKED -> MaterialTheme.colorScheme.errorContainer
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
                // Code
                Text(
                    text = invitation.code,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Type and target
                Text(
                    text = when (invitation.invitationType) {
                        InvitationType.BRANCH -> "Sucursal: ${invitation.branch?.name ?: "N/A"}"
                        InvitationType.BUSINESS -> "Negocio completo"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Duration
                Text(
                    text = if (invitation.accessDurationDays != null) {
                        "Duración: ${invitation.accessDurationDays} días"
                    } else {
                        "Duración: Indefinida"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(invitation.status)
                    
                    if (invitation.status == InvitationStatus.USED && invitation.redeemer != null) {
                        Text(
                            text = "por ${invitation.redeemer.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Created date
                Text(
                    text = "Creado: ${formatDate(invitation.createdAt.toString())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Revoke button (only for pending invitations)
            if (invitation.status == InvitationStatus.PENDING) {
                IconButton(
                    onClick = { showRevokeDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Revocar invitación",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            title = { Text("Revocar invitación") },
            text = { 
                Text("¿Estás seguro de que deseas revocar este código de invitación? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRevoke(invitation.id)
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

private fun formatDate(isoString: String): String {
    return try {
        val instant = kotlinx.datetime.Instant.parse(isoString)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year}"
    } catch (e: Exception) {
        isoString
    }
}
