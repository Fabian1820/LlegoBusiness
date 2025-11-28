package com.llego.shared.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.llego.shared.data.model.BusinessType

/**
 * Controlador de navegación centralizado para las apps de Llego
 * Ahora basado en nichos de negocio: Restaurant, Market, Pharmacy, etc.
 */

class LlegoNavigationController(
    val navController: NavHostController,
    private val businessType: BusinessType
) {

    /**
     * Navega a la pantalla de login
     */
    fun navigateToLogin() {
        navController.navigate(Routes.Auth.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    /**
     * Navega al dashboard principal según el tipo de negocio
     */
    fun navigateToDashboard() {
        val route = Routes.getDashboardRoute(businessType)
        navController.navigate(route) {
            popUpTo(Routes.Auth.LOGIN) { inclusive = true }
        }
    }

    /**
     * Navega al perfil del negocio
     */
    fun navigateToProfile() {
        val route = Routes.getProfileRoute(businessType)
        navController.navigate(route)
    }

    /**
     * Maneja el logout y regresa al login
     */
    fun handleLogout() {
        navigateToLogin()
    }

    /**
     * Navegación a órdenes/pedidos
     */
    fun navigateToOrders() {
        val route = when (businessType) {
            BusinessType.RESTAURANT -> Routes.Restaurant.ORDERS
            BusinessType.MARKET -> Routes.Market.ORDERS
            BusinessType.AGROMARKET -> Routes.Market.ORDERS
            BusinessType.CLOTHING_STORE -> Routes.Market.ORDERS
            BusinessType.PHARMACY -> Routes.Pharmacy.ORDERS
        }
        navController.navigate(route)
    }

    fun navigateToOrderDetail(orderId: String) {
        val route = when (businessType) {
            BusinessType.RESTAURANT -> Routes.Restaurant.orderDetail(orderId)
            BusinessType.MARKET -> Routes.Market.orderDetail(orderId)
            BusinessType.AGROMARKET -> Routes.Market.orderDetail(orderId)
            BusinessType.CLOTHING_STORE -> Routes.Market.orderDetail(orderId)
            BusinessType.PHARMACY -> Routes.Pharmacy.orderDetail(orderId)
        }
        navController.navigate(route)
    }

    // Navegación específica para Restaurant
    fun navigateToMenu() {
        if (businessType == BusinessType.RESTAURANT) {
            navController.navigate(Routes.Restaurant.MENU)
        }
    }

    fun navigateToMenuItem(menuItemId: String) {
        if (businessType == BusinessType.RESTAURANT) {
            navController.navigate(Routes.Restaurant.menuItemDetail(menuItemId))
        }
    }

    // Navegación específica para Market
    fun navigateToProducts() {
        if (businessType == BusinessType.MARKET || businessType == BusinessType.AGROMARKET || businessType == BusinessType.CLOTHING_STORE) {
            navController.navigate(Routes.Market.PRODUCTS)
        }
    }

    fun navigateToProductDetail(productId: String) {
        if (businessType == BusinessType.MARKET || businessType == BusinessType.AGROMARKET || businessType == BusinessType.CLOTHING_STORE) {
            navController.navigate(Routes.Market.productDetail(productId))
        }
    }

    fun navigateToInventory() {
        if (businessType == BusinessType.MARKET || businessType == BusinessType.AGROMARKET || businessType == BusinessType.CLOTHING_STORE) {
            navController.navigate(Routes.Market.INVENTORY)
        }
    }

    // Navegación específica para Pharmacy
    fun navigateToMedicines() {
        if (businessType == BusinessType.PHARMACY) {
            navController.navigate(Routes.Pharmacy.MEDICINES)
        }
    }

    fun navigateToPrescriptions() {
        if (businessType == BusinessType.PHARMACY) {
            navController.navigate(Routes.Pharmacy.PRESCRIPTIONS)
        }
    }

    // Navegación a configuraciones
    fun navigateToSettings() {
        val route = when (businessType) {
            BusinessType.RESTAURANT -> Routes.Restaurant.SETTINGS
            BusinessType.MARKET -> Routes.Market.SETTINGS
            BusinessType.AGROMARKET -> Routes.Market.SETTINGS
            BusinessType.CLOTHING_STORE -> Routes.Market.SETTINGS
            BusinessType.PHARMACY -> Routes.Pharmacy.SETTINGS
        }
        navController.navigate(route)
    }

    fun navigateToChats() {
        val route = when (businessType) {
            BusinessType.RESTAURANT -> Routes.Restaurant.CHATS
            BusinessType.MARKET -> Routes.Market.NOTIFICATIONS
            BusinessType.AGROMARKET -> Routes.Market.NOTIFICATIONS
            BusinessType.CLOTHING_STORE -> Routes.Market.NOTIFICATIONS
            BusinessType.PHARMACY -> Routes.Pharmacy.NOTIFICATIONS
        }
        navController.navigate(route)
    }

    fun navigateToChatDetail(orderId: String) {
        if (businessType == BusinessType.RESTAURANT) {
            navController.navigate(Routes.Restaurant.chatDetail(orderId))
        }
    }

    /**
     * Navegación hacia atrás
     */
    fun navigateBack() {
        navController.popBackStack()
    }

    /**
     * Verifica si se puede navegar hacia atrás
     */
    fun canNavigateBack(): Boolean {
        return navController.previousBackStackEntry != null
    }
}

/**
 * Composable para recordar el controlador de navegación
 */
@Composable
fun rememberLlegoNavigationController(
    businessType: BusinessType,
    navController: NavHostController = rememberNavController()
): LlegoNavigationController {
    return remember(navController, businessType) {
        LlegoNavigationController(navController, businessType)
    }
}