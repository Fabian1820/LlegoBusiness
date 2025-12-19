package com.llego.nichos.restaurant.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.abs
import kotlin.math.roundToLong
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.nichos.restaurant.ui.components.BusinessLocationMap
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsUiState
import com.llego.nichos.restaurant.data.model.*
import kotlinx.coroutines.delay

/**
 * Pantalla de Perfil y Gestión del Restaurante - Unificada
 *
 * IMPORTANTE: Esta pantalla combina la personalización del perfil público
 * con la gestión operacional del negocio.
 *
 * Contenido:
 * SECCIÓN 1 - PERFIL PÚBLICO (Vista del Cliente):
 * - Banner con logo circular superpuesto
 * - Información del negocio (nombre, dirección, rating, tiempo de entrega)
 * - Enlaces sociales (Instagram, Facebook)
 * - Mapa de ubicación
 * - Sucursales/sedes
 *
 * SECCIÓN 2 - GESTIÓN OPERACIONAL:
 * - Estado del negocio (abierto/cerrado)
 * - Horarios de operación
 * - Configuración de entregas (delivery, pickup, costos)
 * - Configuración de pedidos (auto-aceptación, límites)
 * - Métodos de pago
 * - Notificaciones operacionales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfileScreen(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var showShareDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var animateContent by remember { mutableStateOf(false) }

    val uiState by settingsViewModel.uiState.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val user = authUiState.currentUser

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Mi Perfil y Gestión",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(600, easing = EaseOutCubic)
                    )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ===== SECCIÓN 1: PERFIL PÚBLICO =====

                // Banner con logo superpuesto
                item {
                    BannerWithLogoSection()
                }

                // Información del negocio
                item {
                    BusinessInfoSection(user)
                }

                // Enlaces sociales
                item {
                    SocialLinksSection()
                }

                // Mapa de ubicación
                item {
                    LocationMapSection()
                }

                // Sucursales
                item {
                    BranchesSection()
                }

                // Separador de secciones
                item {
                    SectionDivider(title = "GESTIÓN OPERACIONAL")
                }

                // ===== SECCIÓN 2: GESTIÓN OPERACIONAL =====

                when (uiState) {
                    is SettingsUiState.Success -> {
                        settings?.let { currentSettings ->
                            // Estado del negocio destacado
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    BusinessStatusCard(
                                        businessHours = currentSettings.businessHours,
                                        onToggleOpen = { /* TODO: Implementar */ }
                                    )
                                }
                            }

                            // Horarios de operación
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    SettingsSectionCard(
                                        title = "Horarios de Operación",
                                        subtitle = "Configura cuando tu negocio está abierto",
                                        icon = Icons.Default.Schedule,
                                        iconColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        BusinessHoursSection(
                                            businessHours = currentSettings.businessHours,
                                            onUpdate = { hours ->
                                                settingsViewModel.updateSettings(
                                                    currentSettings.copy(businessHours = hours)
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Configuración de entregas
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    SettingsSectionCard(
                                        title = "Configuración de Entregas",
                                        subtitle = "Delivery, pickup y costos de envío",
                                        icon = Icons.Default.DeliveryDining,
                                        iconColor = MaterialTheme.colorScheme.secondary
                                    ) {
                                        DeliverySettingsSection(
                                            deliverySettings = currentSettings.deliverySettings,
                                            onUpdate = { delivery ->
                                                settingsViewModel.updateSettings(
                                                    currentSettings.copy(deliverySettings = delivery)
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Configuración de pedidos
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    SettingsSectionCard(
                                        title = "Configuración de Pedidos",
                                        subtitle = "Auto-aceptación, tiempos y límites",
                                        icon = Icons.Default.ShoppingCart,
                                        iconColor = MaterialTheme.colorScheme.tertiary
                                    ) {
                                        OrderSettingsSection(
                                            orderSettings = currentSettings.orderSettings,
                                            onUpdate = { orders ->
                                                settingsViewModel.updateSettings(
                                                    currentSettings.copy(orderSettings = orders)
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Métodos de pago
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    SettingsSectionCard(
                                        title = "Métodos de Pago",
                                        subtitle = "Selecciona formas de pago aceptadas",
                                        icon = Icons.Default.Payment,
                                        iconColor = Color(178, 214, 154)
                                    ) {
                                        PaymentMethodsSection(
                                            acceptedPaymentMethods = currentSettings.acceptedPaymentMethods,
                                            onUpdate = { methods ->
                                                settingsViewModel.updateSettings(
                                                    currentSettings.copy(acceptedPaymentMethods = methods)
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Notificaciones operacionales
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    SettingsSectionCard(
                                        title = "Notificaciones",
                                        subtitle = "Alertas de pedidos y operaciones",
                                        icon = Icons.Default.Notifications,
                                        iconColor = MaterialTheme.colorScheme.secondary
                                    ) {
                                        NotificationSettingsSection(
                                            notificationSettings = currentSettings.notifications,
                                            onUpdate = { notifications ->
                                                settingsViewModel.updateSettings(
                                                    currentSettings.copy(notifications = notifications)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is SettingsUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    is SettingsUiState.Error -> {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
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

        // Diálogo de compartir
        if (showShareDialog) {
            ShareDialog(
                onDismiss = { showShareDialog = false }
            )
        }

        // Diálogo de cerrar sesión
        if (showLogoutDialog) {
            LogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    authViewModel.logout()
                    showLogoutDialog = false
                }
            )
        }
    }
}

/**
 * Banner con logo circular superpuesto (coincide con StoreDetailView.swift)
 */
@Composable
private fun BannerWithLogoSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Banner de fondo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Gray.copy(alpha = 0.2f),
                            Color.Gray.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        // Gradiente overlay para mejor contraste
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f)
                        )
                    )
                )
        )

        // Logo circular superpuesto en la parte inferior
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp)
                .offset(y = 55.dp) // Sobresale del banner
        ) {
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 15.dp,
                border = BorderStroke(5.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "🍽️",
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 48.sp
                    )
                }
            }
        }
    }

    // Espacio para el logo que sobresale
    Spacer(modifier = Modifier.height(55.dp))
}

/**
 * Sección de información del negocio (coincide con StoreDetailView.swift)
 */
@Composable
private fun BusinessInfoSection(user: com.llego.shared.data.model.User?) {
    val businessName = user?.businessProfile?.businessName ?: "Restaurante La Habana"
    val address = user?.businessProfile?.address ?: "Calle 45 #12-34, Bogotá"
    val rating = user?.businessProfile?.averageRating ?: 4.8

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nombre y Rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = businessName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    ),
                    color = Color.Black,
                    maxLines = 2
                )

                // Dirección
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            // Rating badge
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${rating.toString().take(3)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = Color.Black
                        )
                    }
                    Text(
                        text = "Rating",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = Color.Gray
                    )
                }
            }
        }

        // Delivery time badge
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Entrega en 25 min",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Sección de enlaces sociales (coincide con StoreDetailView.swift)
 */
@Composable
private fun SocialLinksSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Conéctate con nosotros",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = Color.Black
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Instagram button - Colores originales de Instagram
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE91E63),
                                        Color(0xFF9C27B0),
                                        Color(0xFFFF9800)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📷",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Instagram",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                // Facebook button - Color original de Facebook
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1877F2)
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "f",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Facebook",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * Sección de mapa de ubicación interactivo con Google Maps
 */
