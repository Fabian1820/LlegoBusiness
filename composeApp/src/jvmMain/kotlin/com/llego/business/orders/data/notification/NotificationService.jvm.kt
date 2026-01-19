package com.llego.business.orders.data.notification

import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.data.subscription.NewOrderEvent
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.Toolkit
import javax.swing.JOptionPane

/**
 * Implementación JVM/Desktop del servicio de notificaciones para pedidos
 * 
 * Utiliza SystemTray para notificaciones de escritorio cuando está disponible.
 * En caso contrario, usa logging como fallback.
 * 
 * Requirements: 4.1, 4.2, 4.4, 4.6
 */
class JvmNotificationService : NotificationService {

    private var trayIcon: TrayIcon? = null
    private var badgeCount: Int = 0
    
    init {
        setupSystemTray()
    }

    /**
     * Configura el SystemTray si está disponible
     */
    private fun setupSystemTray() {
        if (SystemTray.isSupported()) {
            try {
                val tray = SystemTray.getSystemTray()
                val image = Toolkit.getDefaultToolkit().createImage(
                    javaClass.getResource("/icon.png") ?: return
                )
                trayIcon = TrayIcon(image, "Ya Llego Business").apply {
                    isImageAutoSize = true
                }
                tray.add(trayIcon)
                println("✅ JvmNotificationService: SystemTray initialized")
            } catch (e: Exception) {
                println("⚠️ JvmNotificationService: Could not initialize SystemTray - ${e.message}")
            }
        } else {
            println("⚠️ JvmNotificationService: SystemTray not supported")
        }
    }

    /**
     * Muestra notificación de nuevo pedido
     * 
     * Requirements: 4.1, 4.2
     */
    override fun showNewOrderNotification(event: NewOrderEvent) {
        val order = event.order
        
        val title = "🛒 Nuevo Pedido #${order.orderNumber}"
        val message = if (event.isActiveBranch) {
            "Total: ${order.currency} ${formatCurrency(order.total)}"
        } else {
            val branchName = order.branch?.name ?: "otra sucursal"
            "Pedido en $branchName - Total: ${order.currency} ${formatCurrency(order.total)}"
        }
        
        showNotification(title, message, TrayIcon.MessageType.INFO)
        incrementBadgeCount()
        
        println("📱 JvmNotificationService: NEW_ORDER_NOTIFICATION")
        println("   Title: $title")
        println("   Message: $message")
        println("   BranchId: ${event.branchId}")
        println("   OrderId: ${order.id}")
    }

    /**
     * Muestra notificación de actualización de estado
     * 
     * Requirements: 4.1
     */
    override fun showOrderUpdateNotification(orderId: String, status: OrderStatus) {
        val statusText = status.getDisplayName()
        val title = "📦 Pedido Actualizado"
        val message = "Estado: $statusText"
        
        showNotification(title, message, TrayIcon.MessageType.INFO)
        
        println("📱 JvmNotificationService: ORDER_UPDATE_NOTIFICATION")
        println("   Title: $title")
        println("   Message: $message")
        println("   OrderId: $orderId")
    }

    /**
     * Actualiza el badge de la app
     * 
     * Requirements: 4.6
     */
    override fun updateBadgeCount(pendingCount: Int) {
        badgeCount = pendingCount
        // En desktop, el badge se puede mostrar en el tooltip del tray icon
        trayIcon?.toolTip = if (pendingCount > 0) {
            "Ya Llego Business ($pendingCount pendientes)"
        } else {
            "Ya Llego Business"
        }
        println("📱 JvmNotificationService: Badge count updated to $pendingCount")
    }

    /**
     * Reproduce sonido de nuevo pedido
     * 
     * Requirements: 4.4
     */
    override fun playNewOrderSound() {
        try {
            Toolkit.getDefaultToolkit().beep()
            println("🔔 JvmNotificationService: Sound played")
        } catch (e: Exception) {
            println("❌ JvmNotificationService: Could not play sound - ${e.message}")
        }
    }

    /**
     * Solicita permisos de notificación
     * En desktop, los permisos generalmente están disponibles
     */
    override fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        // En desktop, las notificaciones generalmente no requieren permisos explícitos
        val hasPermission = SystemTray.isSupported()
        onResult(hasPermission)
    }

    /**
     * Verifica si hay permisos de notificación
     */
    override fun hasNotificationPermission(): Boolean {
        return SystemTray.isSupported()
    }

    /**
     * Cancela todas las notificaciones de pedidos
     */
    override fun cancelAllOrderNotifications() {
        // En desktop, las notificaciones del system tray se cierran automáticamente
        resetBadgeCount()
        println("🗑️ JvmNotificationService: All notifications cancelled")
    }

    /**
     * Cancela notificación de un pedido específico
     */
    override fun cancelOrderNotification(orderId: String) {
        // En desktop, no hay forma directa de cancelar notificaciones específicas
        println("🗑️ JvmNotificationService: Cancel notification for order $orderId (no-op on desktop)")
    }

    /**
     * Muestra una notificación usando SystemTray
     */
    private fun showNotification(title: String, message: String, type: TrayIcon.MessageType) {
        trayIcon?.displayMessage(title, message, type)
    }

    /**
     * Incrementa el contador de badge
     */
    private fun incrementBadgeCount() {
        updateBadgeCount(badgeCount + 1)
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
        return String.format("%.2f", value)
    }
}

/**
 * Factory para crear instancias de NotificationService en JVM/Desktop
 */
actual object NotificationServiceFactory {
    actual fun create(): NotificationService = JvmNotificationService()
}
