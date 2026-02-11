package com.llego.business.products.config

import com.llego.shared.data.model.BranchTipo

/**
 * Configuracion de categorias para filtrado de productos.
 */
data class ProductCategoryConfig(
    val id: String,
    val displayName: String,
    val supportedTipos: Set<BranchTipo>
)

/**
 * Proveedor de categorias genericas de producto.
 */
object ProductCategoryProvider {
    private val allCategories = listOf(
        // Restaurante
        ProductCategoryConfig(
            id = "entradas",
            displayName = "Entradas",
            supportedTipos = setOf(BranchTipo.RESTAURANTE)
        ),
        ProductCategoryConfig(
            id = "platos_fuertes",
            displayName = "Platos Fuertes",
            supportedTipos = setOf(BranchTipo.RESTAURANTE)
        ),
        // Compartidas: restaurante / dulceria / cafe
        ProductCategoryConfig(
            id = "postres",
            displayName = "Postres",
            supportedTipos = setOf(BranchTipo.RESTAURANTE, BranchTipo.DULCERIA, BranchTipo.CAFE)
        ),
        // Dulceria
        ProductCategoryConfig(
            id = "chocolates",
            displayName = "Chocolates",
            supportedTipos = setOf(BranchTipo.DULCERIA)
        ),
        ProductCategoryConfig(
            id = "gomitas",
            displayName = "Gomitas",
            supportedTipos = setOf(BranchTipo.DULCERIA)
        ),
        ProductCategoryConfig(
            id = "caramelos",
            displayName = "Caramelos",
            supportedTipos = setOf(BranchTipo.DULCERIA)
        ),
        ProductCategoryConfig(
            id = "galletas",
            displayName = "Galletas",
            supportedTipos = setOf(BranchTipo.DULCERIA)
        ),
        // Tienda / dulceria / cafe
        ProductCategoryConfig(
            id = "snacks",
            displayName = "Snacks",
            supportedTipos = setOf(BranchTipo.TIENDA, BranchTipo.DULCERIA, BranchTipo.CAFE)
        ),
        // Tienda
        ProductCategoryConfig(
            id = "frutas_verduras",
            displayName = "Frutas y Verduras",
            supportedTipos = setOf(BranchTipo.TIENDA)
        ),
        ProductCategoryConfig(
            id = "carnes",
            displayName = "Carnes y Pescados",
            supportedTipos = setOf(BranchTipo.TIENDA)
        ),
        ProductCategoryConfig(
            id = "lacteos",
            displayName = "Lacteos",
            supportedTipos = setOf(BranchTipo.TIENDA)
        ),
        ProductCategoryConfig(
            id = "panaderia",
            displayName = "Panaderia",
            supportedTipos = setOf(BranchTipo.TIENDA, BranchTipo.CAFE)
        ),
        ProductCategoryConfig(
            id = "limpieza",
            displayName = "Limpieza",
            supportedTipos = setOf(BranchTipo.TIENDA)
        ),
        ProductCategoryConfig(
            id = "despensa",
            displayName = "Despensa",
            supportedTipos = setOf(BranchTipo.TIENDA)
        ),
        // Comunes
        ProductCategoryConfig(
            id = "bebidas",
            displayName = "Bebidas",
            supportedTipos = setOf(
                BranchTipo.RESTAURANTE,
                BranchTipo.DULCERIA,
                BranchTipo.TIENDA,
                BranchTipo.CAFE
            )
        ),
        ProductCategoryConfig(
            id = "especiales",
            displayName = "Especiales",
            supportedTipos = setOf(
                BranchTipo.RESTAURANTE,
                BranchTipo.DULCERIA,
                BranchTipo.TIENDA,
                BranchTipo.CAFE
            )
        ),
        ProductCategoryConfig(
            id = "otros",
            displayName = "Otros",
            supportedTipos = setOf(
                BranchTipo.RESTAURANTE,
                BranchTipo.DULCERIA,
                BranchTipo.TIENDA,
                BranchTipo.CAFE
            )
        )
    )

    fun getCategories(branchTipos: Set<BranchTipo> = emptySet()): List<ProductCategoryConfig> {
        if (branchTipos.isEmpty()) return allCategories
        return allCategories.filter { category ->
            category.supportedTipos.any { it in branchTipos }
        }
    }

    fun getCategories(): List<ProductCategoryConfig> {
        return allCategories
    }

    fun getCategories(branchTipos: List<BranchTipo>): List<ProductCategoryConfig> {
        return getCategories(branchTipos.toSet())
    }

    fun getCategoryDisplayName(categoryId: String?): String {
        if (categoryId.isNullOrBlank()) return "Sin categoria"
        return allCategories.firstOrNull { it.id == categoryId }?.displayName ?: categoryId
    }
}
