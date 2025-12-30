package com.llego.nichos.common.utils

import com.llego.nichos.common.config.BusinessConfigProvider
import com.llego.nichos.restaurant.data.model.MenuCategory
import com.llego.nichos.restaurant.data.model.getDisplayName
import com.llego.shared.data.model.BusinessType

/**
 * Obtiene el nombre de categoría para mostrar en el UI
 * Basado en el businessType y la categoría del producto
 */
fun getCategoryDisplayName(
    category: String,
    menuCategory: MenuCategory?,
    businessType: BusinessType
): String {
    // Si es restaurante, usar el MenuCategory
    if (businessType == BusinessType.RESTAURANT && menuCategory != null) {
        return menuCategory.getDisplayName()
    }
    
    // Para otros nichos, buscar en BusinessConfig el nombre de display
    val categories = BusinessConfigProvider.getCategoriesForBusiness(businessType)
    val categoryId = mapToCategoryId(category, businessType)
    
    return categories.find { it.id == categoryId }?.displayName ?: category
}

/**
 * Obtiene el nombre de categoría desde un MenuItem
 */
fun getCategoryDisplayNameFromMenuItem(
    menuCategory: MenuCategory,
    businessType: BusinessType,
    originalCategory: String? = null
): String {
    // Si es restaurante, usar el MenuCategory directamente
    if (businessType == BusinessType.RESTAURANT) {
        return menuCategory.getDisplayName()
    }
    
    // Para otros nichos, intentar usar la categoría original si está disponible
    if (originalCategory != null) {
        val categories = BusinessConfigProvider.getCategoriesForBusiness(businessType)
        val categoryId = mapToCategoryId(originalCategory, businessType)
        return categories.find { it.id == categoryId }?.displayName ?: originalCategory
    }
    
    // Fallback: intentar mapear desde MenuCategory
    return when (menuCategory) {
        MenuCategory.BEVERAGES -> {
            val categories = BusinessConfigProvider.getCategoriesForBusiness(businessType)
            categories.find { it.id == "bebidas" }?.displayName ?: "Bebidas"
        }
        else -> {
            val categories = BusinessConfigProvider.getCategoriesForBusiness(businessType)
            categories.firstOrNull()?.displayName ?: "Producto"
        }
    }
}