@Composable
private fun LocationMapSection() {
    val originalLatitude = 23.1136 // La Habana, Cuba
    val originalLongitude = -82.3666

    var selectedLatitude by remember { mutableStateOf(originalLatitude) }
    var selectedLongitude by remember { mutableStateOf(originalLongitude) }
    var showSaveButton by remember { mutableStateOf(false) }
    var showFullScreenMap by remember { mutableStateOf(false) }

    val hasLocationChange by remember(selectedLatitude, selectedLongitude) {
        mutableStateOf(
            abs(selectedLatitude - originalLatitude) > 0.000001 ||
                    abs(selectedLongitude - originalLongitude) > 0.000001
        )
    }
    val formattedLatitude by remember(selectedLatitude) { mutableStateOf(formatCoordinate(selectedLatitude)) }
    val formattedLongitude by remember(selectedLongitude) { mutableStateOf(formatCoordinate(selectedLongitude)) }

    val onLocationSelected: (Double, Double) -> Unit = { lat, lng ->
        selectedLatitude = lat
        selectedLongitude = lng
        showSaveButton = abs(lat - originalLatitude) > 0.000001 ||
                abs(lng - originalLongitude) > 0.000001
    }

    if (showFullScreenMap) {
        FullScreenMapDialog(
            latitude = selectedLatitude,
            longitude = selectedLongitude,
            onLocationChange = onLocationSelected,
            onReset = {
                selectedLatitude = originalLatitude
                selectedLongitude = originalLongitude
                showSaveButton = false
            },
            onConfirm = {
                // TODO: Guardar ubicación en backend
                showSaveButton = false
            },
            onDismiss = { showFullScreenMap = false },
            hasLocationChange = hasLocationChange
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Ubicación del negocio",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.Black
                        )
                        Text(
                            text = "Toca para abrir el mapa y ajustar",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    onClick = { showFullScreenMap = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Ampliar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ampliar mapa",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            BusinessLocationMap(
                latitude = selectedLatitude,
                longitude = selectedLongitude,
                onLocationSelected = onLocationSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp)),
                isInteractive = false
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Coordenadas actuales:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Latitud: $formattedLatitude",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "Longitud: $formattedLongitude",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.9f)
                    )
                }
            }

            if (showSaveButton) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedLatitude = originalLatitude
                            selectedLongitude = originalLongitude
                            showSaveButton = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color.Gray),
                        enabled = hasLocationChange
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = if (hasLocationChange) Color.Gray else Color.Gray.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "Deshacer",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.2.sp
                                ),
                                color = if (hasLocationChange) Color.Gray else Color.Gray.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            // TODO: Guardar ubicación en backend
                            showSaveButton = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(
                                text = "Guardar",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Text(
                text = "💡 Toca el mapa para abrir en pantalla completa y seleccionar la ubicación exacta",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenMapDialog(
    latitude: Double,
    longitude: Double,
    onLocationChange: (Double, Double) -> Unit,
    onReset: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    hasLocationChange: Boolean
) {
    var contentVisible by remember { mutableStateOf(false) }
    val formattedLat by remember(latitude) { mutableStateOf(formatCoordinate(latitude)) }
    val formattedLng by remember(longitude) { mutableStateOf(formatCoordinate(longitude)) }

    LaunchedEffect(Unit) { contentVisible = true }

    LaunchedEffect(contentVisible) {
        if (!contentVisible) {
            delay(180)
            onDismiss()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Dialog(
        onDismissRequest = { contentVisible = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.96f, animationSpec = tween(200)),
            exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.96f, animationSpec = tween(180))
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                        title = {
                            Text(
                                text = "Seleccionar ubicación",
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { contentVisible = false }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        windowInsets = WindowInsets(0)
                    )

                    BusinessLocationMap(
                        latitude = latitude,
                        longitude = longitude,
                        onLocationSelected = onLocationChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        isInteractive = true
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        color = Color.White,
                        shadowElevation = 12.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "📍 Lat: $formattedLat | Lng: $formattedLng",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Arrastra el pin o toca el mapa para ajustar la ubicación.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onReset()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.5.dp, Color.Gray),
                                    enabled = hasLocationChange
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = if (hasLocationChange) Color.Gray else Color.Gray.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            text = "Deshacer",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = if (hasLocationChange) Color.Gray else Color.Gray.copy(alpha = 0.4f)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        onConfirm()
                                        contentVisible = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                        Text(
                                            text = "Confirmar",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCoordinate(value: Double): String {
    val rounded = (value * 1_000_000.0).roundToLong() / 1_000_000.0
    val text = rounded.toString()
    return if (text.contains(".")) {
        val parts = text.split(".")
        val decimals = parts[1].padEnd(6, '0').take(6)
        parts[0] + "." + decimals
    } else {
        text + ".000000"
    }
}

/**
 * Sección de sucursales (coincide con StoreDetailView.swift)
 */
@Composable
private fun BranchesSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Nuestras Sedes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color.Black
                )
                Text(
                    text = "3 ubicaciones disponibles",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Color.Gray
                )
            }

            TextButton(onClick = { /* TODO */ }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver todas",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Scroll horizontal de sucursales
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(3) { index ->
                BranchCard(branchName = "Sede ${if(index == 0) "Centro" else if(index == 1) "Norte" else "Sur"}")
            }
        }
    }
}

/**
 * Card de sucursal individual
 */
@Composable
private fun BranchCard(branchName: String) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Banner de la sucursal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Gray.copy(alpha = 0.2f),
                                Color.Gray.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            // Información de la sucursal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = branchName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "4.8",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Av. Principal #123",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Diálogo de compartir
 */
@Composable
private fun ShareDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Compartir Perfil",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "Comparte tu perfil de negocio con tus clientes para que puedan encontrarte fácilmente.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Compartir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Separador visual entre secciones
 */
@Composable
private fun SectionDivider(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = Color.White
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

/**
 * Diálogo de logout con confirmación
 */
@Composable
private fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Cerrar Sesión",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas cerrar sesión?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar Sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

// ===== COMPONENTES DE GESTIÓN OPERACIONAL =====
// Copiados del RestaurantSettingsScreen para unificar funcionalidad

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
    val closedColor = MaterialTheme.colorScheme.error       // LlegoError

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
    var isExpanded by remember { mutableStateOf(false) }

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
                        // Icono en círculo
                        Surface(
                            shape = CircleShape,
                            color = iconColor.copy(alpha = 0.15f),
                            border = BorderStroke(2.dp, iconColor.copy(alpha = 0.3f))
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

                        // Icono de expandir/colapsar
                        Surface(
                            shape = CircleShape,
                            color = iconColor.copy(alpha = 0.1f)
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
        DayScheduleRow("Lunes", businessHours.monday)
        DayScheduleRow("Martes", businessHours.tuesday)
        DayScheduleRow("Miércoles", businessHours.wednesday)
        DayScheduleRow("Jueves", businessHours.thursday)
        DayScheduleRow("Viernes", businessHours.friday)
        DayScheduleRow("Sábado", businessHours.saturday)
        DayScheduleRow("Domingo", businessHours.sunday)
    }
}

@Composable
private fun DayScheduleRow(dayName: String, schedule: DaySchedule) {
    val openColor = Color(178, 214, 154)

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
        SettingSwitchRow(
            label = "Servicio de Delivery",
            description = "Permite entregas a domicilio",
            checked = deliverySettings.isDeliveryEnabled,
            onCheckedChange = {
                onUpdate(deliverySettings.copy(isDeliveryEnabled = it))
            }
        )

        if (deliverySettings.isDeliveryEnabled) {
            SettingRow(
                label = "Costo de Delivery",
                value = "$${deliverySettings.deliveryFee}",
                icon = Icons.Default.AttachMoney
            )

            SettingRow(
                label = "Pedido Mínimo",
                value = "$${deliverySettings.minimumOrderAmount}",
                icon = Icons.Default.ShoppingCart
            )

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
        SettingSwitchRow(
            label = "Aceptación Automática",
            description = "Acepta pedidos automáticamente sin revisión manual",
            checked = orderSettings.autoAcceptOrders,
            onCheckedChange = {
                onUpdate(orderSettings.copy(autoAcceptOrders = it))
            }
        )

        SettingRow(
            label = "Buffer de Preparación",
            value = "+${orderSettings.prepTimeBuffer} min",
            icon = Icons.Default.Timer
        )

        if (orderSettings.maxOrdersPerHour != null) {
            SettingRow(
                label = "Pedidos Máximos por Hora",
                value = orderSettings.maxOrdersPerHour.toString(),
                icon = Icons.Default.FormatListNumbered
            )
        }

        SettingSwitchRow(
            label = "Pedidos Anticipados",
            description = "Permite programar pedidos con anticipación",
            checked = orderSettings.allowScheduledOrders,
            onCheckedChange = {
                onUpdate(orderSettings.copy(allowScheduledOrders = it))
            }
        )

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
    val acceptedColor = Color(178, 214, 154)

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
