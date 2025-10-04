package com.llego.nichos.restaurant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.nichos.restaurant.data.model.MenuCategory
import com.llego.nichos.restaurant.data.model.MenuItem
import com.llego.nichos.restaurant.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión del Menú del Restaurante
 */
class MenuViewModel : ViewModel() {

    private val repository = RestaurantRepository.getInstance()

    // Estado de UI
    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    // Categoría seleccionada para filtrar
    private val _selectedCategory = MutableStateFlow<MenuCategory?>(null)
    val selectedCategory: StateFlow<MenuCategory?> = _selectedCategory.asStateFlow()

    // Búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Items filtrados
    val filteredMenuItems: StateFlow<List<MenuItem>> = combine(
        repository.menuItems,
        _selectedCategory,
        _searchQuery
    ) { items, category, query ->
        var filtered = items

        // Filtrar por categoría
        if (category != null) {
            filtered = filtered.filter { it.category == category }
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
                repository.menuItems.collect { items ->
                    _uiState.value = MenuUiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = MenuUiState.Error(e.message ?: "Error al cargar menú")
            }
        }
    }

    fun setCategory(category: MenuCategory?) {
        _selectedCategory.value = category
    }

    fun clearCategory() {
        _selectedCategory.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    // CRUD de items del menú
    fun addMenuItem(item: MenuItem) {
        viewModelScope.launch {
            try {
                repository.addMenuItem(item)
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun updateMenuItem(item: MenuItem) {
        viewModelScope.launch {
            try {
                repository.updateMenuItem(item)
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun deleteMenuItem(itemId: String) {
        viewModelScope.launch {
            try {
                repository.deleteMenuItem(itemId)
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun toggleItemAvailability(itemId: String) {
        viewModelScope.launch {
            try {
                repository.toggleItemAvailability(itemId)
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    // Obtener item por ID
    suspend fun getMenuItemById(itemId: String): MenuItem? {
        return repository.getMenuItemById(itemId)
    }

    // Categorías con conteo de items
    fun getCategoriesWithCount(): Map<MenuCategory, Int> {
        val items = (uiState.value as? MenuUiState.Success)?.menuItems ?: emptyList()
        return items.groupBy { it.category }
            .mapValues { it.value.size }
    }
}

/**
 * Estados de UI para pantalla de menú
 */
sealed class MenuUiState {
    object Loading : MenuUiState()
    data class Success(val menuItems: List<MenuItem>) : MenuUiState()
    data class Error(val message: String) : MenuUiState()
}
