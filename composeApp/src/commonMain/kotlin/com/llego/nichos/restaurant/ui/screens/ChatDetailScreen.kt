package com.llego.nichos.restaurant.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle de chat individual con el cliente
 * Diseño similar a WhatsApp con estilo Llego
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatsViewModel,
    modifier: Modifier = Modifier
) {
    val currentChat by viewModel.currentChat.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Cargar chat al entrar
    LaunchedEffect(orderId) {
        viewModel.loadChatDetail(orderId)
    }

    // Auto-scroll al último mensaje cuando hay nuevos mensajes
    LaunchedEffect(currentChat?.messages?.size) {
        if (currentChat?.messages?.isNotEmpty() == true) {
            coroutineScope.launch {
                listState.animateScrollToItem(currentChat!!.messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            ChatDetailTopBar(
                chat = currentChat,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            ChatInputBar(
                messageInput = messageInput,
                onMessageInputChange = { viewModel.setMessageInput(it) },
                onSendMessage = {
                    if (messageInput.trim().isNotEmpty()) {
                        viewModel.sendMessage(orderId)
                    }
                }
            )
        },
        containerColor = Color(0xFFECE5DD) // Fondo estilo WhatsApp
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentChat == null) {
                // Estado de carga
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                // Lista de mensajes
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = currentChat!!.messages,
                        key = { it.id }
                    ) { message ->
                        AnimatedMessageBubble(message = message)
                    }
                }
            }
        }
    }
}

/**
 * TopBar del chat con info del cliente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailTopBar(
    chat: Chat?,
    onNavigateBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón de volver
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            // Avatar del cliente
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Info del cliente
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = chat?.customerName ?: "Cargando...",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = chat?.orderNumber ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }

            // Icono de llamada (opcional)
            IconButton(onClick = { /* TODO: Implementar llamada */ }) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Llamar",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Burbuja de mensaje animada
 */
@Composable
private fun AnimatedMessageBubble(message: ChatMessage) {
    // Animación de entrada
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(message.id) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(300, easing = EaseOut)
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(300, easing = EaseOutCubic)
        )
    ) {
        when {
            message.isSystemMessage() -> SystemMessageBubble(message)
            message.isFromBusiness() -> BusinessMessageBubble(message)
            else -> CustomerMessageBubble(message)
        }
    }
}

/**
 * Mensaje del negocio (derecha, verde)
 */
@Composable
private fun BusinessMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 4.dp
            ),
            color = Color(0xFFDCF8C6), // Verde claro estilo WhatsApp
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF303030)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF667781)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // Check de leído
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = if (message.isRead) Color(0xFF53BDEB) else Color(0xFF667781),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Mensaje del cliente (izquierda, blanco)
 */
@Composable
private fun CustomerMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 4.dp,
                bottomEnd = 16.dp
            ),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF303030)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF667781),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

/**
 * Mensaje del sistema (centro, gris)
 */
@Composable
private fun SystemMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFFF9E6), // Amarillo suave para avisos
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFFA726),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF856404),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF856404).copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Barra de input para enviar mensajes
 */
@Composable
private fun ChatInputBar(
    messageInput: String,
    onMessageInputChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Campo de texto
            TextField(
                value = messageInput,
                onValueChange = onMessageInputChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Escribe un mensaje...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                maxLines = 4
            )

            // Botón de enviar
            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar mensaje",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
