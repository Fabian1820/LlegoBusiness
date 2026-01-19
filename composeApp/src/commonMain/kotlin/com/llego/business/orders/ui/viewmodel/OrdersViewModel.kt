package com.llego.business.orders.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderItem
import com.llego.business.orders.data.model.OrderModificationState
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.data.repository.OrderItemInput
import com.llego.business.orders.data.repository.OrderRepository
import com.llego.business.orders.data.repository.OrderRepositoryImpl
import com.llego.business.orders.data.repository.OrdersResult
import com.llego.business.orders.data.subscription.SubscriptionManager
import com.llego.shared.data.auth.TokenManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel para gestión de Pedidos integrado con backend GraphQL
 *
 * Requirements: 2.1, 2.7, 2.8, 3.3, 3.4, 5.1, 5.2, 5.4, 5.5, 5.6, 5.7, 5.8, 9.1, 9.2, 9.3, 9.4
 */
class OrdersViewModel(
    tokenManager: TokenManager
) : ViewModel() {

    private val repository: OrderRepository = OrderRepositoryImpl.getInstance(tokenManager)
    private val subscriptionManager = SubscriptionManager.getInstance()

    // Estado de UI
    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    // Lista de pedidos cargados
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Filtro de estado actual
    private val _selectedFilter = MutableStateFlow<OrderStatus?>(null)
    val selectedFilter: StateFlow<OrderStatus?> = _selectedFilter.asStateFlow()

    // Filtro de rango de fecha
    private val _selectedDateRange = MutableStateFlow<DateRangeFilter>(DateRangeFilter.TODAY)
    val selectedDateRange: StateFlow<DateRangeFilter> = _selectedDateRange.asStateFlow()

    // Branch ID actual
    private val _currentBranchId = MutableStateFlow<String?>(null)
    val currentBranchId: StateFlow<String?> = _currentBranchId.asStateFlow()

    // Pedidos filtrados localmente
    val filteredOrders: StateFlow<List<Order>> = combine(
        _orders,
        _selectedFilter
    ) { orders, statusFilter ->
        if (statusFilter != null) {
            orders.filter { it.status == statusFilter }
        } else {
            orders
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estado de modificación de pedido
    private val _modificationState = MutableStateFlow<OrderModificationState?>(null)
    val modificationState: StateFlow<OrderModificationState?> = _modificationState.asStateFlow()

    // Paginación
    private var currentOffset = 0
    private var hasMore = true
    private val pageSize = 50

    init {
        observeNewOrders()
        observeOrderUpdates()
    }

    /**
     * Configura el branch ID actual y carga los pedidos
     */
    fun setCurrentBranchId(branchId: String?) {
        if (branchId != _currentBranchId.value) {
            _currentBranchId.value = branchId
            branchId?.let {
                resetPagination()
                loadOrders()
                subscriptionManager.updateActiveBranch(it)
            }
        }
    }

    /**
     * Carga pedidos desde el backend con los filtros actuales
     * Requirements: 2.1, 2.3, 2.4, 2.5
     */
    fun loadOrders() {
        val branchId = _currentBranchId.value
        if (branchId == null) {
            println("📦 OrdersViewModel.loadOrders(): branchId is null, skipping")
            return
        }

        viewModelScope.launch {
            _uiState.value = OrdersUiState.Loading

            val dateRange = _selectedDateRange.value.getDateRange()

            println("📦 OrdersViewModel.loadOrders()")
            println("   branchId: $branchId")
            println("   filter: ${_selectedDateRange.value} -> $dateRange")
            println("   statusFilter: ${_selectedFilter.value}")

            when (val result = repository.getBranchOrders(
                branchId = branchId,
                status = _selectedFilter.value,
                fromDate = dateRange?.first,
                toDate = dateRange?.second,
                limit = pageSize,
                offset = currentOffset
            )) {
                is OrdersResult.Success -> {
                    println("📦 OrdersViewModel: ✅ Success - ${result.orders.size} orders, hasMore=${result.hasMore}")
                    _orders.value = result.orders
                    hasMore = result.hasMore
                    _uiState.value = OrdersUiState.Success
                }
                is OrdersResult.Error -> {
                    println("📦 OrdersViewModel: ❌ Error - ${result.message}")
                    _uiState.value = OrdersUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Carga más pedidos (paginación)
     */
    fun loadMoreOrders() {
        if (!hasMore || _uiState.value == OrdersUiState.Loading) return

        val branchId = _currentBranchId.value ?: return
        currentOffset += pageSize

        viewModelScope.launch {
            val dateRange = _selectedDateRange.value.getDateRange()

            when (val result = repository.getBranchOrders(
                branchId = branchId,
                status = _selectedFilter.value,
                fromDate = dateRange?.first,
                toDate = dateRange?.second,
                limit = pageSize,
                offset = currentOffset
            )) {
                is OrdersResult.Success -> {
                    _orders.value = _orders.value + result.orders
                    hasMore = result.hasMore
                }
                is OrdersResult.Error -> {
                    // Revert offset on error
                    currentOffset -= pageSize
                }
            }
        }
    }

    /**
     * Recarga pedidos desde el principio
     */
    fun refreshOrders() {
        resetPagination()
        loadOrders()
    }

    private fun resetPagination() {
        currentOffset = 0
        hasMore = true
    }

    /**
     * Observa nuevos pedidos desde suscripciones en tiempo real
     * Requirements: 3.3
     */
    private fun observeNewOrders() {
        viewModelScope.launch {
            subscriptionManager.newOrders.collect { event ->
                if (event.branchId == _currentBranchId.value) {
                    // Agregar nuevo pedido al inicio de la lista
                    _orders.update { currentOrders ->
                        listOf(event.order) + currentOrders
                    }
                }
            }
        }
    }

    /**
     * Observa actualizaciones de pedidos desde suscripciones en tiempo real
     * Requirements: 3.4
     */
    private fun observeOrderUpdates() {
        viewModelScope.launch {
            subscriptionManager.orderUpdates.collect { event ->
                // Actualizar pedido existente en la lista
                _orders.update { currentOrders ->
                    currentOrders.map { order ->
                        if (order.id == event.orderId) {
                            order.copy(
                                status = event.newStatus,
                                updatedAt = event.updatedAt
                            )
                        } else {
                            order
                        }
                    }
                }
            }
        }
    }

    // ==================== FILTROS ====================

    /**
     * Establece filtro por estado
     * Requirements: 9.1, 9.3
     */
    fun setFilter(status: OrderStatus?) {
        _selectedFilter.value = status
        refreshOrders()
    }

    /**
     * Limpia el filtro de estado
     */
    fun clearFilter() {
        _selectedFilter.value = null
        refreshOrders()
    }

    /**
     * Establece filtro de rango de fecha
     * Requirements: 9.2, 9.4
     */
    fun setDateRangeFilter(range: DateRangeFilter) {
        _selectedDateRange.value = range
        refreshOrders()
    }

    // ==================== ACCIONES SOBRE PEDIDOS ====================

    /**
     * Acepta un pedido pendiente
     * Requirements: 5.1
     */
    fun acceptOrder(orderId: String, estimatedMinutes: Int = 30) {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            repository.acceptOrder(orderId, estimatedMinutes)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.value = OrdersUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al aceptar pedido")
                }
        }
    }

    /**
     * Rechaza un pedido pendiente
     * Requirements: 5.2
     */
    fun rejectOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            repository.rejectOrder(orderId, reason)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.value = OrdersUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al rechazar pedido")
                }
        }
    }

    /**
     * Actualiza el estado de un pedido a PREPARING
     * Requirements: 5.3
     */
    fun startPreparingOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            repository.updateOrderStatus(orderId, OrderStatus.PREPARING)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.value = OrdersUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al iniciar preparación")
                }
        }
    }

    /**
     * Marca un pedido como listo para recoger
     * Requirements: 5.4
     */
    fun markOrderReady(orderId: String) {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            repository.markOrderReady(orderId)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.value = OrdersUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al marcar como listo")
                }
        }
    }

    /**
     * Cancela un pedido (lo rechaza con razón de cancelación)
     */
    fun cancelOrder(orderId: String, reason: String = "Cancelado por el negocio") {
        rejectOrder(orderId, reason)
    }

    /**
     * Actualiza el estado de un pedido genéricamente
     * Requirements: 5.3
     */
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus, message: String? = null) {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            repository.updateOrderStatus(orderId, newStatus, message)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.value = OrdersUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al actualizar estado")
                }
        }
    }

    // ==================== MODIFICACIÓN DE ITEMS ====================

    /**
     * Entra en modo de edición de items del pedido
     * Requirements: 6.1
     */
    fun enterEditMode(order: Order) {
        if (!order.isEditable) return

        _modificationState.value = OrderModificationState.fromItems(
            items = order.items,
            originalTotal = order.total
        )
    }

    /**
     * Sale del modo de edición sin guardar cambios
     */
    fun exitEditMode() {
        _modificationState.value = null
    }

    /**
     * Modifica la cantidad de un item
     * Requirements: 6.3
     */
    fun modifyItemQuantity(productId: String, newQuantity: Int) {
        val state = _modificationState.value ?: return
        val quantity = newQuantity.coerceAtLeast(1)

        val updatedItems = state.modifiedItems.map { item ->
            if (item.productId == productId) {
                item.copy(quantity = quantity)
            } else {
                item
            }
        }

        updateModificationState(state, updatedItems)
    }

    /**
     * Elimina un item del pedido
     * Requirements: 6.4
     */
    fun removeItem(productId: String) {
        val state = _modificationState.value ?: return
        val updatedItems = state.modifiedItems.filterNot { it.productId == productId }
        updateModificationState(state, updatedItems)
    }

    /**
     * Agrega un nuevo item al pedido
     * Requirements: 6.5
     */
    fun addItem(productId: String, name: String, price: Double, quantity: Int, imageUrl: String) {
        val state = _modificationState.value ?: return
        val safeQuantity = quantity.coerceAtLeast(1)

        val newItem = OrderItem(
            productId = productId,
            name = name,
            price = price,
            quantity = safeQuantity,
            imageUrl = imageUrl,
            wasModifiedByStore = true
        )

        updateModificationState(state, state.modifiedItems + newItem)
    }

    /**
     * Cancela la edición y restaura los items originales
     */
    fun cancelEdit() {
        _modificationState.value = null
    }

    /**
     * Aplica las modificaciones al pedido en el backend
     * Requirements: 6.6
     */
    fun applyModification(orderId: String, reason: String = "Modificado por el negocio") {
        val state = _modificationState.value ?: return

        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            val itemInputs = state.modifiedItems.map { item ->
                OrderItemInput(
                    productId = item.productId,
                    quantity = item.quantity
                )
            }

            repository.modifyOrderItems(orderId, itemInputs, reason)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _modificationState.value = null
                    _uiState.value = OrdersUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al modificar items")
                }
        }
    }

    private fun updateModificationState(state: OrderModificationState, items: List<OrderItem>) {
        val newTotal = items.sumOf { it.lineTotal }
        val hasChanges = items != state.originalItems

        _modificationState.value = state.copy(
            modifiedItems = items,
            hasChanges = hasChanges,
            newTotal = newTotal
        )
    }

    // ==================== COMENTARIOS ====================

    /**
     * Agrega un comentario a un pedido
     * Requirements: 7.3
     */
    fun addComment(orderId: String, message: String) {
        viewModelScope.launch {
            repository.addOrderComment(orderId, message)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                }
                .onFailure { error ->
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al agregar comentario")
                }
        }
    }

    /**
     * Alias for addComment for backward compatibility
     */
    fun addOrderComment(orderId: String, message: String) = addComment(orderId, message)

    // ==================== UTILIDADES ====================

    /**
     * Actualiza un pedido en la lista local
     */
    private fun updateOrderInList(updatedOrder: Order) {
        _orders.update { currentOrders ->
            currentOrders.map { order ->
                if (order.id == updatedOrder.id) updatedOrder else order
            }
        }
    }

    /**
     * Obtiene un pedido por ID desde la lista local
     */
    fun getOrderById(orderId: String): Order? {
        return _orders.value.firstOrNull { it.id == orderId }
    }

    /**
     * Cuenta pedidos pendientes de aceptación
     */
    fun getPendingOrdersCount(): Int {
        return _orders.value.count { it.status == OrderStatus.PENDING_ACCEPTANCE }
    }

    /**
     * Cuenta pedidos activos (en preparación o listos)
     */
    fun getActiveOrdersCount(): Int {
        return _orders.value.count {
            it.status in listOf(OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP)
        }
    }

    /**
     * Limpia el estado de error
     */
    fun clearError() {
        if (_uiState.value is OrdersUiState.Error || _uiState.value is OrdersUiState.ActionError) {
            _uiState.value = OrdersUiState.Success
        }
    }

    /**
     * Alias for clearError for backward compatibility
     */
    fun clearActionError() = clearError()

    /**
     * Carga items del menú para agregar a pedidos (placeholder)
     * En el futuro esto cargará desde ProductRepository
     */
    fun loadMenuItems(branchId: String? = null) {
        // No-op for now - menu items are loaded from product repository when needed
        // This method exists for backward compatibility
    }
}

