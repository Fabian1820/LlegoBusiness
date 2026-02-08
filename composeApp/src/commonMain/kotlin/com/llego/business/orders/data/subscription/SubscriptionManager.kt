package com.llego.business.orders.data.subscription

import com.apollographql.apollo.ApolloClient
import com.llego.business.orders.data.mappers.toDomain
import com.llego.business.orders.data.model.Order
import com.llego.business.orders.data.model.OrderStatus
import com.llego.multiplatform.graphql.NewBranchOrderSubscription
import com.llego.multiplatform.graphql.BranchOrderUpdatedSubscription
import com.llego.shared.data.network.GraphQLClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.pow

/**
 * Evento de nuevo pedido recibido via suscripciÃ³n
 *
 * @param order El pedido recibido
 * @param branchId ID de la sucursal donde llegÃ³ el pedido
 * @param isActiveBranch Si es la sucursal actualmente activa
 *
 * Requirements: 3.1, 4.1, 4.2
 */
data class NewOrderEvent(
    val order: Order,
    val branchId: String,
    val isActiveBranch: Boolean
)

/**
 * Evento de actualizaciÃ³n de pedido recibido via suscripciÃ³n
 *
 * @param orderId ID del pedido actualizado
 * @param branchId ID de la sucursal del pedido
 * @param newStatus Nuevo estado del pedido
 * @param updatedAt Timestamp de la actualizaciÃ³n
 * @param order Pedido completo actualizado (opcional)
 *
 * Requirements: 3.2, 3.4
 */
data class OrderUpdateEvent(
    val orderId: String,
    val branchId: String,
    val newStatus: OrderStatus,
    val updatedAt: String,
    val order: Order? = null
)

/**
 * Estado de conexiÃ³n de las suscripciones
 */
enum class SubscriptionConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING
}

/**
 * Gestor de suscripciones GraphQL para pedidos en tiempo real
 *
 * Mantiene suscripciones activas para todas las sucursales del negocio,
 * permitiendo recibir notificaciones de nuevos pedidos y actualizaciones
 * en cualquier sucursal.
 *
 * Requirements: 3.1, 3.2, 3.5, 3.6, 3.7
 */
