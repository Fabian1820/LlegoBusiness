package com.llego.app

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.llego.shared.data.model.BusinessType
import com.llego.shared.data.model.toBusinessType
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
import com.llego.shared.ui.business.RegisterBusinessScreen
import com.llego.shared.ui.business.RegisterBusinessViewModel
import com.llego.shared.data.model.hasBusiness
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AppViewModels(
    val auth: AuthViewModel,
    val chats: ChatsViewModel,
    val orders: OrdersViewModel,
    val menu: MenuViewModel,
    val settings: SettingsViewModel,
    val registerBusiness: RegisterBusinessViewModel
)

@Composable
fun App(viewModels: AppViewModels) {
    LlegoBusinessTheme {
        val authViewModel = viewModels.auth

        var isAuthenticated by remember { mutableStateOf(false) }
        var needsBusinessRegistration by remember { mutableStateOf(false) }
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

        // Estado para controlar la carga inicial (verificación de sesión)
        var isCheckingSession by remember { mutableStateOf(true) }

        // Estado para confirmaciones fullscreen
        var confirmationType by remember { mutableStateOf<ConfirmationType?>(null) }
        var confirmationOrderNumber by remember { mutableStateOf("") }

        val chatsViewModel = viewModels.chats
        val ordersViewModel = viewModels.orders
        val menuViewModel = viewModels.menu
        val settingsViewModel = viewModels.settings

        // Scope para operaciones asíncronas
        val scope = rememberCoroutineScope()

        // ✅ SOLUCIÓN: Observar currentBusiness directamente como StateFlow
        // Esto garantiza reactividad cuando loadBusinessData() termine
        val currentBusiness by authViewModel.currentBusiness.collectAsState()
        
        // Derivar currentBusinessType del business observado
        val currentBusinessType: BusinessType? = remember(currentBusiness) {
            currentBusiness?.type?.toBusinessType()
        }

        // Observar estado de autenticación
        LaunchedEffect(authViewModel) {
            authViewModel.uiState.collect { uiState ->
                println("App: uiState actualizado - isLoading=${uiState.isLoading}, isAuthenticated=${uiState.isAuthenticated}, user=${uiState.user?.email}")

                isAuthenticated = uiState.isAuthenticated
                val user = uiState.user

                // La sesión ya fue verificada (exitosa o fallida)
                if (!uiState.isLoading) {
                    println("App: isCheckingSession = false (porque uiState.isLoading=false)")
                    isCheckingSession = false
                }

                if (user != null) {
                    // Verificar si el usuario tiene un negocio registrado
                    val hasBusiness = user.hasBusiness()
                    needsBusinessRegistration = !hasBusiness

                    println("App: user.hasBusiness()=$hasBusiness, needsBusinessRegistration=$needsBusinessRegistration")
                } else {
                    needsBusinessRegistration = false
                    println("App: user es null")
                }

                println("App: Estado final - isCheckingSession=$isCheckingSession, isAuthenticated=$isAuthenticated, needsBusinessRegistration=$needsBusinessRegistration, currentBusinessType=$currentBusinessType")
            }
        }

        // Log cuando currentBusiness cambie (para debug)
        LaunchedEffect(currentBusiness) {
            println("App: currentBusiness cambió a ${currentBusiness?.name}, type=${currentBusiness?.type}, derivedType=$currentBusinessType")
        }

        when {
            // Caso 0: Verificando sesión al iniciar → Loading
            isCheckingSession -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Caso 1: Usuario autenticado SIN negocio → Registro de negocio
            isAuthenticated && needsBusinessRegistration -> {
                RegisterBusinessScreen(
                    onRegisterSuccess = { _ ->
                        // El currentBusinessType se actualizará automáticamente
                        // cuando currentBusiness cambie en el AuthManager
                        needsBusinessRegistration = false
                    },
                    onNavigateBack = {
                        // Cerrar sesión si no quiere registrar negocio
                        authViewModel.logout()
                    },
                    viewModel = viewModels.registerBusiness
                )
            }

            // Caso 2: Usuario autenticado CON negocio y datos cargados → Dashboard
            isAuthenticated && !needsBusinessRegistration && currentBusinessType != null -> {
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
            }

            // Caso 3: Usuario autenticado CON negocio pero datos AÚN cargando → Loading
            isAuthenticated && !needsBusinessRegistration && currentBusinessType == null -> {
                // Mostrar pantalla de carga mientras se obtienen los datos del negocio
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Caso 4: Usuario NO autenticado → Login
            else -> {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { _ ->
                        // isAuthenticated se actualiza automáticamente via uiState
                        // currentBusinessType se actualiza automáticamente via currentBusiness StateFlow
                        // No necesitamos hacer nada aquí, la reactividad se encarga
                    }
                )
            }
        }
    }
}


