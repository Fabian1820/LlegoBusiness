package com.llego.business.orders.data.notification

import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.data.subscription.NewOrderEvent

/**
 * Implementación iOS del servicio de notificaciones para pedidos
 *
 * Esta implementación proporciona la estructura base para notificaciones.
 * La integración completa con UNUserNotificationCenter requiere configuración
 * adicional en el proyecto Xcode (capabilities y entitlements).
 *
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 12.1, 12.2
 */
class IosNotificationService : NotificationService {

    private val branchSwitchHandler = BranchSwitchHandler.getInstance()

    // Estado local para permisos y badge (en producción usar NSUserDefaults via bridge)
    private var notificationPermissionGranted = false
    private var currentBadgeCount = 0

    companion object {
        // Category identifiers (para uso futuro con UNUserNotificationCenter)
        const val CATEGORY_NEW_ORDER = "NEW_ORDER_CATEGORY"
        const val CATEGORY_NEW_ORDER_OTHER_BRANCH = "NEW_ORDER_OTHER_BRANCH_CATEGORY"
        const val CATEGORY_ORDER_UPDATE = "ORDER_UPDATE_CATEGORY"

        // Action identifiers
        const val ACTION_SWITCH_BRANCH = "SWITCH_BRANCH_ACTION"
        const val ACTION_VIEW_ORDER = "VIEW_ORDER_ACTION"

        // UserInfo keys
        const val KEY_BRANCH_ID = "branchId"
        const val KEY_ORDER_ID = "orderId"
        const val KEY_BRANCH_NAME = "branchName"
    }

    /**
     * Muestra notificación de nuevo pedido
     *
     * Requirements: 4.1, 4.2, 4.3
     */
    override fun showNewOrderNotification(event: NewOrderEvent) {
        val order = event.order

        val notificationTitle = "🛒 Nuevo Pedido #${order.orderNumber}"
        val notificationBody = if (event.isActiveBranch) {
            "Total: ${order.currency} ${formatCurrency(order.total)}"
        } else {
            val branchName = order.branch?.name ?: "otra sucursal"
            "Pedido en $branchName - Total: ${order.currency} ${formatCurrency(order.total)}"
        }

        // Log para debugging y para que el bridge Swift pueda interceptar
        println("📱 IosNotificationService: NEW_ORDER_NOTIFICATION")
        println("   Title: $notificationTitle")
        println("   Body: $notificationBody")
        println("   BranchId: ${event.branchId}")
        println("   OrderId: ${order.id}")
        println("   IsActiveBranch: ${event.isActiveBranch}")

        // Incrementar badge count
        incrementBadgeCount()

        // Notificar al sistema (el bridge Swift debe escuchar estos eventos)
        NotificationBridge.onNewOrderNotification?.invoke(
            NotificationData(
                id = "new_order_${order.id}",
                title = notificationTitle,
                body = notificationBody,
                category = if (event.isActiveBranch) CATEGORY_NEW_ORDER else CATEGORY_NEW_ORDER_OTHER_BRANCH,
                userInfo = mapOf(
                    KEY_BRANCH_ID to event.branchId,
                    KEY_ORDER_ID to order.id,
                    KEY_BRANCH_NAME to (order.branch?.name ?: "")
                )
            )
        )
    }

    /**
     * Muestra notificación de actualización de estado
     *
     * Requirements: 4.1
     */
    override fun showOrderUpdateNotification(orderId: String, status: OrderStatus) {
        val statusText = status.getDisplayName()
        val notificationTitle = "📦 Pedido Actualizado"
        val notificationBody = "Estado: $statusText"

        println("📱 IosNotificationService: ORDER_UPDATE_NOTIFICATION")
        println("   Title: $notificationTitle")
        println("   Body: $notificationBody")
        println("   OrderId: $orderId")
        println("   Status: $statusText")

        NotificationBridge.onOrderUpdateNotification?.invoke(
            NotificationData(
                id = "order_update_$orderId",
                title = notificationTitle,
                body = notificationBody,
                category = CATEGORY_ORDER_UPDATE,
                userInfo = mapOf(KEY_ORDER_ID to orderId)
            )
        )
    }

