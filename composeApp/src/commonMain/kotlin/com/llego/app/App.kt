package com.llego.app

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.auth.LoginScreen
import com.llego.shared.ui.theme.LlegoBusinessTheme
import com.llego.shared.data.auth.TokenManager
import com.llego.business.home.ui.screens.BusinessHomeScreen
import com.llego.business.products.ui.screens.AddProductScreen
import com.llego.business.products.ui.screens.AddShowcaseScreen
import com.llego.business.products.ui.screens.AddComboScreen
import com.llego.business.products.ui.screens.ProductDetailScreen
import com.llego.business.products.ui.screens.ComboDetailScreen
import com.llego.business.products.ui.screens.ShowcaseDetailScreen
import com.llego.business.products.ui.screens.ProductSearchScreen
import com.llego.business.profile.ui.screens.BusinessProfileScreen
import com.llego.business.branches.ui.screens.BranchesManagementScreen
import com.llego.business.branches.ui.screens.BranchCreateWizardScreen
import com.llego.business.branches.ui.screens.BranchEditScreen
import com.llego.business.analytics.ui.screens.StatisticsScreen
import com.llego.business.invitations.ui.screens.InvitationDashboard
import com.llego.business.delivery.ui.screens.DeliveryLinkManagementScreen
import com.llego.business.delivery.ui.viewmodel.DeliveryLinkViewModel
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
import com.llego.shared.data.model.ProductsResult
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.business.products.ui.viewmodel.ComboViewModel
import com.llego.business.products.ui.viewmodel.ShowcaseViewModel
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel
import com.llego.business.branches.ui.viewmodel.BranchesManagementViewModel
import com.llego.shared.data.model.hasBranches
import com.llego.shared.ui.business.RegisterBusinessViewModel
import com.llego.shared.ui.branch.BusinessEditScreen
import com.llego.shared.ui.branch.BranchSelectorScreen
import com.llego.shared.ui.branch.BranchSelectorViewModel
import com.llego.shared.ui.screens.MapSelectionScreen
import com.llego.shared.ui.onboarding.OnboardingIntroScreen
import com.llego.shared.ui.onboarding.OnboardingWizardScreen
import com.llego.shared.data.model.hasBusiness
import com.llego.shared.ui.components.atoms.LoadingOverlay
import kotlinx.coroutines.launch

private val mongoObjectIdRegex = Regex("^[a-fA-F0-9]{24}$")

private fun normalizeCategoryId(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) return null
    return if (mongoObjectIdRegex.matches(value)) value else null
}

/**
 * Estado de la pantalla pre-autenticacion.
 * Controla que pantalla se muestra antes de que el usuario inicie sesion.
 */
private enum class PreAuthScreen {
    ONBOARDING,  // Pantallas introductorias (bienvenida + info)
    LOGIN        // Pantalla de login
}

data class AppViewModels(
    val tokenManager: TokenManager,
    val auth: AuthViewModel,
    val orders: OrdersViewModel,
    val products: ProductViewModel,
    val combos: com.llego.business.products.ui.viewmodel.ComboViewModel,
    val showcases: ShowcaseViewModel,
    val settings: SettingsViewModel,
    val registerBusiness: RegisterBusinessViewModel,
    val invitations: InvitationViewModel,
    val deliveryLinks: DeliveryLinkViewModel,
    val branchSelector: BranchSelectorViewModel,
    val branchesManagement: BranchesManagementViewModel
)

