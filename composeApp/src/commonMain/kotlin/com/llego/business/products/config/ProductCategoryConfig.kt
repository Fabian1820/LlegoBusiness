package com.llego.business.products.config

/**
 * Configuracion de categorias para filtrado de productos.
 */
data class ProductCategoryConfig(
    val id: String,
    val displayName: String
)

/**
 * Proveedor de categorias genericas de producto.
 */
object ProductCategoryProvider {
    fun getCategories(): List<ProductCategoryConfig> {
        return listOf(
            // Comida
            ProductCategoryConfig("entradas", "Entradas"),
            ProductCategoryConfig("platos_fuertes", "Platos Fuertes"),
            ProductCategoryConfig("postres", "Postres"),
            // Dulces
            ProductCategoryConfig("chocolates", "Chocolates"),
            ProductCategoryConfig("gomitas", "Gomitas"),
            ProductCategoryConfig("caramelos", "Caramelos"),
            ProductCategoryConfig("galletas", "Galletas"),
            ProductCategoryConfig("snacks", "Snacks"),
            // Supermercado
            ProductCategoryConfig("frutas_verduras", "Frutas y Verduras"),
            ProductCategoryConfig("carnes", "Carnes y Pescados"),
            ProductCategoryConfig("lacteos", "Lacteos"),
            ProductCategoryConfig("panaderia", "Panaderia"),
            ProductCategoryConfig("limpieza", "Limpieza"),
            ProductCategoryConfig("despensa", "Despensa"),
            // Bebidas (comun a todos)
            ProductCategoryConfig("bebidas", "Bebidas"),
            // Especiales
            ProductCategoryConfig("especiales", "Especiales"),
            // Otro
            ProductCategoryConfig("otros", "Otros")
        )
    }

    fun getCategoryDisplayName(categoryId: String?): String {
        if (categoryId.isNullOrBlank()) return "Sin categoria"
        return getCategories().firstOrNull { it.id == categoryId }?.displayName ?: categoryId
    }
}
