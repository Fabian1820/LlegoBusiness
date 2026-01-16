package com.llego.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Modelo de dominio para un producto
 * Alineado con el schema GraphQL ProductType
 * 
 * Campos con valores por defecto del backend:
 * - weight: String! (default: "" si no se especifica en CreateProductInput)
 * - currency: String! (default: "USD" si no se especifica en CreateProductInput)
 * - imageUrl: String! (siempre generado por el backend como presigned URL)
 */
@Serializable
data class Product(
    val id: String,
    val branchId: String,
    val name: String,
    val description: String,
    
    /**
     * Peso del producto. Siempre tiene valor (no nullable).
     * El backend asigna "" como default si no se especifica al crear.
     */
    val weight: String,
    
    val price: Double,
    
    /**
     * Moneda del producto. Siempre tiene valor (no nullable).
     * El backend asigna "USD" como default si no se especifica al crear.
     */
    val currency: String,
    
    val image: String,
    val availability: Boolean,
    val categoryId: String?,
    val createdAt: String,
    
    /**
     * URL presigned para acceder a la imagen del producto.
     * Siempre generado por el backend (no nullable).
     */
    val imageUrl: String,
    
    // Relaciones opcionales (solo cuando se solicitan explícitamente en queries GraphQL)
    
    /**
     * Categoría del producto. Solo disponible cuando se solicita explícitamente
     * mediante fragments de GraphQL.
     */
    val category: ProductCategory? = null,
    
    /**
     * Sucursal del producto. Solo disponible cuando se solicita explícitamente
     * mediante fragments de GraphQL.
     */
    val branch: Branch? = null,
    
    /**
     * Negocio del producto. Solo disponible cuando se solicita explícitamente
     * mediante fragments de GraphQL.
     */
    val business: Business? = null
)

/**
 * Modelo de categoría de producto
 * Alineado con el schema GraphQL ProductCategoryType
 */
@Serializable
data class ProductCategory(
    val id: String,
    val branchType: String,
    val name: String,
    val iconIos: String,
    val iconWeb: String,
    val iconAndroid: String
)

/**
 * Estado del resultado de productos
 */
sealed class ProductsResult {
    data class Success(val products: List<Product>) : ProductsResult()
    data class Error(val message: String) : ProductsResult()
    data object Loading : ProductsResult()
}
