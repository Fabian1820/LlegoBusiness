package com.llego.app

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.llego.shared.data.model.BusinessType
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.auth.LoginScreen
import com.llego.shared.ui.navigation.*
import com.llego.shared.ui.theme.LlegoBusinessTheme
import com.llego.nichos.common.ui.screens.BusinessHomeScreen
import com.llego.nichos.common.ui.screens.AddProductScreen
import com.llego.nichos.restaurant.ui.screens.RestaurantProfileScreen
import com.llego.nichos.restaurant.ui.screens.ChatsScreen
import com.llego.nichos.restaurant.ui.screens.ChatDetailScreen
import com.llego.nichos.restaurant.ui.screens.OrderConfirmationScreen
import com.llego.nichos.restaurant.ui.screens.OrderDetailScreen
import com.llego.nichos.restaurant.ui.screens.ConfirmationType
import com.llego.nichos.common.data.model.Product
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel

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
        var showAddProduct by remember { mutableStateOf(false) }
        var productToEdit by remember { mutableStateOf<Product?>(null) }
        var showOrderDetail by remember { mutableStateOf(false) }
        var selectedOrderId by remember { mutableStateOf<String?>(null) }

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
            // Usuario autenticado - Usa BusinessHomeScreen genérico que se adapta por nicho
            Box(modifier = Modifier) {
                // Contenido principal
                when {
                    showOrderDetail && selectedOrderId != null -> {
                        // Pantalla de detalle del pedido
                        val order = ordersViewModel.getOrderById(selectedOrderId!!)
                        if (order != null) {
                            OrderDetailScreen(
                                order = order,
                                onNavigateBack = {
                                    showOrderDetail = false
                                    selectedOrderId = null
                                },
                                onUpdateStatus = { newStatus ->
                                    // Mostrar pantalla de confirmación según el cambio de estado
                                    when {
                                        // Pedido aceptado
                                        order.status.name == "PENDING" && newStatus.name == "PREPARING" -> {
                                            confirmationType = ConfirmationType.ORDER_ACCEPTED
                                            confirmationOrderNumber = order.orderNumber
                                        }
                                        // Pedido listo
                                        order.status.name == "PREPARING" && newStatus.name == "READY" -> {
                                            confirmationType = ConfirmationType.ORDER_READY
                                            confirmationOrderNumber = order.orderNumber
                                        }
                                    }
                                    ordersViewModel.updateOrderStatus(order.id, newStatus)
                                    showOrderDetail = false
                                    selectedOrderId = null
                                },
                                onNavigateToChat = null // Chat aún no implementado
                            )
                        }
                    }
                    showAddProduct -> {
                        AddProductScreen(
                            businessType = currentBusinessType!!,
                            onNavigateBack = {
                                showAddProduct = false
                                productToEdit = null
                            },
                            onSave = { product ->
                                val productToSave = product.copy(
                                    id = productToEdit?.id ?: "product_${kotlin.random.Random.nextLong()}"
                                )
                                if (productToEdit == null) {
                                    menuViewModel.addProduct(productToSave)
                                } else {
                                    menuViewModel.updateProduct(productToSave)
                                }
                                showAddProduct = false
                                productToEdit = null
                            },
                            existingProduct = productToEdit
                        )
                    }
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
                            settingsViewModel = settingsViewModel,
                            onNavigateBack = { showProfile = false }
                        )
                    }
                    else -> {
                        // ✅ BusinessHomeScreen genérico que se personaliza según el nicho
                        BusinessHomeScreen(
                            authViewModel = authViewModel,
                            businessType = currentBusinessType!!, // Pasa el tipo de negocio
                            onNavigateToProfile = { showProfile = true },
                            onNavigateToChats = { showChats = true },
                            onNavigateToChatDetail = { orderId ->
                                currentChatOrderId = orderId
                                showChatDetail = true
                            },
                            onNavigateToOrderDetail = { orderId ->
                                selectedOrderId = orderId
                                showOrderDetail = true
                            },
                            onNavigateToAddProduct = { product ->
                                productToEdit = product
                                showAddProduct = true
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
