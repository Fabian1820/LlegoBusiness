package com.llego.business.orders.data.model

import kotlinx.serialization.Serializable

/**
 * Estado de modificación de un pedido.
 * 
 * Usa OrderItem del modelo local que contiene MenuItem con precio.
 * El lineTotal se calcula como price * quantity en cada OrderItem.subtotal.
 * 
 * Requirements: 6.7 - Recalcular y mostrar el nuevo total en tiempo real durante la edición
 */
@Serializable
data class OrderModificationState(
    /** Items originales del pedido antes de modificaciones */
    val originalItems: List<OrderItem>,
    /** Items modificados (pueden incluir cambios de cantidad, eliminaciones, adiciones) */
    val modifiedItems: List<OrderItem>,
    /** Indica si el modo de edición está activo */
    val isEditMode: Boolean,
    /** Indica si hay cambios respecto al estado original */
    val hasChanges: Boolean,
    /** Total original del pedido antes de modificaciones */
    val originalTotal: Double,
    /** Nuevo total calculado con los items modificados */
    val newTotal: Double
) {
    /**
     * Diferencia entre el nuevo total y el original.
     * Positivo si aumentó, negativo si disminuyó.
     */
    val totalDifference: Double
        get() = newTotal - originalTotal

    /**
     * Indica si hubo cambio en el precio total.
     */
    val hasPriceChange: Boolean
        get() = kotlin.math.abs(totalDifference) > 0.001

    /**
     * Número de items en el pedido modificado.
     */
    val itemCount: Int
        get() = modifiedItems.size

    /**
     * Número de items originales.
     */
    val originalItemCount: Int
        get() = originalItems.size

    /**
     * Indica si se agregaron items nuevos.
     */
    val hasAddedItems: Boolean
        get() = modifiedItems.size > originalItems.size

    /**
     * Indica si se eliminaron items.
     */
    val hasRemovedItems: Boolean
        get() = modifiedItems.size < originalItems.size

    /**
     * Calcula el lineTotal de un item específico.
     * lineTotal = price * quantity
     */
    fun getItemLineTotal(item: OrderItem): Double {
        return item.price * item.quantity
    }

    /**
     * Recalcula el total sumando todos los lineTotals de los items modificados.
     * Útil para validar que newTotal está correctamente calculado.
     */
    fun calculateTotal(): Double {
        return modifiedItems.sumOf { it.price * it.quantity }
    }

    companion object {
        /**
         * Crea un estado de modificación inicial desde una lista de OrderItem.
         * 
         * @param items Lista de items del pedido
         * @param originalTotal Total original del pedido (puede incluir delivery fee, descuentos, etc.)
         */
        fun fromItems(items: List<OrderItem>, originalTotal: Double): OrderModificationState {
            val calculatedTotal = items.sumOf { it.price * it.quantity }
            return OrderModificationState(
                originalItems = items,
                modifiedItems = items,
                isEditMode = true,
                hasChanges = false,
                originalTotal = originalTotal,
                newTotal = calculatedTotal
            )
        }
    }
}
