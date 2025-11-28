package com.llego.nichos.common.data.model

import com.llego.shared.data.model.BusinessType
import kotlinx.serialization.Serializable

/**
 * Modelo genérico de Producto para todos los nichos
 * Se adapta según el tipo de negocio
 */
@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val isAvailable: Boolean = true,

    // Campos específicos por nicho (opcionales)
    val brand: String? = null,              // Para mercados
    val unit: ProductUnit? = null,          // Para mercados/agromercados
    val stock: Int? = null,                 // Para todos
    val preparationTime: Int? = null,       // Para restaurantes (minutos)

    // Campos específicos para ropa
    val sizes: List<String>? = null,        // ["XS", "S", "M", "L", "XL"]
    val colors: List<ProductColor>? = null, // Colores disponibles
    val material: String? = null,           // Material de la prenda
    val gender: ClothingGender? = null,     // Hombre, Mujer, Unisex

    // Campos específicos para farmacias
    val requiresPrescription: Boolean = false,
    val genericName: String? = null
)

@Serializable
enum class ProductUnit {
    UNIT,       // Unidad
    KG,         // Kilogramo
    LITER,      // Litro
    GRAM,       // Gramo
    PACK        // Paquete
}

@Serializable
data class ProductColor(
    val name: String,           // "Rojo", "Azul", etc.
    val hexCode: String,        // "#FF0000"
    val stock: Int = 0          // Stock por color
)

@Serializable
enum class ClothingGender {
    MEN,        // Hombre
    WOMEN,      // Mujer
    UNISEX,     // Unisex
    KIDS        // Niños
}

// Extension functions para UI
fun Product.getDisplayPrice(): String {
    val formattedPrice = price.formatPrice()
    return if (unit != null && unit != ProductUnit.UNIT) {
        "$$formattedPrice/${unit.getDisplayName()}"
    } else {
        "$$formattedPrice"
    }
}

// Helper para formatear precio (compatible con KMP)
private fun Double.formatPrice(): String {
    val rounded = (this * 100).toInt() / 100.0
    val intPart = rounded.toInt()
    val decimalPart = ((rounded - intPart) * 100).toInt()
    return "$intPart.${decimalPart.toString().padStart(2, '0')}"
}

fun ProductUnit.getDisplayName(): String {
    return when (this) {
        ProductUnit.UNIT -> "ud"
        ProductUnit.KG -> "kg"
        ProductUnit.LITER -> "L"
        ProductUnit.GRAM -> "g"
        ProductUnit.PACK -> "paq"
    }
}

fun ClothingGender.getDisplayName(): String {
    return when (this) {
        ClothingGender.MEN -> "Hombre"
        ClothingGender.WOMEN -> "Mujer"
        ClothingGender.UNISEX -> "Unisex"
        ClothingGender.KIDS -> "Niños"
    }
}

/**
 * Helper para determinar qué campos mostrar según el nicho
 */
fun Product.shouldShowSizes(businessType: BusinessType): Boolean {
    return businessType == BusinessType.CLOTHING_STORE && !sizes.isNullOrEmpty()
}

fun Product.shouldShowColors(businessType: BusinessType): Boolean {
    return businessType == BusinessType.CLOTHING_STORE && !colors.isNullOrEmpty()
}

fun Product.shouldShowBrand(businessType: BusinessType): Boolean {
    return businessType in listOf(
        BusinessType.MARKET,
        BusinessType.AGROMARKET,
        BusinessType.PHARMACY
    ) && brand != null
}

fun Product.shouldShowUnit(businessType: BusinessType): Boolean {
    return businessType in listOf(
        BusinessType.MARKET,
        BusinessType.AGROMARKET
    ) && unit != null
}

fun Product.shouldShowPreparationTime(businessType: BusinessType): Boolean {
    return businessType == BusinessType.RESTAURANT && preparationTime != null
}
