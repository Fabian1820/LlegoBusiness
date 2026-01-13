package com.llego.business.products.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.repositories.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar productos usando GraphQL.
 */
class ProductViewModel(
    tokenManager: TokenManager
) : ViewModel() {

    private val repository = ProductRepository(tokenManager)

    private val _productsState = MutableStateFlow<ProductsResult>(ProductsResult.Loading)
    val productsState: StateFlow<ProductsResult> = _productsState.asStateFlow()

    /**
     * Carga todos los productos o productos filtrados.
     */
    fun loadProducts(
        branchId: String? = null,
        categoryId: String? = null,
        availableOnly: Boolean = false
    ) {
        viewModelScope.launch {
            _productsState.value = ProductsResult.Loading
            _productsState.value = repository.getProducts(
                branchId = branchId,
                categoryId = categoryId,
                availableOnly = availableOnly
            )
        }
    }

    /**
     * Carga productos por IDs especificos.
     */
    fun loadProductsByIds(ids: List<String>) {
        viewModelScope.launch {
            _productsState.value = ProductsResult.Loading
            _productsState.value = repository.getProductsByIds(ids)
        }
    }

    /**
     * Recarga los productos.
     */
    fun refresh() {
        loadProducts()
    }

    // ============= CRUD OPERATIONS =============

    /**
     * Crea un producto usando un path de imagen ya subido.
     */
    suspend fun createProductWithImagePath(
        name: String,
        description: String,
        price: Double,
        imagePath: String,
        branchId: String? = null,
        businessId: String? = null,
        currency: String = "USD",
        weight: String? = null,
        categoryId: String? = null
    ): ProductsResult {
        _productsState.value = ProductsResult.Loading
        val result = repository.createProduct(
            name = name,
            description = description,
            price = price,
            image = imagePath,
            branchId = branchId,
            businessId = businessId,
            currency = currency,
            weight = weight,
            categoryId = categoryId
        )
        _productsState.value = result
        return result
    }

    /**
     * Actualiza un producto usando un path de imagen ya subido.
     */
    suspend fun updateProductWithImagePath(
        productId: String,
        name: String? = null,
        description: String? = null,
        price: Double? = null,
        currency: String? = null,
        weight: String? = null,
        availability: Boolean? = null,
        categoryId: String? = null,
        imagePath: String? = null
    ): ProductsResult {
        _productsState.value = ProductsResult.Loading
        val result = repository.updateProduct(
            productId = productId,
            name = name,
            description = description,
            price = price,
            image = imagePath,
            currency = currency,
            weight = weight,
            availability = availability,
            categoryId = categoryId
        )
        _productsState.value = result
        return result
    }

    /**
     * Elimina un producto y devuelve el resultado de la operacion.
     */
    suspend fun deleteProductBlocking(productId: String): ProductsResult {
        _productsState.value = ProductsResult.Loading
        val result = repository.deleteProduct(productId)
        _productsState.value = result
        return result
    }
}
