package com.llego.business.orders.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class ConfirmationType {
    ORDER_ACCEPTED,
    ORDER_READY
}

@Composable
fun OrderConfirmationScreen(
    type: ConfirmationType,
    orderNumber: String,
    onDismiss: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2500)
        visible = false
        delay(300)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(300)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(accentColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            ConfirmationContent(type = type, orderNumber = orderNumber, accentColor = accentColor)
        }
    }
}

@Composable
private fun ConfirmationContent(
    type: ConfirmationType,
    orderNumber: String,
    accentColor: Color
) {
    val scale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier
                .size(96.dp)
                .scale(scale),
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when (type) {
                        ConfirmationType.ORDER_ACCEPTED -> Icons.Default.CheckCircle
                        ConfirmationType.ORDER_READY -> Icons.Default.Done
                    },
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when (type) {
                ConfirmationType.ORDER_ACCEPTED -> "Pedido aceptado"
                ConfirmationType.ORDER_READY -> "Pedido listo"
            },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = when (type) {
                ConfirmationType.ORDER_ACCEPTED -> "El pedido $orderNumber fue aceptado."
                ConfirmationType.ORDER_READY -> "El pedido $orderNumber esta listo."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .width(180.dp)
                .height(2.dp),
            color = accentColor,
            trackColor = accentColor.copy(alpha = 0.2f)
        )
    }
}
