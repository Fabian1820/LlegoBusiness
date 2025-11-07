package com.llego.nichos.restaurant.data.model

import kotlinx.serialization.Serializable

/**
 * Modelo de Chat para conversaciones entre restaurante y cliente
 * Basado en pedidos individuales
 */
@Serializable
data class Chat(
    val orderId: String,
    val orderNumber: String,
    val customerName: String,
    val customerAvatar: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderType: SenderType,
    val message: String,
    val timestamp: String, // Formato: "HH:mm" o "dd/MM/yyyy HH:mm"
    val fullTimestamp: Long, // Unix timestamp para ordenar
    val isRead: Boolean = false,
    val messageType: MessageType = MessageType.TEXT,
    val replyToMessage: String? = null // Texto del mensaje al que se está respondiendo
)

@Serializable
enum class SenderType {
    CUSTOMER,   // Cliente
    BUSINESS,   // Negocio/Restaurante
    SYSTEM      // Mensajes del sistema (cancelaciones, etc.)
}

@Serializable
enum class MessageType {
    TEXT,           // Mensaje de texto normal
    ORDER_CANCELLED // Notificación de cancelación de pedido
}

// Extension functions para UI
fun Chat.hasUnreadMessages(): Boolean = unreadCount > 0

fun Chat.getDisplayTime(): String {
    return lastMessage?.timestamp ?: createdAt
}

fun ChatMessage.isFromBusiness(): Boolean = senderType == SenderType.BUSINESS

fun ChatMessage.isFromCustomer(): Boolean = senderType == SenderType.CUSTOMER

fun ChatMessage.isSystemMessage(): Boolean = senderType == SenderType.SYSTEM