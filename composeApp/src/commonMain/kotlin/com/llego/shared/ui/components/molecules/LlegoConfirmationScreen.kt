package com.llego.shared.ui.components.molecules

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
 * Tipo de confirmación para pantallas animadas
 */
enum class LlegoConfirmationType {
    SUCCESS,         // Operación exitosa (verde)
    ERROR,           // Error en operación (rojo)
    INFO,            // Información (azul)
    WARNING          // Advertencia (naranja)
}

/**
 * Configuración para pantalla de confirmación
 */
data class LlegoConfirmationConfig(
    val type: LlegoConfirmationType,
    val title: String,
    val message: String,
    val icon: ImageVector? = null, // Si es null, se usa el ícono por defecto del tipo
    val autoCloseDelay: Long = 2500, // Tiempo en milisegundos antes de cerrar automáticamente
    val backgroundColor: Color? = null // Si es null, se usa el color por defecto del tipo
)

/**
 * Pantalla fullscreen animada de confirmación reutilizable
 *
 * Similar a OrderConfirmationScreen pero genérica y reutilizable para cualquier acción:
 * - Login exitoso
 * - Registro exitoso
 * - Negocio creado
 * - Error en operación
 * - Cualquier feedback visual importante
 *
 * Características:
 * - Fullscreen con fondo degradado en color del tipo
 * - Animación de entrada con scale y fade
 * - Icono animado con pulso
 * - Cierre automático configurable
 * - Colores basados en el sistema de diseño Llego
 *
 * @param config Configuración de la confirmación
 * @param onDismiss Callback cuando se cierra la pantalla
 * @param modifier Modificador opcional
 */
@Composable
fun LlegoConfirmationScreen(
    config: LlegoConfirmationConfig,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val successColor = Color(0xFF4CAF50) // Verde éxito
    val warningColor = Color(0xFFF57C00) // Naranja advertencia
    val infoColor = Color(0xFF2196F3)    // Azul información

    var visible by remember { mutableStateOf(false) }

    // Determinar color de fondo según tipo
    val backgroundColor = config.backgroundColor ?: when (config.type) {
        LlegoConfirmationType.SUCCESS -> successColor
        LlegoConfirmationType.ERROR -> errorColor
        LlegoConfirmationType.INFO -> infoColor
        LlegoConfirmationType.WARNING -> warningColor
    }

    // Determinar icono según tipo
    val displayIcon = config.icon ?: when (config.type) {
        LlegoConfirmationType.SUCCESS -> Icons.Default.CheckCircle
        LlegoConfirmationType.ERROR -> Icons.Default.Error
        LlegoConfirmationType.INFO -> Icons.Default.Info
        LlegoConfirmationType.WARNING -> Icons.Default.Warning
    }

    // Animación de entrada y cierre automático
    LaunchedEffect(Unit) {
        visible = true
        delay(config.autoCloseDelay)
        visible = false
        delay(300) // Esperar animación de salida
        onDismiss()
    }

    // Gradiente vertical con el color del tipo
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            backgroundColor,
            backgroundColor.copy(alpha = 0.95f),
            backgroundColor
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
            modifier = modifier
                .fillMaxSize()
                .background(brush = gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            ConfirmationContent(
                icon = displayIcon,
                title = config.title,
                message = config.message
            )
        }
    }
}

/**
 * Contenido animado de la confirmación
 */
@Composable
private fun ConfirmationContent(
    icon: ImageVector,
    title: String,
    message: String
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
        // Icono animado grande con círculo de fondo
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Título en blanco
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje descriptivo en blanco
        Text(
            text = message,
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

/**
 * Helpers para crear configuraciones comunes
 */
object LlegoConfirmationDefaults {

    fun successConfig(
        title: String,
        message: String,
        autoCloseDelay: Long = 2500
    ) = LlegoConfirmationConfig(
        type = LlegoConfirmationType.SUCCESS,
        title = title,
        message = message,
        autoCloseDelay = autoCloseDelay
    )

    fun errorConfig(
        title: String,
        message: String,
        autoCloseDelay: Long = 3000 // Más tiempo para errores
    ) = LlegoConfirmationConfig(
        type = LlegoConfirmationType.ERROR,
        title = title,
        message = message,
        autoCloseDelay = autoCloseDelay
    )

    fun infoConfig(
        title: String,
        message: String,
        autoCloseDelay: Long = 2500
    ) = LlegoConfirmationConfig(
        type = LlegoConfirmationType.INFO,
        title = title,
        message = message,
        autoCloseDelay = autoCloseDelay
    )

    fun warningConfig(
        title: String,
        message: String,
        autoCloseDelay: Long = 3000
    ) = LlegoConfirmationConfig(
        type = LlegoConfirmationType.WARNING,
        title = title,
        message = message,
        autoCloseDelay = autoCloseDelay
    )

    // Configs predefinidas para casos comunes

    fun loginSuccess() = successConfig(
        title = "¡Bienvenido!",
        message = "Has iniciado sesión correctamente"
    )

    fun registerSuccess() = successConfig(
        title = "¡Registro Exitoso!",
        message = "Tu cuenta ha sido creada correctamente"
    )

    fun businessCreated(businessName: String) = successConfig(
        title = "¡Negocio Creado!",
        message = "$businessName ha sido registrado exitosamente"
    )

    fun orderAccepted(orderNumber: String) = successConfig(
        title = "¡Orden Aceptada!",
        message = "El pedido $orderNumber ha sido aceptado exitosamente"
    )

    fun orderReady(orderNumber: String) = successConfig(
        title = "¡Orden Lista!",
        message = "El pedido $orderNumber está listo para entregar"
    )

    fun loginError() = errorConfig(
        title = "Error de Inicio de Sesión",
        message = "No se pudo iniciar sesión. Verifica tus credenciales"
    )

    fun networkError() = errorConfig(
        title = "Error de Conexión",
        message = "No se pudo conectar al servidor. Verifica tu conexión"
    )
}
