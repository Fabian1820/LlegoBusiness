package com.llego.business.home.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.llego.business.home.config.HomeTabConfig
import com.llego.business.home.config.HomeTabIcon
import com.llego.business.home.config.HomeTabsProvider
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.business.products.ui.viewmodel.ShowcaseViewModel
import com.llego.business.analytics.ui.screens.StatisticsScreen
import com.llego.business.orders.ui.screens.ConfirmationType
import com.llego.business.orders.ui.screens.OrdersScreen
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.products.ui.screens.ProductsScreen
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.avatarSmallUrl
import com.llego.shared.ui.auth.AuthViewModel
import org.jetbrains.compose.resources.painterResource

/**
 * Pantalla principal generica sin diferenciacion por tipo de negocio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHomeScreen(
    authViewModel: AuthViewModel,
    businessId: String = "",
    onNavigateBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    onNavigateToInvitations: () -> Unit = {},
    onNavigateToDeliveryManagement: () -> Unit = {},
    showDeliveryManagementAction: Boolean = false,
    deliveryPendingRequestsCount: Int = 0,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onNavigateToOrderDetail: (String) -> Unit = {},
    onNavigateToAddProduct: (Product?) -> Unit = {},
    onNavigateToAddShowcase: () -> Unit = {},
    onNavigateToShowcaseDetail: (com.llego.shared.data.model.Showcase) -> Unit = {},
    onNavigateToEditShowcase: (com.llego.shared.data.model.Showcase) -> Unit = {},
    onNavigateToAddCombo: (com.llego.shared.data.model.Combo?) -> Unit = {},
    onNavigateToProductDetail: (Product) -> Unit = {},
    onNavigateToComboDetail: (com.llego.shared.data.model.Combo) -> Unit = {},
    onShowConfirmation: ((ConfirmationType, String) -> Unit)? = null,
    ordersViewModel: OrdersViewModel,
    productViewModel: ProductViewModel,
    comboViewModel: com.llego.business.products.ui.viewmodel.ComboViewModel,
    showcaseViewModel: ShowcaseViewModel,
    settingsViewModel: SettingsViewModel
) {
    val tabs = HomeTabsProvider.getTabs()
    val safeSelectedTabIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex)
    val selectedTabId = tabs.getOrNull(safeSelectedTabIndex)?.id
    val searchEnabledForCurrentTab = selectedTabId == "orders" || selectedTabId == "products"

    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    val currentBranch by authViewModel.currentBranch.collectAsState()
    val isCatalogOnly = currentBranch?.catalogOnly == true
    val topBarTitle = currentBranch?.name ?: currentBusiness?.name ?: "Mi negocio"
    val branchAvatarUrl = currentBranch?.avatarSmallUrl()?.takeIf { it.isNotBlank() }
        ?: currentBranch?.avatar?.takeIf { it.isNotBlank() }

    var isSearchMode by remember { mutableStateOf(false) }
    var ordersSearchQuery by remember { mutableStateOf("") }
    var productsSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchEnabledForCurrentTab) {
        if (!searchEnabledForCurrentTab) {
            isSearchMode = false
        }
    }

    LaunchedEffect(selectedTabId, currentBranch?.id) {
        if (selectedTabId == "settings" && currentBranch?.id != null) {
            settingsViewModel.loadSettings()
        }
    }

    LaunchedEffect(selectedTabIndex, safeSelectedTabIndex) {
        if (selectedTabIndex != safeSelectedTabIndex) {
            onTabSelected(safeSelectedTabIndex)
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0

    val activeSearchQuery = when (selectedTabId) {
        "orders" -> ordersSearchQuery
        "products" -> productsSearchQuery
        else -> ""
    }

    fun updateActiveSearchQuery(value: String) {
        when (selectedTabId) {
            "orders" -> ordersSearchQuery = value
            "products" -> productsSearchQuery = value
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(shadowElevation = 2.dp, color = MaterialTheme.colorScheme.background) {
                AnimatedContent(
                    targetState = isSearchMode && searchEnabledForCurrentTab,
                    transitionSpec = {
                        slideIntoContainer(
                            towards = if (targetState) {
                                AnimatedContentTransitionScope.SlideDirection.Left
                            } else {
                                AnimatedContentTransitionScope.SlideDirection.Right
                            },
                            animationSpec = tween(220, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(180)) togetherWith
                            slideOutOfContainer(
                                towards = if (targetState) {
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                } else {
                                    AnimatedContentTransitionScope.SlideDirection.Right
                                },
                                animationSpec = tween(220, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(150))
                    },
                    label = "business_home_topbar_transition"
                ) { searchMode ->
                    if (searchMode) {
                        TopAppBar(
                            title = {
                                OutlinedTextField(
                                    value = activeSearchQuery,
                                    onValueChange = { updateActiveSearchQuery(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = if (selectedTabId == "orders") {
                                                "Busca por items del pedido"
                                            } else {
                                                "Busca productos"
                                            }
                                        )
                                    },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        updateActiveSearchQuery("")
                                        isSearchMode = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar busqueda"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            ),
                            windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                        )
                    } else {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            title = {
                                Text(
                                    topBarTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            },
                            actions = {
                                if (searchEnabledForCurrentTab) {
                                    IconButton(onClick = { isSearchMode = true }) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Buscar",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                IconButton(onClick = onNavigateToProfile) {
                                    if (!branchAvatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = branchAvatarUrl,
                                            contentDescription = "Perfil de la sucursal",
                                            modifier = Modifier
                                                .size(28.dp)
                                                .offset(y = 0.5.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Perfil",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!isKeyboardVisible) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth(),
                        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                    ) {
                            val pendingCount = ordersViewModel.getPendingOrdersCount()

                            tabs.forEachIndexed { index, tab ->
                                val isSelected = safeSelectedTabIndex == index

                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { onTabSelected(index) },
                                    icon = {
                                        val showOrdersBadge = tab.id == "orders" && pendingCount > 0 && !isCatalogOnly
                                        val showSettingsBadge = tab.id == "settings" &&
                                            showDeliveryManagementAction &&
                                            !isCatalogOnly &&
                                            deliveryPendingRequestsCount > 0

                                        if (showOrdersBadge || showSettingsBadge) {
                                            val badgeCount = if (showOrdersBadge) pendingCount else deliveryPendingRequestsCount
                                            BadgedBox(
                                                badge = {
                                                    Badge(
                                                        containerColor = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    ) {
                                                        Text(
                                                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
                                                            ),
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            ) {
                                                HomeNavigationTabIcon(
                                                    tab = tab,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            HomeNavigationTabIcon(
                                                tab = tab,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                            )
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                    }
                }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    bottom = paddingValues.calculateBottomPadding()
                )
                .imePadding()
        ) {
            when (tabs[safeSelectedTabIndex].id) {
                "orders" -> {
                    if (isCatalogOnly) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Esta sucursal no recibe pedidos",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        OrdersScreen(
                            viewModel = ordersViewModel,
                            onNavigateToOrderDetail = onNavigateToOrderDetail,
                            searchQuery = ordersSearchQuery,
                            onShowConfirmation = onShowConfirmation
                        )
                    }
                }

                "products" -> {
                    ProductsScreen(
                        viewModel = productViewModel,
                        comboViewModel = comboViewModel,
                        showcaseViewModel = showcaseViewModel,
                        branchId = authViewModel.getCurrentBranchId(),
                        branchTipos = currentBranch?.tipos?.toSet() ?: emptySet(),
                        searchQuery = productsSearchQuery,
                        onNavigateToAddProduct = onNavigateToAddProduct,
                        onNavigateToAddShowcase = onNavigateToAddShowcase,
                        onNavigateToShowcaseDetail = onNavigateToShowcaseDetail,
                        onNavigateToEditShowcase = onNavigateToEditShowcase,
                        onNavigateToAddCombo = onNavigateToAddCombo,
                        onNavigateToProductDetail = onNavigateToProductDetail,
                        onNavigateToComboDetail = onNavigateToComboDetail
                    )
                }

                "statistics" -> {
                    StatisticsScreen(
                        ordersViewModel = ordersViewModel,
                        businessId = businessId,
                        embeddedInHome = true
                    )
                }

                "settings" -> {
                    com.llego.business.settings.ui.screens.SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        authViewModel = authViewModel,
                        onNavigateToDeliveryManagement = onNavigateToDeliveryManagement,
                        onNavigateToInvitations = onNavigateToInvitations,
                        showDeliveryManagementButton = showDeliveryManagementAction && !isCatalogOnly,
                        pendingDeliveryRequestsCount = deliveryPendingRequestsCount,
                        onNavigateBack = { }
                    )
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun HomeNavigationTabIcon(
    tab: HomeTabConfig,
    modifier: Modifier = Modifier
) {
    when (val icon = tab.icon) {
        is HomeTabIcon.Vector -> Icon(
            imageVector = icon.value,
            contentDescription = tab.title,
            modifier = modifier
        )

        is HomeTabIcon.Drawable -> Icon(
            painter = painterResource(icon.value),
            contentDescription = tab.title,
            modifier = modifier
        )
    }
}
