package com.llego.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Modelo de dominio para un combo
 */
@Serializable
data class Combo(
    val id: String,
    val branchId: String,
    val name: String,
    val description: String,
    val image: String? = null,
    val basePrice: Double,
    val finalPrice: Double,
    val savings: Double,
    val discountType: DiscountType,
    val discountValue: Double,
    val availability: Boolean,
    val slots: List<ComboSlot>,
    val representativeProducts: List<RepresentativeProduct> = emptyList(),
    val createdAt: String,
    val imageUrl: String? = null
)

/**
 * Slot personalizado de un combo
 */
@Serializable
data class ComboSlot(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val minSelections: Int = 1,
    val maxSelections: Int = 1,
    val isRequired: Boolean = true,
    val displayOrder: Int = 0,
    val options: List<ComboOption>
)

/**
 * Opción dentro de un slot
 */
@Serializable
data class ComboOption(
    val productId: String,
    val product: Product? = null,
    val isDefault: Boolean = false,
    val priceAdjustment: Double = 0.0,
    val availableModifiers: List<ComboModifier> = emptyList()
)

/**
 * Modificador de una opción
 */
@Serializable
data class ComboModifier(
    val name: String,
    val priceAdjustment: Double
)

/**
 * Producto representativo para mostrar en la vista simplificada
 */
@Serializable
data class RepresentativeProduct(
    val id: String,
    val name: String,
    val imageUrl: String
)

/**
 * Tipo de descuento del combo
 */
@Serializable
enum class DiscountType {
    NONE,
    PERCENTAGE,
    FIXED
}

/**
 * Estado del resultado de combos
 */
sealed class CombosResult {
    data class Success(val combos: List<Combo>) : CombosResult()
    data class Error(val message: String) : CombosResult()
    data object Loading : CombosResult()
}
