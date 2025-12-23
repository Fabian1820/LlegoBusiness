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
                BusinessTabConfig("settings", "Configuración", Icons.Default.Settings)
                // BusinessTabConfig("tutorials", "Tutoriales", Icons.Default.School) // MVP: oculto
            )

            BusinessType.MARKET -> listOf(
                BusinessTabConfig("orders", "Pedidos", Icons.Default.ShoppingCart),
                BusinessTabConfig("products", "Productos", Icons.Default.Inventory),
                BusinessTabConfig("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
                // BusinessTabConfig("tutorials", "Tutoriales", Icons.Default.School) // MVP: oculto
            )

            BusinessType.AGROMARKET -> listOf(
                BusinessTabConfig("orders", "Pedidos", Icons.Default.ShoppingCart),
                BusinessTabConfig("products", "Productos", Icons.Default.Grass),
                BusinessTabConfig("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
                // BusinessTabConfig("tutorials", "Tutoriales", Icons.Default.School) // MVP: oculto
            )

            BusinessType.CLOTHING_STORE -> listOf(
                BusinessTabConfig("orders", "Pedidos", Icons.Default.ShoppingCart),
                BusinessTabConfig("stock", "Stock", Icons.Default.Checkroom),
                BusinessTabConfig("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
                // BusinessTabConfig("tutorials", "Tutoriales", Icons.Default.School) // MVP: oculto
            )

            BusinessType.PHARMACY -> listOf(
                BusinessTabConfig("orders", "Pedidos", Icons.Default.ShoppingCart),
                BusinessTabConfig("medicines", "Medicinas", Icons.Default.Medication),
                BusinessTabConfig("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
                // BusinessTabConfig("tutorials", "Tutoriales", Icons.Default.School) // MVP: oculto
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
            BusinessType.AGROMARKET -> "products"
            BusinessType.CLOTHING_STORE -> "stock"
            BusinessType.PHARMACY -> "medicines"
        }
    }

    /**
     * Verifica si el nicho usa productos (Market, Agromarket, Clothing)
     */
    fun usesProducts(businessType: BusinessType): Boolean {
        return businessType in listOf(
            BusinessType.MARKET,
            BusinessType.AGROMARKET,
            BusinessType.CLOTHING_STORE
        )
    }

    /**
     * Verifica si el nicho usa menú (Restaurant)
     */
    fun usesMenu(businessType: BusinessType): Boolean {
        return businessType == BusinessType.RESTAURANT
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

            BusinessType.AGROMARKET -> listOf(
                BusinessCategoryConfig("frutas", "Frutas Frescas"),
                BusinessCategoryConfig("verduras", "Verduras"),
                BusinessCategoryConfig("hortalizas", "Hortalizas"),
                BusinessCategoryConfig("tuberculos", "Tubérculos"),
                BusinessCategoryConfig("granos", "Granos y Cereales"),
                BusinessCategoryConfig("organicos", "Orgánicos")
            )

            BusinessType.CLOTHING_STORE -> listOf(
                BusinessCategoryConfig("hombres", "Hombres"),
                BusinessCategoryConfig("mujeres", "Mujeres"),
                BusinessCategoryConfig("ninos", "Niños"),
                BusinessCategoryConfig("accesorios", "Accesorios"),
                BusinessCategoryConfig("calzado", "Calzado"),
                BusinessCategoryConfig("deportivo", "Deportivo")
            )

            BusinessType.PHARMACY -> listOf(
                BusinessCategoryConfig("medicamentos", "Medicamentos"),
                BusinessCategoryConfig("vitaminas", "Vitaminas"),
                BusinessCategoryConfig("cuidado_personal", "Cuidado Personal"),
                BusinessCategoryConfig("primeros_auxilios", "Primeros Auxilios"),
                BusinessCategoryConfig("bebes", "Bebés"),
                BusinessCategoryConfig("dermocosmetica", "Dermocosmética")
            )
        }
    }
}


