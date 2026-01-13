package com.llego.business.orders.data.model

import com.llego.shared.data.model.Product
import kotlinx.serialization.Serializable

/**
 * Modelo de Item del Menú para Restaurante
 */
@Serializable
data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: MenuCategory,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val preparationTime: Int = 15, // minutos
    val allergens: List<String> = emptyList(),
    val isVegetarian: Boolean = false,
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val calories: Int? = null,
    val customizations: List<String> = emptyList() // Ej: "Sin cebolla", "Extra queso"
)

@Serializable
enum class MenuCategory {
    APPETIZERS,     // Entradas
    MAIN_COURSES,   // Platos Principales
    DESSERTS,       // Postres
    SIDES,          // Agregos
    BEVERAGES,      // Bebidas
    SPECIALS        // Especiales del día
}

// Extension functions para UI
fun MenuCategory.getDisplayName(): String {
    return when (this) {
        MenuCategory.APPETIZERS -> "Entradas"
        MenuCategory.MAIN_COURSES -> "Principales"
        MenuCategory.DESSERTS -> "Postres"
        MenuCategory.SIDES -> "Agregos"
        MenuCategory.BEVERAGES -> "Bebidas"
        MenuCategory.SPECIALS -> "Especial/Día"
    }
}

fun MenuCategory.getIcon(): String {
    return when (this) {
        MenuCategory.APPETIZERS -> "🥗"
        MenuCategory.MAIN_COURSES -> "🍽️"
        MenuCategory.DESSERTS -> "🍰"
        MenuCategory.SIDES -> "🍟"
        MenuCategory.BEVERAGES -> "🥤"
        MenuCategory.SPECIALS -> "⭐"
    }
}

/**
 * Convierte Product (GraphQL) a MenuItem para mostrar en edicion de pedidos.
 */
fun Product.toMenuItem(): MenuItem {
    return MenuItem(
        id = id,
        name = name,
        description = description,
        price = price,
        category = mapCategoryIdToMenuCategory(categoryId),
        imageUrl = imageUrl ?: image,
        isAvailable = availability,
        preparationTime = 0
    )
}

private fun mapCategoryIdToMenuCategory(categoryId: String?): MenuCategory {
    val normalized = categoryId?.lowercase() ?: return MenuCategory.MAIN_COURSES
    return when {
        normalized.contains("entrada") -> MenuCategory.APPETIZERS
        normalized.contains("postre") -> MenuCategory.DESSERTS
        normalized.contains("bebida") -> MenuCategory.BEVERAGES
        normalized.contains("agrego") || normalized.contains("side") -> MenuCategory.SIDES
        normalized.contains("especial") -> MenuCategory.SPECIALS
        else -> MenuCategory.MAIN_COURSES
    }
}
