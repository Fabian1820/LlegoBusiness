package com.llego.nichos.common.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Configuración de tabs para la navegación inferior
 * GENÉRICA - sin diferenciación por tipo de negocio
 */
data class BusinessTabConfig(
    val id: String,
    val title: String,
    val icon: ImageVector
)

/**
 * Configuración de categorías para filtrado de productos
 */
data class BusinessCategoryConfig(
    val id: String,
    val displayName: String
)

/**
 * Feature flags genéricos para todos los negocios
 * Todos los tipos de negocio tienen las mismas funcionalidades
 */
data class BusinessFeatures(
    val usesProducts: Boolean = true,      // Todos usan productos/menú
    val usesChat: Boolean = true,
    val usesStatistics: Boolean = true,
    val usesWallet: Boolean = true,
    val usesSettings: Boolean = true
)

/**
 * Labels genéricos - sin personalización por nicho
 */
data class BusinessLabels(
    val catalogTitle: String = "Productos",
    val catalogItemSingular: String = "Producto",
    val catalogItemPlural: String = "Productos",
    val addItemTitle: String = "Agregar Producto",
    val editItemTitle: String = "Editar Producto",
    val emptyStateMessage: String = "No hay productos disponibles",
    val searchPlaceholder: String = "Buscar productos..."
)

/**
 * Proveedor de configuración genérica
 * Ya no hay diferenciación por tipo de negocio - todos usan la misma configuración
 */
object BusinessConfigProvider {

    /**
     * Obtiene los tabs de navegación genéricos (iguales para todos)
     */
    fun getTabs(): List<BusinessTabConfig> {
        return listOf(
            BusinessTabConfig("orders", "Pedidos", Icons.Default.ShoppingCart),
            BusinessTabConfig("products", "Productos", Icons.Default.Inventory),
            BusinessTabConfig("wallet", "Wallet", Icons.Default.AccountBalanceWallet),
            BusinessTabConfig("settings", "Ajustes", Icons.Default.Settings)
        )
    }

    /**
     * DEPRECATED: Mantener temporalmente para compatibilidad
     */
    @Deprecated("Ya no hay diferenciación por tipo - usar getTabs()")
    fun getTabsForBusiness(businessType: Any): List<BusinessTabConfig> {
        return getTabs()
    }

    /**
     * Obtiene las feature flags genéricas
     */
    fun getFeatures(): BusinessFeatures {
        return BusinessFeatures(
            usesProducts = true,
            usesChat = true,
            usesStatistics = true,
            usesWallet = true,
            usesSettings = true
        )
    }

    /**
     * DEPRECATED: Mantener temporalmente para compatibilidad
     */
    @Deprecated("Ya no hay diferenciación por tipo - usar getFeatures()")
    fun getFeaturesForBusiness(businessType: Any): BusinessFeatures {
        return getFeatures()
    }

    /**
     * Obtiene los labels genéricos
     */
    fun getLabels(): BusinessLabels {
        return BusinessLabels()
    }

    /**
     * DEPRECATED: Mantener temporalmente para compatibilidad
     */
    @Deprecated("Ya no hay diferenciación por tipo - usar getLabels()")
    fun getLabelsForBusiness(businessType: Any): BusinessLabels {
        return getLabels()
    }

    /**
     * Obtiene las categorías genéricas de productos
     * (combinación de todas las categorías posibles)
     */
    fun getCategories(): List<BusinessCategoryConfig> {
        return listOf(
            // Comida
            BusinessCategoryConfig("entradas", "Entradas"),
            BusinessCategoryConfig("platos_fuertes", "Platos Fuertes"),
            BusinessCategoryConfig("postres", "Postres"),
            // Dulces
            BusinessCategoryConfig("chocolates", "Chocolates"),
            BusinessCategoryConfig("gomitas", "Gomitas"),
            BusinessCategoryConfig("caramelos", "Caramelos"),
            BusinessCategoryConfig("galletas", "Galletas"),
            BusinessCategoryConfig("snacks", "Snacks"),
            // Supermercado
            BusinessCategoryConfig("frutas_verduras", "Frutas y Verduras"),
            BusinessCategoryConfig("carnes", "Carnes y Pescados"),
            BusinessCategoryConfig("lacteos", "Lácteos"),
            BusinessCategoryConfig("panaderia", "Panadería"),
            BusinessCategoryConfig("limpieza", "Limpieza"),
            BusinessCategoryConfig("despensa", "Despensa"),
            // Bebidas (común a todos)
            BusinessCategoryConfig("bebidas", "Bebidas"),
            // Especiales
            BusinessCategoryConfig("especiales", "Especiales"),
            // Otro
            BusinessCategoryConfig("otros", "Otros")
        )
    }

    /**
     * DEPRECATED: Mantener temporalmente para compatibilidad
     */
    @Deprecated("Ya no hay diferenciación por tipo - usar getCategories()")
    fun getCategoriesForBusiness(businessType: Any): List<BusinessCategoryConfig> {
        return getCategories()
    }
}


