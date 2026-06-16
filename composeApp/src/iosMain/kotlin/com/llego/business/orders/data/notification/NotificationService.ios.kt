@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.llego.business.orders.data.notification

import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.data.subscription.NewOrderEvent
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.Foundation.NSError
import platform.UIKit.UIApplication
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Implementación iOS del servicio de notificaciones.
 * Usa APIs nativas (UNUserNotificationCenter + AudioToolbox + UIKit) directamente
 * desde Kotlin/Native — no requiere código Swift adicional.
 *
 * NotificationBridge se conserva como mecanismo opcional de extensión si en el
 * futuro alguien quiere sobreescribir comportamientos desde Swift.
 */
class IosNotificationService : NotificationService {

    private val branchSwitchHandler = BranchSwitchHandler.getInstance()
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    // Cache del último estado conocido; hasNotificationPermission() es síncrono
    // pero las settings de iOS se consultan async — refrescamos cada vez que pedimos
    // o solicitamos autorización.
    private var notificationPermissionGranted = false
    private var currentBadgeCount = 0

    // Sonido del sistema "1007" = «new mail / notification» (sonoro + breve vibración).
    private val systemSoundNewMail: UInt = 1007u

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

        val notificationTitle = "Nuevo pedido #${order.orderNumber}"
        val notificationBody = if (event.isActiveBranch) {
            "Total: ${order.currency} ${formatCurrency(order.total)}"
        } else {
            val branchName = order.branch?.name ?: "otra sucursal"
            "Pedido en $branchName · ${order.currency} ${formatCurrency(order.total)}"
        }

        incrementBadgeCount()

        val content = UNMutableNotificationContent().apply {
            setTitle(notificationTitle)
            setBody(notificationBody)
            setSound(UNNotificationSound.defaultSound())
            setCategoryIdentifier(
                if (event.isActiveBranch) CATEGORY_NEW_ORDER else CATEGORY_NEW_ORDER_OTHER_BRANCH
            )
            setUserInfo(
                mapOf(
                    KEY_BRANCH_ID to event.branchId,
                    KEY_ORDER_ID to order.id,
                    KEY_BRANCH_NAME to (order.branch?.name ?: "")
                )
            )
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "new_order_${order.id}",
            content = content,
            trigger = null // disparar inmediatamente
        )

        center.addNotificationRequest(request) { _: NSError? -> }

        // Hook para extensión futura desde Swift (opcional)
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
        val notificationTitle = "Pedido actualizado"
        val notificationBody = "Estado: $statusText"

        val content = UNMutableNotificationContent().apply {
            setTitle(notificationTitle)
            setBody(notificationBody)
            setSound(UNNotificationSound.defaultSound())
            setCategoryIdentifier(CATEGORY_ORDER_UPDATE)
            setUserInfo(mapOf(KEY_ORDER_ID to orderId))
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "order_update_$orderId",
            content = content,
            trigger = null
        )
        center.addNotificationRequest(request) { _: NSError? -> }

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
        // El acceso a UIApplication.applicationIconBadgeNumber debe hacerse en main thread
        dispatch_async(dispatch_get_main_queue()) {
            UIApplication.sharedApplication.setApplicationIconBadgeNumber(pendingCount.toLong())
        }
        NotificationBridge.onBadgeUpdate?.invoke(pendingCount)
    }

    override fun playNewOrderSound() {
        // 1007 = sonido de "nueva notificación" (incluye vibración corta)
        AudioServicesPlaySystemSound(systemSoundNewMail)
        NotificationBridge.onPlaySound?.invoke()
    }

    override fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        center.requestAuthorizationWithOptions(options) { granted: Boolean, _: NSError? ->
            notificationPermissionGranted = granted
            onResult(granted)
        }
    }

    override fun hasNotificationPermission(): Boolean {
        // Refresca el cache de forma asíncrona; el valor devuelto puede estar un tick
        // por detrás del estado real, pero showNewOrderNotification es no-op sin perms
        // gracias a iOS (cae silente).
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            notificationPermissionGranted =
                status == UNAuthorizationStatusAuthorized ||
                status == UNAuthorizationStatusProvisional
        }
        return notificationPermissionGranted
    }

    override fun cancelAllOrderNotifications() {
        resetBadgeCount()
        center.removeAllDeliveredNotifications()
        center.removeAllPendingNotificationRequests()
        NotificationBridge.onCancelAllNotifications?.invoke()
    }

    override fun cancelOrderNotification(orderId: String) {
        val ids = listOf("new_order_$orderId", "order_update_$orderId")
        center.removePendingNotificationRequestsWithIdentifiers(ids)
        center.removeDeliveredNotificationsWithIdentifiers(ids)
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
