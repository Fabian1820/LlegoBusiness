@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.llego.business.orders.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.business.orders.data.model.MenuItem
import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderActor
import com.llego.business.orders.data.model.OrderComment
import com.llego.business.orders.data.model.CustomerCashKycStatus
import com.llego.business.orders.data.model.OrderItem
import com.llego.business.orders.data.model.OrderModificationState
import com.llego.business.orders.data.model.PaymentAttempt
import com.llego.business.orders.data.model.PaymentAttemptStatus
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.data.model.PaymentStatus
import com.llego.business.orders.data.model.DashboardStats
import com.llego.business.orders.data.model.toMenuItem
import com.llego.business.orders.data.repository.OrderItemInput
import com.llego.business.orders.data.repository.OrderRepository
import com.llego.business.orders.data.repository.OrderRepositoryImpl
import com.llego.business.orders.data.repository.OrdersResult
import com.llego.business.orders.data.subscription.SubscriptionManager
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.Combo
import com.llego.shared.data.model.CombosResult
import com.llego.shared.data.model.ProductsResult
import com.llego.shared.data.repositories.ComboRepository
import com.llego.shared.data.repositories.ProductRepository
import com.llego.business.orders.data.model.OrderComboSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class OrdersViewModel(
    tokenManager: TokenManager
) : ViewModel() {

    private val repository: OrderRepository = OrderRepositoryImpl.getInstance(tokenManager)
    private val productRepository = ProductRepository(tokenManager)
    private val comboRepository = ComboRepository(tokenManager)
    private val subscriptionManager = SubscriptionManager.getInstance()

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    val pendingOrdersCount: StateFlow<Int> = _orders
        .map { list -> list.count { it.status == OrderStatus.PENDING_ACCEPTANCE } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _selectedFilter = MutableStateFlow<OrderStatus?>(null)
    val selectedFilter: StateFlow<OrderStatus?> = _selectedFilter.asStateFlow()

    private val _selectedDateRange = MutableStateFlow(DateRangeFilter.TODAY)
    val selectedDateRange: StateFlow<DateRangeFilter> = _selectedDateRange.asStateFlow()

    private val _currentBranchId = MutableStateFlow<String?>(null)
    val currentBranchId: StateFlow<String?> = _currentBranchId.asStateFlow()

    private val _availableBranchIds = MutableStateFlow<List<String>>(emptyList())
    private val _dashboardStatsState = MutableStateFlow<DashboardStatsUiState>(DashboardStatsUiState.Idle)
    val dashboardStatsState: StateFlow<DashboardStatsUiState> = _dashboardStatsState.asStateFlow()
    private var lastDashboardRequest: Triple<String, String, String?>? = null

    val filteredOrders: StateFlow<List<Order>> = combine(
        _orders,
        _selectedFilter
    ) { currentOrders, statusFilter ->
        if (statusFilter == null) currentOrders else currentOrders.filter { it.status == statusFilter }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _modificationState = MutableStateFlow<OrderModificationState?>(null)
    val modificationState: StateFlow<OrderModificationState?> = _modificationState.asStateFlow()

    private val _activePaymentAttempt = MutableStateFlow<PaymentAttempt?>(null)
    val activePaymentAttempt: StateFlow<PaymentAttempt?> = _activePaymentAttempt.asStateFlow()

    private val _customerCashKycStatus = MutableStateFlow<CustomerCashKycStatus?>(null)
    val customerCashKycStatus: StateFlow<CustomerCashKycStatus?> = _customerCashKycStatus.asStateFlow()
    private val _menuItemsState = MutableStateFlow(MenuItemsUiState())
    val menuItemsState: StateFlow<MenuItemsUiState> = _menuItemsState.asStateFlow()

    private val _comboDefinitions = MutableStateFlow<Map<String, Combo>>(emptyMap())
    val comboDefinitions: StateFlow<Map<String, Combo>> = _comboDefinitions.asStateFlow()

    private val _loadingComboIds = MutableStateFlow<Set<String>>(emptySet())
    val loadingComboIds: StateFlow<Set<String>> = _loadingComboIds.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentOffset = 0
    private var hasMore = true
    private val pageSize = 50

    private var lastSubscribedBranchIds: List<String> = emptyList()
    private var lastSubscribedActiveBranchId: String? = null
    private var lastLoadedMenuBranchId: String? = null
    private val pendingBusinessCommentsByOrder = mutableMapOf<String, MutableList<String>>()

    init {
        observeNewOrders()
        observeOrderUpdates()
    }

    fun setCurrentBranchId(branchId: String?) {
        if (branchId == _currentBranchId.value) return

        if (branchId == null) {
            _currentBranchId.value = null
            _orders.value = emptyList()
            _uiState.value = OrdersUiState.Loading
            _menuItemsState.value = MenuItemsUiState()
            lastLoadedMenuBranchId = null
            subscriptionManager.cancelAllSubscriptions()
            lastSubscribedBranchIds = emptyList()
            lastSubscribedActiveBranchId = null
            return
        }

        // Limpiar el estado inmediatamente para evitar mostrar pedidos de la sucursal anterior
        _orders.value = emptyList()
        _uiState.value = OrdersUiState.Loading
        _currentBranchId.value = branchId
        refreshSubscriptionsIfPossible()

        branchId?.let {
            resetPagination()
            loadOrders()
        }
    }

    fun setSubscribedBranchIds(branchIds: List<String>) {
        val normalized = branchIds.filter { it.isNotBlank() }.distinct().sorted()
        if (normalized == _availableBranchIds.value) return
        _availableBranchIds.value = normalized
        refreshSubscriptionsIfPossible()
    }

    private fun refreshSubscriptionsIfPossible() {
        val activeBranchId = _currentBranchId.value ?: return
        val branchIds = _availableBranchIds.value
        if (branchIds.isEmpty()) {
            subscriptionManager.updateActiveBranch(activeBranchId)
            return
        }

        if (lastSubscribedBranchIds == branchIds && lastSubscribedActiveBranchId == activeBranchId) {
            subscriptionManager.updateActiveBranch(activeBranchId)
            return
        }

        subscriptionManager.subscribeToAllBranches(branchIds, activeBranchId)
        lastSubscribedBranchIds = branchIds
        lastSubscribedActiveBranchId = activeBranchId
    }

    fun loadOrders() {
        val branchId = _currentBranchId.value ?: return

        viewModelScope.launch {
            val shouldShowLoading = _orders.value.isEmpty()
            if (shouldShowLoading) {
                _uiState.value = OrdersUiState.Loading
            }
            val dateRange = _selectedDateRange.value.getDateRange()
            val allOrders = mutableListOf<Order>()
            var fetchOffset = 0
            var shouldContinue = true

            while (shouldContinue) {
                when (val result = repository.getBranchOrders(
                    branchId = branchId,
                    status = _selectedFilter.value,
                    fromDate = dateRange?.first,
                    toDate = dateRange?.second,
                    limit = pageSize,
                    offset = fetchOffset
                )) {
                    is OrdersResult.Success -> {
                        allOrders += result.orders
                        val receivedOrders = result.orders.isNotEmpty()
                        shouldContinue = result.hasMore && receivedOrders
                        fetchOffset += pageSize
                    }
                    is OrdersResult.Error -> {
                        _uiState.value = OrdersUiState.Error(result.message)
                        shouldContinue = false
                        allOrders.clear()
                    }
                }
            }

            if (_uiState.value !is OrdersUiState.Error) {
                _orders.value = allOrders
                currentOffset = 0
                hasMore = false
                _uiState.value = OrdersUiState.Success
            }
            _isRefreshing.value = false
        }
    }

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
                    currentOffset -= pageSize
                }
            }
        }
    }

    fun refreshOrders() {
        _isRefreshing.value = true
        resetPagination()
        loadOrders()
    }

    private fun resetPagination() {
        currentOffset = 0
        hasMore = true
    }

    private fun observeNewOrders() {
        viewModelScope.launch {
            subscriptionManager.newOrders.collect { event ->
                if (event.branchId != _currentBranchId.value) return@collect

                _orders.update { currentOrders ->
                    if (currentOrders.any { it.id == event.order.id }) {
                        currentOrders.map { current ->
                            if (current.id == event.order.id) mergeOrder(current, event.order) else current
                        }
                    } else {
                        listOf(event.order) + currentOrders
                    }
                }
            }
        }
    }

    private fun observeOrderUpdates() {
        viewModelScope.launch {
            subscriptionManager.orderUpdates.collect { event ->
                val incomingOrder = event.order
                if (incomingOrder != null) {
                    updateOrderInList(incomingOrder)
                } else {
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
    }

    fun setFilter(status: OrderStatus?) {
        _selectedFilter.value = status
        refreshOrders()
    }

    fun clearFilter() {
        _selectedFilter.value = null
        refreshOrders()
    }

    fun setDateRangeFilter(range: DateRangeFilter) {
        _selectedDateRange.value = range
        refreshOrders()
    }

    fun acceptOrder(orderId: String, estimatedMinutes: Int = 30, deliveryFee: Double? = null) {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            repository.acceptOrder(orderId, estimatedMinutes, deliveryFee)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.value = OrdersUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al aceptar pedido")
                }
        }
    }

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

    fun startPreparingOrder(orderId: String) {
        val order = getOrderById(orderId) ?: return
        if (order.status != OrderStatus.ACCEPTED) return
        if (order.requiresCompletedPaymentBeforePreparing() && order.paymentStatus != PaymentStatus.COMPLETED) {
            _uiState.value = OrdersUiState.ActionError(
                "No se puede iniciar elaboracion hasta confirmar el pago."
            )
            return
        }

        updateOrderStatus(
            orderId = orderId,
            newStatus = OrderStatus.PREPARING,
            message = "El negocio inicio la elaboracion"
        )
    }

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

    fun cancelOrder(orderId: String, reason: String = "Cancelado por el negocio") {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            repository.cancelOrder(orderId, reason)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.value = OrdersUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al cancelar pedido")
                }
        }
    }

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

    fun enterEditMode(order: Order) {
        val canEditItems = order.canBusinessModifyItems()
        if (!canEditItems) return
        _modificationState.value = OrderModificationState.fromItems(
            items = order.items,
            originalTotal = order.total
        )
    }

    fun exitEditMode() {
        _modificationState.value = null
    }

    fun modifyItemQuantity(itemId: String, newQuantity: Int) {
        val state = _modificationState.value ?: return
        val quantity = newQuantity.coerceAtLeast(1)

        val updatedItems = state.modifiedItems.map { item ->
            if (item.itemId == itemId) item.copy(quantity = quantity) else item
        }

        updateModificationState(state, updatedItems)
    }

    fun removeItem(itemId: String) {
        val state = _modificationState.value ?: return
        val updatedItems = state.modifiedItems.filterNot { it.itemId == itemId }
        updateModificationState(state, updatedItems)
    }

    fun addItem(productId: String, name: String, price: Double, quantity: Int, imageUrl: String) {
        val state = _modificationState.value ?: return
        val safeQuantity = quantity.coerceAtLeast(1)
        val updatedItems = state.modifiedItems.toMutableList()
        val existingItemIndex = updatedItems.indexOfFirst { item ->
            item.itemType.equals("PRODUCT", ignoreCase = true) && item.productId == productId
        }

        if (existingItemIndex >= 0) {
            val existingItem = updatedItems[existingItemIndex]
            updatedItems[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + safeQuantity,
                wasModifiedByStore = true
            )
            updateModificationState(state, updatedItems)
            return
        }

        val newItem = OrderItem(
            itemId = "new_${productId}_${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
            itemType = "PRODUCT",
            productId = productId,
            name = name,
            price = price,
            basePrice = price,
            finalPrice = price,
            quantity = safeQuantity,
            imageUrl = imageUrl,
            wasModifiedByStore = true
        )

        updateModificationState(state, updatedItems + newItem)
    }

    fun cancelEdit() {
        _modificationState.value = null
    }

    fun loadComboDefinition(comboId: String) {
        if (_comboDefinitions.value.containsKey(comboId)) return
        if (_loadingComboIds.value.contains(comboId)) return
        _loadingComboIds.update { it + comboId }
        viewModelScope.launch {
            when (val result = comboRepository.getCombo(comboId)) {
                is CombosResult.Success -> {
                    val combo = result.combos.firstOrNull()
                    if (combo != null) {
                        _comboDefinitions.update { it + (comboId to combo) }
                    }
                }
                else -> Unit
            }
            _loadingComboIds.update { it - comboId }
        }
    }

    fun updateComboItemSelections(itemId: String, newSelections: List<OrderComboSelection>) {
        val state = _modificationState.value ?: return
        val updatedItems = state.modifiedItems.map { item ->
            if (item.itemId == itemId) item.copy(comboSelections = newSelections, wasModifiedByStore = true) else item
        }
        updateModificationState(state, updatedItems)
    }

    fun applyModification(orderId: String, reason: String = "Modificado por el negocio") {
        val state = _modificationState.value ?: return
        val currentOrder = getOrderById(orderId)
        if (currentOrder != null && !currentOrder.canBusinessModifyItems()) {
            _modificationState.value = null
            _uiState.value = OrdersUiState.ActionError(
                "Este pedido ya no permite modificar items en el flujo del negocio."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            val itemInputs = state.modifiedItems.map { item ->
                OrderItemInput(
                    quantity = item.quantity,
                    itemType = item.itemType,
                    productId = if (item.isCombo) null else item.productId,
                    comboId = if (item.isCombo) item.comboId ?: item.itemId else null,
                    comboSelections = item.comboSelections.map { slot ->
                        com.llego.business.orders.data.repository.OrderComboSlotSelectionInput(
                            slotId = slot.slotId,
                            selectedOptions = slot.selectedOptions.map { option ->
                                com.llego.business.orders.data.repository.OrderComboSelectedOptionInput(
                                    productId = option.productId,
                                    quantity = option.quantity,
                                    modifiers = option.modifiers.map { modifier ->
                                        com.llego.business.orders.data.repository.OrderComboModifierInput(
                                            name = modifier.name
                                        )
                                    }
                                )
                            }
                        )
                    },
                    showcaseId = if (item.isShowcase) item.itemId else null,
                    description = item.requestDescription
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
        val newTotal = items.sumOf { it.finalPrice * it.quantity }
        val hasChanges = items != state.originalItems

        _modificationState.value = state.copy(
            modifiedItems = items,
            hasChanges = hasChanges,
            newTotal = newTotal
        )
    }

    fun addComment(orderId: String, message: String) {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return

        val optimisticCommentId = "local_${kotlin.time.Clock.System.now().toEpochMilliseconds()}"
        val optimisticComment = OrderComment(
            id = optimisticCommentId,
            author = OrderActor.BUSINESS,
            message = trimmed,
            timestamp = kotlin.time.Clock.System.now().toString()
        )

        registerPendingBusinessComment(orderId, trimmed)
        appendOptimisticComment(orderId, optimisticComment)

        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            repository.addOrderComment(orderId, trimmed)
                .onSuccess { updatedOrder ->
                    updateOrderInList(updatedOrder)
                    _uiState.value = OrdersUiState.Success
                    refreshOrderById(orderId)
                }
                .onFailure { error ->
                    unregisterPendingBusinessComment(orderId, trimmed)
                    removeOptimisticComment(orderId, optimisticCommentId)
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al agregar comentario")
                }
        }
    }

    private fun appendOptimisticComment(orderId: String, comment: OrderComment) {
        _orders.update { currentOrders ->
            currentOrders.map { order ->
                if (order.id == orderId) {
                    order.copy(comments = listOf(comment) + order.comments)
                } else {
                    order
                }
            }
        }
    }

    private fun removeOptimisticComment(orderId: String, commentId: String) {
        _orders.update { currentOrders ->
            currentOrders.map { order ->
                if (order.id == orderId) {
                    order.copy(comments = order.comments.filterNot { it.id == commentId })
                } else {
                    order
                }
            }
        }
    }

    private fun refreshOrderById(orderId: String) {
        viewModelScope.launch {
            repository.getOrder(orderId)
                .onSuccess { freshOrder ->
                    if (freshOrder != null) {
                        updateOrderInList(freshOrder)
                    }
                }
        }
    }

    fun refreshOrderDetail(orderId: String, branchId: String) {
        _isRefreshing.value = true
        viewModelScope.launch {
            repository.getOrder(orderId)
                .onSuccess { freshOrder ->
                    if (freshOrder != null) {
                        updateOrderInList(freshOrder)

                        val shouldLoadPayment = freshOrder.status in setOf(
                            OrderStatus.PENDING_PAYMENT,
                            OrderStatus.PAYMENT_IN_PROGRESS
                        ) && freshOrder.paymentStatus != PaymentStatus.COMPLETED
                        if (shouldLoadPayment) {
                            loadActivePaymentAttempt(orderId)
                        } else {
                            clearActivePaymentAttempt()
                        }

                        val merchantId = freshOrder.businessId.takeIf { it.isNotBlank() }
                        val customerId = (freshOrder.customer?.id?.takeIf { it.isNotBlank() }
                            ?: freshOrder.customerId.takeIf { it.isNotBlank() })
                        if (!merchantId.isNullOrBlank() && !customerId.isNullOrBlank()) {
                            loadCustomerCashKycStatus(
                                merchantId = merchantId,
                                branchId = branchId.takeIf { it.isNotBlank() },
                                customerId = customerId
                            )
                        }
                    }
                }
            loadMenuItems(branchId, forceRefresh = true)
            _isRefreshing.value = false
        }
    }

    private fun registerPendingBusinessComment(orderId: String, message: String) {
        pendingBusinessCommentsByOrder.getOrPut(orderId) { mutableListOf() }.add(message.trim())
    }

    private fun unregisterPendingBusinessComment(orderId: String, message: String) {
        val pending = pendingBusinessCommentsByOrder[orderId] ?: return
        val index = pending.indexOfFirst { it == message.trim() }
        if (index >= 0) {
            pending.removeAt(index)
        }
        if (pending.isEmpty()) {
            pendingBusinessCommentsByOrder.remove(orderId)
        }
    }

    private fun normalizeIncomingComments(orderId: String, incomingComments: List<OrderComment>): List<OrderComment> {
        val pending = pendingBusinessCommentsByOrder[orderId] ?: return incomingComments
        if (pending.isEmpty()) return incomingComments

        val remaining = pending.toMutableList()
        val normalized = incomingComments.map { comment ->
            if (comment.author != OrderActor.CUSTOMER) {
                return@map comment
            }
            val index = remaining.indexOfFirst { it == comment.message.trim() }
            if (index >= 0) {
                remaining.removeAt(index)
                comment.copy(author = OrderActor.BUSINESS)
            } else {
                comment
            }
        }

        if (remaining.isEmpty()) {
            pendingBusinessCommentsByOrder.remove(orderId)
        } else {
            pendingBusinessCommentsByOrder[orderId] = remaining
        }
        return normalized
    }

    fun addOrderComment(orderId: String, message: String) = addComment(orderId, message)

    private fun updateOrderInList(updatedOrder: Order) {
        _orders.update { currentOrders ->
            var found = false
            val updated = currentOrders.map { existing ->
                if (existing.id == updatedOrder.id) {
                    found = true
                    mergeOrder(existing, updatedOrder)
                } else {
                    existing
                }
            }
            if (!found) listOf(updatedOrder) + updated else updated
        }
    }

    private fun mergeOrder(existing: Order, incoming: Order): Order {
        if (!incoming.isLikelyPartial()) return incoming

        val hasItemChanges = incoming.items.isNotEmpty()
        val hasMonetaryChanges = incoming.subtotal != 0.0 || incoming.deliveryFee != 0.0 || incoming.total != 0.0
        val hasStatusUpdate = incoming.updatedAt.isNotBlank() || incoming.lastStatusAt.isNotBlank() || incoming.timeline.isNotEmpty()
        val hasCommentUpdate = incoming.comments.isNotEmpty()
        val hasOperationalUpdate = hasStatusUpdate || incoming.paymentMethod.isNotBlank() || incoming.deliveryMode.isNotBlank()
        val shouldTakeIncomingStatus = hasStatusUpdate || incoming.status != OrderStatus.PENDING_ACCEPTANCE || existing.status == OrderStatus.PENDING_ACCEPTANCE

        return existing.copy(
            subtotal = if (hasItemChanges || hasMonetaryChanges) incoming.subtotal else existing.subtotal,
            deliveryFee = if (hasItemChanges || hasMonetaryChanges) incoming.deliveryFee else existing.deliveryFee,
            total = if (hasItemChanges || hasMonetaryChanges) incoming.total else existing.total,
            status = if (shouldTakeIncomingStatus) incoming.status else existing.status,
            deliveryMode = if (incoming.deliveryMode.isNotBlank()) incoming.deliveryMode else existing.deliveryMode,
            paymentMethod = if (hasOperationalUpdate && incoming.paymentMethod.isNotBlank()) incoming.paymentMethod else existing.paymentMethod,
            paymentStatus = if (hasOperationalUpdate && incoming.paymentMethod.isNotBlank()) incoming.paymentStatus else existing.paymentStatus,
            paidAt = if (hasOperationalUpdate) incoming.paidAt ?: existing.paidAt else existing.paidAt,
            deadlineAt = if (hasOperationalUpdate) incoming.deadlineAt ?: existing.deadlineAt else existing.deadlineAt,
            updatedAt = incoming.updatedAt.ifBlank { existing.updatedAt },
            lastStatusAt = incoming.lastStatusAt.ifBlank { existing.lastStatusAt },
            deliveryPersonId = incoming.deliveryPersonId ?: existing.deliveryPersonId,
            estimatedDeliveryTime = incoming.estimatedDeliveryTime ?: existing.estimatedDeliveryTime,
            items = if (hasItemChanges) incoming.items else existing.items,
            discounts = if (incoming.discounts.isNotEmpty()) incoming.discounts else existing.discounts,
            timeline = if (incoming.timeline.isNotEmpty()) incoming.timeline else existing.timeline,
            comments = if (hasCommentUpdate) normalizeIncomingComments(existing.id, incoming.comments) else existing.comments,
            isEditable = if (hasItemChanges || hasStatusUpdate) incoming.isEditable else existing.isEditable,
            canCancel = if (hasItemChanges || hasStatusUpdate) incoming.canCancel else existing.canCancel,
            estimatedMinutesRemaining = incoming.estimatedMinutesRemaining ?: existing.estimatedMinutesRemaining,
            estimatedMinutes = incoming.estimatedMinutes ?: existing.estimatedMinutes
        )
    }

    private fun Order.isLikelyPartial(): Boolean {
        return branchId.isBlank() || businessId.isBlank() || createdAt.isBlank() || currency.isBlank()
    }

    fun getOrderById(orderId: String): Order? {
        return _orders.value.firstOrNull { it.id == orderId }
    }

    fun getPendingOrdersCount(): Int {
        return _orders.value.count { it.status == OrderStatus.PENDING_ACCEPTANCE }
    }

    fun getActiveOrdersCount(): Int {
        return _orders.value.count {
            it.status !in listOf(
                OrderStatus.DELIVERED,
                OrderStatus.CANCELLED
            )
        }
    }

    fun clearError() {
        if (_uiState.value is OrdersUiState.Error || _uiState.value is OrdersUiState.ActionError) {
            _uiState.value = OrdersUiState.Success
        }
    }

    fun clearActionError() = clearError()

    fun loadActivePaymentAttempt(orderId: String) {
        viewModelScope.launch {
            repository.getActivePaymentAttempt(orderId)
                .onSuccess { attempt ->
                    _activePaymentAttempt.value = attempt
                }
                .onFailure {
                    _activePaymentAttempt.value = null
                }
        }
    }

    fun clearActivePaymentAttempt() {
        _activePaymentAttempt.value = null
    }

    fun loadCustomerCashKycStatus(
        merchantId: String,
        branchId: String?,
        customerId: String
    ) {
        viewModelScope.launch {
            repository.getCustomerCashKycStatus(
                merchantId = merchantId,
                branchId = branchId,
                customerId = customerId
            )
                .onSuccess { status ->
                    _customerCashKycStatus.value = status
                }
                .onFailure {
                    _customerCashKycStatus.value = null
                }
        }
    }

    fun clearCustomerCashKycStatus() {
        _customerCashKycStatus.value = null
    }

    fun confirmPaymentReceived(orderId: String, paymentAttemptId: String) {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.ActionInProgress(orderId)

            repository.confirmPaymentReceived(paymentAttemptId)
                .onSuccess { confirmedAttempt ->
                    _activePaymentAttempt.value = confirmedAttempt
                    refreshOrderById(orderId)
                    if (confirmedAttempt.status != PaymentAttemptStatus.AWAITING_BUSINESS) {
                        loadActivePaymentAttempt(orderId)
                    }
                    _uiState.value = OrdersUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = OrdersUiState.ActionError(error.message ?: "Error al confirmar pago")
                }
        }
    }

    fun loadMenuItems(branchId: String? = null, forceRefresh: Boolean = false) {
        val effectiveBranchId = branchId?.takeIf { it.isNotBlank() }
            ?: _currentBranchId.value?.takeIf { it.isNotBlank() }

        if (effectiveBranchId == null) {
            _menuItemsState.value = MenuItemsUiState()
            lastLoadedMenuBranchId = null
            return
        }

        val currentMenuState = _menuItemsState.value
        if (!forceRefresh &&
            effectiveBranchId == lastLoadedMenuBranchId &&
            currentMenuState.items.isNotEmpty() &&
            currentMenuState.error == null
        ) {
            return
        }

        viewModelScope.launch {
            _menuItemsState.value = if (effectiveBranchId == lastLoadedMenuBranchId) {
                currentMenuState.copy(isLoading = true, error = null)
            } else {
                MenuItemsUiState(isLoading = true)
            }

            val allProducts = mutableListOf<com.llego.shared.data.model.Product>()
            var cursor: String? = null
            var hasNextPage = true

            while (hasNextPage) {
                when (
                    val result = productRepository.getProducts(
                        branchId = effectiveBranchId,
                        availableOnly = true,
                        first = 100,
                        after = cursor
                    )
                ) {
                    is ProductsResult.Success -> {
                        allProducts += result.products
                        hasNextPage = result.hasNextPage && !result.endCursor.isNullOrBlank()
                        cursor = result.endCursor
                    }

                    is ProductsResult.Error -> {
                        _menuItemsState.value = MenuItemsUiState(
                            isLoading = false,
                            items = emptyList(),
                            error = result.message
                        )
                        return@launch
                    }

                    ProductsResult.Loading -> {
                        _menuItemsState.value = MenuItemsUiState(
                            isLoading = false,
                            items = emptyList(),
                            error = "No se pudieron cargar los productos"
                        )
                        return@launch
                    }
                }
            }

            val menuItems = allProducts
                .distinctBy { it.id }
                .map { it.toMenuItem() }
                .sortedBy { it.name.lowercase() }

            lastLoadedMenuBranchId = effectiveBranchId
            _menuItemsState.value = MenuItemsUiState(
                isLoading = false,
                items = menuItems,
                error = null
            )
        }
    }

    fun loadDashboardStats(
        businessId: String,
        fromDate: String,
        toDate: String,
        branchId: String? = null,
        forceRefresh: Boolean = false
    ) {
        if (businessId.isBlank()) {
            _dashboardStatsState.value = DashboardStatsUiState.Error("Negocio no seleccionado")
            return
        }

        val currentRequest = Triple(businessId, fromDate, branchId)
        if (!forceRefresh && lastDashboardRequest == currentRequest && _dashboardStatsState.value is DashboardStatsUiState.Success) {
            return
        }

        viewModelScope.launch {
            _dashboardStatsState.value = DashboardStatsUiState.Loading
            val result = repository.getDashboardStats(businessId, fromDate, toDate, branchId)
            result
                .onSuccess { stats ->
                    if (stats == null) {
                        _dashboardStatsState.value = DashboardStatsUiState.Error("No se recibieron estadisticas")
                    } else {
                        lastDashboardRequest = currentRequest
                        _dashboardStatsState.value = DashboardStatsUiState.Success(stats)
                    }
                }
                .onFailure { error ->
                    _dashboardStatsState.value = DashboardStatsUiState.Error(
                        error.message ?: "Error al obtener estadisticas"
                    )
                }
        }
    }
}

sealed class OrdersUiState {
    data object Loading : OrdersUiState()
    data object Success : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
    data class ActionInProgress(val orderId: String) : OrdersUiState()
    data class ActionError(val message: String) : OrdersUiState()
}

sealed class DashboardStatsUiState {
    data object Idle : DashboardStatsUiState()
    data object Loading : DashboardStatsUiState()
    data class Success(val stats: DashboardStats) : DashboardStatsUiState()
    data class Error(val message: String) : DashboardStatsUiState()
}

data class MenuItemsUiState(
    val isLoading: Boolean = false,
    val items: List<MenuItem> = emptyList(),
    val error: String? = null
)

enum class DateRangeFilter(val displayName: String) {
    TODAY("Hoy"),
    YESTERDAY("Ayer"),
    LAST_WEEK("Ultima semana"),
    LAST_MONTH("Ultimo mes"),
    CUSTOM("Seleccionar"),
    ALL("Todos");

    fun getDateRange(): Pair<String, String>? {
        val now = Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())
        val timezone = TimeZone.currentSystemDefault()
        val today: LocalDate = now.toLocalDateTime(timezone).date

        fun LocalDate.toIsoDateTimeStart(): String = "${this}T00:00:00Z"
        fun LocalDate.toIsoDateTimeEnd(): String = "${this}T23:59:59Z"

        return when (this) {
            TODAY -> Pair(today.toIsoDateTimeStart(), today.toIsoDateTimeEnd())
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
            CUSTOM -> null
            ALL -> null
        }
    }
}
