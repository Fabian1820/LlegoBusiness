package com.llego.business.more.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Support
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.llego.business.settings.data.model.NotificationSettings
import com.llego.shared.data.platform.appVersionString
import com.llego.business.settings.ui.viewmodel.SettingsUiState
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.shared.data.model.AuthResult
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla "Más" — agrupa accesos secundarios del negocio y configuración.
 * Estructura:
 *   1. Negocio  (frecuencia alta): Perfil, Cambiar sucursal, Marketing, Choferes, Códigos
 *   2. Ajustes  (frecuencia baja): Notificaciones, Privacidad, Soporte, Cuenta
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToChangeBranch: () -> Unit,
    onNavigateToMarketing: () -> Unit,
    onNavigateToDeliveryManagement: () -> Unit,
    onNavigateToInvitations: () -> Unit,
    showDeliveryManagementButton: Boolean = true,
    pendingDeliveryRequestsCount: Int = 0
) {
    var animateContent by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletionProcessing by remember { mutableStateOf(false) }
    var deletionFeedback by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val uiState by settingsViewModel.uiState.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val currentUser = authUiState.user

    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(500)) +
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(500, easing = EaseOutCubic)
                )
        ) {
            when (uiState) {
                is SettingsUiState.Success, is SettingsUiState.Error -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ===== NEGOCIO =====
                        item {
                            MoreSection(
                                title = "Negocio",
                                icon = Icons.Default.Dashboard,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                MoreRow(
                                    title = "Promociones",
                                    subtitle = "Diseña anuncios para el feed",
                                    icon = Icons.Default.Campaign,
                                    onClick = onNavigateToMarketing
                                )
                                if (showDeliveryManagementButton) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    val deliverySubtitle = if (pendingDeliveryRequestsCount > 0) {
                                        "$pendingDeliveryRequestsCount vínculos pendientes"
                                    } else {
                                        "Administra los choferes vinculados"
                                    }
                                    MoreRow(
                                        title = "Choferes asociados",
                                        subtitle = deliverySubtitle,
                                        icon = Icons.Default.DeliveryDining,
                                        onClick = onNavigateToDeliveryManagement
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                MoreRow(
                                    title = "Códigos de invitación",
                                    subtitle = "Da acceso a colaboradores",
                                    icon = Icons.Default.CardGiftcard,
                                    onClick = onNavigateToInvitations
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                MoreRow(
                                    title = "Cambiar sucursal",
                                    subtitle = "Administrar otra de tus sucursales",
                                    icon = Icons.Default.SwapHoriz,
                                    onClick = onNavigateToChangeBranch
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                MoreRow(
                                    title = "Perfil del negocio",
                                    subtitle = "Logo, banner, horarios y ubicación",
                                    icon = Icons.Default.Storefront,
                                    onClick = onNavigateToProfile
                                )
                            }
                        }

                        // ===== AJUSTES =====
                        item {
                            MoreSection(
                                title = "Ajustes",
                                icon = Icons.Default.Tune,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                NotificationsBlock(
                                    notificationSettings = settings?.notifications ?: NotificationSettings(
                                        newOrderSound = true,
                                        orderStatusUpdates = true,
                                        customerMessages = true,
                                        dailySummary = true
                                    ),
                                    onUpdate = { notifications ->
                                        settings?.let {
                                            settingsViewModel.updateSettings(
                                                it.copy(notifications = notifications)
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        item {
                            MoreSection(
                                title = "Datos y privacidad",
                                icon = Icons.Default.PrivacyTip,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                PrivacyBlock()
                            }
                        }

                        item {
                            MoreSection(
                                title = "Soporte",
                                icon = Icons.Default.Help,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                SupportBlock()
                            }
                        }

                        item {
                            MoreSection(
                                title = "Cuenta",
                                icon = Icons.Default.ManageAccounts,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                AccountBlock(
                                    isScheduled = currentUser?.scheduledDeletionAt != null,
                                    scheduledAtIso = currentUser?.scheduledDeletionAt,
                                    isProcessing = deletionProcessing,
                                    onRequestDeletion = { showDeleteConfirm = true },
                                    onCancelDeletion = {
                                        coroutineScope.launch {
                                            deletionProcessing = true
                                            val result = authViewModel.cancelAccountDeletion()
                                            deletionProcessing = false
                                            deletionFeedback = when (result) {
                                                is AuthResult.Success -> "Solicitud de eliminación cancelada."
                                                is AuthResult.Error -> result.message
                                                else -> null
                                            }
                                        }
                                    }
                                )
                                deletionFeedback?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                is SettingsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { if (!deletionProcessing) showDeleteConfirm = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Eliminar cuenta") },
                text = {
                    Text(
                        "Tu cuenta y todos sus datos se eliminarán de forma permanente 30 días después de esta solicitud. " +
                            "Puedes cancelar el proceso iniciando sesión durante ese período. ¿Deseas continuar?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                deletionProcessing = true
                                val result = authViewModel.requestAccountDeletion()
                                deletionProcessing = false
                                showDeleteConfirm = false
                                deletionFeedback = when (result) {
                                    is AuthResult.Success -> "Eliminación programada. Tu cuenta se borrará en 30 días."
                                    is AuthResult.Error -> result.message
                                    else -> null
                                }
                            }
                        },
                        enabled = !deletionProcessing
                    ) {
                        Text(
                            if (deletionProcessing) "Procesando..." else "Eliminar",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirm = false },
                        enabled = !deletionProcessing
                    ) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
private fun MoreSection(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LlegoCustomShapes.infoCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = LlegoShapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            content()
        }
    }
}

@Composable
private fun MoreRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = LlegoShapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = LlegoShapes.small,
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NotificationsBlock(
    notificationSettings: NotificationSettings,
    onUpdate: (NotificationSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SwitchRow(
            label = "Notificaciones de pedidos",
            description = "Alerta sonora cuando llega un pedido nuevo",
            checked = notificationSettings.newOrderSound,
            onCheckedChange = { onUpdate(notificationSettings.copy(newOrderSound = it)) }
        )
        SwitchRow(
            label = "Recordatorios de horario",
            description = "Próximamente",
            checked = false,
            enabled = false,
            onCheckedChange = { }
        )
        SwitchRow(
            label = "Resumen diario",
            description = "Próximamente",
            checked = false,
            enabled = false,
            onCheckedChange = { }
        )
    }
}

@Composable
private fun PrivacyBlock() {
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MoreRow(
            title = "Política de privacidad",
            subtitle = "Lee nuestra política de privacidad",
            icon = Icons.Default.PrivacyTip,
            onClick = { runCatching { uriHandler.openUri("https://llegobackend-production.up.railway.app/privacy") } }
        )
        MoreRow(
            title = "Términos y condiciones",
            subtitle = "Consulta los términos de servicio",
            icon = Icons.Default.Description,
            onClick = { runCatching { uriHandler.openUri("https://llegobackend-production.up.railway.app/terms") } }
        )
    }
}

@Composable
private fun SupportBlock() {
    val uriHandler = LocalUriHandler.current
    var showContactDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MoreRow(
            title = "Contactar soporte",
            subtitle = "Escríbenos por WhatsApp o correo",
            icon = Icons.Default.Support,
            onClick = { showContactDialog = true }
        )
        MoreRow(
            title = "Versión de la app",
            subtitle = appVersionString(),
            icon = Icons.Default.Info,
            onClick = { /* informativo */ }
        )
    }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Support,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Contactar soporte") },
            text = { Text("Elige cómo quieres comunicarte con nosotros.") },
            confirmButton = {
                TextButton(onClick = {
                    showContactDialog = false
                    runCatching { uriHandler.openUri("https://wa.me/5358412294") }
                }) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(" WhatsApp")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showContactDialog = false
                    runCatching { uriHandler.openUri("mailto:rubianclaude@gmail.com?subject=Soporte%20Llego%20Negocios") }
                }) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(" Correo")
                }
            }
        )
    }
}

@Composable
private fun AccountBlock(
    isScheduled: Boolean,
    scheduledAtIso: String?,
    isProcessing: Boolean,
    onRequestDeletion: () -> Unit,
    onCancelDeletion: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isScheduled) {
            val displayDate = scheduledAtIso?.take(10) ?: ""
            Text(
                text = "Tu cuenta está programada para eliminarse el $displayDate. Puedes cancelar la solicitud en cualquier momento antes de esa fecha.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MoreRow(
                title = if (isProcessing) "Cancelando..." else "Cancelar eliminación",
                subtitle = "Restaurar el acceso normal a tu cuenta",
                icon = Icons.Default.Restore,
                onClick = { if (!isProcessing) onCancelDeletion() }
            )
        } else {
            Text(
                text = "Al solicitar la eliminación, tu cuenta y todos sus datos asociados se borrarán de forma permanente 30 días después. Durante ese período puedes cancelar la solicitud iniciando sesión.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MoreRow(
                title = if (isProcessing) "Procesando..." else "Eliminar mi cuenta",
                subtitle = "Programa la eliminación permanente en 30 días",
                icon = Icons.Default.DeleteForever,
                onClick = { if (!isProcessing) onRequestDeletion() },
                isDestructive = true
            )
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = LlegoShapes.small,
        colors = CardDefaults.cardColors(
            containerColor = if (checked && enabled) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            if (checked) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            )
        }
    }
}
