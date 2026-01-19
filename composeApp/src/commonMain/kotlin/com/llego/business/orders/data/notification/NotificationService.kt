package com.llego.business.orders.data.notification

import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.data.subscription.NewOrderEvent

/**
 * Servicio multiplataforma para gestionar notificaciones de pedidos
 * 
 * Maneja notificaciones locales y push para nuevos pedidos y actualizaciones,
 * incluyendo sonidos, badges y acciones de cambio de sucursal.
 * 
 * Requirements: 4.1, 4.2, 4.4, 4.6
 */
interface NotificationService {

    /**
     * Muestra una notificación de nuevo pedido
     * 
     * Si el pedido es de la sucursal activa, muestra notificación simple.
     * Si es de otra sucursal, incluye acción "Cambiar para ver".
     * 
     * @param event Evento de nuevo pedido con información del pedido y sucursal
     * 
     * Requirements: 4.1, 4.2
     */
    fun showNewOrderNotification(event: NewOrderEvent)

    /**
     * Muestra una notificación de actualización de estado de pedido
     * 
     * @param orderId ID del pedido actualizado
     * @param status Nuevo estado del pedido
     * 
     * Requirements: 4.1
     */
    fun showOrderUpdateNotification(orderId: String, status: OrderStatus)

    /**
     * Actualiza el contador de badge de la app
     * 
     * @param pendingCount Número de pedidos pendientes
     * 
     * Requirements: 4.6
     */
    fun updateBadgeCount(pendingCount: Int)

    /**
     * Reproduce el sonido distintivo de nuevo pedido
     * 
     * Requirements: 4.4
     */
    fun playNewOrderSound()

    /**
     * Solicita permisos de notificación al usuario
     * 
     * @param onResult Callback con el resultado (true si se concedieron permisos)
     */
    fun requestNotificationPermission(onResult: (Boolean) -> Unit)

    /**
     * Verifica si los permisos de notificación están concedidos
     * 
     * @return true si los permisos están concedidos
     */
    fun hasNotificationPermission(): Boolean

    /**
     * Cancela todas las notificaciones de pedidos
     */
    fun cancelAllOrderNotifications()

    /**
     * Cancela la notificación de un pedido específico
     * 
     * @param orderId ID del pedido cuya notificación se cancelará
     */
    fun cancelOrderNotification(orderId: String)
}

/**
 * Datos de notificación para cambio de sucursal
 * 
 * Contiene la información necesaria para cambiar de sucursal
 * cuando el usuario toca la acción "Cambiar para ver"
 * 
 * Requirements: 12.1
 */
data class BranchSwitchNotificationData(
    val branchId: String,
    val orderId: String,
    val branchName: String? = null
)

/**
 * Factory object para crear instancias de NotificationService
 * Cada plataforma implementa la creación de la instancia específica
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6
 */
expect object NotificationServiceFactory {
    /**
     * Crea una instancia del servicio de notificaciones
     * 
     * @return Instancia de NotificationService específica de la plataforma
     */
    fun create(): NotificationService
}

