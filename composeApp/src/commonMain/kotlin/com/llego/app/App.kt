package com.llego.app

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.llego.shared.data.model.BusinessType
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.auth.LoginScreen
import com.llego.shared.ui.navigation.*
import com.llego.shared.ui.theme.LlegoBusinessTheme
import com.llego.nichos.common.ui.screens.BusinessHomeScreen
import com.llego.nichos.common.ui.screens.AddProductScreen
import com.llego.nichos.common.ui.screens.ProductDetailScreen
import com.llego.nichos.restaurant.ui.screens.RestaurantProfileScreen
import com.llego.nichos.restaurant.ui.screens.StatisticsScreen
import com.llego.nichos.restaurant.ui.screens.ChatsScreen
import com.llego.nichos.restaurant.ui.screens.ChatDetailScreen
import com.llego.nichos.restaurant.ui.screens.OrderConfirmationScreen
import com.llego.nichos.restaurant.ui.screens.OrderDetailScreen
import com.llego.nichos.restaurant.ui.screens.ConfirmationType
import com.llego.nichos.common.data.model.Product
import com.llego.nichos.restaurant.data.model.OrderStatus
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        var showStatistics by remember { mutableStateOf(false) }
        var showChats by remember { mutableStateOf(false) }
        var showChatDetail by remember { mutableStateOf(false) }
        var currentChatOrderId by remember { mutableStateOf<String?>(null) }
        var showAddProduct by remember { mutableStateOf(false) }
        var productToEdit by remember { mutableStateOf<Product?>(null) }
        var showProductDetail by remember { mutableStateOf(false) }
        var productToView by remember { mutableStateOf<Product?>(null) }
        var showOrderDetail by remember { mutableStateOf(false) }
        var selectedOrderId by remember { mutableStateOf<String?>(null) }
        var selectedHomeTabIndex by rememberSaveable { mutableStateOf(0) }

        // Estado para confirmaciones fullscreen
        var confirmationType by remember { mutableStateOf<ConfirmationType?>(null) }
        var confirmationOrderNumber by remember { mutableStateOf("") }

        val chatsViewModel = viewModels.chats
        val ordersViewModel = viewModels.orders
        val menuViewModel = viewModels.menu
        val settingsViewModel = viewModels.settings
        
        // Scope para operaciones asíncronas
        val scope = rememberCoroutineScope()

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
                // Contenido principal con navegación condicional
                when {
                    showOrderDetail && selectedOrderId != null -> {
                        // Pantalla de detalle del pedido con animación
                        AnimatedVisibility(
                            visible = showOrderDetail && selectedOrderId != null,
                            enter = slideInVertically(
                                initialOffsetY = { it }, // Empieza desde abajo (altura completa)
                                animationSpec = tween(durationMillis = 350)
                            ) + fadeIn(animationSpec = tween(durationMillis = 350)),
                            exit = slideOutVertically(
                                targetOffsetY = { it }, // Sale hacia abajo (altura completa)
                                animationSpec = tween(durationMillis = 350)
                            ) + fadeOut(animationSpec = tween(durationMillis = 350))
                        ) {
                            val order = ordersViewModel.getOrderById(selectedOrderId!!)
                            if (order != null) {
                                OrderDetailScreen(
                                    order = order,
                                    ordersViewModel = ordersViewModel,
                                    onNavigateBack = {
                                        showOrderDetail = false
                                        selectedOrderId = null
                                    },
                                    onAcceptOrder = { minutes ->
                                        showOrderDetail = false
                                        selectedOrderId = null

                                        scope.launch {
                                            delay(350)
                                            ordersViewModel.updateOrderStatus(
                                                order.id,
                                                OrderStatus.PREPARING,
                                                minutes
                                            )
                                            delay(100)
                                            confirmationType = ConfirmationType.ORDER_ACCEPTED
                                            confirmationOrderNumber = order.orderNumber
                                        }
                                    },
                                    onUpdateStatus = { newStatus ->
                                        // Primero cerrar la pantalla con animación
                                        showOrderDetail = false
                                        selectedOrderId = null

                                        // Esperar a que la animación de salida termine
                                        scope.launch {
                                            delay(350) // Duración de la animación de salida

                                            // Actualizar el estado del pedido
                                            ordersViewModel.updateOrderStatus(order.id, newStatus)

                                            // Pequeño delay adicional para suavizar
                                            delay(100)

                                            // Mostrar pantalla de confirmación
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
                                        }
                                    },
                                    onSubmitModification = { modifiedOrder, state, note ->
                                        chatsViewModel.createModificationMessage(
                                            orderId = modifiedOrder.id,
                                            orderNumber = modifiedOrder.orderNumber,
                                            customerName = modifiedOrder.customer.name,
                                            note = note,
                                            originalItems = state.originalItems,
                                            modifiedItems = state.modifiedItems,
                                            originalTotal = state.originalTotal,
                                            newTotal = state.newTotal
                                        )
                                        showOrderDetail = false
                                        selectedOrderId = null
                                    }
                                )
                            }
                        }
                    }
                    showProductDetail && productToView != null -> {
                        // Pantalla de detalle del producto (solo lectura)
                        ProductDetailScreen(
                            product = productToView!!,
                            businessType = currentBusinessType!!,
                            onNavigateBack = {
                                showProductDetail = false
                                productToView = null
                            },
                            onEdit = {
                                // Cambiar a modo edición
                                productToEdit = productToView
                                showProductDetail = false
                                productToView = null
                                showAddProduct = true
                            }
                        )
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
                    // MVP: chats deshabilitados en la app de negocios
                    /*
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
                    */
                    showStatistics -> {
                        StatisticsScreen(
                            ordersViewModel = ordersViewModel,
                            onNavigateBack = { showStatistics = false }
                        )
                    }
                    showProfile -> {
                        RestaurantProfileScreen(
                            authViewModel = authViewModel,
                            onNavigateBack = { showProfile = false }
                        )
                    }
                    else -> {
                        // ✅ BusinessHomeScreen genérico que se personaliza según el nicho
                        BusinessHomeScreen(
                            authViewModel = authViewModel,
                            businessType = currentBusinessType!!, // Pasa el tipo de negocio
                            onNavigateToProfile = { showProfile = true },
                            onNavigateToStatistics = { showStatistics = true },
                            onNavigateToChats = { showChats = true },
                            selectedTabIndex = selectedHomeTabIndex,
                            onTabSelected = { selectedHomeTabIndex = it },
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
                            onNavigateToProductDetail = { product ->
                                productToView = product
                                showProductDetail = true
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


