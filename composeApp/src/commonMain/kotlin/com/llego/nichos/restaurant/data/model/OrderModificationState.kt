package com.llego.nichos.restaurant.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderModificationState(
    val originalItems: List<OrderItem>,
    val modifiedItems: List<OrderItem>,
    val isEditMode: Boolean,
    val hasChanges: Boolean,
    val originalTotal: Double,
    val newTotal: Double
) {
    val totalDifference: Double
        get() = newTotal - originalTotal

    val hasPriceChange: Boolean
        get() = totalDifference != 0.0
}