class SubscriptionManager(
    private val apolloClient: ApolloClient = GraphQLClient.apolloClient
) {
    // Scope para las coroutines de suscripciÃ³n
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Mapa de suscripciones activas por branchId
    // Key format: "new_{branchId}" o "update_{branchId}"
    private val activeSubscriptions = mutableMapOf<String, Job>()

    // ID de la sucursal actualmente activa
    private var activeBranchId: String? = null

    // Lista de todas las sucursales suscritas
    private val subscribedBranchIds = mutableSetOf<String>()

    // SharedFlow para nuevos pedidos
    private val _newOrders = MutableSharedFlow<NewOrderEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val newOrders: SharedFlow<NewOrderEvent> = _newOrders.asSharedFlow()

    // SharedFlow para actualizaciones de pedidos
    private val _orderUpdates = MutableSharedFlow<OrderUpdateEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val orderUpdates: SharedFlow<OrderUpdateEvent> = _orderUpdates.asSharedFlow()

    // Estado de conexiÃ³n
    private val _connectionState = MutableSharedFlow<SubscriptionConnectionState>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val connectionState: SharedFlow<SubscriptionConnectionState> = _connectionState.asSharedFlow()

    // ConfiguraciÃ³n de reconexiÃ³n
    private val initialRetryDelayMs = 1000L
    private val maxRetryDelayMs = 30000L
    private val maxRetries = 10


    init {
        // Emitir estado inicial
        scope.launch {
            _connectionState.emit(SubscriptionConnectionState.DISCONNECTED)
        }
    }

    /**
     * Suscribe a todas las sucursales proporcionadas
     *
     * Cancela suscripciones existentes y crea nuevas para cada sucursal.
     * Esto permite recibir notificaciones de pedidos en cualquier sucursal
     * del negocio.
     *
     * @param branchIds Lista de IDs de sucursales a suscribir
     * @param activeBranchId ID de la sucursal actualmente activa
     *
     * Requirements: 3.7
     */
    fun subscribeToAllBranches(branchIds: List<String>, activeBranchId: String) {
        // Cancelar suscripciones existentes
        cancelAllSubscriptions()

        // Actualizar estado
        this.activeBranchId = activeBranchId
        subscribedBranchIds.clear()
        subscribedBranchIds.addAll(branchIds)

        // Crear suscripciones para cada sucursal
        branchIds.forEach { branchId ->
            val isActive = branchId == activeBranchId
            subscribeToNewOrders(branchId, isActive)
            subscribeToOrderUpdates(branchId)
        }

        scope.launch {
            _connectionState.emit(SubscriptionConnectionState.CONNECTING)
        }
    }

    /**
     * Actualiza la sucursal activa
     *
     * Cambia el flag de sucursal activa para que las notificaciones
     * se muestren correctamente segÃºn si el pedido es de la sucursal
     * activa o no.
     *
     * @param newActiveBranchId ID de la nueva sucursal activa
     *
     * Requirements: 3.5
     */
    fun updateActiveBranch(newActiveBranchId: String) {
        activeBranchId = newActiveBranchId
    }

    /**
     * Suscribe a nuevos pedidos de una sucursal especÃ­fica
     *
     * @param branchId ID de la sucursal
     *
     * Requirements: 3.1
     */
    @Suppress("UNUSED_PARAMETER")
    private fun subscribeToNewOrders(branchId: String, isActiveBranch: Boolean) {
        val subscriptionKey = "new_$branchId"

        // Cancelar suscripciÃ³n existente si hay
        activeSubscriptions[subscriptionKey]?.cancel()

        val job = scope.launch {
            var retryCount = 0

            while (true) {
                try {
                    apolloClient.subscription(
                        NewBranchOrderSubscription(branchId = branchId)
                    ).toFlow()
                        .catch { e ->
                            throw e
                        }
                        .collect { response ->
                            // ConexiÃ³n exitosa, resetear contador de reintentos
                            retryCount = 0
                            _connectionState.emit(SubscriptionConnectionState.CONNECTED)

                            response.data?.newBranchOrder?.let { order ->
                                val domainOrder = order.toDomain()
                                val event = NewOrderEvent(
                                    order = domainOrder,
                                    branchId = branchId,
                                    isActiveBranch = branchId == activeBranchId
                                )
                                _newOrders.emit(event)
                            }
                        }
                } catch (e: Exception) {
                    // Manejar reconexiÃ³n con backoff exponencial
                    retryCount++
                    if (retryCount > maxRetries) {
                        break
                    }

                    _connectionState.emit(SubscriptionConnectionState.RECONNECTING)
                    val delayMs = calculateBackoffDelay(retryCount)
                    delay(delayMs)
                }
            }
        }

        activeSubscriptions[subscriptionKey] = job
    }

    /**
     * Suscribe a actualizaciones de pedidos de una sucursal especÃ­fica
     *
     * @param branchId ID de la sucursal
     *
     * Requirements: 3.2
     */
    private fun subscribeToOrderUpdates(branchId: String) {
        val subscriptionKey = "update_$branchId"

        // Cancelar suscripciÃ³n existente si hay
        activeSubscriptions[subscriptionKey]?.cancel()

        val job = scope.launch {
            var retryCount = 0

            while (true) {
                try {
                    apolloClient.subscription(
                        BranchOrderUpdatedSubscription(branchId = branchId)
                    ).toFlow()
                        .catch { e ->
                            throw e
                        }
                        .collect { response ->
                            // ConexiÃ³n exitosa, resetear contador de reintentos
                            retryCount = 0
                            _connectionState.emit(SubscriptionConnectionState.CONNECTED)

                            response.data?.branchOrderUpdated?.let { order ->
                                val domainOrder = order.toDomain()
                                val event = OrderUpdateEvent(
                                    orderId = domainOrder.id,
                                    branchId = branchId,
                                    newStatus = domainOrder.status,
                                    updatedAt = domainOrder.updatedAt,
                                    order = domainOrder
                                )
                                _orderUpdates.emit(event)
                            }
                        }
                } catch (e: Exception) {
                    // Manejar reconexiÃ³n con backoff exponencial
                    retryCount++
                    if (retryCount > maxRetries) {
                        break
                    }

                    _connectionState.emit(SubscriptionConnectionState.RECONNECTING)
                    val delayMs = calculateBackoffDelay(retryCount)
                    delay(delayMs)
                }
            }
        }

        activeSubscriptions[subscriptionKey] = job
    }


    /**
     * Calcula el delay de backoff exponencial para reconexiÃ³n
     *
     * @param retryCount NÃºmero de intento actual
     * @return Delay en milisegundos
     *
     * Requirements: 3.6
     */
    private fun calculateBackoffDelay(retryCount: Int): Long {
        val exponentialDelay = initialRetryDelayMs * 2.0.pow(retryCount - 1).toLong()
        return min(exponentialDelay, maxRetryDelayMs)
    }

    /**
     * Cancela todas las suscripciones activas
     */
    fun cancelAllSubscriptions() {
        activeSubscriptions.values.forEach { job ->
            job.cancel()
        }
        activeSubscriptions.clear()
        subscribedBranchIds.clear()

        scope.launch {
            _connectionState.emit(SubscriptionConnectionState.DISCONNECTED)
        }
    }

    /**
     * Cancela suscripciones de una sucursal especÃ­fica
     *
     * @param branchId ID de la sucursal
     */
    fun cancelSubscriptionsForBranch(branchId: String) {
        val newKey = "new_$branchId"
        val updateKey = "update_$branchId"

        activeSubscriptions[newKey]?.cancel()
        activeSubscriptions.remove(newKey)

        activeSubscriptions[updateKey]?.cancel()
        activeSubscriptions.remove(updateKey)

        subscribedBranchIds.remove(branchId)
    }

    /**
     * Obtiene el nÃºmero de suscripciones activas
     *
     * @return NÃºmero de suscripciones activas
     */
    fun getActiveSubscriptionCount(): Int = activeSubscriptions.size

    /**
     * Obtiene las sucursales actualmente suscritas
     *
     * @return Set de IDs de sucursales suscritas
     */
    fun getSubscribedBranchIds(): Set<String> = subscribedBranchIds.toSet()

    /**
     * Verifica si hay suscripciones activas para una sucursal
     *
     * @param branchId ID de la sucursal
     * @return true si hay suscripciones activas
     */
    fun hasActiveSubscriptions(branchId: String): Boolean {
        val newKey = "new_$branchId"
        val updateKey = "update_$branchId"
        return activeSubscriptions[newKey]?.isActive == true ||
               activeSubscriptions[updateKey]?.isActive == true
    }

    /**
     * Reconecta todas las suscripciones
     *
     * Ãštil cuando se detecta pÃ©rdida de conexiÃ³n a nivel de aplicaciÃ³n
     *
     * Requirements: 3.6
     */
    fun reconnectAll() {
        val branchIds = subscribedBranchIds.toList()
        val currentActiveBranch = activeBranchId

        if (branchIds.isNotEmpty() && currentActiveBranch != null) {
            subscribeToAllBranches(branchIds, currentActiveBranch)
        }
    }

    companion object {
        private val instance: SubscriptionManager by lazy { SubscriptionManager() }

        /**
         * Obtiene una instancia singleton del SubscriptionManager
         */
        fun getInstance(): SubscriptionManager = instance
    }
}
