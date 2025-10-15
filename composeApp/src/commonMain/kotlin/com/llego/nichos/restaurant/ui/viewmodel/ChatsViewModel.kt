package com.llego.nichos.restaurant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.nichos.restaurant.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de chats y mensajería
 */
class ChatsViewModel : ViewModel() {

    private val _chatsState = MutableStateFlow<ChatsUiState>(ChatsUiState.Loading)
    val chatsState: StateFlow<ChatsUiState> = _chatsState.asStateFlow()

    private val _currentChat = MutableStateFlow<Chat?>(null)
    val currentChat: StateFlow<Chat?> = _currentChat.asStateFlow()

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    // Mock timestamp counter - en producción usaríamos un timestamp real
    private var mockTimestampCounter = 1700000000000L

    // Mock data storage - en producción esto vendría de un repositorio
    private val mockChatsData = mutableListOf(
        createMockChat("order_001", "#1234", "Juan Pérez", 2),
        createMockChat("order_002", "#1235", "María García", 0),
        createMockChat("order_003", "#1236", "Carlos López", 0)
    )

    private fun getCurrentTimestamp(): Long {
        mockTimestampCounter += 1000
        return mockTimestampCounter
    }

    init {
        loadChats()
    }

    fun loadChats() {
        viewModelScope.launch {
            _chatsState.value = ChatsUiState.Loading
            delay(500) // Simular carga
            _chatsState.value = ChatsUiState.Success(mockChatsData.sortedByDescending {
                it.lastMessage?.fullTimestamp ?: 0
            })
        }
    }

    fun loadChatDetail(orderId: String) {
        viewModelScope.launch {
            val chat = mockChatsData.find { it.orderId == orderId }
            _currentChat.value = chat

            // Marcar mensajes como leídos
            if (chat != null && chat.unreadCount > 0) {
                val updatedChat = chat.copy(
                    unreadCount = 0,
                    messages = chat.messages.map { it.copy(isRead = true) }
                )
                val index = mockChatsData.indexOfFirst { it.orderId == orderId }
                if (index != -1) {
                    mockChatsData[index] = updatedChat
                    _currentChat.value = updatedChat
                    loadChats() // Refrescar lista
                }
            }
        }
    }

    fun setMessageInput(text: String) {
        _messageInput.value = text
    }

    fun sendMessage(orderId: String) {
        val messageText = _messageInput.value.trim()
        if (messageText.isEmpty()) return

        viewModelScope.launch {
            val currentTime = getCurrentTimestamp()
            val newMessage = ChatMessage(
                id = "msg_${currentTime}",
                senderId = "business_001",
                senderType = SenderType.BUSINESS,
                message = messageText,
                timestamp = formatTimestamp(currentTime),
                fullTimestamp = currentTime,
                isRead = true,
                messageType = MessageType.TEXT
            )

            // Actualizar chat con nuevo mensaje
            val chatIndex = mockChatsData.indexOfFirst { it.orderId == orderId }
            if (chatIndex != -1) {
                val chat = mockChatsData[chatIndex]
                val updatedMessages = chat.messages + newMessage
                val updatedChat = chat.copy(
                    messages = updatedMessages,
                    lastMessage = newMessage,
                    updatedAt = formatTimestamp(currentTime)
                )
                mockChatsData[chatIndex] = updatedChat
                _currentChat.value = updatedChat
            }

            // Limpiar input
            _messageInput.value = ""

            // Simular respuesta automática del cliente
            simulateCustomerResponse(orderId, currentTime)
        }
    }

    private suspend fun simulateCustomerResponse(orderId: String, afterTimestamp: Long) {
        delay(2000) // Esperar 2 segundos

        val responses = listOf(
            "¡Perfecto, gracias!",
            "Entendido, gracias por la información",
            "Ok, muchas gracias",
            "De acuerdo",
            "¡Excelente!"
        )

        val currentTime = getCurrentTimestamp()
        val responseMessage = ChatMessage(
            id = "msg_${currentTime}",
            senderId = "customer_001",
            senderType = SenderType.CUSTOMER,
            message = responses.random(),
            timestamp = formatTimestamp(currentTime),
            fullTimestamp = currentTime,
            isRead = false,
            messageType = MessageType.TEXT
        )

        val chatIndex = mockChatsData.indexOfFirst { it.orderId == orderId }
        if (chatIndex != -1) {
            val chat = mockChatsData[chatIndex]
            val updatedMessages = chat.messages + responseMessage
            val updatedChat = chat.copy(
                messages = updatedMessages,
                lastMessage = responseMessage,
                unreadCount = if (_currentChat.value?.orderId == orderId) 0 else 1,
                updatedAt = formatTimestamp(currentTime)
            )
            mockChatsData[chatIndex] = updatedChat
            _currentChat.value = updatedChat
            loadChats() // Refrescar lista
        }
    }

