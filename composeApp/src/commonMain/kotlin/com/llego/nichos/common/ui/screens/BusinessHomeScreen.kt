package com.llego.nichos.common.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
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
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToChatDetail: (String) -> Unit = {},
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onNavigateToOrderDetail: (String) -> Unit = {},
    onNavigateToAddProduct: (com.llego.nichos.common.data.model.Product?) -> Unit = {},
    onNavigateToProductDetail: (com.llego.nichos.common.data.model.Product) -> Unit = {},
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

    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var isSearchOverlayVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (!isSearchOverlayVisible) {
                TopAppBar(
                    title = {
                        Text(
                            businessName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    actions = {
                        // Chats ocultos en MVP

                        IconButton(onClick = onNavigateToStatistics) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Estadísticas",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

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
            }
        },
        bottomBar = {
            val showBottomBar = !isKeyboardVisible && !isSearchOverlayVisible
            if (showBottomBar) {
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
                                onClick = { onTabSelected(index) },
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(
                start = paddingValues.calculateStartPadding(layoutDirection),
                top = if (isSearchOverlayVisible) 0.dp else paddingValues.calculateTopPadding(),
                end = paddingValues.calculateEndPadding(layoutDirection),
                bottom = paddingValues.calculateBottomPadding()
            )
        ) {
            // Renderizar contenido según el tab seleccionado y el tipo de negocio
            when (tabs[selectedTabIndex].id) {
                "orders" -> {
                    // Pantalla de Pedidos (común para todos)
                    OrdersScreen(
                        viewModel = ordersViewModel,
                        onNavigateToOrderDetail = onNavigateToOrderDetail,
                        onShowConfirmation = onShowConfirmation
                    )
                }
                "menu" -> {
                    // Pantalla de Menú (solo para restaurantes)
                    MenuScreen(
                        viewModel = menuViewModel,
                        businessType = businessType,
                        onNavigateToAddProduct = onNavigateToAddProduct,
                        onNavigateToProductDetail = onNavigateToProductDetail,
                        onSearchVisibilityChange = { isSearchOverlayVisible = it }
                    )
                }
                "products", "stock" -> {
                    // Pantalla de Productos/Stock (mercados, agromercados, tiendas)
                    // TODO: Implementar ProductsScreen cuando esté listo
                    // Por ahora mostramos MenuScreen como placeholder con categorías adaptadas
                    MenuScreen(
                        viewModel = menuViewModel,
                        businessType = businessType,
                        onNavigateToAddProduct = onNavigateToAddProduct,
                        onNavigateToProductDetail = onNavigateToProductDetail,
                        onSearchVisibilityChange = { isSearchOverlayVisible = it }
                    )
                }
                "medicines" -> {
                    // Pantalla de Medicinas (farmacias)
                    // TODO: Implementar MedicinesScreen cuando esté listo
                    MenuScreen(
                        viewModel = menuViewModel,
                        businessType = businessType,
                        onNavigateToAddProduct = onNavigateToAddProduct,
                        onNavigateToProductDetail = onNavigateToProductDetail,
                        onSearchVisibilityChange = { isSearchOverlayVisible = it }
                    )
                }
                "wallet" -> {
                    // Pantalla de Wallet (común para todos)
                    WalletScreen(
                        onNavigateBack = { /* No hacemos nada, ya estamos en el tab */ }
                    )
                }
                "settings" -> {
                    // Pantalla de Configuración (solo para restaurantes por ahora)
                    if (businessType == BusinessType.RESTAURANT) {
                        com.llego.nichos.restaurant.ui.screens.SettingsScreen(
                            settingsViewModel = settingsViewModel,
                            onNavigateBack = { /* No hacemos nada, ya estamos en el tab */ }
                        )
                    }
                }
                // "tutorials" -> {
                //     // Pantalla de Tutoriales (común para todos) - oculto en MVP
                //     TutorialsScreen(
                //         onNavigateBack = { /* No hacemos nada, ya estamos en el tab */ }
                //     )
                // }
            }
        }
    }
}
