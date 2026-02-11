package com.llego.business.settings.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.settings.data.model.*
import com.llego.business.profile.ui.components.UserInfoSection
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.business.settings.ui.viewmodel.SettingsUiState
import com.llego.shared.data.model.AuthResult
import com.llego.shared.data.model.UpdateUserInput
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de Configuracion del Restaurante
 * Incluye: Notificaciones, Datos y privacidad, Soporte
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var animateContent by remember { mutableStateOf(false) }
    var ownerSaveMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val uiState by settingsViewModel.uiState.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val currentUser = authUiState.user

    // Animacion de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    LaunchedEffect(ownerSaveMessage) {
        ownerSaveMessage?.let {
            delay(3000)
            ownerSaveMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(600, easing = EaseOutCubic)
                    )
        ) {
            when (uiState) {
                is SettingsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            UserInfoSection(
                                user = currentUser,
                                onSave = { name, username, phone ->
                                    currentUser?.let { user ->
                                        coroutineScope.launch {
                                            val normalizedUsername = username.trim().removePrefix("@")
                                            val input = UpdateUserInput(
                                                name = name.takeIf { it != user.name },
                                                username = normalizedUsername.takeIf {
                                                    it.isNotBlank() && it != user.username
                                                },
                                                phone = phone.takeIf { it != user.phone }
                                            )

                                            when (val result = authViewModel.updateUser(input)) {
                                                is AuthResult.Success -> {
                                                    ownerSaveMessage = "OK: Usuario actualizado correctamente"
                                                }
                                                is AuthResult.Error -> {
                                                    ownerSaveMessage = "Error: ${result.message}"
                                                }
                                                else -> {}
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        ownerSaveMessage?.let { message ->
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = LlegoCustomShapes.infoCard
                                ) {
                                    Text(
                                        text = message,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Notificaciones
                        item {
                            SettingsSection(
                                title = "Notificaciones",
                                icon = Icons.Default.Notifications,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                NotificationSettingsSection(
                                    notificationSettings = settings?.notifications ?: com.llego.business.settings.data.model.NotificationSettings(
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

                        // Datos y Privacidad
                        item {
                            SettingsSection(
                                title = "Datos y Privacidad",
                                icon = Icons.Default.PrivacyTip,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                DataPrivacySection()
                            }
                        }

                        // Soporte
                        item {
                            SettingsSection(
                                title = "Soporte",
                                icon = Icons.Default.Help,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                SupportSection()
                            }
                        }
                    }
                }
                is SettingsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is SettingsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "Error al cargar la configuracion",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Seccion de configuracion con titulo y contenido - Colores Llego
 */
@Composable
private fun SettingsSection(
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
private fun AccountSecuritySection(
    onEmailChange: () -> Unit,
    onPasswordChange: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsRow(
            title = "Email",
            subtitle = "Cambiar direccion de correo electronico",
            icon = Icons.Default.Email,
            onClick = onEmailChange
        )
        SettingsRow(
            title = "Contrasena",
            subtitle = "Cambiar contrasena de acceso",
            icon = Icons.Default.Lock,
            onClick = onPasswordChange
        )
    }
}

/**
 * Seccion de Notificaciones (reutilizada de BusinessProfileScreen)
 */
@Composable
private fun NotificationSettingsSection(
    notificationSettings: com.llego.business.settings.data.model.NotificationSettings,
    onUpdate: (com.llego.business.settings.data.model.NotificationSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingSwitchRow(
            label = "Notificaciones de Pedidos",
            description = "Recibe alertas cuando lleguen nuevos pedidos",
            checked = notificationSettings.newOrderSound,
            onCheckedChange = {
                onUpdate(notificationSettings.copy(newOrderSound = it))
            }
        )

        SettingSwitchRow(
            label = "Notificaciones de Horarios",
            description = "Recibe recordatorios sobre horarios de operacion",
            checked = notificationSettings.orderStatusUpdates,
            onCheckedChange = {
                onUpdate(notificationSettings.copy(orderStatusUpdates = it))
            }
        )

        SettingSwitchRow(
            label = "Resumen Diario",
            description = "Recibe un resumen diario de las operaciones",
            checked = notificationSettings.dailySummary,
            onCheckedChange = {
                onUpdate(notificationSettings.copy(dailySummary = it))
            }
        )
    }
}

@Composable
private fun DataPrivacySection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsRow(
            title = "Politica de Privacidad",
            subtitle = "Lee nuestra politica de privacidad",
            icon = Icons.Default.PrivacyTip,
            onClick = { /* TODO */ }
        )
        SettingsRow(
            title = "Terminos y Condiciones",
            subtitle = "Consulta los terminos de servicio",
            icon = Icons.Default.Description,
            onClick = { /* TODO */ }
        )
        SettingsRow(
            title = "Exportar Datos",
            subtitle = "Descarga una copia de tus datos",
            icon = Icons.Default.Download,
            onClick = { /* TODO */ }
        )
        SettingsRow(
            title = "Eliminar Cuenta",
            subtitle = "Elimina permanentemente tu cuenta",
            icon = Icons.Default.Delete,
            onClick = { /* TODO */ },
            isDestructive = true
        )
    }
}

/**
 * Seccion de Soporte
 */
@Composable
private fun SupportSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsRow(
            title = "Centro de Ayuda",
            subtitle = "Encuentra respuestas a tus preguntas",
            icon = Icons.Default.Help,
            onClick = { /* TODO */ }
        )
        SettingsRow(
            title = "Contactar Soporte",
            subtitle = "Habla con nuestro equipo de soporte",
            icon = Icons.Default.Support,
            onClick = { /* TODO */ }
        )
        SettingsRow(
            title = "Reportar un Problema",
            subtitle = "Notifica errores o problemas tecnicos",
            icon = Icons.Default.BugReport,
            onClick = { /* TODO */ }
        )
        SettingsRow(
            title = "Version de la App",
            subtitle = "1.0.0",
            icon = Icons.Default.Info,
            onClick = { /* TODO */ }
        )
    }
}

/**
 * Fila de configuracion individual - Colores Llego
 */
@Composable
private fun SettingsRow(
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun SettingSwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = LlegoShapes.small,
        colors = CardDefaults.cardColors(
            containerColor = if (checked) {
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
