package com.llego.business.chats.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.business.chats.data.model.*
import com.llego.business.orders.data.model.OrderItem
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

    private val _replyingTo = MutableStateFlow<ChatMessage?>(null)
    val replyingTo: StateFlow<ChatMessage?> = _replyingTo.asStateFlow()

    // Mock timestamp counter - en producción usaríamos un timestamp real
    private var mockTimestampCounter = 1700000000000L

    // Mock data storage - en producción esto vendría de un repositorio
    private val mockChatsData = mutableListOf(
        createMockChat("ORD001", "#1234", "Juan Perez", 2),
        createMockChat("ORD005", "#1238", "Lucia Ramirez", 0),
        createMockChat("ORD003", "#1236", "Carlos Lopez", 0)
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

    fun setReplyingTo(message: ChatMessage?) {
        _replyingTo.value = message
    }

    fun cancelReply() {
        _replyingTo.value = null
    }

    fun sendMessage(orderId: String) {
        val messageText = _messageInput.value.trim()
        if (messageText.isEmpty()) return

        viewModelScope.launch {
            val currentTime = getCurrentTimestamp()
            val replyText = _replyingTo.value?.message

            val newMessage = ChatMessage(
                id = "msg_${currentTime}",
                senderId = "business_001",
                senderType = SenderType.BUSINESS,
                message = messageText,
                timestamp = formatTimestamp(currentTime),
                fullTimestamp = currentTime,
                isRead = true,
                messageType = MessageType.TEXT,
                replyToMessage = replyText
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

            // Limpiar input y estado de respuesta
            _messageInput.value = ""
            _replyingTo.value = null

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

    fun createModificationMessage(
        orderId: String,
        orderNumber: String,
        customerName: String,
        note: String,
        originalItems: List<OrderItem>,
        modifiedItems: List<OrderItem>,
        originalTotal: Double,
        newTotal: Double
    ): String {
        viewModelScope.launch {
            val currentTime = getCurrentTimestamp()
            val summary = buildModificationSummary(
                orderNumber = orderNumber,
                note = note,
                originalItems = originalItems,
                modifiedItems = modifiedItems,
                originalTotal = originalTotal,
                newTotal = newTotal
            )
            val systemMessage = ChatMessage(
                id = "msg_${currentTime}",
                senderId = "system",
                senderType = SenderType.SYSTEM,
                message = summary,
                timestamp = formatTimestamp(currentTime),
                fullTimestamp = currentTime,
                isRead = false,
                messageType = MessageType.ORDER_MODIFIED
            )

            val existingChat = mockChatsData.find { it.orderId == orderId }
            if (existingChat == null) {
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
                mockChatsData.add(0, newChat)
                _currentChat.value = newChat
                loadChats()
            } else {
                val chatIndex = mockChatsData.indexOfFirst { it.orderId == orderId }
                if (chatIndex != -1) {
                    val updatedMessages = existingChat.messages + systemMessage
                    val updatedChat = existingChat.copy(
                        messages = updatedMessages,
                        lastMessage = systemMessage,
                        updatedAt = formatTimestamp(currentTime)
                    )
                    mockChatsData[chatIndex] = updatedChat
                    mockChatsData.removeAt(chatIndex)
                    mockChatsData.add(0, updatedChat)
                    if (_currentChat.value?.orderId == orderId) {
                        _currentChat.value = updatedChat
                    }
                    loadChats()
                }
            }
        }
        return orderId
    }

    private fun buildModificationSummary(
        orderNumber: String,
        note: String,
        originalItems: List<OrderItem>,
        modifiedItems: List<OrderItem>,
        originalTotal: Double,
        newTotal: Double
    ): String {
        val originalById = originalItems.associateBy { it.id }
        val modifiedById = modifiedItems.associateBy { it.id }

        val addedItems = modifiedItems.filter { it.id !in originalById }
        val removedItems = originalItems.filter { it.id !in modifiedById }
        val editedItems = modifiedItems.mapNotNull { item ->
            val original = originalById[item.id] ?: return@mapNotNull null
            val quantityChanged = original.quantity != item.quantity
            val instructionsChanged = original.specialInstructions != item.specialInstructions
            if (!quantityChanged && !instructionsChanged) {
                return@mapNotNull null
            }
            val changes = mutableListOf<String>()
            if (quantityChanged) {
                changes.add("cant ${original.quantity}->${item.quantity}")
            }
            if (instructionsChanged) {
                val before = original.specialInstructions ?: "sin"
                val after = item.specialInstructions ?: "sin"
                changes.add("instr ${before}->${after}")
            }
            "${item.menuItem.name} (${changes.joinToString(", ")})"
        }

        val lines = mutableListOf<String>()
        lines.add("Pedido modificado $orderNumber")
        if (addedItems.isNotEmpty()) {
            val addedSummary = addedItems.joinToString(separator = "; ") { formatOrderItemLine(it) }
            lines.add("Agregados: $addedSummary")
        }
        if (removedItems.isNotEmpty()) {
            val removedSummary = removedItems.joinToString(separator = "; ") { formatOrderItemLine(it) }
            lines.add("Eliminados: $removedSummary")
        }
        if (editedItems.isNotEmpty()) {
            val editedSummary = editedItems.joinToString(separator = "; ")
            lines.add("Editados: $editedSummary")
        }
        val finalSummary = modifiedItems.joinToString(separator = "; ") { formatOrderItemLine(it) }
        lines.add("Items finales: $finalSummary")
        lines.add("Totales: $originalTotal -> $newTotal")
        lines.add("Comentario: $note")
        return lines.joinToString("\n")
    }

    private fun formatOrderItemLine(item: OrderItem): String {
        val base = "${item.quantity}x ${item.menuItem.name}"
        val instructions = item.specialInstructions?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
        return base + instructions
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


