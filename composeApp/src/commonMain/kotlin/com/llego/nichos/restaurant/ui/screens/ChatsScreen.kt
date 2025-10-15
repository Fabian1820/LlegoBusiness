package com.llego.nichos.restaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llego.nichos.restaurant.data.model.Chat
import com.llego.nichos.restaurant.data.model.hasUnreadMessages
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.ChatsUiState

/**
 * Pantalla de Chats - Lista de conversaciones con clientes
 * Diseño moderno estilo WhatsApp con identidad Llego
 */
@Composable
fun ChatsScreen(
    onChatClick: (String) -> Unit, // orderId
    onNavigateBack: (() -> Unit)? = null,
    viewModel: ChatsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val chatsState by viewModel.chatsState.collectAsStateWithLifecycle()

    // Recargar chats al entrar
    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header mejorado
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón de volver si existe onNavigateBack
                if (onNavigateBack != null) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Conversaciones",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Contador de chats con mensajes no leídos
                when (chatsState) {
                    is ChatsUiState.Success -> {
                        val unreadChats = (chatsState as ChatsUiState.Success).chats.count { it.hasUnreadMessages() }
                        if (unreadChats > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary
                            ) {
                                Text(
                                    text = unreadChats.toString(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        // Contenido según estado
        when (chatsState) {
            is ChatsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is ChatsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = (chatsState as ChatsUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadChats() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is ChatsUiState.Success -> {
                val chats = (chatsState as ChatsUiState.Success).chats
                if (chats.isEmpty()) {
                    EmptyChatsView()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(
                            items = chats,
                            key = { it.orderId }
                        ) { chat ->
                            ChatCard(
                                chat = chat,
                                onClick = { onChatClick(chat.orderId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de Chat Individual - Diseño mejorado estilo WhatsApp/Telegram
 */
@Composable
private fun ChatCard(
    chat: Chat,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (chat.hasUnreadMessages())
            Color(0xFFF0F8FF) // Azul muy claro si hay mensajes sin leer
        else
            Color.White
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar mejorado
                Box(
                    modifier = Modifier.size(56.dp)
                ) {
                    // Fondo circular con gradiente
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Badge con número de mensajes no leídos
                    if (chat.unreadCount > 0) {
                        Surface(
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.TopEnd),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (chat.unreadCount > 9) "9+" else chat.unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
                                    )
                                )
                            }
                        }
                    }
                }

                // Contenido del chat
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Nombre del cliente y hora
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chat.customerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = chat.lastMessage?.timestamp ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (chat.hasUnreadMessages())
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Gray
                        )
                    }

                    // Número de pedido
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = chat.orderNumber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // Último mensaje
                    chat.lastMessage?.let { message ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Indicador de quién envió el mensaje
                            if (message.senderType == com.llego.nichos.restaurant.data.model.SenderType.BUSINESS) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = if (message.isRead)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = message.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (chat.hasUnreadMessages())
                                    Color.Black
                                else
                                    Color.Gray,
                                fontWeight = if (chat.hasUnreadMessages())
                                    FontWeight.Medium
                                else
                                    FontWeight.Normal,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(start = 84.dp),
                color = Color(0xFFE0E0E0)
            )
        }
    }
}

/**
 * Vista cuando no hay chats
 */
@Composable
private fun EmptyChatsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "No hay conversaciones",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                text = "Las conversaciones con tus clientes aparecerán aquí",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}