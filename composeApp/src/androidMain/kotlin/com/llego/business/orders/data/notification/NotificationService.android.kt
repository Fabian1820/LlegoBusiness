package com.llego.business.orders.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.llego.business.orders.data.model.OrderStatus
import com.llego.business.orders.data.subscription.NewOrderEvent

/**
 * Implementación Android del servicio de notificaciones para pedidos
 * 
 * Utiliza NotificationChannel para Android 8.0+ y NotificationCompat
 * para compatibilidad con versiones anteriores.
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 12.1, 12.2
 */
class AndroidNotificationService(
    private val context: Context
) : NotificationService {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val branchSwitchHandler = BranchSwitchHandler.getInstance()
    private var branchSwitchReceiver: BroadcastReceiver? = null
    
    companion object {
        // Channel IDs
        const val CHANNEL_NEW_ORDERS = "new_orders_channel"
        const val CHANNEL_ORDER_UPDATES = "order_updates_channel"
        
        // Notification IDs base
        private const val NEW_ORDER_NOTIFICATION_BASE = 1000
        private const val ORDER_UPDATE_NOTIFICATION_BASE = 2000
        
        // Intent actions
        const val ACTION_SWITCH_BRANCH = "com.llego.business.ACTION_SWITCH_BRANCH"
        const val ACTION_VIEW_ORDER = "com.llego.business.ACTION_VIEW_ORDER"
        const val EXTRA_BRANCH_ID = "extra_branch_id"
        const val EXTRA_ORDER_ID = "extra_order_id"
        const val EXTRA_BRANCH_NAME = "extra_branch_name"
    }

    init {
        createNotificationChannels()
        registerBranchSwitchReceiver()
    }

    /**
     * Crea los canales de notificación para Android 8.0+
     * 
     * Requirements: 4.4 (sonido distintivo)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para nuevos pedidos - alta prioridad con sonido
            val newOrdersChannel = NotificationChannel(
                CHANNEL_NEW_ORDERS,
                "Nuevos Pedidos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos pedidos entrantes"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                
                // Configurar sonido de notificación
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            
            // Canal para actualizaciones de pedidos - prioridad media
            val orderUpdatesChannel = NotificationChannel(
                CHANNEL_ORDER_UPDATES,
                "Actualizaciones de Pedidos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de cambios de estado en pedidos"
                enableVibration(true)
            }
            
            manager.createNotificationChannel(newOrdersChannel)
            manager.createNotificationChannel(orderUpdatesChannel)
        }
    }

    /**
     * Registra el BroadcastReceiver para manejar la acción "Cambiar para ver"
     * 
     * Requirements: 12.1, 12.2
     */
    private fun registerBranchSwitchReceiver() {
        branchSwitchReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACTION_SWITCH_BRANCH) {
                    val branchId = intent.getStringExtra(EXTRA_BRANCH_ID) ?: return
                    val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: return
                    val branchName = intent.getStringExtra(EXTRA_BRANCH_NAME)
                    
                    println("📱 AndroidNotificationService: Branch switch action received")
                    println("   BranchId: $branchId")
                    println("   OrderId: $orderId")
                    println("   BranchName: $branchName")
                    
                    // Delegar al BranchSwitchHandler
                    branchSwitchHandler.handleBranchSwitchFromNotification(
                        branchId = branchId,
                        orderId = orderId,
                        branchName = branchName
                    )
                    
                    // Cancelar la notificación
                    cancelOrderNotification(orderId)
                    
                    // Abrir la app
                    openApp(branchId, orderId)
                }
            }
        }
        
        val filter = IntentFilter(ACTION_SWITCH_BRANCH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(branchSwitchReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(branchSwitchReceiver, filter)
        }
    }

    /**
     * Abre la app y navega al pedido
     */
    private fun openApp(branchId: String, orderId: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_BRANCH_ID, branchId)
            putExtra(EXTRA_ORDER_ID, orderId)
        }
        intent?.let { context.startActivity(it) }
    }


    /**
     * Muestra notificación de nuevo pedido
     * 
     * Requirements: 4.1, 4.2, 4.3
     */
    override fun showNewOrderNotification(event: NewOrderEvent) {
        if (!hasNotificationPermission()) {
            println("⚠️ AndroidNotificationService: No notification permission")
            return
        }

        val order = event.order
        val notificationId = NEW_ORDER_NOTIFICATION_BASE + order.orderNumber.hashCode()
        
        // Intent para abrir la app al tocar la notificación
        val contentIntent = createContentIntent(event.branchId, order.id)
        
        val builder = NotificationCompat.Builder(context, CHANNEL_NEW_ORDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Usar icono personalizado
            .setContentTitle("🛒 Nuevo Pedido #${order.orderNumber}")
            .setContentText("Total: ${order.currency} ${String.format("%.2f", order.total)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        
        // Si es de otra sucursal, agregar información y acción de cambio
        if (!event.isActiveBranch) {
            val branchName = order.branch?.name ?: "otra sucursal"
            builder.setContentText("Pedido en $branchName - Total: ${order.currency} ${String.format("%.2f", order.total)}")
            
            // Agregar acción "Cambiar para ver"
            val switchIntent = createSwitchBranchIntent(event.branchId, order.id, branchName)
            builder.addAction(
                android.R.drawable.ic_menu_directions,
                "Cambiar para ver",
                switchIntent
            )
        }
        
        // Agregar estilo expandido con más información
        val itemsText = order.items.take(3).joinToString("\n") { 
            "• ${it.quantity}x ${it.name}" 
        }
        val moreItems = if (order.items.size > 3) "\n... y ${order.items.size - 3} más" else ""
        
        builder.setStyle(
            NotificationCompat.BigTextStyle()
                .bigText("$itemsText$moreItems\n\nTotal: ${order.currency} ${String.format("%.2f", order.total)}")
        )
        
        try {
            notificationManager.notify(notificationId, builder.build())
            println("✅ AndroidNotificationService: Notification shown for order ${order.orderNumber}")
        } catch (e: SecurityException) {
            println("❌ AndroidNotificationService: SecurityException - ${e.message}")
        }
    }

    /**
     * Muestra notificación de actualización de estado
     * 
     * Requirements: 4.1
     */
    override fun showOrderUpdateNotification(orderId: String, status: OrderStatus) {
        if (!hasNotificationPermission()) {
            println("⚠️ AndroidNotificationService: No notification permission")
            return
        }

        val notificationId = ORDER_UPDATE_NOTIFICATION_BASE + orderId.hashCode()
        val statusText = status.getDisplayName()
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ORDER_UPDATES)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Usar icono personalizado
            .setContentTitle("📦 Pedido Actualizado")
            .setContentText("Estado: $statusText")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
        
        try {
            notificationManager.notify(notificationId, builder.build())
            println("✅ AndroidNotificationService: Update notification shown for order $orderId")
        } catch (e: SecurityException) {
            println("❌ AndroidNotificationService: SecurityException - ${e.message}")
        }
    }

    /**
     * Actualiza el badge de la app
     * 
     * Requirements: 4.6
     */
    override fun updateBadgeCount(pendingCount: Int) {
        // En Android, el badge se maneja automáticamente por el sistema
        // basado en las notificaciones activas. Algunos launchers soportan
        // ShortcutBadger pero no es universal.
        println("📱 AndroidNotificationService: Badge count updated to $pendingCount")
        
        // Para launchers que soporten badges, podemos usar la API de shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                // El badge se actualiza automáticamente basado en notificaciones
            } catch (e: Exception) {
                println("⚠️ AndroidNotificationService: Could not update badge - ${e.message}")
            }
        }
    }

    /**
     * Reproduce sonido de nuevo pedido
     * 
     * Requirements: 4.4
     */
    override fun playNewOrderSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone?.play()
            println("🔔 AndroidNotificationService: Sound played")
        } catch (e: Exception) {
            println("❌ AndroidNotificationService: Could not play sound - ${e.message}")
        }
    }

    /**
     * Solicita permisos de notificación (Android 13+)
     */
    override fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // En Android 13+, el permiso debe solicitarse en runtime
            // Esto debe manejarse desde la Activity
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            onResult(hasPermission)
        } else {
            // En versiones anteriores, el permiso se concede automáticamente
            onResult(true)
        }
    }

    /**
     * Verifica si hay permisos de notificación
     */
    override fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            notificationManager.areNotificationsEnabled()
        }
    }

    /**
     * Cancela todas las notificaciones de pedidos
     */
    override fun cancelAllOrderNotifications() {
        notificationManager.cancelAll()
        println("🗑️ AndroidNotificationService: All notifications cancelled")
    }

    /**
     * Cancela notificación de un pedido específico
     */
    override fun cancelOrderNotification(orderId: String) {
        val newOrderNotificationId = NEW_ORDER_NOTIFICATION_BASE + orderId.hashCode()
        val updateNotificationId = ORDER_UPDATE_NOTIFICATION_BASE + orderId.hashCode()
        
        notificationManager.cancel(newOrderNotificationId)
        notificationManager.cancel(updateNotificationId)
        println("🗑️ AndroidNotificationService: Notification cancelled for order $orderId")
    }

    /**
     * Crea intent para abrir la app al tocar la notificación
     */
    private fun createContentIntent(branchId: String, orderId: String): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_BRANCH_ID, branchId)
            putExtra(EXTRA_ORDER_ID, orderId)
        } ?: Intent()
        
        return PendingIntent.getActivity(
            context,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Crea intent para la acción "Cambiar para ver"
     * 
     * Requirements: 4.3, 12.1
     */
    private fun createSwitchBranchIntent(
        branchId: String,
        orderId: String,
        branchName: String
    ): PendingIntent {
        val intent = Intent(ACTION_SWITCH_BRANCH).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_BRANCH_ID, branchId)
            putExtra(EXTRA_ORDER_ID, orderId)
            putExtra(EXTRA_BRANCH_NAME, branchName)
        }
        
        return PendingIntent.getBroadcast(
            context,
            "$branchId$orderId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/**
 * Factory para crear instancias de NotificationService en Android
 * Requiere inicialización con Context antes de usar
 */
actual object NotificationServiceFactory {
    private var appContext: Context? = null
    
    /**
     * Inicializa el factory con el contexto de la aplicación
     * Debe llamarse una vez al inicio de la app (en Application o MainActivity)
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }
    
    actual fun create(): NotificationService {
        val context = appContext
            ?: throw IllegalStateException("NotificationServiceFactory no inicializado. Llama a initialize(context) primero.")
        return AndroidNotificationService(context)
    }
}

