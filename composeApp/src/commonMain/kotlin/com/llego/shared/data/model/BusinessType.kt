package com.llego.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Tipos de negocios (nichos) soportados por Llego
 */
@Serializable
enum class BusinessType {
    RESTAURANT,      // Restaurante
    MARKET,          // Mercado/Supermercado
    AGROMARKET,      // Agromercado
    CLOTHING_STORE,  // Tienda de Ropa
    PHARMACY         // Farmacia
}

/**
 * Mapeo de BusinessType a string legible
 */
fun BusinessType.toDisplayName(): String = when (this) {
    BusinessType.RESTAURANT -> "Restaurante"
    BusinessType.MARKET -> "Supermercado"
    BusinessType.AGROMARKET -> "Agromercado"
    BusinessType.CLOTHING_STORE -> "Tienda de Ropa"
    BusinessType.PHARMACY -> "Farmacia"
}

/**
 * Mapeo de BusinessType a tipo de backend (string usado en GraphQL)
 */
fun BusinessType.toBackendType(): String = when (this) {
    BusinessType.RESTAURANT -> "restaurant"
    BusinessType.MARKET -> "market"
    BusinessType.AGROMARKET -> "agromarket"
    BusinessType.CLOTHING_STORE -> "clothing"
    BusinessType.PHARMACY -> "pharmacy"
}

/**
 * Parsea string del backend a BusinessType
 */
fun String.toBusinessType(): BusinessType? = when (this.lowercase()) {
    "restaurant" -> BusinessType.RESTAURANT
    "market", "grocery" -> BusinessType.MARKET
    "agromarket" -> BusinessType.AGROMARKET
    "clothing", "clothing_store" -> BusinessType.CLOTHING_STORE
    "pharmacy" -> BusinessType.PHARMACY
    else -> null
}
