package com.llego.business.home.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.home.config.HomeTabsProvider
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.business.orders.ui.screens.ConfirmationType
import com.llego.business.orders.ui.screens.OrdersScreen
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.products.ui.screens.ProductsScreen
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.business.wallet.ui.screens.WalletScreen
import com.llego.shared.data.model.Product
import com.llego.shared.ui.auth.AuthViewModel

/**
 * Pantalla principal generica sin diferenciacion por tipo de negocio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHomeScreen(
    authViewModel: AuthViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToStatistics: () -> Unit = {},
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onNavigateToOrderDetail: (String) -> Unit = {},
    onNavigateToAddProduct: (Product?) -> Unit = {},
    onNavigateToProductDetail: (Product) -> Unit = {},
    onShowConfirmation: ((ConfirmationType, String) -> Unit)? = null,
    ordersViewModel: OrdersViewModel,
    productViewModel: ProductViewModel,
    settingsViewModel: SettingsViewModel
) {
    val tabs = HomeTabsProvider.getTabs()

    val currentBusiness by authViewModel.currentBusiness.collectAsState()
    val businessName = currentBusiness?.name ?: "Mi negocio"

    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0

    Scaffold(
        topBar = {
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
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Estadisticas",
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
        },
        bottomBar = {
            if (!isKeyboardVisible) {
                Surface(
                    shadowElevation = 16.dp,
                    tonalElevation = 0.dp
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(
                start = paddingValues.calculateStartPadding(layoutDirection),
                top = paddingValues.calculateTopPadding(),
                end = paddingValues.calculateEndPadding(layoutDirection),
                bottom = paddingValues.calculateBottomPadding()
            )
        ) {
            when (tabs[selectedTabIndex].id) {
                "orders" -> {
                    OrdersScreen(
                        viewModel = ordersViewModel,
                        onNavigateToOrderDetail = onNavigateToOrderDetail,
                        onShowConfirmation = onShowConfirmation
                    )
                }
                "products" -> {
                    ProductsScreen(
                        viewModel = productViewModel,
                        branchId = authViewModel.getCurrentBranchId(),
                        onNavigateToAddProduct = onNavigateToAddProduct,
                        onNavigateToProductDetail = onNavigateToProductDetail
                    )
                }
                "wallet" -> {
                    WalletScreen(onNavigateBack = { })
                }
                "settings" -> {
                    com.llego.business.settings.ui.screens.SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { }
                    )
                }
            }
        }
    }
}
