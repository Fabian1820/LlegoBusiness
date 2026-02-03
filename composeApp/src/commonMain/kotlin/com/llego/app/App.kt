package com.llego.app

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.auth.LoginScreen
import com.llego.shared.ui.theme.LlegoBusinessTheme
import com.llego.business.home.ui.screens.BusinessHomeScreen
import com.llego.business.products.ui.screens.AddProductScreen
import com.llego.business.products.ui.screens.ProductDetailScreen
import com.llego.business.products.ui.screens.ProductSearchScreen
import com.llego.business.profile.ui.screens.BusinessProfileScreen
import com.llego.business.branches.ui.screens.BranchesManagementScreen
import com.llego.business.branches.ui.screens.BranchCreateScreen
import com.llego.business.analytics.ui.screens.StatisticsScreen
import com.llego.business.invitations.ui.screens.InvitationDashboard
import com.llego.business.orders.ui.screens.OrderConfirmationScreen
import com.llego.business.orders.ui.screens.OrderDetailScreen
import com.llego.business.orders.ui.screens.ConfirmationType
import com.llego.business.orders.ui.components.BranchSwitchConfirmation
import com.llego.business.orders.ui.components.BranchSwitchConfirmationData
import com.llego.business.orders.ui.components.BranchNotFoundSnackbar
import com.llego.business.orders.data.notification.BranchSwitchHandler
import com.llego.business.orders.data.notification.BranchSwitchResult
import com.llego.business.orders.data.subscription.SubscriptionManager
import com.llego.shared.data.model.Product
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel
import com.llego.shared.data.model.hasBranches
import com.llego.shared.ui.business.RegisterBusinessScreen
import com.llego.shared.ui.business.RegisterBusinessViewModel
import com.llego.shared.ui.branch.BranchSelectorScreen
import com.llego.shared.ui.screens.MapSelectionScreen
import com.llego.shared.data.model.hasBusiness
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AppViewModels(
    val auth: AuthViewModel,
    val orders: OrdersViewModel,
    val products: ProductViewModel,
    val settings: SettingsViewModel,
    val registerBusiness: RegisterBusinessViewModel,
    val invitations: InvitationViewModel
)

