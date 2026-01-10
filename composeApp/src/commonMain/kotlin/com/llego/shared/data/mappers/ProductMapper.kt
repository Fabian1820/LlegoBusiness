package com.llego.shared.data.mappers

import com.llego.shared.data.model.Product as GraphQLProduct
import com.llego.nichos.common.data.model.Product as LocalProduct
import com.llego.nichos.common.data.model.ProductUnit

/**
 * Mapea un producto de GraphQL al modelo de dominio local
 */
fun GraphQLProduct.toLocalProduct(): LocalProduct {
    return LocalProduct(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        imageUrl = this.image, // GraphQL usa 'image', local usa 'imageUrl'
        category = this.categoryId ?: "general", // Mapear categoryId a category string
        isAvailable = this.availability,

        // Campos del backend GraphQL
        branchId = this.branchId,
        currency = this.currency,
        weight = this.weight,
        categoryId = this.categoryId,

        // Parsear el weight para determinar unidad si es posible
        unit = parseProductUnit(this.weight),

        // Campos opcionales que GraphQL no provee (usar valores por defecto)
        brand = null,
        stock = null,
        preparationTime = null,
        variants = emptyList(),
        varieties = emptyList(),
        isVegetarian = false,
        isVegan = false,
        isGlutenFree = false,
        allergens = emptyList(),
        calories = null,
        sizes = null,
        colors = null,
        material = null,
        gender = null,
        requiresPrescription = false,
        genericName = null
    )
}

/**
 * Intenta parsear el weight para determinar la unidad del producto
 * Ejemplos: "1kg", "500g", "1L", "1ud"
 */
private fun parseProductUnit(weight: String): ProductUnit? {
    return when {
        weight.contains("kg", ignoreCase = true) -> ProductUnit.KG
        weight.contains("g", ignoreCase = true) -> ProductUnit.GRAM
        weight.contains("l", ignoreCase = true) -> ProductUnit.LITER
        weight.contains("ud", ignoreCase = true) -> ProductUnit.UNIT
        weight.contains("paq", ignoreCase = true) -> ProductUnit.PACK
        else -> null
    }
}

/**
 * Mapea una lista de productos GraphQL a productos locales
 */
fun List<GraphQLProduct>.toLocalProducts(): List<LocalProduct> {
    return this.map { it.toLocalProduct() }
}
