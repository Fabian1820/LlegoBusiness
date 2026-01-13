package com.llego.business.settings.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.business.orders.data.model.PaymentMethod
import com.llego.business.orders.data.model.getDisplayName
import com.llego.business.settings.data.model.*
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.business.settings.ui.viewmodel.SettingsUiState
import kotlinx.coroutines.delay

/**
 * Pantalla de Configuración del Restaurante
 * Incluye: Cuenta y seguridad, Notificaciones, Métodos de pago, Datos y privacidad, Soporte
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

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
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

                        // Métodos de Pago
                        item {
                            SettingsSection(
                                title = "Métodos de Pago",
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
                                text = "Error al cargar la configuración",
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
 * Sección de configuración con título y contenido - Colores Llego
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            content()
        }
    }
}

/**
 * Sección de Cuenta y Seguridad
 */
@Composable
private fun AccountSecuritySection(
    onEmailChange: () -> Unit,
    onPasswordChange: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsRow(
            title = "Email",
            subtitle = "Cambiar dirección de correo electrónico",
            icon = Icons.Default.Email,
            onClick = onEmailChange
        )
        SettingsRow(
            title = "Contraseña",
            subtitle = "Cambiar contraseña de acceso",
            icon = Icons.Default.Lock,
            onClick = onPasswordChange
        )
    }
}

/**
 * Sección de Notificaciones (reutilizada de BusinessProfileScreen)
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
            description = "Recibe recordatorios sobre horarios de operación",
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
 * Sección de Métodos de Pago - Colores Llego
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
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAccepted)
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    else
                        Color(0xFFF5F5F5)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isAccepted)
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    else
                        Color(0xFFE0E0E0)
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
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = if (isAccepted)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        ) {
                            Icon(
                                imageVector = when (method) {
                                    PaymentMethod.CASH -> Icons.Default.Money
                                    PaymentMethod.CARD -> Icons.Default.CreditCard
                                    PaymentMethod.TRANSFER -> Icons.Default.AccountBalance
                                    PaymentMethod.DIGITAL_WALLET -> Icons.Default.PhoneAndroid
                                },
                                contentDescription = null,
                                tint = if (isAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(8.dp)
                            )
                        }
                        Text(
                            text = method.getDisplayName(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isAccepted) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
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
                            checkedColor = MaterialTheme.colorScheme.secondary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkmarkColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

/**
 * Sección de Datos y Privacidad
 */
@Composable
private fun DataPrivacySection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsRow(
            title = "Política de Privacidad",
            subtitle = "Lee nuestra política de privacidad",
            icon = Icons.Default.PrivacyTip,
            onClick = { /* TODO */ }
        )
        SettingsRow(
            title = "Términos y Condiciones",
            subtitle = "Consulta los términos de servicio",
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
 * Sección de Soporte
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
            subtitle = "Notifica errores o problemas técnicos",
            icon = Icons.Default.BugReport,
            onClick = { /* TODO */ }
        )
        SettingsRow(
            title = "Versión de la App",
            subtitle = "1.0.0",
            icon = Icons.Default.Info,
            onClick = { /* TODO */ }
        )
    }
}

/**
 * Fila de configuración individual - Colores Llego
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
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDestructive)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
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
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Row de configuración con switch - Colores Llego
 */
@Composable
private fun SettingSwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked)
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            else
                Color(0xFFF5F5F5)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (checked)
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            else
                Color(0xFFE0E0E0)
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
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                    checkedTrackColor = MaterialTheme.colorScheme.secondary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            )
        }
    }
}

