package com.llego.business.products.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.Product
import com.llego.shared.data.model.ProductCategory
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.model.VariantList
import com.llego.shared.data.model.VariantOptionDraft
import com.llego.shared.data.repositories.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ProductCategoriesUiState(
    val isLoading: Boolean = false,
    val categories: List<ProductCategory> = emptyList(),
    val error: String? = null
)

data class VariantListsUiState(
    val isLoading: Boolean = false,
    val variantLists: List<VariantList> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para gestionar productos usando GraphQL.
 */
class ProductViewModel(
    tokenManager: TokenManager
) : ViewModel() {
    companion object {
        const val DEFAULT_PRODUCTS_TYPE_FILTER = "ALL"
        const val DEFAULT_PRODUCTS_PAGE_SIZE = 20
    }

    private data class ProductsQuery(
        val branchId: String?,
        val categoryId: String?,
        val availableOnly: Boolean,
        val first: Int
    )

    private data class ProductsFullQuery(
        val branchId: String?,
        val categoryId: String?,
        val availableOnly: Boolean
    )

    private val repository = ProductRepository(tokenManager)

    private val _productsState = MutableStateFlow<ProductsResult>(ProductsResult.Loading)
    val productsState: StateFlow<ProductsResult> = _productsState.asStateFlow()
    private val _productCategoriesState = MutableStateFlow(ProductCategoriesUiState())
    val productCategoriesState: StateFlow<ProductCategoriesUiState> = _productCategoriesState.asStateFlow()
    private val _variantListsState = MutableStateFlow(VariantListsUiState())
    val variantListsState: StateFlow<VariantListsUiState> = _variantListsState.asStateFlow()
    private val _isLoadingMoreProducts = MutableStateFlow(false)
    val isLoadingMoreProducts: StateFlow<Boolean> = _isLoadingMoreProducts.asStateFlow()
    private val _loadMoreProductsError = MutableStateFlow<String?>(null)
    val loadMoreProductsError: StateFlow<String?> = _loadMoreProductsError.asStateFlow()
    private val _selectedProductsCategoryId = MutableStateFlow<String?>(null)
    val selectedProductsCategoryId: StateFlow<String?> = _selectedProductsCategoryId.asStateFlow()
    private val _selectedProductsTypeFilter = MutableStateFlow(DEFAULT_PRODUCTS_TYPE_FILTER)
    val selectedProductsTypeFilter: StateFlow<String> = _selectedProductsTypeFilter.asStateFlow()
    private var lastLoadedProductsQuery: ProductsQuery? = null
    private var lastFailedLoadMoreCursor: String? = null
    private val fullyLoadedProductsQueries = mutableSetOf<ProductsFullQuery>()
    private var fullLoadJob: Job? = null
    private var fullLoadQueryInProgress: ProductsFullQuery? = null
    private var lastLoadedBranchTipos: Set<BranchTipo> = emptySet()
    private var lastLoadedBranchIdForCategories: String? = null
    private var lastLoadedBranchIdForVariantLists: String? = null

    /**
     * Carga todos los productos o productos filtrados.
     * @param first Límite de productos a cargar (default: 100)
     */
    fun loadProducts(
        branchId: String? = null,
        categoryId: String? = null,
        availableOnly: Boolean = false,
        first: Int = 100,
        force: Boolean = false
    ) {
        val fullQuery = ProductsFullQuery(
            branchId = branchId,
            categoryId = categoryId,
            availableOnly = availableOnly
        )
        val query = ProductsQuery(
            branchId = branchId,
            categoryId = categoryId,
            availableOnly = availableOnly,
            first = first
        )

        if (!force &&
            query == lastLoadedProductsQuery &&
            _productsState.value is ProductsResult.Success
        ) {
            return
        }

        viewModelScope.launch {
            fullLoadJob?.cancel()
            fullLoadQueryInProgress = null
            _isLoadingMoreProducts.value = false
            _loadMoreProductsError.value = null
            lastFailedLoadMoreCursor = null
            fullyLoadedProductsQueries.remove(fullQuery)
            val shouldShowLoading = force || _productsState.value !is ProductsResult.Success
            if (shouldShowLoading) {
                _productsState.value = ProductsResult.Loading
            }

            val result = repository.getProducts(
                branchId = branchId,
                categoryId = categoryId,
                availableOnly = availableOnly,
                first = first,
                after = null
            )
            _productsState.value = result
            if (result is ProductsResult.Success) {
                lastLoadedProductsQuery = query
                if (!result.hasNextPage) {
                    fullyLoadedProductsQueries += fullQuery
                }
            }
        }
    }

    fun ensureProductsLoaded(
        branchId: String? = null,
        categoryId: String? = null,
        availableOnly: Boolean = false,
        first: Int = 100
    ) {
        loadProducts(
            branchId = branchId,
            categoryId = categoryId,
            availableOnly = availableOnly,
            first = first,
            force = false
        )
    }

    fun loadMoreProducts(force: Boolean = false) {
        if (_isLoadingMoreProducts.value) return

        val currentState = _productsState.value as? ProductsResult.Success ?: return
        if (!currentState.hasNextPage || currentState.endCursor.isNullOrBlank()) return
        if (!force && currentState.endCursor == lastFailedLoadMoreCursor) return

        val query = lastLoadedProductsQuery ?: return

        viewModelScope.launch {
            _isLoadingMoreProducts.value = true
            _loadMoreProductsError.value = null
            try {
                when (
                    val result = repository.getProducts(
                        branchId = query.branchId,
                        categoryId = query.categoryId,
                        availableOnly = query.availableOnly,
                        first = query.first,
                        after = currentState.endCursor
                    )
                ) {
                    is ProductsResult.Success -> {
                        val mergedProducts = (currentState.products + result.products)
                            .distinctBy { it.id }
                        val mergedState = ProductsResult.Success(
                            products = mergedProducts,
                            hasNextPage = result.hasNextPage,
                            endCursor = result.endCursor
                        )
                        _productsState.value = mergedState
                        lastFailedLoadMoreCursor = null
                        if (!mergedState.hasNextPage) {
                            fullyLoadedProductsQueries += ProductsFullQuery(
                                branchId = query.branchId,
                                categoryId = query.categoryId,
                                availableOnly = query.availableOnly
                            )
                        }
                    }

                    is ProductsResult.Error -> {
                        // Keep current page and cursor if "load more" fails.
                        _productsState.value = currentState
                        lastFailedLoadMoreCursor = currentState.endCursor
                        _loadMoreProductsError.value = result.message
                    }

                    is ProductsResult.Loading -> {
                        _productsState.value = currentState
                        lastFailedLoadMoreCursor = currentState.endCursor
                        _loadMoreProductsError.value = "No se pudo cargar más productos"
                    }
                }
            } finally {
                _isLoadingMoreProducts.value = false
            }
        }
    }

    fun invalidateProductsCache() {
        lastLoadedProductsQuery = null
        fullyLoadedProductsQueries.clear()
        _isLoadingMoreProducts.value = false
        _loadMoreProductsError.value = null
        lastFailedLoadMoreCursor = null
        _productsState.value = ProductsResult.Loading
    }

    fun setSelectedProductsCategoryId(categoryId: String?) {
        _selectedProductsCategoryId.value = categoryId
    }

    fun setSelectedProductsTypeFilter(typeFilter: String) {
        _selectedProductsTypeFilter.value = typeFilter.ifBlank { DEFAULT_PRODUCTS_TYPE_FILTER }
    }

    /**
     * Carga productos por IDs especificos.
     */
    fun loadProductsByIds(ids: List<String>) {
        viewModelScope.launch {
            _isLoadingMoreProducts.value = false
            _loadMoreProductsError.value = null
            lastFailedLoadMoreCursor = null
            _productsState.value = ProductsResult.Loading
            _productsState.value = repository.getProductsByIds(ids)
            lastLoadedProductsQuery = null
            fullyLoadedProductsQueries.clear()
        }
    }

    fun ensureAllProductsLoaded(
        branchId: String? = null,
        categoryId: String? = null,
        availableOnly: Boolean = false,
        first: Int = 100
    ) {
        val fullQuery = ProductsFullQuery(
            branchId = branchId,
            categoryId = categoryId,
            availableOnly = availableOnly
        )

        val currentState = _productsState.value as? ProductsResult.Success
        if (fullQuery in fullyLoadedProductsQueries &&
            currentState != null &&
            !currentState.hasNextPage
        ) {
            return
        }

        if (fullLoadJob?.isActive == true && fullLoadQueryInProgress == fullQuery) {
            return
        }

        fullLoadJob?.cancel()
        fullLoadQueryInProgress = fullQuery

        fullLoadJob = viewModelScope.launch {
            try {
                _isLoadingMoreProducts.value = false
                _loadMoreProductsError.value = null
                lastFailedLoadMoreCursor = null
                if (currentState == null) {
                    _productsState.value = ProductsResult.Loading
                }

                val allProducts = mutableListOf<Product>()
                var after: String? = null
                var hasNextPage = true

                while (hasNextPage) {
                    when (
                        val page = repository.getProducts(
                            branchId = branchId,
                            categoryId = categoryId,
                            availableOnly = availableOnly,
                            first = first,
                            after = after
                        )
                    ) {
                        is ProductsResult.Success -> {
                            allProducts += page.products
                            hasNextPage = page.hasNextPage && !page.endCursor.isNullOrBlank()
                            after = page.endCursor
                        }

                        is ProductsResult.Error -> {
                            _productsState.value = page
                            return@launch
                        }

                        is ProductsResult.Loading -> {
                            _productsState.value = ProductsResult.Error("No se pudo completar la carga de productos")
                            return@launch
                        }
                    }
                }

                _productsState.value = ProductsResult.Success(
                    products = allProducts.distinctBy { it.id },
                    hasNextPage = false,
                    endCursor = null
                )
                lastLoadedProductsQuery = ProductsQuery(
                    branchId = branchId,
                    categoryId = categoryId,
                    availableOnly = availableOnly,
                    first = first
                )
                fullyLoadedProductsQueries += fullQuery
            } finally {
                if (fullLoadQueryInProgress == fullQuery) {
                    fullLoadQueryInProgress = null
                }
            }
        }
    }

    /**
     * Recarga los productos.
     */
    fun refresh() {
        val query = lastLoadedProductsQuery
        if (query != null) {
            loadProducts(
                branchId = query.branchId,
                categoryId = query.categoryId,
                availableOnly = query.availableOnly,
                first = query.first,
                force = true
            )
        } else {
            loadProducts(force = true)
        }
    }

    fun loadProductCategories(
        branchId: String? = null,
        branchTipos: Set<BranchTipo>,
        force: Boolean = false
    ) {
        val normalizedTipos = branchTipos.toSet()
        if (!force &&
            branchId == lastLoadedBranchIdForCategories &&
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

            val categoriesResult = if (!branchId.isNullOrBlank()) {
                repository.getApplicableCategoriesByBranch(branchId)
                    .fold(
                        onSuccess = { fromBranch ->
                            if (fromBranch.isNotEmpty()) {
                                Result.success(fromBranch)
                            } else {
                                repository.getProductCategories(normalizedTipos)
                            }
                        },
                        onFailure = {
                            repository.getProductCategories(normalizedTipos)
                        }
                    )
            } else {
                repository.getProductCategories(normalizedTipos)
            }

            categoriesResult
                .onSuccess { categories ->
                    lastLoadedBranchIdForCategories = branchId
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

    fun loadVariantLists(branchId: String, force: Boolean = false) {
        if (!force &&
            branchId == lastLoadedBranchIdForVariantLists &&
            _variantListsState.value.variantLists.isNotEmpty()
        ) {
            return
        }

        viewModelScope.launch {
            _variantListsState.value = _variantListsState.value.copy(
                isLoading = true,
                error = null
            )

            repository.getVariantLists(branchId)
                .onSuccess { variantLists ->
                    lastLoadedBranchIdForVariantLists = branchId
                    _variantListsState.value = VariantListsUiState(
                        isLoading = false,
                        variantLists = variantLists.sortedBy { it.name.lowercase() },
                        error = null
                    )
                }
                .onFailure { throwable ->
                    _variantListsState.value = _variantListsState.value.copy(
                        isLoading = false,
                        error = throwable.message ?: "No se pudieron cargar las listas de variantes"
                    )
                }
        }
    }

    suspend fun createVariantList(
        branchId: String,
        name: String,
        description: String?,
        options: List<VariantOptionDraft>
    ): Result<VariantList> {
        val result = repository.createVariantList(
            branchId = branchId,
            name = name,
            description = description,
            options = options
        )
        if (result.isSuccess) {
            loadVariantLists(branchId = branchId, force = true)
        }
        return result
    }

    suspend fun updateVariantList(
        branchId: String,
        variantListId: String,
        name: String?,
        description: String?,
        options: List<VariantOptionDraft>?
    ): Result<VariantList> {
        val result = repository.updateVariantList(
            variantListId = variantListId,
            name = name,
            description = description,
            options = options
        )
        if (result.isSuccess) {
            loadVariantLists(branchId = branchId, force = true)
        }
        return result
    }

    suspend fun deleteVariantList(branchId: String, variantListId: String): Result<Boolean> {
        val result = repository.deleteVariantList(variantListId)
        if (result.isSuccess) {
            loadVariantLists(branchId = branchId, force = true)
        }
        return result
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
        categoryId: String? = null,
        variantListIds: List<String>? = null
    ): ProductsResult {
        invalidateProductsCache()
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
            categoryId = categoryId,
            variantListIds = variantListIds
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
        imagePath: String? = null,
        variantListIds: List<String>? = null
    ): ProductsResult {
        invalidateProductsCache()
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
            categoryId = categoryId,
            variantListIds = variantListIds
        )
        _productsState.value = result
        return result
    }

    /**
     * Elimina un producto y devuelve el resultado de la operacion.
     */
    suspend fun deleteProductBlocking(productId: String): ProductsResult {
        invalidateProductsCache()
        _productsState.value = ProductsResult.Loading
        val result = repository.deleteProduct(productId)
        _productsState.value = result
        return result
    }

    suspend fun toggleProductAvailability(productId: String, availability: Boolean): ProductsResult {
        val previousState = _productsState.value as? ProductsResult.Success
        val previousProducts = previousState?.products
        val previousHasNextPage = previousState?.hasNextPage ?: false
        val previousEndCursor = previousState?.endCursor

        if (previousProducts == null) {
            val result = repository.updateProduct(
                productId = productId,
                availability = availability
            )
            if (result is ProductsResult.Success) {
                invalidateProductsCache()
            }
            return result
        }

        val optimisticProducts = previousProducts.map { product ->
            if (product.id == productId) {
                product.copy(availability = availability)
            } else {
                product
            }
        }
        _productsState.value = ProductsResult.Success(
            products = optimisticProducts,
            hasNextPage = previousHasNextPage,
            endCursor = previousEndCursor
        )

        return when (val result = repository.updateProduct(productId = productId, availability = availability)) {
            is ProductsResult.Success -> {
                val serverProduct = result.products.firstOrNull()
                _productsState.value = ProductsResult.Success(
                    products = if (serverProduct != null) {
                        optimisticProducts.replaceById(serverProduct)
                    } else {
                        optimisticProducts
                    },
                    hasNextPage = previousHasNextPage,
                    endCursor = previousEndCursor
                )
                result
            }

            is ProductsResult.Error -> {
                _productsState.value = ProductsResult.Success(
                    products = previousProducts,
                    hasNextPage = previousHasNextPage,
                    endCursor = previousEndCursor
                )
                result
            }

            is ProductsResult.Loading -> {
                _productsState.value = ProductsResult.Success(
                    products = previousProducts,
                    hasNextPage = previousHasNextPage,
                    endCursor = previousEndCursor
                )
                ProductsResult.Error("No se pudo actualizar disponibilidad")
            }
        }
    }

    private fun List<Product>.replaceById(updated: Product): List<Product> {
        return map { existing ->
            if (existing.id == updated.id) updated else existing
        }
    }
}
