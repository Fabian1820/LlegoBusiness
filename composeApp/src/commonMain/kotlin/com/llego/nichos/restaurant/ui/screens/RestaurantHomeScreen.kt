package com.llego.nichos.restaurant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import com.llego.shared.ui.auth.AuthViewModel

/**
 * Pantalla principal del Restaurante con Bottom Navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantHomeScreen(
    authViewModel: AuthViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToChatDetail: (String) -> Unit = {},
    onShowConfirmation: ((ConfirmationType, String) -> Unit)? = null,
    chatsViewModel: ChatsViewModel,
    ordersViewModel: OrdersViewModel,
    menuViewModel: MenuViewModel,
    settingsViewModel: SettingsViewModel
) {
    var selectedTab by remember { mutableStateOf(RestaurantTab.ORDERS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Restaurante La Habana",
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
                    modifier = Modifier.height(72.dp)
                ) {
                    val pendingCount = ordersViewModel.getPendingOrdersCount()

                    RestaurantTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                // Agregar badge solo al tab de Pedidos si hay pedidos pendientes
                                if (tab == RestaurantTab.ORDERS && pendingCount > 0) {
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
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
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
            when (selectedTab) {
                RestaurantTab.ORDERS -> OrdersScreen(
                    viewModel = ordersViewModel,
                    onNavigateToChat = { orderId, orderNumber, customerName ->
                        // Crear chat automáticamente y navegar al detalle
                        chatsViewModel.createCancellationChat(orderId, orderNumber, customerName)
                        onNavigateToChatDetail(orderId)
                    },
                    onShowConfirmation = onShowConfirmation
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