    /**
     * Actualiza el badge de la app
     *
     * Requirements: 4.6
     */
    override fun updateBadgeCount(pendingCount: Int) {
        currentBadgeCount = pendingCount
        println("📱 IosNotificationService: Badge count updated to $pendingCount")

        // Notificar al bridge Swift para actualizar el badge real
        NotificationBridge.onBadgeUpdate?.invoke(pendingCount)
    }

    /**
     * Reproduce sonido de nuevo pedido
     *
     * Requirements: 4.4
     */
    override fun playNewOrderSound() {
        println("🔔 IosNotificationService: Play sound requested")
        NotificationBridge.onPlaySound?.invoke()
    }

    /**
     * Solicita permisos de notificación
     */
    override fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        println("📱 IosNotificationService: Requesting notification permission")

        // Delegar al bridge Swift que tiene acceso a UNUserNotificationCenter
        NotificationBridge.requestPermission { granted ->
            notificationPermissionGranted = granted
            onResult(granted)
        }
    }

    /**
     * Verifica si hay permisos de notificación
     */
    override fun hasNotificationPermission(): Boolean {
        return notificationPermissionGranted
    }

    /**
     * Cancela todas las notificaciones de pedidos
     */
    override fun cancelAllOrderNotifications() {
        println("🗑️ IosNotificationService: Cancel all notifications requested")
        resetBadgeCount()
        NotificationBridge.onCancelAllNotifications?.invoke()
    }

    /**
     * Cancela notificación de un pedido específico
     */
    override fun cancelOrderNotification(orderId: String) {
        println("🗑️ IosNotificationService: Cancel notification for order $orderId")
        NotificationBridge.onCancelNotification?.invoke(orderId)
    }

    /**
     * Incrementa el contador de badge
     */
    private fun incrementBadgeCount() {
        updateBadgeCount(currentBadgeCount + 1)
    }

    /**
     * Resetea el contador de badge
     */
    private fun resetBadgeCount() {
        updateBadgeCount(0)
    }

    /**
     * Formatea un valor de moneda
     */
    private fun formatCurrency(value: Double): String {
        val intPart = value.toLong()
        val decPart = ((value - intPart) * 100).toLong()
        return "$intPart.${decPart.toString().padStart(2, '0')}"
    }
}

/**
 * Datos de notificación para el bridge Swift
 */
data class NotificationData(
    val id: String,
    val title: String,
    val body: String,
    val category: String,
    val userInfo: Map<String, String>
)

/**
 * Bridge para comunicación con código Swift nativo
 *
 * El código Swift debe configurar estos callbacks para manejar
 * las notificaciones reales usando UNUserNotificationCenter.
 *
 * Ejemplo de uso en Swift:
 * ```swift
 * NotificationBridge.shared.onNewOrderNotification = { data in
 *     // Crear y mostrar UNNotification
 * }
 * ```
 *
 * Requirements: 12.1, 12.2
 */
object NotificationBridge {
    var onNewOrderNotification: ((NotificationData) -> Unit)? = null
    var onOrderUpdateNotification: ((NotificationData) -> Unit)? = null
    var onBadgeUpdate: ((Int) -> Unit)? = null
    var onPlaySound: (() -> Unit)? = null
    var onCancelAllNotifications: (() -> Unit)? = null
    var onCancelNotification: ((String) -> Unit)? = null
    var requestPermission: ((callback: (Boolean) -> Unit) -> Unit) = { callback ->
        // Default: assume permission granted for development
        callback(true)
    }

    /**
     * Maneja la acción "Cambiar para ver" desde una notificación
     *
     * Este método debe ser llamado desde Swift cuando el usuario
     * toca la acción de cambio de sucursal en una notificación.
     *
     * @param branchId ID de la sucursal destino
     * @param orderId ID del pedido a mostrar
     * @param branchName Nombre de la sucursal (opcional)
     *
     * Requirements: 12.1, 12.2
     */
    fun handleBranchSwitchAction(branchId: String, orderId: String, branchName: String? = null) {
        println("📱 NotificationBridge: Branch switch action received")
        println("   BranchId: $branchId")
        println("   OrderId: $orderId")
        println("   BranchName: $branchName")

        BranchSwitchHandler.getInstance().handleBranchSwitchFromNotification(
            branchId = branchId,
            orderId = orderId,
            branchName = branchName
        )
    }
}

/**
 * Factory para crear instancias de NotificationService en iOS
 */
actual object NotificationServiceFactory {
    actual fun create(): NotificationService = IosNotificationService()
}
