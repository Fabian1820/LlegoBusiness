package com.llego.business.settings.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.business.settings.ui.viewmodel.SettingsUiState
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import kotlinx.coroutines.delay

/**
 * Pantalla de Configuracion del Restaurante
 * Incluye: Cuenta y seguridad, Notificaciones, Metodos de pago, Datos y privacidad, Soporte
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var animateContent by remember { mutableStateOf(false) }

    val uiState by settingsViewModel.uiState.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()

    // Animacion de entrada
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
                        // Cuenta y Seguridad
                        item {
                            SettingsSection(
                                title = "Cuenta y Seguridad",
                                icon = Icons.Default.Lock,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                AccountSecuritySection(
                                    onEmailChange = { /* TODO */ },
                                    onPasswordChange = { /* TODO */ }
                                )
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

                        // Metodos de Pago
                        item {
                            SettingsSection(
                                title = "Metodos de Pago",
                                icon = Icons.Default.Payment,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                PaymentMethodsSection(
                                    acceptedPaymentMethods = settings?.acceptedPaymentMethods ?: emptyList(),
                                    onUpdate = { methods ->
                                        settings?.let {
                                            settingsViewModel.updateSettings(
                                                it.copy(acceptedPaymentMethods = methods)
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
            label = "Mensajes de Clientes",
            description = "Notificaciones de mensajes de clientes",
            checked = notificationSettings.customerMessages,
            onCheckedChange = {
                onUpdate(notificationSettings.copy(customerMessages = it))
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

/**
 * Seccion de Metodos de Pago - Colores Llego
 */
@Composable
private fun PaymentMethodsSection(
    acceptedPaymentMethods: List<PaymentMethod>,
    onUpdate: (List<PaymentMethod>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PaymentMethod.values().forEach { method ->
            val isAccepted = acceptedPaymentMethods.contains(method)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val updated = if (isAccepted) {
                            acceptedPaymentMethods - method
                        } else {
                            acceptedPaymentMethods + method
                        }
                        onUpdate(updated)
                    },
                shape = LlegoShapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = if (isAccepted) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(
                    1.dp,
                    if (isAccepted) {
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isAccepted) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Icon(
                                imageVector = when (method) {
                                    PaymentMethod.CASH -> Icons.Default.Money
                                    PaymentMethod.CARD -> Icons.Default.CreditCard
                                    PaymentMethod.TRANSFER -> Icons.Default.AccountBalance
                                    PaymentMethod.DIGITAL_WALLET -> Icons.Default.PhoneAndroid
                                },
                                contentDescription = null,
                                tint = if (isAccepted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(8.dp)
                            )
                        }
                        Text(
                            text = method.getDisplayName(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isAccepted) FontWeight.SemiBold else FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Checkbox(
                        checked = isAccepted,
                        onCheckedChange = { checked ->
                            val updated = if (checked) {
                                acceptedPaymentMethods + method
                            } else {
                                acceptedPaymentMethods - method
                            }
                            onUpdate(updated)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
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
