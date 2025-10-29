package com.llego.nichos.restaurant.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Tipo de confirmación
 */
enum class ConfirmationType {
    ORDER_ACCEPTED,  // Orden aceptada con éxito
    ORDER_READY      // Orden lista para entregar
}

/**
 * Pantalla animada de confirmación de acciones en pedidos
 * Fullscreen con fondo degradado usando color primario de la app
 * Se muestra brevemente y se cierra automáticamente a los 2.5 segundos
 */
@Composable
fun OrderConfirmationScreen(
    type: ConfirmationType,
    orderNumber: String,
    onDismiss: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var visible by remember { mutableStateOf(false) }

    // Animación de entrada
    LaunchedEffect(Unit) {
        visible = true
        // Esperar 2.5 segundos y cerrar automáticamente
        delay(2500)
        visible = false
        delay(300) // Esperar a que termine la animación de salida
        onDismiss()
    }

    // Gradiente vertical con color primario de la app (sin opacidad)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor,
            primaryColor.copy(alpha = 0.95f),
            primaryColor
        )
    )

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
                .background(brush = gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            ConfirmationContent(
                type = type,
                orderNumber = orderNumber
            )
        }
    }
}

/**
 * Contenido de confirmación para pantalla fullscreen
 */
@Composable
private fun ConfirmationContent(
    type: ConfirmationType,
    orderNumber: String
) {
    // Animación del icono con efecto de pulso
    val scale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
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
        // Icono animado grande
        Surface(
            modifier = Modifier
                .size(140.dp)
                .scale(scale),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.25f)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (type) {
                        ConfirmationType.ORDER_ACCEPTED -> Icons.Default.CheckCircle
                        ConfirmationType.ORDER_READY -> Icons.Default.Done
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Título en blanco
        Text(
            text = when (type) {
                ConfirmationType.ORDER_ACCEPTED -> "¡Orden Aceptada!"
                ConfirmationType.ORDER_READY -> "¡Orden Lista!"
            },
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje descriptivo en blanco
        Text(
            text = when (type) {
                ConfirmationType.ORDER_ACCEPTED ->
                    "El pedido $orderNumber ha sido aceptado exitosamente"
                ConfirmationType.ORDER_READY ->
                    "El pedido $orderNumber está listo para entregar"
            },
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White.copy(alpha = 0.9f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Indicador de progreso en blanco
        LinearProgressIndicator(
            modifier = Modifier
                .width(200.dp)
                .height(3.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
    }
}