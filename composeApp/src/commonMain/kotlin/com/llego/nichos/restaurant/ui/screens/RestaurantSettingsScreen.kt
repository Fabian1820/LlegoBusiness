package com.llego.nichos.restaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsUiState

/**
 * Pantalla de Configuración/Gestión del Restaurante
 */
@Composable
fun RestaurantSettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        when (uiState) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SettingsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = (uiState as SettingsUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadSettings() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is SettingsUiState.Success -> {
                settings?.let { currentSettings ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Estado del negocio
                        item {
                            BusinessStatusCard(
                                businessHours = currentSettings.businessHours,
                                onToggleOpen = { /* Not implemented yet */ }
                            )
                        }

                        // Horarios de operación
                        item {
                            SettingsSectionCard(
                                title = "Horarios de Operación",
                                icon = Icons.Default.Schedule,
                                iconTint = Color(0xFF2196F3)
                            ) {
                                BusinessHoursSection(
                                    businessHours = currentSettings.businessHours,
                                    onUpdate = { hours ->
                                        viewModel.updateSettings(
                                            currentSettings.copy(businessHours = hours)
                                        )
                                    }
                                )
                            }
                        }

                        // Configuración de entregas
                        item {
                            SettingsSectionCard(
                                title = "Configuración de Entregas",
                                icon = Icons.Default.DeliveryDining,
                                iconTint = Color(0xFF9C27B0)
                            ) {
                                DeliverySettingsSection(
                                    deliverySettings = currentSettings.deliverySettings,
                                    onUpdate = { delivery ->
                                        viewModel.updateSettings(
                                            currentSettings.copy(deliverySettings = delivery)
                                        )
                                    }
                                )
                            }
                        }

                        // Configuración de pedidos
                        item {
                            SettingsSectionCard(
                                title = "Configuración de Pedidos",
                                icon = Icons.Default.ShoppingCart,
                                iconTint = Color(0xFFFF9800)
                            ) {
                                OrderSettingsSection(
                                    orderSettings = currentSettings.orderSettings,
                                    onUpdate = { orders ->
                                        viewModel.updateSettings(
                                            currentSettings.copy(orderSettings = orders)
                                        )
                                    }
                                )
                            }
                        }

                        // Métodos de pago
                        item {
                            SettingsSectionCard(
                                title = "Métodos de Pago Aceptados",
                                icon = Icons.Default.Payment,
                                iconTint = Color(0xFF4CAF50)
                            ) {
                                PaymentMethodsSection(
                                    acceptedPaymentMethods = currentSettings.acceptedPaymentMethods,
                                    onUpdate = { methods ->
                                        viewModel.updateSettings(
                                            currentSettings.copy(acceptedPaymentMethods = methods)
                                        )
                                    }
                                )
                            }
                        }

                        // Notificaciones
                        item {
                            SettingsSectionCard(
                                title = "Notificaciones",
                                icon = Icons.Default.Notifications,
                                iconTint = Color(0xFFE91E63)
                            ) {
                                NotificationSettingsSection(
                                    notificationSettings = currentSettings.notifications,
                                    onUpdate = { notifications ->
                                        viewModel.updateSettings(
                                            currentSettings.copy(notifications = notifications)
                                        )
                                    }
                                )
                            }
                        }

                        // Espacio final
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de estado del negocio (abierto/cerrado)
 */
@Composable
private fun BusinessStatusCard(
    businessHours: BusinessHours,
    onToggleOpen: (Boolean) -> Unit
) {
    val isCurrentlyOpen = businessHours.isCurrentlyOpen()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyOpen)
                Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCurrentlyOpen)
                        Color(0xFF4CAF50) else Color(0xFFE53935)
                ) {
                    Icon(
                        imageVector = if (isCurrentlyOpen)
                            Icons.Default.Store else Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp)
                    )
                }
                Column {
                    Text(
                        text = "Estado del Negocio",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = if (isCurrentlyOpen) "Abierto" else "Cerrado",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentlyOpen)
                                Color(0xFF4CAF50) else Color(0xFFE53935)
                        )
                    )
                }
            }
            // Note: Toggle would require updating the current day's schedule
            // For now, display only
        }
    }
}

/**
 * Card contenedor para secciones de configuración
 */
@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Divider()
            content()
        }
    }
}

/**
 * Sección de horarios de operación
 */
@Composable
private fun BusinessHoursSection(
    businessHours: BusinessHours,
    onUpdate: (BusinessHours) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Lunes
        DayScheduleRow("Lunes", businessHours.monday)
        // Martes
        DayScheduleRow("Martes", businessHours.tuesday)
        // Miércoles
        DayScheduleRow("Miércoles", businessHours.wednesday)
        // Jueves
        DayScheduleRow("Jueves", businessHours.thursday)
        // Viernes
        DayScheduleRow("Viernes", businessHours.friday)
        // Sábado
        DayScheduleRow("Sábado", businessHours.saturday)
        // Domingo
        DayScheduleRow("Domingo", businessHours.sunday)
    }
}

