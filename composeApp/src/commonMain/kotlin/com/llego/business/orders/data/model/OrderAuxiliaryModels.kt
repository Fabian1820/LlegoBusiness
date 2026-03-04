package com.llego.business.orders.data.model

import kotlinx.serialization.Serializable

/**
 * Item de pedido alineado con backend OrderItemType
 */
@Serializable
data class OrderItem(
    val itemId: String,
    val itemType: String,
    val productId: String? = null,
    val name: String,
    val price: Double,
    val basePrice: Double = price,
    val finalPrice: Double = price,
    val quantity: Int,
    val imageUrl: String? = null,
    val requestDescription: String? = null,
    val wasModifiedByStore: Boolean = false
) {
    /**
     * Total de linea calculado (finalPrice * quantity)
     */
    val lineTotal: Double get() = finalPrice * quantity

    val isShowcase: Boolean get() = itemType.equals("SHOWCASE", ignoreCase = true)
}

/**
 * DirecciÃ³n de entrega alineada con backend DeliveryAddressType
 */
@Serializable
data class DeliveryAddress(
    val street: String,
    val city: String? = null,
    val reference: String? = null,
    val coordinates: Coordinates? = null
)

/**
 * Coordenadas alineadas con backend CoordinatesType
 * El backend usa formato GeoJSON: [longitude, latitude]
 */
@Serializable
data class Coordinates(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList()
) {
    /**
     * Longitud (primer elemento del array de coordenadas)
     */
    val longitude: Double get() = coordinates.getOrNull(0) ?: 0.0
    
    /**
     * Latitud (segundo elemento del array de coordenadas)
     */
    val latitude: Double get() = coordinates.getOrNull(1) ?: 0.0
    
    companion object {
        /**
         * Crea coordenadas desde latitud y longitud
         */
        fun fromLatLng(latitude: Double, longitude: Double): Coordinates {
            return Coordinates(
                type = "Point",
                coordinates = listOf(longitude, latitude)
            )
        }
    }
}

/**
 * Entrada del timeline del pedido alineada con backend OrderTimelineType
 */
@Serializable
data class OrderTimelineEntry(
    val status: OrderStatus,
    val timestamp: String,
    val message: String,
    val actor: OrderActor
)

/**
 * Comentario del pedido alineado con backend OrderCommentType
 */
@Serializable
data class OrderComment(
    val id: String,
    val author: OrderActor,
    val message: String,
    val timestamp: String
)

/**
 * Descuento del pedido alineado con backend OrderDiscountType
 */
@Serializable
data class OrderDiscount(
    val id: String,
    val title: String,
    val amount: Double,
    val type: DiscountType
)

