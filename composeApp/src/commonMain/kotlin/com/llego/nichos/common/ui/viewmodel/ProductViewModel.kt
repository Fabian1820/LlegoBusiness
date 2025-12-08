package com.llego.nichos.common.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.repositories.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar productos usando GraphQL
 */
class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _productsState = MutableStateFlow<ProductsResult>(ProductsResult.Loading)
    val productsState: StateFlow<ProductsResult> = _productsState.asStateFlow()

    /**
     * Carga todos los productos
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
     * Carga productos por IDs específicos
     */
    fun loadProductsByIds(ids: List<String>) {
        viewModelScope.launch {
            _productsState.value = ProductsResult.Loading
            _productsState.value = repository.getProductsByIds(ids)
        }
    }

    /**
     * Recarga los productos
     */
    fun refresh() {
        loadProducts()
    }
}
