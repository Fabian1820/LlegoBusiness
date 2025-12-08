package com.llego.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Modelo de dominio para un producto
 */
@Serializable
data class Product(
    val id: String,
    val branchId: String,
    val name: String,
    val description: String,
    val weight: String,
    val price: Double,
    val currency: String,
    val image: String,
    val availability: Boolean,
    val categoryId: String?,
    val createdAt: String
)

/**
 * Estado del resultado de productos
 */
sealed class ProductsResult {
    data class Success(val products: List<Product>) : ProductsResult()
    data class Error(val message: String) : ProductsResult()
    data object Loading : ProductsResult()
}
