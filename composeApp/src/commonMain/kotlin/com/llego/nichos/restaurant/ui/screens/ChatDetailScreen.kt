package com.llego.nichos.restaurant.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.llego.nichos.restaurant.data.model.*
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

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
    val replyingTo by viewModel.replyingTo.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val bottomTolerancePx = with(density) { 2.dp.roundToPx() }
    val imeBottom = WindowInsets.ime.getBottom(density)

    var hasInitialScroll by remember { mutableStateOf(false) }
    var lastMessageCount by remember { mutableStateOf(0) }
    var unreadCount by remember { mutableStateOf(0) }
    var lastReadIndex by remember { mutableStateOf(-1) }

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) {
                return@derivedStateOf true
            }
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            val endOffset = lastVisible.offset + lastVisible.size
            val isLastItem = lastVisible.index == totalItems - 1
            val endLimit = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
            isLastItem && endOffset <= endLimit + bottomTolerancePx
        }
    }

    // Cargar chat al entrar
    LaunchedEffect(orderId) {
        viewModel.loadChatDetail(orderId)
    }

    LaunchedEffect(currentChat?.orderId) {
        hasInitialScroll = false
        lastMessageCount = 0
        unreadCount = 0
        lastReadIndex = -1
    }

    suspend fun scrollToLatest(animate: Boolean) {
        val messageCount = currentChat?.messages?.size ?: 0
        if (messageCount == 0) {
            return
        }
        val lastIndex = messageCount - 1
        val listNotReady = listState.layoutInfo.visibleItemsInfo.isEmpty()
        when {
            listNotReady || !animate -> listState.scrollToItem(lastIndex)
            else -> listState.animateScrollToItem(lastIndex)
        }
    }

    // Auto-scroll solo si estamos al final; si no, marcar mensajes no leidos
    LaunchedEffect(currentChat?.messages?.size) {
        val messages = currentChat?.messages.orEmpty()
        val messageCount = messages.size
        if (messageCount == 0) {
            lastMessageCount = 0
            return@LaunchedEffect
        }

        if (!hasInitialScroll) {
            scrollToLatest(animate = false)
            hasInitialScroll = true
            lastMessageCount = messageCount
            unreadCount = 0
            return@LaunchedEffect
        }

        val added = messageCount - lastMessageCount
        if (added <= 0) {
            lastMessageCount = messageCount
            return@LaunchedEffect
        }

        if (isAtBottom) {
            scrollToLatest(animate = true)
            unreadCount = 0
        } else {
            val unreadAdded = messages
                .drop(lastMessageCount)
                .count { it.isFromCustomer() }
            if (unreadAdded > 0) {
                unreadCount += unreadAdded
            }
        }
        lastMessageCount = messageCount
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            unreadCount = 0
        }
    }

    LaunchedEffect(imeBottom) {
        if (imeBottom > 0 && isAtBottom) {
            scrollToLatest(animate = false)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex > lastReadIndex) {
                    lastReadIndex = lastVisibleIndex
                }
            }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ChatTopBar(
                chat = currentChat,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            ChatInputBar(
                messageInput = messageInput,
                replyingTo = replyingTo,
                onMessageInputChange = { viewModel.setMessageInput(it) },
                onCancelReply = { viewModel.cancelReply() },
                onSendMessage = {
                    if (messageInput.trim().isNotEmpty()) {
                        viewModel.sendMessage(orderId)
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FA) // Fondo claro consistente con Llego
    ) { paddingValues ->
        val chat = currentChat
        if (chat == null) {
            // Estado de carga
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Lista de mensajes
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = chat.messages,
                        key = { it.id }
                    ) { message ->
                        SwipeableMessageBubble(
                            message = message,
                            onSwipeToReply = {
                                if (!message.isSystemMessage()) {
                                    viewModel.setReplyingTo(message)
                                }
                            }
                        )
                    }
                }

                val localUnreadSnapshot by remember(chat, lastReadIndex) {
                    derivedStateOf {
                        if (chat.messages.isEmpty()) {
                            return@derivedStateOf (-1 to 0)
                        }
                        val safeLastReadIndex = lastReadIndex.coerceAtMost(chat.messages.lastIndex)
                        var count = 0
                        var firstUnreadIndex = -1
                        for (index in (safeLastReadIndex + 1) until chat.messages.size) {
                            if (chat.messages[index].isFromCustomer()) {
                                if (firstUnreadIndex == -1) {
                                    firstUnreadIndex = index
                                }
                                count++
                            }
                        }
                        firstUnreadIndex to count
                    }
                }
                val oldestUnreadIndex = localUnreadSnapshot.first
                val displayUnreadCount = localUnreadSnapshot.second

                AnimatedVisibility(
                    visible = displayUnreadCount > 0,
                    enter = fadeIn(animationSpec = tween(180)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(220, easing = EaseOutCubic)
                        ),
                    exit = fadeOut(animationSpec = tween(140)) +
                        slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(180, easing = EaseInCubic)
                        ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                val targetIndex = oldestUnreadIndex
                                if (targetIndex >= 0) {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(targetIndex)
                                    }
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayUnreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * TopBar del chat con información del cliente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    chat: Chat?,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            if (chat != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar del cliente
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Info del cliente
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = chat.customerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = chat.orderNumber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            } else {
                Text("Cargando...")
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Implementar llamada */ }) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Llamar",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Burbuja de mensaje con swipe-to-reply (estilo WhatsApp)
 */
@Composable
private fun SwipeableMessageBubble(
    message: ChatMessage,
    onSwipeToReply: () -> Unit = {}
) {
    // Animación de entrada
    var visible by remember { mutableStateOf(false) }

    // Estado del swipe
    var offsetX by remember { mutableStateOf(0f) }
    val swipeThreshold = 120f

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
        // Solo permitir swipe si no es mensaje del sistema
        if (message.isSystemMessage()) {
            SystemMessageBubble(message)
        } else {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icono de respuesta que aparece al hacer swipe
                val iconAlpha = (abs(offsetX) / swipeThreshold).coerceIn(0f, 1f)
                if (iconAlpha > 0f) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = "Responder",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = iconAlpha),
                        modifier = Modifier
                            .align(if (message.isFromBusiness()) Alignment.CenterEnd else Alignment.CenterStart)
                            .padding(horizontal = 16.dp)
                            .size(24.dp)
                            .graphicsLayer {
                                alpha = iconAlpha
                                scaleX = iconAlpha
                                scaleY = iconAlpha
                            }
                    )
                }

                // Contenido del mensaje con gesture
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (abs(offsetX) >= swipeThreshold) {
                                        onSwipeToReply()
                                    }
                                    offsetX = 0f
                                },
                                onDragCancel = {
                                    offsetX = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    // Solo permitir swipe hacia la dirección correcta
                                    val newOffset = offsetX + dragAmount
                                    offsetX = if (message.isFromBusiness()) {
                                        // Mensaje del negocio: swipe hacia la izquierda
                                        newOffset.coerceIn(-swipeThreshold * 1.5f, 0f)
                                    } else {
                                        // Mensaje del cliente: swipe hacia la derecha
                                        newOffset.coerceIn(0f, swipeThreshold * 1.5f)
                                    }
                                }
                            )
                        }
                ) {
                    when {
                        message.isFromBusiness() -> BusinessMessageBubble(message)
                        else -> CustomerMessageBubble(message)
                    }
                }
            }
        }
    }
}

