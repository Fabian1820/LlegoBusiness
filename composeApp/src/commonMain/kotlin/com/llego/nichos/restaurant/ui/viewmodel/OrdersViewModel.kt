package com.llego.nichos.restaurant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.nichos.common.data.model.toMenuItem
import com.llego.nichos.restaurant.data.model.MenuItem
import com.llego.nichos.restaurant.data.model.Order
import com.llego.nichos.restaurant.data.model.OrderItem
import com.llego.nichos.restaurant.data.model.OrderModificationState
import com.llego.nichos.restaurant.data.model.OrderStatus
import com.llego.nichos.restaurant.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de Pedidos del Restaurante
 */
class OrdersViewModel : ViewModel() {

    private val repository = RestaurantRepository.getInstance()

    // Estado de UI
    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    // Filtro de estado actual
    private val _selectedFilter = MutableStateFlow<OrderStatus?>(null)
    val selectedFilter: StateFlow<OrderStatus?> = _selectedFilter.asStateFlow()

    // Filtro de rango de fecha
    private val _selectedDateRange = MutableStateFlow<DateRangeFilter>(DateRangeFilter.TODAY)
    val selectedDateRange: StateFlow<DateRangeFilter> = _selectedDateRange.asStateFlow()

    // Pedidos filtrados
    val filteredOrders: StateFlow<List<Order>> = combine(
        repository.orders,
        _selectedFilter,
        _selectedDateRange
    ) { orders, statusFilter, dateFilter ->
        var filtered = orders
        
        // Filtrar por estado
        if (statusFilter != null) {
            filtered = filtered.filter { it.status == statusFilter }
        }
        
        // Filtrar por rango de fecha
        filtered = dateFilter.filterOrders(filtered)
        
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _modificationState = MutableStateFlow<OrderModificationState?>(null)
    val modificationState: StateFlow<OrderModificationState?> = _modificationState.asStateFlow()

    val menuItems: StateFlow<List<MenuItem>> = repository.products
        .map { products -> products.map { it.toMenuItem() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var itemIdCounter = 0

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.Loading
            try {
                repository.orders.collect { orders ->
                    _uiState.value = OrdersUiState.Success(orders)
                }
            } catch (e: Exception) {
                _uiState.value = OrdersUiState.Error(e.message ?: "Error al cargar pedidos")
            }
        }
    }

    fun setFilter(status: OrderStatus?) {
        _selectedFilter.value = status
    }

    fun clearFilter() {
        _selectedFilter.value = null
    }

    fun setDateRangeFilter(range: DateRangeFilter) {
        _selectedDateRange.value = range
    }

    // Acciones sobre pedidos
    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            try {
                repository.acceptOrder(orderId)
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun startPreparingOrder(orderId: String) {
        viewModelScope.launch {
            try {
                repository.startPreparingOrder(orderId)
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun markOrderReady(orderId: String) {
        viewModelScope.launch {
            try {
                repository.markOrderReady(orderId)
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            try {
                repository.cancelOrder(orderId)
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus, estimatedTime: Int? = null) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, newStatus, estimatedTime)
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    fun enterEditMode(order: Order) {
        val normalizedItems = normalizeItems(order.id, order.items)
        val newTotal = normalizedItems.sumOf { it.subtotal }
        _modificationState.value = OrderModificationState(
            originalItems = normalizedItems,
            modifiedItems = normalizedItems,
            isEditMode = true,
            hasChanges = false,
            originalTotal = order.total,
            newTotal = newTotal
        )
    }

    fun exitEditMode() {
        _modificationState.value = null
    }

    fun modifyItemQuantity(itemId: String, newQuantity: Int) {
        val state = _modificationState.value ?: return
        val updatedItems = state.modifiedItems.map { item ->
            if (item.id == itemId) {
                item.copy(quantity = newQuantity.coerceAtLeast(1))
            } else {
                item
            }
        }
        updateModificationState(state, updatedItems)
    }

    fun removeItem(itemId: String) {
        val state = _modificationState.value ?: return
        val updatedItems = state.modifiedItems.filterNot { it.id == itemId }
        updateModificationState(state, updatedItems)
    }

    fun addItem(menuItem: MenuItem, quantity: Int, instructions: String?) {
        val state = _modificationState.value ?: return
        val normalizedInstructions = instructions?.trim()?.takeIf { it.isNotEmpty() }
        val safeQuantity = quantity.coerceAtLeast(1)
        val newItem = OrderItem(
            id = nextItemId(),
            menuItem = menuItem,
            quantity = safeQuantity,
            specialInstructions = normalizedInstructions,
            subtotal = menuItem.price * safeQuantity
        )
        updateModificationState(state, state.modifiedItems + newItem)
    }

    fun modifyItemInstructions(itemId: String, instructions: String?) {
        val state = _modificationState.value ?: return
        val normalizedInstructions = instructions?.trim()?.takeIf { it.isNotEmpty() }
        val updatedItems = state.modifiedItems.map { item ->
            if (item.id == itemId) {
                item.copy(specialInstructions = normalizedInstructions)
            } else {
                item
            }
        }
        updateModificationState(state, updatedItems)
    }

    fun cancelEdit() {
        _modificationState.value = null
    }

    fun applyModification(orderId: String) {
        val state = _modificationState.value ?: return
        viewModelScope.launch {
            try {
                repository.updateOrderItems(orderId, state.modifiedItems, state.newTotal)
            } catch (e: Exception) {
                // TODO: Manejar error
            } finally {
                _modificationState.value = null
            }
        }
    }

    // Obtener pedido por ID
    fun getOrderById(orderId: String): Order? {
        return (uiState.value as? OrdersUiState.Success)?.orders
            ?.firstOrNull { it.id == orderId }
    }

    // Estadísticas útiles
    fun getPendingOrdersCount(): Int {
        return (uiState.value as? OrdersUiState.Success)?.orders
            ?.count { it.status == OrderStatus.PENDING } ?: 0
    }

    fun getActiveOrdersCount(): Int {
        return (uiState.value as? OrdersUiState.Success)?.orders
            ?.count { it.status in listOf(OrderStatus.PREPARING, OrderStatus.READY) } ?: 0
    }

    private fun updateModificationState(state: OrderModificationState, items: List<OrderItem>) {
        val (normalizedItems, newTotal) = recalculateTotals(items)
        val hasChanges = state.originalItems != normalizedItems
        _modificationState.value = state.copy(
            modifiedItems = normalizedItems,
            hasChanges = hasChanges,
            newTotal = newTotal,
            isEditMode = true
        )
    }

    private fun recalculateTotals(items: List<OrderItem>): Pair<List<OrderItem>, Double> {
        val normalizedItems = items.map { item ->
            val subtotal = item.menuItem.price * item.quantity
            if (item.subtotal == subtotal) item else item.copy(subtotal = subtotal)
        }
        val total = normalizedItems.sumOf { it.subtotal }
        return normalizedItems to total
    }

    private fun normalizeItems(orderId: String, items: List<OrderItem>): List<OrderItem> {
        return items.mapIndexed { index, item ->
            val resolvedId = if (item.id.isNotBlank()) item.id else "$orderId-item-${index + 1}"
            val subtotal = item.menuItem.price * item.quantity
            if (item.id == resolvedId && item.subtotal == subtotal) {
                item
            } else {
                item.copy(id = resolvedId, subtotal = subtotal)
            }
        }
    }

    private fun nextItemId(): String {
        itemIdCounter += 1
        return "added-item-$itemIdCounter"
    }
}

/**
 * Estados de UI para pantalla de pedidos
 */
sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Success(val orders: List<Order>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

/**
 * Filtros de rango de fecha para pedidos
 */
enum class DateRangeFilter(val displayName: String) {
    TODAY("Hoy"),
    YESTERDAY("Ayer"),
    LAST_WEEK("Última Semana"),
    CUSTOM("Seleccionar");

    /**
     * Filtra los pedidos según el rango de fecha seleccionado
     */
    fun filterOrders(orders: List<Order>): List<Order> {
        // Por ahora retornamos todos los pedidos
        // TODO: Implementar filtrado real cuando se integre con backend
        return orders
    }

    private fun parseOrderDate(dateString: String): Long {
        // Formato esperado: "2024-10-06T12:30:00" o similar
        // Por ahora retornamos un timestamp mock
        // TODO: Implementar parseo real con librería de fechas
        return 0L
    }

    private fun getStartOfDay(timestamp: Long): Long {
        // Simplificado - en producción usar librería de fechas
        return timestamp - (timestamp % 86400000L)
    }

    private fun getEndOfDay(timestamp: Long): Long {
        return getStartOfDay(timestamp) + 86399999L
    }
}