@Composable
fun App(viewModels: AppViewModels) {
    LlegoBusinessTheme {
        val authViewModel = viewModels.auth


        var isAuthenticated by remember { mutableStateOf(false) }
        var needsBusinessRegistration by remember { mutableStateOf(false) }
        var canCancelBusinessRegistration by remember { mutableStateOf(false) }
        var isExploringWithoutBusiness by remember { mutableStateOf(false) }
        var preAuthScreen by remember { mutableStateOf(PreAuthScreen.ONBOARDING) }
        val navigator = rememberAppNavigatorState()
        var hasRestoredHomeState by remember { mutableStateOf(false) }
        val openMapSelection = navigator::openMapSelection

        // Estado para controlar la carga inicial (verificacion de sesion + datos de negocio)
        var isCheckingSession by remember { mutableStateOf(true) }
        var initialLoadComplete by remember { mutableStateOf(false) }

        // Estado para confirmaciÃ³n de cambio de sucursal - Requirements: 12.4
        var branchSwitchConfirmation by remember { mutableStateOf<BranchSwitchConfirmationData?>(null) }
        var showBranchNotFound by remember { mutableStateOf(false) }

        // BranchSwitchHandler y SubscriptionManager - Requirements: 12.2, 12.5
        val branchSwitchHandler = remember { BranchSwitchHandler.getInstance() }
        val subscriptionManager = remember { SubscriptionManager.getInstance() }
        val ordersViewModel = viewModels.orders
        val productViewModel = viewModels.products
        val comboViewModel = viewModels.combos
        val showcaseViewModel = viewModels.showcases
        val settingsViewModel = viewModels.settings

        // Observar currentBusiness, currentBranch y branches
        val currentBusiness by authViewModel.currentBusiness.collectAsState()
        val currentBranch by authViewModel.currentBranch.collectAsState()
        val branches by authViewModel.branches.collectAsState()

        val shouldHandleNativeBack =
            navigator.canConsumeBackPress() ||
                (!isAuthenticated && preAuthScreen == PreAuthScreen.LOGIN) ||
                (isAuthenticated && needsBusinessRegistration && !isExploringWithoutBusiness) ||
                (isAuthenticated && !needsBusinessRegistration && currentBranch != null)

        PlatformBackHandler(enabled = shouldHandleNativeBack) {
            if (navigator.consumeBackPress()) {
                return@PlatformBackHandler
            }

            when {
                !isAuthenticated && preAuthScreen == PreAuthScreen.LOGIN -> {
                    preAuthScreen = PreAuthScreen.ONBOARDING
                }

                isAuthenticated && needsBusinessRegistration && !isExploringWithoutBusiness -> {
                    if (canCancelBusinessRegistration) {
                        needsBusinessRegistration = false
                        canCancelBusinessRegistration = false
                        isExploringWithoutBusiness = false
                    } else {
                        authViewModel.logout()
                    }
                }

                isAuthenticated && !needsBusinessRegistration && currentBranch != null -> {
                    authViewModel.clearCurrentBranch()
                }
            }
        }

        // Observar estado de autenticacion
        // isLoading ahora se mantiene true hasta que sesion + datos de negocio esten listos
        LaunchedEffect(authViewModel) {
            authViewModel.uiState.collect { uiState ->

                isAuthenticated = uiState.isAuthenticated
                val user = uiState.user
                viewModels.branchSelector.onAuthUserChanged(user?.id)

                // La sesion ya fue verificada y datos cargados (exitosa o fallida)
                if (!uiState.isLoading) {
                    isCheckingSession = false
                    initialLoadComplete = true
                }

                if (user != null) {
                    val hasBusiness = user.hasBusiness()
                    val hasBranches = user.hasBranches()
                    val shouldRegister = !hasBusiness && !hasBranches

                    if (!canCancelBusinessRegistration) {
                        needsBusinessRegistration = shouldRegister
                        isExploringWithoutBusiness = shouldRegister
                    }

                    if (shouldRegister) {
                        if (!canCancelBusinessRegistration) {
                            canCancelBusinessRegistration = false
                        }
                    } else {
                        isExploringWithoutBusiness = false
                    }

                } else {
                    needsBusinessRegistration = false
                    canCancelBusinessRegistration = false
                    isExploringWithoutBusiness = false
                }
            }
        }

        // Restaurar/limpiar estado de navegacion al cambiar autenticacion
        LaunchedEffect(isAuthenticated) {
            if (!isAuthenticated) {
                navigator.resetForNewSession(homeTabIndex = 0)
                hasRestoredHomeState = false
                isExploringWithoutBusiness = false
                preAuthScreen = PreAuthScreen.ONBOARDING
                return@LaunchedEffect
            }

            if (!hasRestoredHomeState) {
                val savedTab = viewModels.tokenManager.getLastHomeTabIndex()
                    ?.coerceIn(0, 4)
                    ?: 0
                navigator.resetForNewSession(homeTabIndex = savedTab)
                hasRestoredHomeState = true
            }
        }

        // Log cuando currentBusiness o currentBranch cambien (para debug)
        LaunchedEffect(currentBusiness, currentBranch) {
        }

        LaunchedEffect(currentBranch) {
            ordersViewModel.setCurrentBranchId(currentBranch?.id)
            ordersViewModel.loadMenuItems(currentBranch?.id)
        }

        LaunchedEffect(branches) {
            ordersViewModel.setSubscribedBranchIds(branches.map { it.id })
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
                navigator.selectedOrderId = orderId
                navigator.showOrderDetail = true
                navigator.selectedHomeTabIndex = 0
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // Caso 0: Carga inicial - sesion + datos de negocio/sucursales
                isCheckingSession || (!initialLoadComplete && isAuthenticated) -> {
                    LoadingOverlay(message = "Cargando")
                }

                // Caso 1: Usuario autenticado SIN negocio o agregando negocio → Wizard de registro
                isAuthenticated && needsBusinessRegistration && !isExploringWithoutBusiness -> {
                    OnboardingWizardScreen(
                        registerBusinessViewModel = viewModels.registerBusiness,
                        authViewModel = authViewModel,
                        onSuccess = {
                            viewModels.registerBusiness.resetState()
                            viewModels.branchSelector.loadBusinesses(forceLoading = true)
                            needsBusinessRegistration = false
                            canCancelBusinessRegistration = false
                            isExploringWithoutBusiness = false
                        },
                        onBack = {
                            if (canCancelBusinessRegistration) {
                                needsBusinessRegistration = false
                                canCancelBusinessRegistration = false
                                isExploringWithoutBusiness = false
                            } else {
                                authViewModel.logout()
                            }
                        },
                        onOpenMapSelection = openMapSelection
                    )
                }

                // Caso 2: Usuario autenticado CON negocio y sucursal seleccionada â†’ Dashboard
                isAuthenticated && !needsBusinessRegistration && currentBranch != null -> {
                    MainBusinessFlow(
                        navigator = navigator,
                        authViewModel = authViewModel,
                        ordersViewModel = ordersViewModel,
                        productViewModel = productViewModel,
                        comboViewModel = comboViewModel,
                        showcaseViewModel = showcaseViewModel,
                        settingsViewModel = settingsViewModel,
                        branchesManagementViewModel = viewModels.branchesManagement,
                        branchSelectorViewModel = viewModels.branchSelector,
                        invitationViewModel = viewModels.invitations,
                        deliveryLinkViewModel = viewModels.deliveryLinks,
                        tokenManager = viewModels.tokenManager,
                        branchSwitchConfirmation = branchSwitchConfirmation,
                        onDismissBranchSwitchConfirmation = { branchSwitchConfirmation = null },
                        showBranchNotFound = showBranchNotFound,
                        onDismissBranchNotFound = { showBranchNotFound = false },
                        onOpenMapSelection = openMapSelection,
                        onNavigateBackToBranchSelector = {
                            authViewModel.clearCurrentBranch()
                        }
                    )
                }

                // Caso 3: Usuario explorando sin negocio → Mostrar selector de sucursales (vacío) con opción de crear
                isAuthenticated && isExploringWithoutBusiness && currentBranch == null -> {
                    BranchSelectionFlow(
                        navigator = navigator,
                        authViewModel = authViewModel,
                        branches = branches,
                        productViewModel = viewModels.products,
                        branchSelectorViewModel = viewModels.branchSelector,
                        invitationViewModel = viewModels.invitations,
                        onOpenMapSelection = openMapSelection,
                        onStartBusinessRegistration = {
                            navigator.branchCreateBusinessId = null
                            viewModels.registerBusiness.resetState()
                            canCancelBusinessRegistration = true
                            needsBusinessRegistration = true
                            isExploringWithoutBusiness = false
                        }
                    )
                }

                // Caso 4: Usuario autenticado CON negocio pero SIN sucursal seleccionada
                isAuthenticated && !needsBusinessRegistration && currentBranch == null -> {
                    BranchSelectionFlow(
                        navigator = navigator,
                        authViewModel = authViewModel,
                        branches = branches,
                        productViewModel = viewModels.products,
                        branchSelectorViewModel = viewModels.branchSelector,
                        invitationViewModel = viewModels.invitations,
                        onOpenMapSelection = openMapSelection,
                        onStartBusinessRegistration = {
                            navigator.branchCreateBusinessId = null
                            viewModels.registerBusiness.resetState()
                            canCancelBusinessRegistration = true
                            needsBusinessRegistration = true
                        }
                    )
                }

                // Caso 5: Usuario NO autenticado → Onboarding / Login
                else -> {
                    when (preAuthScreen) {
                        PreAuthScreen.ONBOARDING -> {
                            OnboardingIntroScreen(
                                onFinish = {
                                    preAuthScreen = PreAuthScreen.LOGIN
                                }
                            )
                        }

                        PreAuthScreen.LOGIN -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AuthFlow(authViewModel)
                                // Boton de retroceso flotante sobre el login
                                IconButton(
                                    onClick = {
                                        preAuthScreen = PreAuthScreen.ONBOARDING
                                    },
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .padding(start = 4.dp, top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (navigator.showMapSelection) {
                MapSelectionScreen(
                    title = navigator.mapSelectionTitle,
                    initialLatitude = navigator.mapSelectionInitialLat,
                    initialLongitude = navigator.mapSelectionInitialLng,
                    onNavigateBack = {
                        navigator.closeMapSelection()
                    },
                    onLocationConfirmed = { lat, lng ->
                        navigator.mapSelectionCallback?.invoke(lat, lng)
                        navigator.closeMapSelection()
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthFlow(authViewModel: AuthViewModel) {
    LoginScreen(
        viewModel = authViewModel,
        onLoginSuccess = {
            // isAuthenticated se actualiza via uiState.
        }
    )
}

@Composable
private fun BranchSelectionFlow(
    navigator: AppNavigatorState,
    authViewModel: AuthViewModel,
    branches: List<com.llego.shared.data.model.Branch>,
    productViewModel: ProductViewModel,
    branchSelectorViewModel: BranchSelectorViewModel,
    invitationViewModel: InvitationViewModel,
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit,
    onStartBusinessRegistration: () -> Unit
) {
    when {
        navigator.businessToEdit != null -> {
            val business = navigator.businessToEdit!!
            BusinessEditScreen(
                business = business,
                branches = branches.filter { it.businessId == business.id },
                onNavigateBack = { navigator.businessToEdit = null },
                onBusinessUpdated = { updatedBusiness ->
                    navigator.businessToEdit = updatedBusiness
                },
                onDataChanged = {
                    authViewModel.reloadUserData()
                    branchSelectorViewModel.loadBusinesses(forceLoading = true)
                },
                onError = { _ -> },
                authViewModel = authViewModel
            )
        }

        navigator.branchToEdit != null -> {
            BranchEditScreen(
                branch = navigator.branchToEdit!!,
                onNavigateBack = { navigator.branchToEdit = null },
                onSuccess = { _ ->
                    navigator.branchToEdit = null
                    authViewModel.reloadUserData()
                    branchSelectorViewModel.loadBusinesses(forceLoading = true)
                },
                onError = { _ -> },
                authViewModel = authViewModel
            )
        }

        navigator.branchCreateBusinessId != null -> {
            BranchCreateWizardScreen(
                businessId = navigator.branchCreateBusinessId ?: "",
                onNavigateBack = { navigator.branchCreateBusinessId = null },
                onSuccess = { _ ->
                    navigator.branchCreateBusinessId = null
                    authViewModel.reloadUserData()
                    branchSelectorViewModel.loadBusinesses(forceLoading = true)
                },
                onError = { _ -> },
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                onOpenMapSelection = onOpenMapSelection
            )
        }

        else -> {
            BranchSelectorScreen(
                branchSelectorViewModel = branchSelectorViewModel,
                branches = branches,
                onBranchSelected = { branch ->
                    authViewModel.setCurrentBranch(branch)
                },
                onEditBusiness = { business ->
                    navigator.businessToEdit = business
                },
                onAddBusiness = onStartBusinessRegistration,
                onAddBranch = { businessId ->
                    navigator.branchCreateBusinessId = businessId
                },
                onLogout = {
                    authViewModel.logout()
                },
                invitationViewModel = invitationViewModel,
                authViewModel = authViewModel
            )
        }
    }
}

@Composable
private fun MainBusinessFlow(
    navigator: AppNavigatorState,
    authViewModel: AuthViewModel,
    ordersViewModel: OrdersViewModel,
    productViewModel: ProductViewModel,
    comboViewModel: ComboViewModel,
    showcaseViewModel: ShowcaseViewModel,
    settingsViewModel: SettingsViewModel,
    branchesManagementViewModel: BranchesManagementViewModel,
    branchSelectorViewModel: BranchSelectorViewModel,
    invitationViewModel: InvitationViewModel,
    deliveryLinkViewModel: DeliveryLinkViewModel,
    tokenManager: TokenManager,
    branchSwitchConfirmation: BranchSwitchConfirmationData?,
    onDismissBranchSwitchConfirmation: () -> Unit,
    showBranchNotFound: Boolean,
    onDismissBranchNotFound: () -> Unit,
    onOpenMapSelection: (String, Double, Double, (Double, Double) -> Unit) -> Unit,
    onNavigateBackToBranchSelector: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val productsState by productViewModel.productsState.collectAsState()
    val productCategoriesState by productViewModel.productCategoriesState.collectAsState()
    val variantListsState by productViewModel.variantListsState.collectAsState()
    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    val branches by authViewModel.branches.collectAsState()
    val currentBranch by authViewModel.currentBranch.collectAsState()
    val deliveryEntryPointState by deliveryLinkViewModel.uiState.collectAsState()
    val categoryNameById = remember(productCategoriesState.categories) {
        productCategoriesState.categories.associate { it.id to it.name }
    }
    val variantListNameById = remember(variantListsState.variantLists) {
        variantListsState.variantLists.associate { it.id to it.name }
    }

    LaunchedEffect(currentBranch?.id, currentBranch?.useAppMessaging) {
        val branch = currentBranch
        if (branch != null) {
            deliveryLinkViewModel.loadEntryPoint(
                branchId = branch.id,
                branchUsesAppMessaging = branch.useAppMessaging
            )
        } else {
            deliveryLinkViewModel.loadEntryPoint(
                branchId = null,
                branchUsesAppMessaging = true
            )
        }
    }

    LaunchedEffect(currentBranch?.id, currentBranch?.tipos) {
        productViewModel.loadProductCategories(
            branchId = currentBranch?.id,
            branchTipos = currentBranch?.tipos?.toSet() ?: emptySet()
        )
    }

    LaunchedEffect(currentBranch?.id) {
        currentBranch?.id?.let { branchId ->
            productViewModel.loadVariantLists(branchId = branchId)
        }
    }

    Box(modifier = Modifier) {
        when {
            navigator.showProductSearch -> {
                ProductSearchScreen(
                    productsState = productsState,
                    onNavigateBack = { navigator.showProductSearch = false },
                    onProductSelected = { product ->
                        navigator.showProductSearch = false
                        navigator.productToView = product
                        navigator.showProductDetail = true
                    },
                    onRetry = { productViewModel.loadProducts(branchId = authViewModel.getCurrentBranchId()) }
                )
            }

            navigator.showOrderDetail && navigator.selectedOrderId != null -> {
                AnimatedVisibility(
                    visible = navigator.showOrderDetail && navigator.selectedOrderId != null,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 350)
                    ) + fadeIn(animationSpec = tween(durationMillis = 350)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(durationMillis = 350)
                    ) + fadeOut(animationSpec = tween(durationMillis = 350))
                ) {
                    val order = ordersViewModel.getOrderById(navigator.selectedOrderId!!)
                    if (order != null) {
                        OrderDetailScreen(
                            order = order,
                            ordersViewModel = ordersViewModel,
                            onNavigateBack = {
                                navigator.showOrderDetail = false
                                navigator.selectedOrderId = null
                            }
                        )
                    }
                }
            }

            navigator.showProductDetail && navigator.productToView != null -> {
                ProductDetailScreen(
                    product = navigator.productToView!!,
                    categoryNameById = categoryNameById,
                    variantListNameById = variantListNameById,
                    onNavigateBack = {
                        navigator.showProductDetail = false
                        navigator.productToView = null
                    },
                    onEdit = {
                        navigator.productToEdit = navigator.productToView
                        navigator.showProductDetail = false
                        navigator.productToView = null
                        navigator.showAddProduct = true
                    }
                )
            }

            navigator.showAddCombo -> {
                AddComboScreen(
                    viewModel = comboViewModel,
                    productViewModel = productViewModel,
                    branchId = authViewModel.getCurrentBranchId() ?: "",
                    combo = navigator.comboToEdit,
                    onNavigateBack = {
                        navigator.showAddCombo = false
                        navigator.comboToEdit = null
                    }
                )
            }

            navigator.showComboDetail && navigator.comboToView != null -> {
                ComboDetailScreen(
                    combo = navigator.comboToView!!,
                    onNavigateBack = {
                        navigator.showComboDetail = false
                        navigator.comboToView = null
                    },
                    onEdit = {
                        navigator.comboToEdit = navigator.comboToView
                        navigator.showComboDetail = false
                        navigator.comboToView = null
                        navigator.showAddCombo = true
                    }
                )
            }

            navigator.showShowcaseDetail && navigator.showcaseToView != null -> {
                ShowcaseDetailScreen(
                    showcase = navigator.showcaseToView!!,
                    onNavigateBack = {
                        navigator.showShowcaseDetail = false
                        navigator.showcaseToView = null
                    },
                    onEdit = {
                        navigator.showcaseToEdit = navigator.showcaseToView
                        navigator.showShowcaseDetail = false
                        navigator.showcaseToView = null
                        navigator.showAddShowcase = true
                    }
                )
            }

            navigator.showAddProduct -> {
                AddProductScreen(
                    branchId = authViewModel.getCurrentBranchId(),
                    categories = productCategoriesState.categories,
                    variantLists = variantListsState.variantLists,
                    variantListsLoading = variantListsState.isLoading,
                    variantListsError = variantListsState.error,
                    onCreateVariantList = { branchId, name, description, options ->
                        productViewModel.createVariantList(
                            branchId = branchId,
                            name = name,
                            description = description,
                            options = options
                        )
                    },
                    onNavigateBack = {
                        navigator.showAddProduct = false
                        navigator.productToEdit = null
                    },
                    onSave = { form ->
                        val currentBranchId = authViewModel.getCurrentBranchId()
                        scope.launch {
                            if (currentBranchId == null) {
                                println("❌ Error: No hay sucursal seleccionada")
                                return@launch
                            }
                            if (form.imagePath.isNullOrBlank() && navigator.productToEdit == null) {
                                println("❌ Error: No hay imagen subida para el nuevo producto")
                                return@launch
                            }

                            println("📤 Guardando producto: ${form.name}, imagen: ${form.imagePath}")

                            val result = if (navigator.productToEdit == null) {
                                productViewModel.createProductWithImagePath(
                                    name = form.name,
                                    description = form.description.ifBlank { form.name },
                                    price = form.price,
                                    imagePath = form.imagePath ?: "",
                                    branchId = currentBranchId,
                                    currency = form.currency,
                                    weight = form.weight,
                                    categoryId = normalizeCategoryId(form.categoryId),
                                    variantListIds = form.variantListIds.takeIf { it.isNotEmpty() }
                                )
                            } else {
                                productViewModel.updateProductWithImagePath(
                                    productId = navigator.productToEdit!!.id,
                                    name = form.name,
                                    description = form.description,
                                    price = form.price,
                                    currency = form.currency,
                                    weight = form.weight,
                                    availability = form.availability,
                                    categoryId = normalizeCategoryId(form.categoryId),
                                    imagePath = form.imagePath,
                                    variantListIds = form.variantListIds
                                )
                            }

                            when (result) {
                                is ProductsResult.Success -> {
                                    println("✅ Producto guardado exitosamente")
                                    productViewModel.loadProducts(branchId = currentBranchId)
                                    navigator.showAddProduct = false
                                    navigator.productToEdit = null
                                }

                                is ProductsResult.Error -> {
                                    println("❌ Error al guardar producto: ${result.message}")
                                }

                                is ProductsResult.Loading -> {}
                            }
                        }
                    },
                    existingProduct = navigator.productToEdit
                )
            }

            navigator.showAddShowcase -> {
                AddShowcaseScreen(
                    viewModel = showcaseViewModel,
                    branchId = authViewModel.getCurrentBranchId(),
                    existingShowcase = navigator.showcaseToEdit,
                    onNavigateBack = {
                        navigator.showAddShowcase = false
                        navigator.showcaseToEdit = null
                        authViewModel.getCurrentBranchId()?.let { branchId ->
                            showcaseViewModel.loadShowcases(branchId, activeOnly = false)
                        }
                    }
                )
            }

            navigator.showStatistics -> {
                val currentBusiness by authViewModel.currentBusiness.collectAsState()
                StatisticsScreen(
                    ordersViewModel = ordersViewModel,
                    businessId = currentBusiness?.id.orEmpty(),
                    onNavigateBack = { navigator.showStatistics = false }
                )
            }

            navigator.businessToEdit != null -> {
                val business = navigator.businessToEdit!!
                BusinessEditScreen(
                    business = business,
                    branches = branches.filter { it.businessId == business.id },
                    onNavigateBack = { navigator.businessToEdit = null },
                    onBusinessUpdated = { updatedBusiness ->
                        navigator.businessToEdit = updatedBusiness
                    },
                    onDataChanged = {
                        authViewModel.reloadUserData()
                        branchSelectorViewModel.loadBusinesses(forceLoading = true)
                    },
                    onError = { _ -> },
                    authViewModel = authViewModel
                )
            }

            navigator.branchCreateBusinessId != null -> {
                BranchCreateWizardScreen(
                    businessId = navigator.branchCreateBusinessId ?: "",
                    onNavigateBack = { navigator.branchCreateBusinessId = null },
                    onSuccess = { _ ->
                        navigator.branchCreateBusinessId = null
                        authViewModel.reloadUserData()
                        branchSelectorViewModel.loadBusinesses(forceLoading = true)
                    },
                    onError = { _ -> },
                    authViewModel = authViewModel,
                    productViewModel = productViewModel,
                    onOpenMapSelection = onOpenMapSelection
                )
            }

            navigator.branchToEdit != null -> {
                BranchEditScreen(
                    branch = navigator.branchToEdit!!,
                    onNavigateBack = { navigator.branchToEdit = null },
                    onSuccess = { _ ->
                        navigator.branchToEdit = null
                        authViewModel.reloadUserData()
                        branchSelectorViewModel.loadBusinesses(forceLoading = true)
                    },
                    onError = { _ -> },
                    authViewModel = authViewModel
                )
            }

            navigator.showBranchesManagement -> {
                BranchSelectorScreen(
                    branchSelectorViewModel = branchSelectorViewModel,
                    branches = branches,
                    onBranchSelected = { branch ->
                        authViewModel.setCurrentBranch(branch)
                        navigator.showBranchesManagement = false
                    },
                    onEditBusiness = { business ->
                        navigator.businessToEdit = business
                        navigator.showBranchesManagement = false
                    },
                    onAddBusiness = {
                        navigator.branchCreateBusinessId = null
                        navigator.showBranchesManagement = false
                    },
                    onAddBranch = { businessId ->
                        navigator.branchCreateBusinessId = businessId
                        navigator.showBranchesManagement = false
                    },
                    onLogout = {
                        authViewModel.logout()
                    },
                    invitationViewModel = invitationViewModel,
                    authViewModel = authViewModel
                )
            }

            navigator.showDeliveryManagement -> {
                val targetBranch = currentBranch
                if (targetBranch != null) {
                    DeliveryLinkManagementScreen(
                        viewModel = deliveryLinkViewModel,
                        branchId = targetBranch.id,
                        branchName = targetBranch.name,
                        initialBranchUsesAppMessaging = targetBranch.useAppMessaging,
                        onNavigateBack = { navigator.showDeliveryManagement = false },
                        onDeliveryModeEnabled = { authViewModel.reloadUserData() }
                    )
                } else {
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
                                modifier = Modifier.height(12.dp)
                            )
                            androidx.compose.material3.Text(
                                text = "Buscando sucursal..."
                            )
                        }
                    }
                }
            }

            navigator.showInvitations -> {
                val currentBusiness by authViewModel.currentBusiness.collectAsState()

                if (currentBusiness != null) {
                    InvitationDashboard(
                        viewModel = invitationViewModel,
                        businessId = currentBusiness!!.id,
                        businessName = currentBusiness!!.name,
                        branches = branches.map { it.id to it.name },
                        onNavigateBack = { navigator.showInvitations = false }
                    )
                }
            }

            navigator.showProfile -> {
                BusinessProfileScreen(
                    authViewModel = authViewModel,
                    productViewModel = productViewModel,
                    onNavigateBack = { navigator.showProfile = false },
                    onOpenMapSelection = onOpenMapSelection
                )
            }

            else -> {
                BusinessHomeScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = onNavigateBackToBranchSelector,
                    onNavigateToProfile = { navigator.showProfile = true },
                    onNavigateToInvitations = { navigator.showInvitations = true },
                    onNavigateToDeliveryManagement = { navigator.showDeliveryManagement = true },
                    showDeliveryManagementAction = true,
                    deliveryPendingRequestsCount = deliveryEntryPointState.pendingRequestCount,
                    selectedTabIndex = navigator.selectedHomeTabIndex,
                    onTabSelected = {
                        navigator.selectedHomeTabIndex = it
                        tokenManager.saveLastHomeTabIndex(it)
                    },
                    onNavigateToOrderDetail = { orderId ->
                        navigator.selectedOrderId = orderId
                        navigator.showOrderDetail = true
                    },
                    onNavigateToAddProduct = { product ->
                        navigator.productToEdit = product
                        navigator.showAddProduct = true
                    },
                    onNavigateToAddShowcase = {
                        navigator.showShowcaseDetail = false
                        navigator.showcaseToView = null
                        navigator.showcaseToEdit = null
                        navigator.showAddShowcase = true
                    },
                    onNavigateToShowcaseDetail = { showcase ->
                        navigator.showShowcaseDetail = true
                        navigator.showcaseToView = showcase
                    },
                    onNavigateToEditShowcase = { showcase ->
                        navigator.showShowcaseDetail = false
                        navigator.showcaseToView = null
                        navigator.showcaseToEdit = showcase
                        navigator.showAddShowcase = true
                    },
                    onNavigateToAddCombo = { combo ->
                        navigator.comboToEdit = combo
                        navigator.showAddCombo = true
                    },
                    onNavigateToProductDetail = { product ->
                        navigator.productToView = product
                        navigator.showProductDetail = true
                    },
                    onNavigateToComboDetail = { combo ->
                        navigator.comboToView = combo
                        navigator.showComboDetail = true
                    },
                    onShowConfirmation = { type, orderNumber ->
                        navigator.showConfirmation(type, orderNumber)
                    },
                    businessId = currentBusiness?.id.orEmpty(),
                    ordersViewModel = ordersViewModel,
                    productViewModel = productViewModel,
                    comboViewModel = comboViewModel,
                    showcaseViewModel = showcaseViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }

        navigator.confirmationType?.let { type ->
            OrderConfirmationScreen(
                type = type,
                orderNumber = navigator.confirmationOrderNumber,
                onDismiss = {
                    navigator.dismissConfirmation()
                }
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            BranchSwitchConfirmation(
                data = branchSwitchConfirmation,
                onDismiss = onDismissBranchSwitchConfirmation
            )

            BranchNotFoundSnackbar(
                visible = showBranchNotFound,
                onDismiss = onDismissBranchNotFound
            )
        }
    }
}