/**
 * Estados de UI para pantalla de pedidos
 */
sealed class OrdersUiState {
    object Loading : OrdersUiState()
    object Success : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
    data class ActionInProgress(val orderId: String) : OrdersUiState()
    data class ActionError(val message: String) : OrdersUiState()
}

/**
 * Filtros de rango de fecha para pedidos
 */
enum class DateRangeFilter(val displayName: String) {
    TODAY("Hoy"),
    YESTERDAY("Ayer"),
    LAST_WEEK("Última Semana"),
    LAST_MONTH("Último Mes"),
    CUSTOM("Seleccionar"),
    ALL("Todos");

    /**
     * Obtiene el rango de fechas en formato ISO para este filtro
     * @return Par (fromDate, toDate) o null para "Todos" o "Custom"
     */
    fun getDateRange(): Pair<String, String>? {
        val now = Clock.System.now()
        val timezone = TimeZone.currentSystemDefault()
        val today: LocalDate = now.toLocalDateTime(timezone).date

        fun LocalDate.toIsoDateTimeStart(): String = "${this}T00:00:00Z"
        fun LocalDate.toIsoDateTimeEnd(): String = "${this}T23:59:59Z"

        return when (this) {
            TODAY -> {
                Pair(today.toIsoDateTimeStart(), today.toIsoDateTimeEnd())
            }
            YESTERDAY -> {
                val yesterday = today.minus(DatePeriod(days = 1))
                Pair(yesterday.toIsoDateTimeStart(), yesterday.toIsoDateTimeEnd())
            }
            LAST_WEEK -> {
                val weekAgo = today.minus(DatePeriod(days = 7))
                Pair(weekAgo.toIsoDateTimeStart(), today.toIsoDateTimeEnd())
            }
            LAST_MONTH -> {
                val monthAgo = today.minus(DatePeriod(months = 1))
                Pair(monthAgo.toIsoDateTimeStart(), today.toIsoDateTimeEnd())
            }
            CUSTOM -> null // Custom requires explicit date range
            ALL -> null
        }
    }
}
