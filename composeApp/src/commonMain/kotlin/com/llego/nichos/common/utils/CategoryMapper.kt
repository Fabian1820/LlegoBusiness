package com.llego.nichos.common.utils

import com.llego.nichos.common.config.BusinessConfigProvider
import com.llego.nichos.restaurant.data.model.MenuCategory
import com.llego.shared.data.model.BusinessType

/**
 * Mapper para convertir MenuCategory a category ID genérico
 */
fun MenuCategory.toCategoryId(): String {
    return when (this) {
        MenuCategory.APPETIZERS -> "entradas"
        MenuCategory.MAIN_COURSES -> "platos_fuertes"
        MenuCategory.DESSERTS -> "postres"
        MenuCategory.SIDES -> "agregos"
        MenuCategory.BEVERAGES -> "bebidas"
        MenuCategory.SPECIALS -> "especiales"
    }
}

/**
 * Mapea categoría de Product (String) a MenuCategory
 * Función de extensión para facilitar el uso
 */
fun mapToMenuCategory(category: String, businessType: BusinessType): MenuCategory {
    val categoryLower = category.lowercase()

    if (businessType != BusinessType.RESTAURANT) {
        // Para otros nichos, intentar mapear a una categoría de restaurante equivalente
        // o usar MAIN_COURSES como default
        return when {
            categoryLower.contains("bebida") -> MenuCategory.BEVERAGES
            else -> MenuCategory.MAIN_COURSES
        }
    }

    // Para restaurante, mapear directamente
    return when {
        categoryLower.contains("entrada") -> MenuCategory.APPETIZERS
        categoryLower.contains("principal") || categoryLower.contains("plato fuerte") -> MenuCategory.MAIN_COURSES
        categoryLower.contains("postre") -> MenuCategory.DESSERTS
        categoryLower.contains("agrego") || categoryLower.contains("acompañamiento") -> MenuCategory.SIDES
        categoryLower.contains("bebida") -> MenuCategory.BEVERAGES
        categoryLower.contains("especial") -> MenuCategory.SPECIALS
        else -> MenuCategory.MAIN_COURSES // Default
    }
}


/**
 * Mapea la categoría de Product a un ID de categoría según BusinessConfig
 * Esto permite que los filtros funcionen correctamente para todos los nichos
 */
fun mapToCategoryId(category: String, businessType: BusinessType): String? {
    val categories = BusinessConfigProvider.getCategoriesForBusiness(businessType)
    
    // Buscar coincidencia exacta o parcial
    val categoryLower = category.lowercase()
    
    return when (businessType) {
        BusinessType.MARKET -> when {
            categoryLower.contains("fruta") || categoryLower.contains("verdura") -> "frutas_verduras"
            categoryLower.contains("carne") || categoryLower.contains("pescado") -> "carnes"
            categoryLower.contains("lácteo") || categoryLower.contains("lacteo") || categoryLower.contains("queso") || categoryLower.contains("leche") -> "lacteos"
            categoryLower.contains("pan") || categoryLower.contains("panadería") || categoryLower.contains("panaderia") -> "panaderia"
            categoryLower.contains("bebida") -> "bebidas"
            categoryLower.contains("limpieza") -> "limpieza"
            categoryLower.contains("despensa") || categoryLower.contains("arroz") || categoryLower.contains("frijol") -> "despensa"
            else -> null
        }
        BusinessType.CANDY_STORE -> when {
            categoryLower.contains("chocolate") -> "chocolates"
            categoryLower.contains("gomita") -> "gomitas"
            categoryLower.contains("caramelo") -> "caramelos"
            categoryLower.contains("galleta") -> "galletas"
            categoryLower.contains("snack") -> "snacks"
            categoryLower.contains("bebida") -> "bebidas"
            else -> null
        }
        BusinessType.RESTAURANT -> when {
            categoryLower.contains("entrada") -> "entradas"
            categoryLower.contains("principal") || categoryLower.contains("plato fuerte") -> "platos_fuertes"
            categoryLower.contains("postre") -> "postres"
            categoryLower.contains("agrego") || categoryLower.contains("acompañamiento") -> "agregos"
            categoryLower.contains("bebida") -> "bebidas"
            categoryLower.contains("especial") -> "especiales"
            else -> null
        }
    }
}