@Composable
private fun DayScheduleRow(dayName: String, schedule: DaySchedule) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (schedule.isOpen) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = schedule.getDisplayText(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (schedule.isOpen) Color(0xFF4CAF50) else Color.Gray
            )
        }
    }
}

/**
 * Sección de configuración de entregas
 */
@Composable
private fun DeliverySettingsSection(
    deliverySettings: DeliverySettings,
    onUpdate: (DeliverySettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Servicio de delivery habilitado
        SettingSwitchRow(
            label = "Servicio de Delivery",
            description = "Permite entregas a domicilio",
            checked = deliverySettings.isDeliveryEnabled,
            onCheckedChange = {
                onUpdate(deliverySettings.copy(isDeliveryEnabled = it))
            }
        )

        if (deliverySettings.isDeliveryEnabled) {
            // Costo de delivery
            SettingRow(
                label = "Costo de Delivery",
                value = "$${deliverySettings.deliveryFee}",
                icon = Icons.Default.AttachMoney
            )

            // Monto mínimo
            SettingRow(
                label = "Pedido Mínimo",
                value = "$${deliverySettings.minimumOrderAmount}",
                icon = Icons.Default.ShoppingCart
            )

            // Radio de entrega
            SettingRow(
                label = "Radio de Entrega",
                value = "${deliverySettings.deliveryRadius} km",
                icon = Icons.Default.MyLocation
            )
        }

        Divider()

        // Servicio de pickup habilitado
        SettingSwitchRow(
            label = "Servicio de Recogida",
            description = "Permite recoger pedidos en el local",
            checked = deliverySettings.isPickupEnabled,
            onCheckedChange = {
                onUpdate(deliverySettings.copy(isPickupEnabled = it))
            }
        )
    }
}

/**
 * Sección de configuración de pedidos
 */
@Composable
private fun OrderSettingsSection(
    orderSettings: OrderSettings,
    onUpdate: (OrderSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Aceptación automática
        SettingSwitchRow(
            label = "Aceptación Automática",
            description = "Acepta pedidos automáticamente sin revisión manual",
            checked = orderSettings.autoAcceptOrders,
            onCheckedChange = {
                onUpdate(orderSettings.copy(autoAcceptOrders = it))
            }
        )

        // Buffer de tiempo de preparación
        SettingRow(
            label = "Buffer de Preparación",
            value = "${orderSettings.prepTimeBuffer} minutos extra",
            icon = Icons.Default.Timer
        )

        // Pedidos máximos por hora
        if (orderSettings.maxOrdersPerHour != null) {
            SettingRow(
                label = "Pedidos Máximos por Hora",
                value = orderSettings.maxOrdersPerHour.toString(),
                icon = Icons.Default.FormatListNumbered
            )
        }

        // Pedidos programados
        SettingSwitchRow(
            label = "Pedidos Anticipados",
            description = "Permite programar pedidos con anticipación",
            checked = orderSettings.allowScheduledOrders,
            onCheckedChange = {
                onUpdate(orderSettings.copy(allowScheduledOrders = it))
            }
        )

        // Política de cancelación
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Política de Cancelación",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = orderSettings.cancelationPolicy,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Sección de métodos de pago
 */
@Composable
private fun PaymentMethodsSection(
    acceptedPaymentMethods: List<PaymentMethod>,
    onUpdate: (List<PaymentMethod>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PaymentMethod.values().forEach { method ->
            val isAccepted = acceptedPaymentMethods.contains(method)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (isAccepted) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (method) {
                                PaymentMethod.CASH -> Icons.Default.Money
                                PaymentMethod.CARD -> Icons.Default.CreditCard
                                PaymentMethod.TRANSFER -> Icons.Default.AccountBalance
                                PaymentMethod.DIGITAL_WALLET -> Icons.Default.PhoneAndroid
                            },
                            contentDescription = null,
                            tint = if (isAccepted) Color(0xFF4CAF50) else Color.Gray
                        )
                        Text(
                            text = method.getDisplayName(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isAccepted) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isAccepted) Color(0xFF4CAF50) else Color.Gray
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
                        }
                    )
                }
            }
        }
    }
}

/**
 * Sección de configuración de notificaciones
 */
@Composable
private fun NotificationSettingsSection(
    notificationSettings: NotificationSettings,
    onUpdate: (NotificationSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingSwitchRow(
            label = "Sonido de Nuevos Pedidos",
            description = "Reproduce sonido cuando lleguen nuevos pedidos",
            checked = notificationSettings.newOrderSound,
            onCheckedChange = {
                onUpdate(notificationSettings.copy(newOrderSound = it))
            }
        )

        SettingSwitchRow(
            label = "Actualizaciones de Estado",
            description = "Recibe actualizaciones del estado de los pedidos",
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
 * Row de configuración con label y valor
 */
@Composable
private fun SettingRow(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

/**
 * Row de configuración con switch
 */
@Composable
private fun SettingSwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (checked) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF4CAF50),
                    checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                )
            )
        }
    }
}
