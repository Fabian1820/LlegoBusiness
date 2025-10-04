package com.llego.nichos.restaurant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.shared.ui.auth.AuthViewModel

/**
 * Pantalla principal del Restaurante con Bottom Navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantHomeScreen(
    authViewModel: AuthViewModel,
    onNavigateToProfile: () -> Unit,
    ordersViewModel: OrdersViewModel = viewModel(),
    menuViewModel: MenuViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(RestaurantTab.ORDERS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Restaurante La Havana",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    // Badge con contador de pedidos pendientes
                    val pendingCount = ordersViewModel.getPendingOrdersCount()
                    if (pendingCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text(
                                        text = pendingCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onSurface
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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                RestaurantTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                RestaurantTab.ORDERS -> OrdersScreen(
                    viewModel = ordersViewModel
                )
                RestaurantTab.MENU -> MenuScreen(
                    viewModel = menuViewModel
                )
                RestaurantTab.SETTINGS -> RestaurantSettingsScreen(
                    viewModel = settingsViewModel
                )
            }
        }
    }
}

/**
 * Tabs de navegación del restaurante
 */
enum class RestaurantTab(
    val title: String,
    val icon: ImageVector
) {
    ORDERS("Pedidos", Icons.Default.ShoppingCart),
    MENU("Menú", Icons.Default.Restaurant),
    SETTINGS("Gestión", Icons.Default.Settings)
}
