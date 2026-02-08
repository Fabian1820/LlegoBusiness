@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.llego.business.invitations.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.invitations.data.model.BusinessAccess
import com.llego.shared.ui.theme.LlegoCustomShapes
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun BusinessAccessListItem(
    access: BusinessAccess,
    onRevoke: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    var showRevokeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.productCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con nombre de usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Column {
                        Text(
                            text = access.user.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        access.user.email?.let { email ->
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status badge
                Surface(
                    shape = LlegoCustomShapes.secondaryButton,
                    color = when {
                        access.isExpired -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        access.isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = when {
                            access.isExpired -> "Expirado"
                            access.isActive -> "Activo"
                            else -> "Inactivo"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = when {
                            access.isExpired -> MaterialTheme.colorScheme.error
                            access.isActive -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Access info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tipo de acceso
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Acceso completo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Fecha de concesión
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = formatDate(access.grantedAt.toString()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expiración info
            if (access.expiresAt != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (access.isExpired) Icons.Default.Warning else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (access.isExpired) {
                            MaterialTheme.colorScheme.error
                        } else if (access.daysUntilExpiration != null && access.daysUntilExpiration!! < 7) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = if (access.isExpired) {
                            "Expiró el ${formatDate(access.expiresAt.toString())}"
                        } else if (access.daysUntilExpiration != null) {
                            when {
                                access.daysUntilExpiration == 0 -> "Expira hoy"
                                access.daysUntilExpiration == 1 -> "Expira mañana"
                                access.daysUntilExpiration!! < 7 -> "Expira en ${access.daysUntilExpiration} días"
                                else -> "Expira el ${formatDate(access.expiresAt.toString())}"
                            }
                        } else {
                            "Expira el ${formatDate(access.expiresAt.toString())}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (access.isExpired) {
                            MaterialTheme.colorScheme.error
                        } else if (access.daysUntilExpiration != null && access.daysUntilExpiration!! < 7) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Acceso indefinido",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Actions - Only show revoke button if active and user has permission
            if (access.isActive && onRevoke != null) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                OutlinedButton(
                    onClick = { showRevokeDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = LlegoCustomShapes.secondaryButton,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Revocar acceso")
                }
            }
        }
    }

    // Revoke confirmation dialog
    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Revocar acceso") },
            text = {
                Text("¿Estás seguro de que deseas revocar el acceso de ${access.user.name} a este negocio? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRevoke?.invoke(access.id)
                        showRevokeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Revocar")
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

private fun formatDate(dateString: String): String {
    return try {
        val date = kotlinx.datetime.Instant.parse(dateString)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        "${date.dayOfMonth}/${date.monthNumber}/${date.year}"
    } catch (e: Exception) {
        dateString.take(10) // Fallback to first 10 chars
    }
}
