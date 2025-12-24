package com.llego.nichos.restaurant.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsUiState
import kotlinx.coroutines.delay

/**
 * Pantalla de Configuración/Gestión del Restaurante
 *
 * OBJETIVO: Configuración operacional del negocio
 *
 * Contenido:
 * - Estado del negocio (abierto/cerrado)
 * - Horarios de operación
 * - Configuración de entregas (delivery, pickup, costos)
 * - Configuración de pedidos (auto-aceptación, límites)
 * - Métodos de pago
 * - Notificaciones operacionales
 * - Gestión de menú y productos (futuro)
 * - Comisiones y facturación (futuro)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantSettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var animateContent by remember { mutableStateOf(false) }

    // Dialogs
    var showHoursDialog by remember { mutableStateOf(false) }
    var showDeliveryDialog by remember { mutableStateOf(false) }

    // Animación de entrada idéntica a Perfil
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        when (uiState) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Cargando configuración...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
            is SettingsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(16.dp)
                                )
                            }
                            Text(
                                text = "Error al cargar",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = (uiState as SettingsUiState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Button(
                                onClick = { viewModel.loadSettings() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reintentar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            is SettingsUiState.Success -> {
                AnimatedVisibility(
                    visible = animateContent,
                    enter = fadeIn(animationSpec = tween(600)) +
                            slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(600, easing = EaseOutCubic)
                            )
                ) {
                    settings?.let { currentSettings ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Estado del negocio destacado
                            item {
                                BusinessStatusCard(
                                    businessHours = currentSettings.businessHours,
                                    onToggleOpen = { /* TODO: Implementar */ }
                                )
                            }

                            // Horarios de operación
                            item {
                                SettingsSectionCard(
                                    title = "Horarios de Operación",
                                    subtitle = "Configura cuando tu negocio está abierto",
                                    icon = Icons.Default.Schedule,
                                    iconColor = MaterialTheme.colorScheme.primary
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
                                    subtitle = "Delivery, pickup y costos de envío",
                                    icon = Icons.Default.DeliveryDining,
                                    iconColor = MaterialTheme.colorScheme.secondary
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
                                    subtitle = "Auto-aceptación, tiempos y límites",
                                    icon = Icons.Default.ShoppingCart,
                                    iconColor = MaterialTheme.colorScheme.tertiary
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
                                    title = "Métodos de Pago",
                                    subtitle = "Selecciona formas de pago aceptadas",
                                    icon = Icons.Default.Payment,
                                    iconColor = MaterialTheme.colorScheme.secondary
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

                            // Notificaciones operacionales
                            item {
                                SettingsSectionCard(
                                    title = "Notificaciones",
                                    subtitle = "Alertas de pedidos y operaciones",
                                    icon = Icons.Default.Notifications,
                                    iconColor = MaterialTheme.colorScheme.tertiary
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
}

/**
 * Card de estado del negocio (abierto/cerrado) con estilo Llego
 */
@Composable
private fun BusinessStatusCard(
    businessHours: BusinessHours,
    onToggleOpen: (Boolean) -> Unit
) {
    val isCurrentlyOpen = businessHours.isCurrentlyOpen()
    val infiniteTransition = rememberInfiniteTransition()

    // Colores Llego
    val openColor = Color(178, 214, 154)      // LlegoAccentPrimary - Verde claro
    val closedColor = Color(0xFFD32F2F)       // LlegoError

    // Animación de pulso para el estado abierto
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isCurrentlyOpen) openColor.copy(alpha = 0.3f) else closedColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            if (isCurrentlyOpen) openColor.copy(alpha = 0.08f)
                            else closedColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono con animación
                    Box(contentAlignment = Alignment.Center) {
                        // Círculo de pulso
                        if (isCurrentlyOpen) {
                            Surface(
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                                color = openColor.copy(alpha = pulseAlpha * 0.3f)
                            ) {}
                        }

                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = if (isCurrentlyOpen) openColor else closedColor,
                            border = BorderStroke(3.dp, Color.White)
                        ) {
                            Icon(
                                imageVector = if (isCurrentlyOpen) Icons.Default.Store else Icons.Default.StoreMallDirectory,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Estado del Negocio",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.Gray
                        )
                        Text(
                            text = if (isCurrentlyOpen) "Abierto Ahora" else "Cerrado",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentlyOpen) openColor else closedColor
                            )
                        )

                        // Horario del día actual
                        Text(
                            text = "Hoy: ${businessHours.getCurrentDaySchedule().getDisplayText()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Badge de estado
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCurrentlyOpen) openColor.copy(alpha = 0.15f) else closedColor.copy(alpha = 0.15f),
                    border = BorderStroke(
                        2.dp,
                        if (isCurrentlyOpen) openColor.copy(alpha = 0.5f) else closedColor.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isCurrentlyOpen) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isCurrentlyOpen) openColor else closedColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card contenedor para secciones de configuración con estilo Llego
 */
@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header clickeable para expandir/colapsar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    iconColor.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icono en círculo con mejor contraste
                        Surface(
                            shape = CircleShape,
                            color = iconColor.copy(alpha = 0.2f),
                            border = BorderStroke(2.dp, iconColor.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(12.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        // Icono de expandir/colapsar con mejor contraste
                        Surface(
                            shape = CircleShape,
                            color = iconColor.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                                tint = iconColor,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            // Contenido con animación
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .padding(top = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFE0E0E0)
                    )
                    content()
                }
            }
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
    val openColor = Color(178, 214, 154)      // LlegoAccentPrimary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isOpen)
                openColor.copy(alpha = 0.15f)
            else
                Color(0xFFF5F5F5)
        ),
        border = BorderStroke(
            1.dp,
            if (schedule.isOpen)
                openColor.copy(alpha = 0.4f)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (schedule.isOpen) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (schedule.isOpen) openColor else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Text(
                text = schedule.getDisplayText(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (schedule.isOpen) openColor else Color.Gray
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

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color(0xFFE0E0E0)
        )

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
            value = "+${orderSettings.prepTimeBuffer} min",
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
 * Sección de métodos de pago - Estilo Llego moderno
 */
@Composable
private fun PaymentMethodsSection(
    acceptedPaymentMethods: List<PaymentMethod>,
    onUpdate: (List<PaymentMethod>) -> Unit
) {
    val acceptedColor = Color(178, 214, 154)  // LlegoAccentPrimary

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
                        acceptedColor.copy(alpha = 0.15f)
                    else
                        Color(0xFFF5F5F5)
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isAccepted)
                        acceptedColor.copy(alpha = 0.4f)
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
                            shape = CircleShape,
                            color = if (isAccepted)
                                acceptedColor.copy(alpha = 0.3f)
                            else
                                Color.Gray.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                imageVector = when (method) {
                                    PaymentMethod.CASH -> Icons.Default.Money
                                    PaymentMethod.CARD -> Icons.Default.CreditCard
                                    PaymentMethod.TRANSFER -> Icons.Default.AccountBalance
                                    PaymentMethod.DIGITAL_WALLET -> Icons.Default.PhoneAndroid
                                },
                                contentDescription = null,
                                tint = if (isAccepted) acceptedColor else Color.Gray,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(8.dp)
                            )
                        }
                        Text(
                            text = method.getDisplayName(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isAccepted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAccepted) acceptedColor else Color.Gray
                            )
                        )
                    }

                    // Checkbox con animación
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
                            checkedColor = acceptedColor,
                            uncheckedColor = Color.Gray,
                            checkmarkColor = Color.White
                        )
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
 * Row de configuración con label y valor - Estilo Llego
 */
@Composable
private fun SettingRow(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(6.dp)
                        )
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = value,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

/**
 * Row de configuración con switch - Estilo Llego
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
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else
                Color(0xFFF5F5F5)
        ),
        border = BorderStroke(
            1.5.dp,
            if (checked)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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
                        color = if (checked) MaterialTheme.colorScheme.primary else Color.Black
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
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
        }
    }
}
