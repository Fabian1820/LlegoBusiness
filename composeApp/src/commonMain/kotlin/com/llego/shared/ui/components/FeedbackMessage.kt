package com.llego.shared.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class FeedbackType {
    SUCCESS,
    ERROR,
    INFO
}

data class FeedbackMessage(
    val message: String,
    val type: FeedbackType = FeedbackType.INFO,
    val duration: Long = 3000L
)

@Composable
fun FeedbackMessageDisplay(
    message: FeedbackMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember(message) { mutableStateOf(message != null) }

    LaunchedEffect(message) {
        if (message != null) {
            visible = true
            delay(message.duration)
            visible = false
            delay(300) // Esperar animación de salida
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible && message != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = modifier
    ) {
        message?.let {
            FeedbackCard(message = it)
        }
    }
}

@Composable
private fun FeedbackCard(message: FeedbackMessage) {
    val (icon, bgColor, iconColor) = when (message.type) {
        FeedbackType.SUCCESS -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFF4CAF50),
            Color.White
        )
        FeedbackType.ERROR -> Triple(
            Icons.Default.Error,
            Color(0xFFF44336),
            Color.White
        )
        FeedbackType.INFO -> Triple(
            Icons.Default.Info,
            Color(0xFF2196F3),
            Color.White
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp,
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                color = iconColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
