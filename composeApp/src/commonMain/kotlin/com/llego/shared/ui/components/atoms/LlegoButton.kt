package com.llego.shared.ui.components.atoms

// import androidx.compose.animation.animateFloatAsState  // TODO: Habilitar cuando esté soportado en KMP
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.shared.ui.theme.LlegoCustomShapes

/**
 * Botón personalizado de Llego con animaciones estilo iOS
 * Incluye efectos de spring y estados de loading
 */

@Composable
fun LlegoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: LlegoButtonVariant = LlegoButtonVariant.PRIMARY,
    size: LlegoButtonSize = LlegoButtonSize.MEDIUM,
    shape: Shape = LlegoCustomShapes.primaryButton
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Animación de escala para efecto de press estilo iOS - TODO: Implementar cuando sea soportado en KMP
    var isPressed by remember { mutableStateOf(false) }
    val scale = 1f // by animateFloatAsState(
        // targetValue = if (isPressed) 0.95f else 1f,
        // animationSpec = spring(
        //     dampingRatio = Spring.DampingRatioMediumBouncy,
        //     stiffness = Spring.StiffnessLow
        // ),
        // label = "button_scale"
    // )

    // Colores según la variante
    val buttonColors = getButtonColors(variant, enabled)
    val buttonPadding = getButtonPadding(size)
    val textStyle = getButtonTextStyle(size)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(
                color = if (enabled) buttonColors.container else buttonColors.disabledContainer,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading
            ) {
                onClick()
            }
            .then(
                Modifier.padding(buttonPadding)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(textStyle.fontSize.value.dp),
                color = buttonColors.content,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = textStyle.copy(
                    color = if (enabled) buttonColors.content else buttonColors.disabledContent,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }

    // Detectar estado pressed
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is androidx.compose.foundation.interaction.PressInteraction.Press -> {
                    isPressed = true
                }
                is androidx.compose.foundation.interaction.PressInteraction.Release -> {
                    isPressed = false
                }
                is androidx.compose.foundation.interaction.PressInteraction.Cancel -> {
                    isPressed = false
                }
            }
        }
    }
}

/**
 * Variantes de botón
 */
enum class LlegoButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    TEXT
}

/**
 * Tamaños de botón
 */
enum class LlegoButtonSize {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * Colores del botón según la variante
 */
data class ButtonColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color
)

@Composable
private fun getButtonColors(variant: LlegoButtonVariant, enabled: Boolean): ButtonColors {
    val colorScheme = MaterialTheme.colorScheme

    return when (variant) {
        LlegoButtonVariant.PRIMARY -> ButtonColors(
            container = colorScheme.primary,
            content = colorScheme.onPrimary,
            disabledContainer = colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContent = colorScheme.onSurface.copy(alpha = 0.38f)
        )
        LlegoButtonVariant.SECONDARY -> ButtonColors(
            container = colorScheme.secondary,
            content = colorScheme.onSecondary,
            disabledContainer = colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContent = colorScheme.onSurface.copy(alpha = 0.38f)
        )
        LlegoButtonVariant.OUTLINE -> ButtonColors(
            container = Color.Transparent,
            content = colorScheme.primary,
            disabledContainer = Color.Transparent,
            disabledContent = colorScheme.onSurface.copy(alpha = 0.38f)
        )
        LlegoButtonVariant.TEXT -> ButtonColors(
            container = Color.Transparent,
            content = colorScheme.primary,
            disabledContainer = Color.Transparent,
            disabledContent = colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun getButtonPadding(size: LlegoButtonSize): PaddingValues {
    return when (size) {
        LlegoButtonSize.SMALL -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        LlegoButtonSize.MEDIUM -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        LlegoButtonSize.LARGE -> PaddingValues(horizontal = 32.dp, vertical = 16.dp)
    }
}

@Composable
private fun getButtonTextStyle(size: LlegoButtonSize) = when (size) {
    LlegoButtonSize.SMALL -> MaterialTheme.typography.labelMedium
    LlegoButtonSize.MEDIUM -> MaterialTheme.typography.labelLarge
    LlegoButtonSize.LARGE -> MaterialTheme.typography.titleMedium
}
