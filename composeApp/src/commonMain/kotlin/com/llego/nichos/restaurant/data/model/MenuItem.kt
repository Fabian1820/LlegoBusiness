package com.llego.nichos.restaurant.data.model

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