/**
 * Mensaje del negocio (derecha, color primario Llego)
 */
@Composable
private fun BusinessMessageBubble(
    message: ChatMessage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 4.dp
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Indicador de respuesta si existe
                message.replyToMessage?.let { replyTo ->
                    ReplyIndicator(
                        replyToMessage = replyTo,
                        isOwnMessage = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // Check de leído con colores Llego
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = if (message.isRead)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Mensaje del cliente (izquierda, blanco con borde Llego)
 */
@Composable
private fun CustomerMessageBubble(
    message: ChatMessage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 4.dp,
                bottomEnd = 16.dp
            ),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Indicador de respuesta si existe
                message.replyToMessage?.let { replyTo ->
                    ReplyIndicator(
                        replyToMessage = replyTo,
                        isOwnMessage = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

/**
 * Mensaje del sistema (centro, color secundario Llego)
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
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Indicador visual de respuesta estilo WhatsApp
 */
@Composable
private fun ReplyIndicator(
    replyToMessage: String,
    isOwnMessage: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isOwnMessage)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Línea vertical de color
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(
                        color = if (isOwnMessage)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isOwnMessage) "Tú" else "Cliente",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isOwnMessage)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                )
                Text(
                    text = replyToMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Barra de input para enviar mensajes - Diseño Llego con preview de respuesta
 */
@Composable
private fun ChatInputBar(
    messageInput: String,
    replyingTo: ChatMessage?,
    onMessageInputChange: (String) -> Unit,
    onCancelReply: () -> Unit,
    onSendMessage: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Preview de respuesta estilo WhatsApp
            AnimatedVisibility(
                visible = replyingTo != null,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                if (replyingTo != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Línea vertical de color
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(40.dp)
                                    .background(
                                        color = if (replyingTo.isFromBusiness())
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.secondary,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )

                            // Contenido del mensaje
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = if (replyingTo.isFromBusiness()) "Tú" else "Cliente",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (replyingTo.isFromBusiness())
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.secondary
                                    )
                                )
                                Text(
                                    text = replyingTo.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Botón cerrar
                            IconButton(
                                onClick = onCancelReply,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar respuesta",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Campo de texto con estilo Llego y borde
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF5F5F5),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                ) {
                    TextField(
                        value = messageInput,
                        onValueChange = onMessageInputChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Escribe un mensaje...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 4
                    )
                }

                // Botón de enviar con color Llego
                FloatingActionButton(
                    onClick = onSendMessage,
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar mensaje",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
