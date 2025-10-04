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
    SOUPS,          // Sopas
    SALADS,         // Ensaladas
    MAIN_COURSES,   // Platos Principales
    SIDES,          // Acompañamientos
    DESSERTS,       // Postres
    BEVERAGES,      // Bebidas
    ALCOHOLIC,      // Bebidas Alcohólicas
    KIDS_MENU,      // Menú Infantil
    SPECIALS        // Especiales del día
}

// Extension functions para UI
fun MenuCategory.getDisplayName(): String {
    return when (this) {
        MenuCategory.APPETIZERS -> "Entradas"
        MenuCategory.SOUPS -> "Sopas"
        MenuCategory.SALADS -> "Ensaladas"
        MenuCategory.MAIN_COURSES -> "Platos Principales"
        MenuCategory.SIDES -> "Acompañamientos"
        MenuCategory.DESSERTS -> "Postres"
        MenuCategory.BEVERAGES -> "Bebidas"
        MenuCategory.ALCOHOLIC -> "Bebidas Alcohólicas"
        MenuCategory.KIDS_MENU -> "Menú Infantil"
        MenuCategory.SPECIALS -> "Especiales del Día"
    }
}

fun MenuCategory.getIcon(): String {
    return when (this) {
        MenuCategory.APPETIZERS -> "🥗"
        MenuCategory.SOUPS -> "🍲"
        MenuCategory.SALADS -> "🥙"
        MenuCategory.MAIN_COURSES -> "🍽️"
        MenuCategory.SIDES -> "🍟"
        MenuCategory.DESSERTS -> "🍰"
        MenuCategory.BEVERAGES -> "🥤"
        MenuCategory.ALCOHOLIC -> "🍷"
        MenuCategory.KIDS_MENU -> "👶"
        MenuCategory.SPECIALS -> "⭐"
    }
}