@Composable
fun App(viewModels: AppViewModels) {
    LlegoBusinessTheme {
        val authViewModel = viewModels.auth


        var isAuthenticated by remember { mutableStateOf(false) }
        var needsBusinessRegistration by remember { mutableStateOf(false) }
        var canCancelBusinessRegistration by remember { mutableStateOf(false) }
        var showProfile by remember { mutableStateOf(false) }
        var showBranchesManagement by remember { mutableStateOf(false) }
        var showStatistics by remember { mutableStateOf(false) }
        var showInvitations by remember { mutableStateOf(false) }
        var showAddProduct by remember { mutableStateOf(false) }
        var productToEdit by remember { mutableStateOf<Product?>(null) }
        var showProductDetail by remember { mutableStateOf(false) }
        var productToView by remember { mutableStateOf<Product?>(null) }
        var showProductSearch by remember { mutableStateOf(false) }
        var showOrderDetail by remember { mutableStateOf(false) }
        var selectedOrderId by remember { mutableStateOf<String?>(null) }
        var selectedHomeTabIndex by rememberSaveable { mutableStateOf(0) }

        var branchCreateBusinessId by remember { mutableStateOf<String?>(null) }
        
        // Estado para la pantalla de selecciÃ³n de mapa
        var showMapSelection by remember { mutableStateOf(false) }
        var mapSelectionTitle by remember { mutableStateOf("") }
        var mapSelectionInitialLat by remember { mutableStateOf(0.0) }
        var mapSelectionInitialLng by remember { mutableStateOf(0.0) }
        var mapSelectionCallback by remember { mutableStateOf<((Double, Double) -> Unit)?>(null) }
        val openMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit = { title, lat, lng, callback ->
            mapSelectionTitle = title
            mapSelectionInitialLat = lat
            mapSelectionInitialLng = lng
            mapSelectionCallback = callback
            showMapSelection = true
        }

        // Estado para controlar la carga inicial (verificaciÃ³n de sesiÃ³n)
        var isCheckingSession by remember { mutableStateOf(true) }
        var isResolvingBusiness by remember { mutableStateOf(false) }
        var initialLoadComplete by remember { mutableStateOf(false) }

        // Estado para confirmaciones fullscreen
        var confirmationType by remember { mutableStateOf<ConfirmationType?>(null) }
        var confirmationOrderNumber by remember { mutableStateOf("") }
        
        // Estado para confirmaciÃ³n de cambio de sucursal - Requirements: 12.4
        var branchSwitchConfirmation by remember { mutableStateOf<BranchSwitchConfirmationData?>(null) }
        var showBranchNotFound by remember { mutableStateOf(false) }
        
        // BranchSwitchHandler y SubscriptionManager - Requirements: 12.2, 12.5
        val branchSwitchHandler = remember { BranchSwitchHandler.getInstance() }
        val subscriptionManager = remember { SubscriptionManager.getInstance() }
        val ordersViewModel = viewModels.orders
        val productViewModel = viewModels.products
        val settingsViewModel = viewModels.settings
        val productsState by productViewModel.productsState.collectAsState()

        // Scope para operaciones asÃ­ncronas
        val scope = rememberCoroutineScope()

        // Observar currentBusiness, currentBranch y branches
        val currentBusiness by authViewModel.currentBusiness.collectAsState()
        val currentBranch by authViewModel.currentBranch.collectAsState()
        val branches by authViewModel.branches.collectAsState()

        // Observar estado de autenticaciÃ³n
        LaunchedEffect(authViewModel) {
            authViewModel.uiState.collect { uiState ->

                isAuthenticated = uiState.isAuthenticated
                val user = uiState.user

                // La sesiÃ³n ya fue verificada (exitosa o fallida)
                if (!uiState.isLoading) {
                    isCheckingSession = false
                    initialLoadComplete = true
                }

                if (user != null) {
                    // Verificar si el usuario tiene un negocio registrado O acceso a sucursales (v?a c?digo de invitaci?n)
                    val hasBusiness = user.hasBusiness()
                    val hasBranches = user.hasBranches()
                    val shouldRegister = !hasBusiness && !hasBranches

                    // Solo actualizar el flujo autom?tico si no estamos en modo "agregar negocio"
                    if (!canCancelBusinessRegistration) {
                        needsBusinessRegistration = shouldRegister
                    }

                    if (shouldRegister) {
                        canCancelBusinessRegistration = false
                    }

                } else {
                    needsBusinessRegistration = false
                    canCancelBusinessRegistration = false
                }


            }
        }

        // OPTIMIZADO: Eliminar delay artificial y resolver basado en datos reales
        LaunchedEffect(isAuthenticated, currentBusiness, branches) {
            if (!isAuthenticated) {
                isResolvingBusiness = false
                initialLoadComplete = true
                return@LaunchedEffect
            }

            // Mostrar loading solo si estamos autenticados pero aún no tenemos datos
            if (initialLoadComplete && currentBusiness == null && branches.isEmpty()) {
                isResolvingBusiness = true
            } else if (currentBusiness != null || branches.isNotEmpty()) {
                // Tenemos datos, ocultar loading
                isResolvingBusiness = false
            }
        }

        // Log cuando currentBusiness o currentBranch cambien (para debug)
        LaunchedEffect(currentBusiness, currentBranch) {
        }

        LaunchedEffect(currentBranch) {
            ordersViewModel.setCurrentBranchId(currentBranch?.id)
            ordersViewModel.loadMenuItems(currentBranch?.id)
        }
        
        // Observar eventos de cambio de sucursal desde notificaciones - Requirements: 12.2, 12.3, 12.4, 12.5
        val pendingSwitchEvent by branchSwitchHandler.pendingSwitchEvent.collectAsState()
        
        // Ejecutar cambio de sucursal pendiente cuando hay branches disponibles
        LaunchedEffect(pendingSwitchEvent, branches) {
            if (pendingSwitchEvent != null && branches.isNotEmpty()) {
                branchSwitchHandler.executePendingSwitch(
                    branches = branches,
                    currentBranch = currentBranch,
                    setCurrentBranch = { branch ->
                        authViewModel.setCurrentBranch(branch)
                        // Actualizar suscripciones para la nueva sucursal activa - Requirements: 12.5
                        subscriptionManager.updateActiveBranch(branch.id)
                    }
                )
            }
        }
        
        // Observar resultados de cambio de sucursal para mostrar confirmaciÃ³n - Requirements: 12.4
        LaunchedEffect(Unit) {
            branchSwitchHandler.switchResult.collect { result ->
                when (result) {
                    is BranchSwitchResult.Success -> {
                        branchSwitchConfirmation = BranchSwitchConfirmationData(
                            previousBranchName = result.previousBranchName,
                            newBranchName = result.newBranchName,
                            orderId = result.orderId
                        )
                    }
                    is BranchSwitchResult.BranchNotFound -> {
                        showBranchNotFound = true
                    }
                    is BranchSwitchResult.Error -> {
                        // Manejar error si es necesario
                    }
                }
            }
        }
        
        // Observar navegaciÃ³n a detalle de pedido desde cambio de sucursal - Requirements: 12.3
        LaunchedEffect(Unit) {
            branchSwitchHandler.navigateToOrder.collect { orderId ->
                // Navegar al detalle del pedido
                selectedOrderId = orderId
                showOrderDetail = true
                // Cambiar a la pestaÃ±a de pedidos
                selectedHomeTabIndex = 0
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
        when {
            isAuthenticated && isResolvingBusiness -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        androidx.compose.foundation.layout.Spacer(
                            modifier = Modifier.height(16.dp)
                        )
                        androidx.compose.material3.Text(
                            text = if (needsBusinessRegistration) {
                                "Preparando registro..."
                            } else {
                                "Cargando tu negocio..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Caso 0: Carga inicial - Mostrar splash screen unificada
            isCheckingSession || (!initialLoadComplete && isAuthenticated) -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        androidx.compose.foundation.layout.Spacer(
                            modifier = Modifier.height(16.dp)
                        )
                        androidx.compose.material3.Text(
                            text = "Cargando...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Caso 1: Usuario autenticado SIN negocio â†’ Registro de negocio
            isAuthenticated && needsBusinessRegistration -> {
                RegisterBusinessScreen(
                    onRegisterSuccess = {
                        needsBusinessRegistration = false
                        canCancelBusinessRegistration = false
                    },
                    onNavigateBack = {
                        if (canCancelBusinessRegistration) {
                            needsBusinessRegistration = false
                            canCancelBusinessRegistration = false
                        } else {
                            // Cerrar sesi?n si no quiere registrar negocio
                            authViewModel.logout()
                        }
                    },
                    viewModel = viewModels.registerBusiness,
                    onOpenMapSelection = openMapSelection,
                    invitationViewModel = viewModels.invitations,
                    authViewModel = authViewModel
                )
            }

            // Caso 2: Usuario autenticado CON negocio y sucursal seleccionada â†’ Dashboard
            isAuthenticated && !needsBusinessRegistration && currentBranch != null -> {
            Box(modifier = Modifier) {
                // Contenido principal con navegaciÃ³n condicional
                when {
                    showProductSearch -> {
                        ProductSearchScreen(
                            productsState = productsState,
                            onNavigateBack = { showProductSearch = false },
                            onProductSelected = { product ->
                                showProductSearch = false
                                productToView = product
                                showProductDetail = true
                            },
                            onRetry = { productViewModel.loadProducts(branchId = authViewModel.getCurrentBranchId()) }
                        )
                    }
                    showOrderDetail && selectedOrderId != null -> {
                        // Pantalla de detalle del pedido con animaciÃ³n
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
                                    }
                                )
                            }
                        }
                    }
                    showProductDetail && productToView != null -> {
                        // Pantalla de detalle del producto (solo lectura)
                        ProductDetailScreen(
                            product = productToView!!,
                            onNavigateBack = {
                                showProductDetail = false
                                productToView = null
                            },
                            onEdit = {
                                // Cambiar a modo ediciÃ³n
                                productToEdit = productToView
                                showProductDetail = false
                                productToView = null
                                showAddProduct = true
                            }
                        )
                    }
                    showAddProduct -> {
                        AddProductScreen(
                            branchId = authViewModel.getCurrentBranchId(),  // Pasar branchId actual
                            onNavigateBack = {
                                showAddProduct = false
                                productToEdit = null
                            },
                            onSave = { form ->
                                val currentBranchId = authViewModel.getCurrentBranchId()
                                scope.launch {
                                    if (currentBranchId == null) {
                                        return@launch
                                    }
                                    if (form.imagePath.isNullOrBlank() && productToEdit == null) {
                                        return@launch
                                    }

                                    if (productToEdit == null) {
                                        productViewModel.createProductWithImagePath(
                                            name = form.name,
                                            description = form.description,
                                            price = form.price,
                                            imagePath = form.imagePath ?: "",
                                            branchId = currentBranchId,
                                            currency = form.currency,
                                            weight = form.weight,
                                            categoryId = form.categoryId
                                        )
                                    } else {
                                        productViewModel.updateProductWithImagePath(
                                            productId = productToEdit!!.id,
                                            name = form.name,
                                            description = form.description,
                                            price = form.price,
                                            currency = form.currency,
                                            weight = form.weight,
                                            availability = form.availability,
                                            categoryId = form.categoryId,
                                            imagePath = form.imagePath
                                        )
                                    }
                                    productViewModel.loadProducts(branchId = currentBranchId)
                                }
                                showAddProduct = false
                                productToEdit = null
                            },
                            existingProduct = productToEdit
                        )
                    }
                    showStatistics -> {
                        StatisticsScreen(
                            ordersViewModel = ordersViewModel,
                            onNavigateBack = { showStatistics = false }
                        )
                    }
                    showBranchesManagement -> {
                        BranchesManagementScreen(
                            authViewModel = authViewModel,
                            onNavigateBack = { showBranchesManagement = false },
                            onOpenMapSelection = openMapSelection
                        )
                    }
                    showInvitations -> {
                        val currentBusiness by authViewModel.currentBusiness.collectAsState()
                        val branches by authViewModel.branches.collectAsState()
                        
                        if (currentBusiness != null) {
                            InvitationDashboard(
                                viewModel = viewModels.invitations,
                                businessId = currentBusiness!!.id,
                                businessName = currentBusiness!!.name,
                                branches = branches.map { it.id to it.name },
                                onNavigateBack = { showInvitations = false }
                            )
                        }
                    }
                    showProfile -> {
                        BusinessProfileScreen(
                            authViewModel = authViewModel,
                            onNavigateBack = { showProfile = false },
                            onNavigateToBranches = { showBranchesManagement = true },
                            onNavigateToInvitations = { showInvitations = true }
                        )
                    }
                    else -> {
                        // BusinessHomeScreen genÃ©rico (sin diferenciaciÃ³n por tipo)
                        BusinessHomeScreen(
                            authViewModel = authViewModel,
                            onNavigateToProfile = { showProfile = true },
                            onNavigateToStatistics = { showStatistics = true },
                            selectedTabIndex = selectedHomeTabIndex,
                            onTabSelected = { selectedHomeTabIndex = it },
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
                            onNavigateToProductSearch = {
                                showProductSearch = true
                            },
                            onShowConfirmation = { type, orderNumber ->
                                confirmationType = type
                                confirmationOrderNumber = orderNumber
                            },
                            ordersViewModel = ordersViewModel,
                            productViewModel = productViewModel,
                            settingsViewModel = settingsViewModel
                        )
                    }
                }

                // ConfirmaciÃ³n fullscreen sobre todo
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
                
                // ConfirmaciÃ³n de cambio de sucursal - Requirements: 12.4
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    BranchSwitchConfirmation(
                        data = branchSwitchConfirmation,
                        onDismiss = { branchSwitchConfirmation = null }
                    )
                    
                    BranchNotFoundSnackbar(
                        visible = showBranchNotFound,
                        onDismiss = { showBranchNotFound = false }
                    )
                }
            }
            }

            // Caso 3: Usuario autenticado CON negocio pero SIN sucursal seleccionada
            isAuthenticated && !needsBusinessRegistration && currentBranch == null -> {
                if (branchCreateBusinessId != null) {
                    BranchCreateScreen(
                        businessId = branchCreateBusinessId ?: "",
                        onNavigateBack = { branchCreateBusinessId = null },
                        onSuccess = { _ ->
                            branchCreateBusinessId = null
                            authViewModel.reloadUserData()
                        },
                        onError = { _ -> },
                        authViewModel = authViewModel,
                        onOpenMapSelection = openMapSelection
                    )
                } else {
                    BranchSelectorScreen(
                        branches = branches,
                        onBranchSelected = { branch ->
                            authViewModel.setCurrentBranch(branch)
                        },
                        onAddBusiness = {
                            branchCreateBusinessId = null
                            canCancelBusinessRegistration = true
                            needsBusinessRegistration = true
                        },
                        onAddBranch = { businessId ->
                            branchCreateBusinessId = businessId
                        },
                        onNavigateBack = {
                            authViewModel.logout()
                        },
                        invitationViewModel = viewModels.invitations,
                        authViewModel = authViewModel
                    )
                }
            }

            // Caso 4: Usuario NO autenticado â†’ Login
            else -> {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        // isAuthenticated se actualiza automÃ¡ticamente via uiState
                        // currentBranch se carga automÃ¡ticamente cuando hay sucursales
                        // No necesitamos hacer nada aquÃ­, la reactividad se encarga
                    }
                )
            }
        }
            if (showMapSelection) {
                MapSelectionScreen(
                    title = mapSelectionTitle,
                    initialLatitude = mapSelectionInitialLat,
                    initialLongitude = mapSelectionInitialLng,
                    onNavigateBack = {
                        showMapSelection = false
                        mapSelectionCallback = null
                    },
                    onLocationConfirmed = { lat, lng ->
                        mapSelectionCallback?.invoke(lat, lng)
                        showMapSelection = false
                        mapSelectionCallback = null
                    }
                )
            }
        }
    }
}



