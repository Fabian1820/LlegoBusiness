package com.llego.nichos.common.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.nichos.common.config.BusinessConfigProvider
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import com.llego.nichos.restaurant.ui.screens.OrdersScreen
import com.llego.nichos.restaurant.ui.screens.MenuScreen
import com.llego.nichos.restaurant.ui.screens.TutorialsScreen
import com.llego.nichos.restaurant.ui.screens.ConfirmationType
import com.llego.shared.data.model.BusinessType
import com.llego.shared.ui.auth.AuthViewModel

/**
 * Pantalla principal genérica para todos los nichos
 * Se adapta automáticamente según el BusinessType usando BusinessConfigProvider
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHomeScreen(
    authViewModel: AuthViewModel,
    businessType: BusinessType,
    onNavigateToProfile: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToChatDetail: (String) -> Unit = {},
    onShowConfirmation: ((ConfirmationType, String) -> Unit)? = null,
    chatsViewModel: ChatsViewModel,
    ordersViewModel: OrdersViewModel,
    menuViewModel: MenuViewModel,
    settingsViewModel: SettingsViewModel
) {
    // Obtener configuración dinámica según el nicho
    val tabs = BusinessConfigProvider.getTabsForBusiness(businessType)
    val currentUser by authViewModel.uiState.collectAsState()
    val businessName = currentUser.currentUser?.businessProfile?.businessName ?: "Mi Negocio"

    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        businessName, // ✅ Dinámico según el negocio logueado
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    // Botón de Chats/Mensajes con badge
                    val pendingCount = ordersViewModel.getPendingOrdersCount()
                    IconButton(onClick = onNavigateToChats) {
                        BadgedBox(
                            badge = {
                                if (pendingCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Text(
                                            text = pendingCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ModeComment,
                                contentDescription = "Chats",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Botón de perfil
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                tonalElevation = 0.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                    windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                ) {
                    val pendingCount = ordersViewModel.getPendingOrdersCount()

                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTabIndex == index

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                // Agregar badge solo al tab de Pedidos si hay pedidos pendientes
                                if (tab.id == "orders" && pendingCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Text(
                                                    text = pendingCount.toString(),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
                                                    ),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = tab.icon, // ✅ Icono específico por nicho
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = tab.icon, // ✅ Icono específico por nicho
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title, // ✅ Texto específico por nicho
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
                                unselectedTextColor = Color.Gray.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Renderizar contenido según el tab seleccionado y el tipo de negocio
            when (tabs[selectedTabIndex].id) {
                "orders" -> {
                    // Pantalla de Pedidos (común para todos)
                    OrdersScreen(
                        viewModel = ordersViewModel,
                        onNavigateToChat = { orderId, orderNumber, customerName ->
                            chatsViewModel.createCancellationChat(orderId, orderNumber, customerName)
                            onNavigateToChatDetail(orderId)
                        },
                        onShowConfirmation = onShowConfirmation
                    )
                }
                "menu" -> {
                    // Pantalla de Menú (solo para restaurantes)
                    MenuScreen(
                        viewModel = menuViewModel,
                        businessType = businessType
                    )
                }
                "products", "stock" -> {
                    // Pantalla de Productos/Stock (mercados, agromercados, tiendas)
                    // TODO: Implementar ProductsScreen cuando esté listo
                    // Por ahora mostramos MenuScreen como placeholder con categorías adaptadas
                    MenuScreen(
                        viewModel = menuViewModel,
                        businessType = businessType
                    )
                }
                "medicines" -> {
                    // Pantalla de Medicinas (farmacias)
                    // TODO: Implementar MedicinesScreen cuando esté listo
                    MenuScreen(
                        viewModel = menuViewModel,
                        businessType = businessType
                    )
                }
                "wallet" -> {
                    // Pantalla de Wallet (común para todos)
                    WalletScreen(
                        onNavigateBack = { /* No hacemos nada, ya estamos en el tab */ }
                    )
                }
                "tutorials" -> {
                    // Pantalla de Tutoriales (común para todos)
                    TutorialsScreen(
                        onNavigateBack = { /* No hacemos nada, ya estamos en el tab */ }
                    )
                }
            }
        }
    }
}
