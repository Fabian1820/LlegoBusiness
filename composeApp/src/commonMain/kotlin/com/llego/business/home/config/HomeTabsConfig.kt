package com.llego.business.home.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Configuracion de tabs para la navegacion inferior del home.
 */
data class HomeTabConfig(
    val id: String,
    val title: String,
    val icon: ImageVector
)

/**
 * Proveedor de tabs genericos (iguales para todos los negocios).
 */
object HomeTabsProvider {
    fun getTabs(): List<HomeTabConfig> {
        return listOf(
            HomeTabConfig("orders", "Pedidos", Icons.Default.ShoppingCart),
            HomeTabConfig("products", "Productos", Icons.Default.Inventory),
            HomeTabConfig("wallet", "Wallet", Icons.Default.AccountBalanceWallet),
            HomeTabConfig("settings", "Ajustes", Icons.Default.Settings)
        )
    }
}
