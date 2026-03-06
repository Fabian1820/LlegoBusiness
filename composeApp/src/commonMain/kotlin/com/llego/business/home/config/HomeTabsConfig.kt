package com.llego.business.home.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import llegobusiness.composeapp.generated.resources.Res
import llegobusiness.composeapp.generated.resources.nav_orders
import llegobusiness.composeapp.generated.resources.nav_products
import org.jetbrains.compose.resources.DrawableResource

/**
 * Configuracion de tabs para la navegacion inferior del home.
 */
data class HomeTabConfig(
    val id: String,
    val title: String,
    val icon: HomeTabIcon
)

sealed interface HomeTabIcon {
    data class Vector(val value: ImageVector) : HomeTabIcon
    data class Drawable(val value: DrawableResource) : HomeTabIcon
}

/**
 * Proveedor de tabs genericos (iguales para todos los negocios).
 */
object HomeTabsProvider {
    fun getTabs(): List<HomeTabConfig> {
        return listOf(
            HomeTabConfig("orders", "Pedidos", HomeTabIcon.Drawable(Res.drawable.nav_orders)),
            HomeTabConfig("products", "Productos", HomeTabIcon.Drawable(Res.drawable.nav_products)),
            HomeTabConfig("statistics", "Estadisticas", HomeTabIcon.Vector(Icons.Default.BarChart)),
            HomeTabConfig("settings", "Ajustes", HomeTabIcon.Vector(Icons.Default.Settings))
        )
    }
}
