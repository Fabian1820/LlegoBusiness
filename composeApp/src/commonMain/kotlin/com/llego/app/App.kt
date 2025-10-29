package com.llego.app

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.llego.shared.data.model.BusinessType
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.auth.LoginScreen
import com.llego.shared.ui.navigation.*
import com.llego.shared.ui.theme.LlegoBusinessTheme
import com.llego.nichos.restaurant.ui.screens.RestaurantHomeScreen
import com.llego.nichos.restaurant.ui.screens.RestaurantProfileScreen
import com.llego.nichos.restaurant.ui.screens.ChatsScreen
import com.llego.nichos.restaurant.ui.screens.ChatDetailScreen
import com.llego.nichos.restaurant.ui.screens.OrderConfirmationScreen
import com.llego.nichos.restaurant.ui.screens.ConfirmationType
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.nichos.market.ui.screens.MarketProfileScreen

data class AppViewModels(
    val auth: AuthViewModel,
    val chats: ChatsViewModel,
    val orders: OrdersViewModel,
    val menu: MenuViewModel,
    val settings: SettingsViewModel
)

@Composable
fun App(viewModels: AppViewModels) {
    LlegoBusinessTheme {
        val authViewModel = viewModels.auth

        var isAuthenticated by remember { mutableStateOf(false) }
        var currentBusinessType by remember { mutableStateOf<BusinessType?>(null) }
        var showProfile by remember { mutableStateOf(false) }
        var showChats by remember { mutableStateOf(false) }
        var showChatDetail by remember { mutableStateOf(false) }
        var currentChatOrderId by remember { mutableStateOf<String?>(null) }

        // Estado para confirmaciones fullscreen
        var confirmationType by remember { mutableStateOf<ConfirmationType?>(null) }
        var confirmationOrderNumber by remember { mutableStateOf("") }

        val chatsViewModel = viewModels.chats
        val ordersViewModel = viewModels.orders
        val menuViewModel = viewModels.menu
        val settingsViewModel = viewModels.settings

        // Observar estado de autenticación
        LaunchedEffect(authViewModel) {
            authViewModel.uiState.collect { uiState ->
                isAuthenticated = uiState.isAuthenticated
                currentBusinessType = uiState.currentUser?.businessType
            }
        }

        if (isAuthenticated && currentBusinessType != null) {
            // Usuario autenticado - mostrar pantalla según tipo de negocio
            when (currentBusinessType) {
                BusinessType.RESTAURANT -> {
                    Box(modifier = Modifier) {
                        // Contenido principal
                        when {
                            showChatDetail && currentChatOrderId != null -> {
                                ChatDetailScreen(
                                    orderId = currentChatOrderId!!,
                                    onNavigateBack = {
                                        showChatDetail = false
                                        currentChatOrderId = null
                                        showChats = true
                                    },
                                    viewModel = chatsViewModel
                                )
                            }
                            showChats -> {
                                ChatsScreen(
                                    onChatClick = { orderId ->
                                        currentChatOrderId = orderId
                                        showChatDetail = true
                                        showChats = false
                                    },
                                    onNavigateBack = { showChats = false },
                                    viewModel = chatsViewModel
                                )
                            }
                            showProfile -> {
                                RestaurantProfileScreen(
                                    authViewModel = authViewModel,
                                    onNavigateBack = { showProfile = false }
                                )
                            }
                            else -> {
                                RestaurantHomeScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToProfile = { showProfile = true },
                                    onNavigateToChats = { showChats = true },
                                    onNavigateToChatDetail = { orderId ->
                                        currentChatOrderId = orderId
                                        showChatDetail = true
                                    },
                                    onShowConfirmation = { type, orderNumber ->
                                        confirmationType = type
                                        confirmationOrderNumber = orderNumber
                                    },
                                    chatsViewModel = chatsViewModel,
                                    ordersViewModel = ordersViewModel,
                                    menuViewModel = menuViewModel,
                                    settingsViewModel = settingsViewModel
                                )
                            }
                        }

                        // Confirmación fullscreen sobre todo
                        confirmationType?.let { type ->
                            OrderConfirmationScreen(
                                type = type,
                                orderNumber = confirmationOrderNumber,
                                onDismiss = {
                                    confirmationType = null
                                    confirmationOrderNumber = ""
                                }
                            )
                        }
                    }
                }
                BusinessType.GROCERY -> {
                    MarketProfileScreen(
                        authViewModel = authViewModel
                    )
                }
                BusinessType.PHARMACY -> {
                    // TODO: Implementar pantalla de pharmacy
                    RestaurantProfileScreen(
                        authViewModel = authViewModel
                    )
                }
                else -> {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = { businessType ->
                            isAuthenticated = true
                            currentBusinessType = businessType
                        }
                    )
                }
            }
        } else {
            // Usuario no autenticado - mostrar login
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { businessType ->
                    isAuthenticated = true
                    currentBusinessType = businessType
                }
            )
        }
    }
}
