package com.llego.business.orders.data.repository

import com.llego.business.orders.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Resultado de operaciones de pedidos con paginación
 */
sealed class OrdersResult {
    data class Success(
        val orders: List<Order>,
        val totalCount: Int,
        val hasMore: Boolean
    ) : OrdersResult()
    
    data class Error(val message: String) : OrdersResult()
}

/**
 * Input para modificar items de un pedido
 */
data class OrderItemInput(
    val productId: String,
    val quantity: Int
)

/**
 * Interfaz del repositorio de pedidos con backend GraphQL
 * 
 * Define métodos para queries, mutations y subscriptions
 * - Usa Result<T> para operaciones que pueden fallar
 * - Usa Flow<T> para subscriptions
 * 
 * Requirements: 2.1, 2.2, 2.6, 5.1, 5.2, 5.3, 5.4, 6.6, 7.3
 */
interface OrderRepository {
    
    // ==================== QUERIES ====================
    
    /**
     * Obtiene pedidos de una sucursal con filtros opcionales
     * 
     * @param branchId ID de la sucursal
     * @param status Filtro por estado del pedido (opcional)
     * @param fromDate Fecha inicial del rango (opcional, formato ISO)
     * @param toDate Fecha final del rango (opcional, formato ISO)
     * @param limit Límite de resultados (default: 50)
     * @param offset Offset para paginación (default: 0)
     * @return OrdersResult con lista de pedidos o error
     * 
     * Requirements: 2.1, 2.3, 2.4, 2.5
     */
    suspend fun getBranchOrders(
        branchId: String,
        status: OrderStatus? = null,
        fromDate: String? = null,
        toDate: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): OrdersResult
    
    /**
     * Obtiene pedidos pendientes de una sucursal
     * Solo retorna pedidos que requieren acción inmediata
     * 
     * @param branchId ID de la sucursal
     * @return Lista de pedidos pendientes
     * 
     * Requirements: 2.2
     */
    suspend fun getPendingBranchOrders(branchId: String): Result<List<Order>>
    
    /**
     * Obtiene un pedido específico por ID
     * 
     * @param orderId ID del pedido
     * @return Pedido o null si no existe
     * 
     * Requirements: 2.6
     */
    suspend fun getOrder(orderId: String): Result<Order?>
    
    /**
     * Obtiene estadísticas de pedidos
     * 
     * @param fromDate Fecha inicial del rango (formato ISO)
     * @param toDate Fecha final del rango (formato ISO)
     * @param branchId ID de la sucursal (opcional, si no se pasa retorna stats del negocio)
     * @return Estadísticas de pedidos o null si hay error
     * 
     * Requirements: 11.1
     */
    suspend fun getOrderStats(
        fromDate: String,
        toDate: String,
        branchId: String? = null
    ): Result<OrderStats?>
    
    // ==================== MUTATIONS ====================
    
    /**
     * Acepta un pedido pendiente
     * 
     * @param orderId ID del pedido
     * @param estimatedMinutes Tiempo estimado de preparación en minutos
     * @return Pedido actualizado o error
     * 
     * Requirements: 5.1
     */
    suspend fun acceptOrder(orderId: String, estimatedMinutes: Int): Result<Order>
    
    /**
     * Rechaza un pedido pendiente
     * 
     * @param orderId ID del pedido
     * @param reason Razón del rechazo
     * @return Pedido actualizado o error
     * 
     * Requirements: 5.2
     */
    suspend fun rejectOrder(orderId: String, reason: String): Result<Order>
    
    /**
     * Actualiza el estado de un pedido
     * 
     * @param orderId ID del pedido
     * @param status Nuevo estado
     * @param message Mensaje opcional para el timeline
     * @return Pedido actualizado o error
     * 
     * Requirements: 5.3
     */
    suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus,
        message: String? = null
    ): Result<Order>
    
    /**
     * Marca un pedido como listo para recoger
     * 
     * @param orderId ID del pedido
     * @return Pedido actualizado o error
     * 
     * Requirements: 5.4
     */
    suspend fun markOrderReady(orderId: String): Result<Order>
    
    /**
     * Modifica los items de un pedido
     * 
     * @param orderId ID del pedido
     * @param items Lista de items actualizados
     * @param reason Razón de la modificación
     * @return Pedido actualizado o error
     * 
     * Requirements: 6.6
     */
    suspend fun modifyOrderItems(
        orderId: String,
        items: List<OrderItemInput>,
        reason: String
    ): Result<Order>
    
    /**
     * Agrega un comentario a un pedido
     * 
     * @param orderId ID del pedido
     * @param message Contenido del comentario
     * @return Pedido actualizado o error
     * 
     * Requirements: 7.3
     */
    suspend fun addOrderComment(orderId: String, message: String): Result<Order>
    
    // ==================== SUBSCRIPTIONS ====================
    
    /**
     * Suscripción a nuevos pedidos de una sucursal
     * 
     * @param branchId ID de la sucursal
     * @return Flow que emite nuevos pedidos
     * 
     * Requirements: 3.1
     */
    fun subscribeToNewOrders(branchId: String): Flow<Order>
    
    /**
     * Suscripción a actualizaciones de pedidos de una sucursal
     * 
     * @param branchId ID de la sucursal
     * @return Flow que emite pedidos actualizados
     * 
     * Requirements: 3.2
     */
    fun subscribeToOrderUpdates(branchId: String): Flow<Order>
}


