package com.llego.nichos.restaurant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.nichos.restaurant.data.model.Order
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

    // Pedidos filtrados
    val filteredOrders: StateFlow<List<Order>> = combine(
        repository.orders,
        _selectedFilter
    ) { orders, filter ->
        if (filter == null) {
            orders
        } else {
            orders.filter { it.status == filter }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, newStatus)
            } catch (e: Exception) {
                // TODO: Manejar error
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
}

/**
 * Estados de UI para pantalla de pedidos
 */
sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Success(val orders: List<Order>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}
