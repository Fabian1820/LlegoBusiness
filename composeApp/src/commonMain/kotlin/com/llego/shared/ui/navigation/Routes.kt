package com.llego.shared.ui.navigation

import com.llego.shared.data.model.BusinessType

/**
 * Definición centralizada de rutas de navegación para todo el sistema Llego
 * Ahora basado en nichos de negocio: Restaurant, Market, Pharmacy, etc.
 */

object Routes {

    // Rutas de autenticación compartidas
    object Auth {
        const val LOGIN = "login"
        const val FORGOT_PASSWORD = "forgot_password"
        const val VERIFY_CODE = "verify_code"
        const val RESET_PASSWORD = "reset_password"
    }

    // Rutas para nicho Restaurant
    object Restaurant {
        const val DASHBOARD = "restaurant/dashboard"
        const val ORDERS = "restaurant/orders"
        const val ORDER_DETAIL = "restaurant/orders/{orderId}"
        const val MENU = "restaurant/menu"
        const val MENU_ITEM_DETAIL = "restaurant/menu/{menuItemId}"
        const val ANALYTICS = "restaurant/analytics"
        const val WALLET = "restaurant/wallet"
        const val PROFILE = "restaurant/profile"
        const val SETTINGS = "restaurant/settings"
        const val TUTORIALS = "restaurant/tutorials"
        const val CHATS = "restaurant/chats"
        const val CHAT_DETAIL = "restaurant/chats/{orderId}"

        // Funciones helper para rutas con parámetros
        fun orderDetail(orderId: String) = "restaurant/orders/$orderId"
        fun menuItemDetail(menuItemId: String) = "restaurant/menu/$menuItemId"
        fun chatDetail(orderId: String) = "restaurant/chats/$orderId"
    }

    // Rutas para nicho Market/Grocery
    object Market {
        const val DASHBOARD = "market/dashboard"
        const val ORDERS = "market/orders"
        const val ORDER_DETAIL = "market/orders/{orderId}"
        const val PRODUCTS = "market/products"
        const val PRODUCT_DETAIL = "market/products/{productId}"
        const val INVENTORY = "market/inventory"
        const val ANALYTICS = "market/analytics"
        const val WALLET = "market/wallet"
        const val PROFILE = "market/profile"
        const val SETTINGS = "market/settings"
        const val NOTIFICATIONS = "market/notifications"

        // Funciones helper para rutas con parámetros
        fun orderDetail(orderId: String) = "market/orders/$orderId"
        fun productDetail(productId: String) = "market/products/$productId"
    }

    // Rutas para nicho Pharmacy (preparado para el futuro)
    object Pharmacy {
        const val DASHBOARD = "pharmacy/dashboard"
        const val ORDERS = "pharmacy/orders"
        const val ORDER_DETAIL = "pharmacy/orders/{orderId}"
        const val MEDICINES = "pharmacy/medicines"
        const val MEDICINE_DETAIL = "pharmacy/medicines/{medicineId}"
        const val PRESCRIPTIONS = "pharmacy/prescriptions"
        const val WALLET = "pharmacy/wallet"
        const val PROFILE = "pharmacy/profile"
        const val SETTINGS = "pharmacy/settings"
        const val NOTIFICATIONS = "pharmacy/notifications"

        // Funciones helper para rutas con parámetros
        fun orderDetail(orderId: String) = "pharmacy/orders/$orderId"
        fun medicineDetail(medicineId: String) = "pharmacy/medicines/$medicineId"
    }

    // Rutas de configuración y utilidades
    object Settings {
        const val ACCOUNT = "settings/account"
        const val PRIVACY = "settings/privacy"
        const val NOTIFICATIONS = "settings/notifications"
        const val HELP = "settings/help"
        const val ABOUT = "settings/about"
    }

    // Helper para obtener rutas por tipo de negocio
    fun getDashboardRoute(businessType: BusinessType): String {
        return when (businessType) {
            BusinessType.RESTAURANT -> Restaurant.DASHBOARD
            BusinessType.MARKET -> Market.DASHBOARD
            BusinessType.AGROMARKET -> Market.DASHBOARD
            BusinessType.CLOTHING_STORE -> Market.DASHBOARD
            BusinessType.PHARMACY -> Pharmacy.DASHBOARD
        }
    }

    fun getProfileRoute(businessType: BusinessType): String {
        return when (businessType) {
            BusinessType.RESTAURANT -> Restaurant.PROFILE
            BusinessType.MARKET -> Market.PROFILE
            BusinessType.AGROMARKET -> Market.PROFILE
            BusinessType.CLOTHING_STORE -> Market.PROFILE
            BusinessType.PHARMACY -> Pharmacy.PROFILE
        }
    }
}

/**
 * Tipos de navegación para diferentes secciones
 */
enum class NavigationType {
    AUTH,        // Navegación durante la autenticación
    BUSINESS,    // Navegación dentro de la app de negocios
    DRIVER,      // Navegación dentro de la app de choferes
    SETTINGS     // Navegación en configuraciones
}

/**
 * Argumentos de navegación estándar
 */
object NavArgs {
    const val ORDER_ID = "orderId"
    const val PRODUCT_ID = "productId"
    const val USER_ID = "userId"
    const val BUSINESS_ID = "businessId"
}

/**
 * Definición de bottom navigation items para cada tipo de app
 */
data class NavigationItem(
    val route: String,
    val labelResId: String,
    val iconResId: String,
    val badgeCount: Int? = null
)

// TODO: Crear NavigationItems específicos para cada nicho cuando se implementen dashboards completos
/*
object NavigationItems {

    // Items de navegación para Restaurant
    val restaurantItems = listOf(
        NavigationItem(
            route = Routes.Restaurant.DASHBOARD,
            labelResId = "Dashboard",
            iconResId = "ic_dashboard"
        ),
        NavigationItem(
            route = Routes.Restaurant.ORDERS,
            labelResId = "Pedidos",
            iconResId = "ic_orders"
        ),
        NavigationItem(
            route = Routes.Restaurant.MENU,
            labelResId = "Menú",
            iconResId = "ic_menu"
        ),
        NavigationItem(
            route = Routes.Restaurant.PROFILE,
            labelResId = "Perfil",
            iconResId = "ic_profile"
        )
    )

    // Items de navegación para Market
    val marketItems = listOf(
        NavigationItem(
            route = Routes.Market.DASHBOARD,
            labelResId = "Dashboard",
            iconResId = "ic_dashboard"
        ),
        NavigationItem(
            route = Routes.Market.ORDERS,
            labelResId = "Pedidos",
            iconResId = "ic_orders"
        ),
        NavigationItem(
            route = Routes.Market.PRODUCTS,
            labelResId = "Productos",
            iconResId = "ic_products"
        ),
        NavigationItem(
            route = Routes.Market.PROFILE,
            labelResId = "Perfil",
            iconResId = "ic_profile"
        )
    )
}
*/