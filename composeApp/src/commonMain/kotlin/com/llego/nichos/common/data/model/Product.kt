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

    // Tipo de producto (Individual o Varios)
    val productType: ProductType = ProductType.INDIVIDUAL,

    // Campos específicos por nicho (opcionales)
    val brand: String? = null,              // Para mercados
    val unit: ProductUnit? = null,          // Para mercados/agromercados
    val stock: Int? = null,                 // Para todos
    val preparationTime: Int? = null,       // Para restaurantes (minutos)

    // Variantes del producto (para agregos, guarniciones, etc.)
    val variants: List<ProductVariantGroup> = emptyList(),

    val varieties: List<String> = emptyList(), // Variedades/modificadores/agregos (ej: "Extra queso", "Con papas", etc.)

    // Campos específicos para restaurantes
    val isVegetarian: Boolean = false,
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val allergens: List<String> = emptyList(),
    val calories: Int? = null,

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

/**
 * Tipo de producto
 */
@Serializable
enum class ProductType {
    INDIVIDUAL,  // Producto individual con variantes
    MULTIPLE     // Varios productos en una imagen
}

/**
 * Grupo de variantes para un producto
 * Ejemplo: "Guarniciones" -> ["Papas fritas", "Ensalada", "Arroz"]
 */
@Serializable
data class ProductVariantGroup(
    val id: String,
    val name: String,                    // Nombre del grupo (ej: "Guarniciones", "Agregos", "Tamaño")
    val options: List<VariantOption>,    // Opciones disponibles
    val isRequired: Boolean = false,     // Si es obligatorio seleccionar una opción
    val allowMultiple: Boolean = true    // Si se pueden seleccionar múltiples opciones
)

/**
 * Opción individual dentro de un grupo de variantes
 */
@Serializable
data class VariantOption(
    val id: String,
    val name: String,           // Nombre de la opción (ej: "Extra queso")
    val priceModifier: Double = 0.0  // Modificador de precio (+/- precio)
)

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

fun ProductType.getDisplayName(): String {
    return when (this) {
        ProductType.INDIVIDUAL -> "Individual"
        ProductType.MULTIPLE -> "Varios"
    }
}

/**
 * Helper para determinar qué campos mostrar según el nicho
 */
fun Product.shouldShowSizes(businessType: BusinessType): Boolean {
    return false // Solo para CLOTHING_STORE que ya no existe
}

fun Product.shouldShowColors(businessType: BusinessType): Boolean {
    return false // Solo para CLOTHING_STORE que ya no existe
}

fun Product.shouldShowBrand(businessType: BusinessType): Boolean {
    return businessType == BusinessType.MARKET && brand != null
}

fun Product.shouldShowUnit(businessType: BusinessType): Boolean {
    return businessType == BusinessType.MARKET && unit != null
}

fun Product.shouldShowPreparationTime(businessType: BusinessType): Boolean {
    return businessType == BusinessType.RESTAURANT && preparationTime != null
}

/**
 * Convierte Product (genérico) a MenuItem para compatibilidad con MenuScreen
 * Mapea la categoría correctamente según el tipo de negocio
 */
fun Product.toMenuItem(businessType: com.llego.shared.data.model.BusinessType = com.llego.shared.data.model.BusinessType.RESTAURANT): com.llego.nichos.restaurant.data.model.MenuItem {
    val menuCategory = com.llego.nichos.common.utils.mapToMenuCategory(this.category, businessType)
    
    return com.llego.nichos.restaurant.data.model.MenuItem(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        category = menuCategory,
        imageUrl = this.imageUrl,
        isAvailable = this.isAvailable,
        preparationTime = this.preparationTime ?: 0, // Usa 0 si es null
        allergens = this.allergens,
        isVegetarian = this.isVegetarian,
        isVegan = this.isVegan,
        isGlutenFree = this.isGlutenFree,
        calories = this.calories
    )
}