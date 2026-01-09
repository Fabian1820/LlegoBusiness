package com.llego.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Tipos de negocios (nichos) soportados por Llego
 */
@Serializable
enum class BusinessType {
    RESTAURANT,      // Restaurante
    MARKET,          // Tienda/Supermercado
    CANDY_STORE      // Dulcería
}

/**
 * Mapeo de BusinessType a string legible
 */
fun BusinessType.toDisplayName(): String = when (this) {
    BusinessType.RESTAURANT -> "Restaurante"
    BusinessType.MARKET -> "Tienda"
    BusinessType.CANDY_STORE -> "Dulcería"
}

/**
 * Mapeo de BusinessType a tipo de backend (string usado en GraphQL)
 */
fun BusinessType.toBackendType(): String = when (this) {
    BusinessType.RESTAURANT -> "restaurant"
    BusinessType.MARKET -> "market"
    BusinessType.CANDY_STORE -> "candy_store"
}

/**
 * Parsea string del backend a BusinessType
 */
fun String.toBusinessType(): BusinessType? = when (this.lowercase()) {
    "restaurant" -> BusinessType.RESTAURANT
    "market", "grocery" -> BusinessType.MARKET
    "candy_store", "candy" -> BusinessType.CANDY_STORE
    else -> null
}
