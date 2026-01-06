package com.llego.nichos.restaurant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.nichos.restaurant.data.model.MenuCategory
import com.llego.nichos.restaurant.data.repository.RestaurantRepository
import com.llego.nichos.market.data.repository.MarketRepository
import com.llego.nichos.agromarket.data.repository.AgromarketRepository
import com.llego.nichos.clothing.data.repository.ClothingRepository
import com.llego.nichos.common.data.model.Product
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.BusinessType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión del Menú/Productos
 * Soporta múltiples tipos de negocio
 */
class MenuViewModel(
    tokenManager: TokenManager,
    initialBusinessType: BusinessType = BusinessType.RESTAURANT
) : ViewModel() {

    private var businessType = MutableStateFlow(initialBusinessType)

    private val restaurantRepository = RestaurantRepository.getInstance(tokenManager)
    private val marketRepository = MarketRepository.getInstance(tokenManager)
    private val agromarketRepository = AgromarketRepository.getInstance(tokenManager)
    private val clothingRepository = ClothingRepository.getInstance(tokenManager)

    // Estado de UI
    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    // Categoría seleccionada para filtrar (MenuCategory para restaurante, String para otros)
    private val _selectedCategory = MutableStateFlow<MenuCategory?>(null)
    val selectedCategory: StateFlow<MenuCategory?> = _selectedCategory.asStateFlow()
    
    // Categoría seleccionada como String (para nichos no-restaurante)
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    // Búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Flow de Products según el tipo de negocio (unificado)
    private val productsFlow: Flow<List<Product>> = businessType.flatMapLatest { type ->
        when (type) {
            BusinessType.RESTAURANT -> restaurantRepository.products
            BusinessType.MARKET -> marketRepository.products
            BusinessType.AGROMARKET -> agromarketRepository.products
            BusinessType.CLOTHING_STORE -> clothingRepository.products
            BusinessType.PHARMACY -> flowOf(emptyList()) // TODO: Implementar cuando haya PharmacyRepository
        }
    }

    // Products filtrados (unificado)
    val filteredProducts: StateFlow<List<Product>> = combine(
        productsFlow,
        businessType,
        _selectedCategory,
        _selectedCategoryId,
        _searchQuery
    ) { products, type, category, categoryId, query ->
        var filtered = products

        // Filtrar por categoría
        if (type == BusinessType.RESTAURANT && category != null) {
            // Para restaurante, filtrar por MenuCategory
            filtered = filtered.filter { product ->
                val productCategory = com.llego.nichos.common.utils.mapToMenuCategory(product.category, type)
                productCategory == category
            }
        } else if (type != BusinessType.RESTAURANT && categoryId != null) {
            // Para otros nichos, filtrar por categoryId
            filtered = filtered.filter { product ->
                val productCategoryId = com.llego.nichos.common.utils.mapToCategoryId(
                    product.category,
                    type
                )
                productCategoryId == categoryId
            }
        }

        // Filtrar por búsqueda
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }

        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadMenuItems()
    }

    fun loadMenuItems() {
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading
            try {
                productsFlow.collect { products ->
                    _uiState.value = MenuUiState.Success(products)
                }
            } catch (e: Exception) {
                _uiState.value = MenuUiState.Error(e.message ?: "Error al cargar productos")
            }
        }
    }

    fun setCategory(category: MenuCategory?) {
        _selectedCategory.value = category
        _selectedCategoryId.value = null
    }
    
    fun setCategoryId(categoryId: String?) {
        _selectedCategoryId.value = categoryId
        _selectedCategory.value = null
    }

    fun clearCategory() {
        _selectedCategory.value = null
        _selectedCategoryId.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    // CRUD de productos (unificado)
    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                when (businessType.value) {
                    BusinessType.RESTAURANT -> restaurantRepository.addProduct(product)
                    BusinessType.MARKET -> marketRepository.addProduct(product)
                    BusinessType.AGROMARKET -> agromarketRepository.addProduct(product)
                    BusinessType.CLOTHING_STORE -> clothingRepository.addProduct(product)
                    BusinessType.PHARMACY -> {} // TODO
                }
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                when (businessType.value) {
                    BusinessType.RESTAURANT -> restaurantRepository.updateProduct(product)
                    BusinessType.MARKET -> marketRepository.updateProduct(product)
                    BusinessType.AGROMARKET -> agromarketRepository.updateProduct(product)
                    BusinessType.CLOTHING_STORE -> clothingRepository.updateProduct(product)
                    BusinessType.PHARMACY -> {} // TODO
                }
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                when (businessType.value) {
                    BusinessType.RESTAURANT -> restaurantRepository.deleteProduct(productId)
                    BusinessType.MARKET -> marketRepository.deleteProduct(productId)
                    BusinessType.AGROMARKET -> agromarketRepository.deleteProduct(productId)
                    BusinessType.CLOTHING_STORE -> clothingRepository.deleteProduct(productId)
                    BusinessType.PHARMACY -> {} // TODO
                }
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun toggleProductAvailability(productId: String) {
        viewModelScope.launch {
            try {
                when (businessType.value) {
                    BusinessType.RESTAURANT -> restaurantRepository.toggleProductAvailability(productId)
                    BusinessType.MARKET -> marketRepository.toggleProductAvailability(productId)
                    BusinessType.AGROMARKET -> agromarketRepository.toggleProductAvailability(productId)
                    BusinessType.CLOTHING_STORE -> clothingRepository.toggleProductAvailability(productId)
                    BusinessType.PHARMACY -> {} // TODO
                }
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    // Obtener producto por ID
    suspend fun getProductById(productId: String): Product? {
        return when (businessType.value) {
            BusinessType.RESTAURANT -> restaurantRepository.getProductById(productId)
            BusinessType.MARKET -> marketRepository.getProductById(productId)
            BusinessType.AGROMARKET -> agromarketRepository.getProductById(productId)
            BusinessType.CLOTHING_STORE -> clothingRepository.getProductById(productId)
            BusinessType.PHARMACY -> null // TODO
        }
    }
    
    // Funciones de compatibilidad legacy (deprecated) - Eliminadas para simplificar
    // Si se necesitan, importar MenuItem y las funciones de conversión

    // Actualizar el tipo de negocio y recargar datos
    fun updateBusinessType(newBusinessType: BusinessType) {
        if (businessType.value != newBusinessType) {
            businessType.value = newBusinessType
            _selectedCategory.value = null // Limpiar categoría al cambiar de nicho
            loadMenuItems()
        }
    }

    // Categorías con conteo de productos
    fun getCategoriesWithCount(): Map<String, Int> {
        val products = filteredProducts.value
        val categories = com.llego.nichos.common.config.BusinessConfigProvider.getCategoriesForBusiness(businessType.value)
        
        return products.groupBy { product ->
            // Obtener el nombre de categoría para display
            val categoryId = com.llego.nichos.common.utils.mapToCategoryId(product.category, businessType.value)
            categories.find { it.id == categoryId }?.displayName ?: product.category
        }.mapValues { it.value.size }
    }
}

/**
 * Estados de UI para pantalla de menú/productos
 */
sealed class MenuUiState {
    object Loading : MenuUiState()
    data class Success(val products: List<Product>) : MenuUiState()
    data class Error(val message: String) : MenuUiState()
}