    /**
     * Crea un chat automáticamente cuando se cancela un pedido
     */
    fun createCancellationChat(orderId: String, orderNumber: String, customerName: String): String {
        viewModelScope.launch {
            // Buscar si ya existe un chat para este pedido
            val existingChat = mockChatsData.find { it.orderId == orderId }

            if (existingChat == null) {
                // Crear nuevo chat con mensaje de sistema sobre cancelación
                val currentTime = getCurrentTimestamp()
                val systemMessage = ChatMessage(
                    id = "msg_${currentTime}",
                    senderId = "system",
                    senderType = SenderType.SYSTEM,
                    message = "Pedido $orderNumber cancelado. Por favor, explica el motivo al cliente.",
                    timestamp = formatTimestamp(currentTime),
                    fullTimestamp = currentTime,
                    isRead = false,
                    messageType = MessageType.ORDER_CANCELLED
                )

                val newChat = Chat(
                    orderId = orderId,
                    orderNumber = orderNumber,
                    customerName = customerName,
                    messages = listOf(systemMessage),
                    lastMessage = systemMessage,
                    unreadCount = 0,
                    createdAt = formatTimestamp(currentTime),
                    updatedAt = formatTimestamp(currentTime)
                )

                mockChatsData.add(0, newChat) // Agregar al inicio
                loadChats()
            } else {
                // Agregar mensaje de cancelación al chat existente
                val currentTime = getCurrentTimestamp()
                val systemMessage = ChatMessage(
                    id = "msg_${currentTime}",
                    senderId = "system",
                    senderType = SenderType.SYSTEM,
                    message = "Pedido $orderNumber cancelado. Por favor, explica el motivo al cliente.",
                    timestamp = formatTimestamp(currentTime),
                    fullTimestamp = currentTime,
                    isRead = false,
                    messageType = MessageType.ORDER_CANCELLED
                )

                val chatIndex = mockChatsData.indexOfFirst { it.orderId == orderId }
                if (chatIndex != -1) {
                    val chat = mockChatsData[chatIndex]
                    val updatedMessages = chat.messages + systemMessage
                    val updatedChat = chat.copy(
                        messages = updatedMessages,
                        lastMessage = systemMessage,
                        updatedAt = formatTimestamp(currentTime)
                    )
                    mockChatsData[chatIndex] = updatedChat

                    // Mover al inicio de la lista
                    mockChatsData.removeAt(chatIndex)
                    mockChatsData.add(0, updatedChat)
                    loadChats()
                }
            }
        }
        return orderId
    }

    private fun formatTimestamp(millis: Long): String {
        // Formato simple: "HH:mm"
        val hours = (millis / 3600000) % 24
        val minutes = (millis / 60000) % 60
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    }

    private fun createMockChat(
        orderId: String,
        orderNumber: String,
        customerName: String,
        unreadCount: Int
    ): Chat {
        val baseTimestamp = getCurrentTimestamp() - (orderId.takeLast(1).toIntOrNull() ?: 0) * 3600000

        val messages = mutableListOf<ChatMessage>()

        // Mensaje inicial del cliente
        messages.add(
            ChatMessage(
                id = "msg_${orderId}_1",
                senderId = "customer_${orderId.takeLast(3)}",
                senderType = SenderType.CUSTOMER,
                message = "Hola, ¿cuánto tiempo tardará mi pedido?",
                timestamp = formatTimestamp(baseTimestamp),
                fullTimestamp = baseTimestamp,
                isRead = true
            )
        )

        // Respuesta del negocio
        messages.add(
            ChatMessage(
                id = "msg_${orderId}_2",
                senderId = "business_001",
                senderType = SenderType.BUSINESS,
                message = "Hola ${customerName.split(" ").first()}, tu pedido estará listo en 30 minutos aproximadamente.",
                timestamp = formatTimestamp(baseTimestamp + 120000),
                fullTimestamp = baseTimestamp + 120000,
                isRead = true
            )
        )

        // Último mensaje
        val lastMessageTimestamp = baseTimestamp + 240000
        val lastMessage = if (unreadCount > 0) {
            ChatMessage(
                id = "msg_${orderId}_3",
                senderId = "customer_${orderId.takeLast(3)}",
                senderType = SenderType.CUSTOMER,
                message = "¿Cuánto falta para que llegue mi pedido?",
                timestamp = formatTimestamp(lastMessageTimestamp),
                fullTimestamp = lastMessageTimestamp,
                isRead = false
            )
        } else {
            ChatMessage(
                id = "msg_${orderId}_3",
                senderId = "business_001",
                senderType = SenderType.BUSINESS,
                message = "Tu pedido está en camino",
                timestamp = formatTimestamp(lastMessageTimestamp),
                fullTimestamp = lastMessageTimestamp,
                isRead = true
            )
        }

        messages.add(lastMessage)

        return Chat(
            orderId = orderId,
            orderNumber = orderNumber,
            customerName = customerName,
            messages = messages,
            lastMessage = lastMessage,
            unreadCount = unreadCount,
            createdAt = formatTimestamp(baseTimestamp),
            updatedAt = formatTimestamp(lastMessageTimestamp)
        )
    }
}

sealed class ChatsUiState {
    data object Loading : ChatsUiState()
    data class Success(val chats: List<Chat>) : ChatsUiState()
    data class Error(val message: String) : ChatsUiState()
}
