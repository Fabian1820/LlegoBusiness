package com.llego.business.products.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.ProductCategory
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.repositories.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductCategoriesUiState(
    val isLoading: Boolean = false,
    val categories: List<ProductCategory> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para gestionar productos usando GraphQL.
 */
class ProductViewModel(
    tokenManager: TokenManager
) : ViewModel() {

    private val repository = ProductRepository(tokenManager)

    private val _productsState = MutableStateFlow<ProductsResult>(ProductsResult.Loading)
    val productsState: StateFlow<ProductsResult> = _productsState.asStateFlow()
    private val _productCategoriesState = MutableStateFlow(ProductCategoriesUiState())
    val productCategoriesState: StateFlow<ProductCategoriesUiState> = _productCategoriesState.asStateFlow()
    private var lastLoadedBranchTipos: Set<BranchTipo> = emptySet()

    /**
     * Carga todos los productos o productos filtrados.
     * @param first Límite de productos a cargar (default: 100)
     */
    fun loadProducts(
        branchId: String? = null,
        categoryId: String? = null,
        availableOnly: Boolean = false,
        first: Int = 100
    ) {
        viewModelScope.launch {
            _productsState.value = ProductsResult.Loading
            _productsState.value = repository.getProducts(
                branchId = branchId,
                categoryId = categoryId,
                availableOnly = availableOnly,
                first = first
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

    fun loadProductCategories(branchTipos: Set<BranchTipo>, force: Boolean = false) {
        val normalizedTipos = branchTipos.toSet()
        if (!force &&
            normalizedTipos == lastLoadedBranchTipos &&
            _productCategoriesState.value.categories.isNotEmpty()
        ) {
            return
        }

        viewModelScope.launch {
            _productCategoriesState.value = _productCategoriesState.value.copy(
                isLoading = true,
                error = null
            )

            repository.getProductCategories(normalizedTipos)
                .onSuccess { categories ->
                    lastLoadedBranchTipos = normalizedTipos
                    _productCategoriesState.value = ProductCategoriesUiState(
                        isLoading = false,
                        categories = categories,
                        error = null
                    )
                }
                .onFailure { throwable ->
                    _productCategoriesState.value = _productCategoriesState.value.copy(
                        isLoading = false,
                        error = throwable.message ?: "No se pudieron cargar las categorías"
                    )
                }
        }
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
