package com.llego.business.orders.data.notification

import com.llego.shared.data.model.Branch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Evento de cambio de sucursal desde notificación
 * 
 * Contiene la información necesaria para cambiar de sucursal
 * y navegar al pedido correspondiente.
 * 
 * Requirements: 12.2, 12.3
 */
data class BranchSwitchEvent(
    val branchId: String,
    val orderId: String,
    val branchName: String? = null
)

/**
 * Resultado del cambio de sucursal
 * 
 * Requirements: 12.4
 */
sealed class BranchSwitchResult {
    data class Success(
        val previousBranchName: String?,
        val newBranchName: String,
        val orderId: String
    ) : BranchSwitchResult()
    
    data class Error(val message: String) : BranchSwitchResult()
    
    data object BranchNotFound : BranchSwitchResult()
}

/**
 * Handler para cambio rápido de sucursal desde notificaciones
 * 
 * Gestiona el flujo de cambio de sucursal cuando el usuario
 * toca la acción "Cambiar para ver" en una notificación de
 * pedido de otra sucursal.
 * 
 * Requirements: 12.2, 12.3
 */
class BranchSwitchHandler {
    
    // Flow para eventos de cambio de sucursal pendientes
    private val _pendingSwitchEvent = MutableStateFlow<BranchSwitchEvent?>(null)
    val pendingSwitchEvent: StateFlow<BranchSwitchEvent?> = _pendingSwitchEvent.asStateFlow()
    
    // Flow para resultados de cambio de sucursal (para mostrar confirmación)
    private val _switchResult = MutableSharedFlow<BranchSwitchResult>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val switchResult: SharedFlow<BranchSwitchResult> = _switchResult.asSharedFlow()
    
    // Flow para navegación a detalle de pedido
    private val _navigateToOrder = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val navigateToOrder: SharedFlow<String> = _navigateToOrder.asSharedFlow()
    
    /**
     * Procesa un evento de cambio de sucursal desde notificación
     * 
     * Este método es llamado cuando el usuario toca la acción
     * "Cambiar para ver" en una notificación.
     * 
     * @param branchId ID de la sucursal destino
     * @param orderId ID del pedido a mostrar
     * @param branchName Nombre de la sucursal (opcional, para confirmación)
     * 
     * Requirements: 12.1, 12.2
     */
    fun handleBranchSwitchFromNotification(
        branchId: String,
        orderId: String,
        branchName: String? = null
    ) {
        _pendingSwitchEvent.value = BranchSwitchEvent(
            branchId = branchId,
            orderId = orderId,
            branchName = branchName
        )
    }
    
    /**
     * Ejecuta el cambio de sucursal
     * 
     * Busca la sucursal en la lista de sucursales disponibles,
     * cambia la sucursal activa y emite el resultado.
     * 
     * @param branches Lista de sucursales disponibles
     * @param currentBranch Sucursal actualmente activa
     * @param setCurrentBranch Función para cambiar la sucursal activa
     * 
     * Requirements: 12.2, 12.3, 12.4
     */
    suspend fun executePendingSwitch(
        branches: List<Branch>,
        currentBranch: Branch?,
        setCurrentBranch: (Branch) -> Unit
    ) {
        val event = _pendingSwitchEvent.value ?: return
        
        // Buscar la sucursal destino
        val targetBranch = branches.find { it.id == event.branchId }
        
        if (targetBranch == null) {
            _switchResult.emit(BranchSwitchResult.BranchNotFound)
            clearPendingSwitch()
            return
        }
        
        // Si ya estamos en la sucursal correcta, solo navegar
        if (currentBranch?.id == targetBranch.id) {
            _navigateToOrder.emit(event.orderId)
            clearPendingSwitch()
            return
        }
        
        // Cambiar sucursal
        val previousBranchName = currentBranch?.name
        setCurrentBranch(targetBranch)
        
        // Emitir resultado para mostrar confirmación
        _switchResult.emit(
            BranchSwitchResult.Success(
                previousBranchName = previousBranchName,
                newBranchName = targetBranch.name,
                orderId = event.orderId
            )
        )
        
        // Navegar al pedido
        _navigateToOrder.emit(event.orderId)
        
        // Limpiar evento pendiente
        clearPendingSwitch()
    }
    
    /**
     * Limpia el evento de cambio pendiente
     */
    fun clearPendingSwitch() {
        _pendingSwitchEvent.value = null
    }
    
    /**
     * Verifica si hay un cambio de sucursal pendiente
     */
    fun hasPendingSwitch(): Boolean {
        return _pendingSwitchEvent.value != null
    }
    
    companion object {
        @Volatile
        private var instance: BranchSwitchHandler? = null
        
        /**
         * Obtiene una instancia singleton del BranchSwitchHandler
         */
        fun getInstance(): BranchSwitchHandler {
            return instance ?: synchronized(this) {
                instance ?: BranchSwitchHandler().also { instance = it }
            }
        }
    }
}
