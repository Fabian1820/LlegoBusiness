package com.llego.nichos.common.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.llego.shared.data.model.BusinessType

/**
 * Configuración de tabs para la navegación inferior
 */
data class BusinessTabConfig(
    val id: String,
    val title: String,
    val icon: ImageVector
)

/**
 * Configuración de categorías para filtrado de productos/menú
 */
data class BusinessCategoryConfig(
    val id: String,
    val displayName: String
)

/**
 * Feature flags por tipo de negocio
 * Controla qué funcionalidades están habilitadas
 */
data class BusinessFeatures(
    val usesMenu: Boolean = false,
    val usesProducts: Boolean = false,
    val usesChat: Boolean = true,
    val usesStatistics: Boolean = true,
    val usesWallet: Boolean = true,
    val usesSettings: Boolean = false,
    val usesTutorials: Boolean = false,
    val usesPrescriptions: Boolean = false, // Para farmacias
    val usesInventoryTracking: Boolean = false
)

/**
 * Labels y textos personalizados por nicho
 */
data class BusinessLabels(
    val businessName: String,
    val catalogTitle: String,
    val catalogItemSingular: String,
    val catalogItemPlural: String,
    val addItemTitle: String,
    val editItemTitle: String,
    val emptyStateMessage: String,
    val searchPlaceholder: String
)

/**
 * Proveedor de configuración por tipo de negocio
 * Centraliza todas las personalizaciones según el nicho
 */
object BusinessConfigProvider {

    /**
     * Obtiene los tabs de navegación según el tipo de negocio
     */
    fun getTabsForBusiness(businessType: BusinessType): List<BusinessTabConfig> {
        return when (businessType) {
            BusinessType.RESTAURANT -> listOf(
                BusinessTabConfig("orders", "Pedidos", Icons.Default.ShoppingCart),
                BusinessTabConfig("menu", "Menú", Icons.Default.Restaurant),
                BusinessTabConfig("wallet", "Wallet", Icons.Default.AccountBalanceWallet),
                BusinessTabConfig("settings", "Ajustes", Icons.Default.Settings)
            )

            BusinessType.MARKET -> listOf(
                BusinessTabConfig("orders", "Pedidos", Icons.Default.ShoppingCart),
                BusinessTabConfig("products", "Productos", Icons.Default.Inventory),
                BusinessTabConfig("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
            )

            BusinessType.CANDY_STORE -> listOf(
                BusinessTabConfig("orders", "Pedidos", Icons.Default.ShoppingCart),
                BusinessTabConfig("products", "Productos", Icons.Default.Inventory),
                BusinessTabConfig("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
            )
        }
    }

    /**
     * Obtiene el nombre personalizado del segundo tab según el nicho
     * Útil para saber qué pantalla renderizar
     */
    fun getContentTabId(businessType: BusinessType): String {
        return when (businessType) {
            BusinessType.RESTAURANT -> "menu"
            BusinessType.MARKET -> "products"
            BusinessType.CANDY_STORE -> "products"
        }
    }

    /**
     * Verifica si el nicho usa productos (Market, Candy Store)
     */
    fun usesProducts(businessType: BusinessType): Boolean {
        return businessType in listOf(
            BusinessType.MARKET,
            BusinessType.CANDY_STORE
        )
    }

    /**
     * Verifica si el nicho usa menú (Restaurant)
     */
    fun usesMenu(businessType: BusinessType): Boolean {
        return businessType == BusinessType.RESTAURANT
    }

    /**
     * Obtiene las feature flags según el tipo de negocio
     */
    fun getFeaturesForBusiness(businessType: BusinessType): BusinessFeatures {
        return when (businessType) {
            BusinessType.RESTAURANT -> BusinessFeatures(
                usesMenu = true,
                usesProducts = false,
                usesChat = true,
                usesStatistics = true,
                usesWallet = true,
                usesSettings = true,
                usesTutorials = false,
                usesPrescriptions = false,
                usesInventoryTracking = false
            )

            BusinessType.MARKET -> BusinessFeatures(
                usesMenu = false,
                usesProducts = true,
                usesChat = true,
                usesStatistics = true,
                usesWallet = true,
                usesSettings = false,
                usesTutorials = false,
                usesPrescriptions = false,
                usesInventoryTracking = true
            )

            BusinessType.CANDY_STORE -> BusinessFeatures(
                usesMenu = false,
                usesProducts = true,
                usesChat = true,
                usesStatistics = true,
                usesWallet = true,
                usesSettings = false,
                usesTutorials = false,
                usesPrescriptions = false,
                usesInventoryTracking = true
            )
        }
    }

    /**
     * Obtiene los labels personalizados según el tipo de negocio
     */
    fun getLabelsForBusiness(businessType: BusinessType): BusinessLabels {
        return when (businessType) {
            BusinessType.RESTAURANT -> BusinessLabels(
                businessName = "Restaurante",
                catalogTitle = "Menú",
                catalogItemSingular = "Platillo",
                catalogItemPlural = "Platillos",
                addItemTitle = "Agregar Platillo",
                editItemTitle = "Editar Platillo",
                emptyStateMessage = "No hay platillos en el menú",
                searchPlaceholder = "Buscar platillos..."
            )

            BusinessType.MARKET -> BusinessLabels(
                businessName = "Supermercado",
                catalogTitle = "Productos",
                catalogItemSingular = "Producto",
                catalogItemPlural = "Productos",
                addItemTitle = "Agregar Producto",
                editItemTitle = "Editar Producto",
                emptyStateMessage = "No hay productos disponibles",
                searchPlaceholder = "Buscar productos..."
            )

            BusinessType.CANDY_STORE -> BusinessLabels(
                businessName = "Dulcería",
                catalogTitle = "Dulces",
                catalogItemSingular = "Dulce",
                catalogItemPlural = "Dulces",
                addItemTitle = "Agregar Dulce",
                editItemTitle = "Editar Dulce",
                emptyStateMessage = "No hay dulces disponibles",
                searchPlaceholder = "Buscar dulces..."
            )
        }
    }

    /**
     * Obtiene las categorías de filtrado según el tipo de negocio
     */
    fun getCategoriesForBusiness(businessType: BusinessType): List<BusinessCategoryConfig> {
        return when (businessType) {
            BusinessType.RESTAURANT -> listOf(
                BusinessCategoryConfig("entradas", "Entradas"),
                BusinessCategoryConfig("platos_fuertes", "Platos Fuertes"),
                BusinessCategoryConfig("postres", "Postres"),
                BusinessCategoryConfig("bebidas", "Bebidas"),
                BusinessCategoryConfig("especiales", "Especiales")
            )

            BusinessType.MARKET -> listOf(
                BusinessCategoryConfig("frutas_verduras", "Frutas y Verduras"),
                BusinessCategoryConfig("carnes", "Carnes y Pescados"),
                BusinessCategoryConfig("lacteos", "Lácteos"),
                BusinessCategoryConfig("panaderia", "Panadería"),
                BusinessCategoryConfig("bebidas", "Bebidas"),
                BusinessCategoryConfig("limpieza", "Limpieza"),
                BusinessCategoryConfig("despensa", "Despensa")
            )

            BusinessType.CANDY_STORE -> listOf(
                BusinessCategoryConfig("chocolates", "Chocolates"),
                BusinessCategoryConfig("gomitas", "Gomitas"),
                BusinessCategoryConfig("caramelos", "Caramelos"),
                BusinessCategoryConfig("galletas", "Galletas"),
                BusinessCategoryConfig("snacks", "Snacks"),
                BusinessCategoryConfig("bebidas", "Bebidas")
            )
        }
    }
}


