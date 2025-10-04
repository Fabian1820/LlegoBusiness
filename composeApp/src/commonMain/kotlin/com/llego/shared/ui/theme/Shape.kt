package com.llego.shared.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Sistema de formas del diseño Llego
 * Basado en las especificaciones del documento LLEGO_DESIGN_SYSTEM.md
 */

val LlegoShapes = Shapes(
    // Formas pequeñas para elementos como chips, badges
    extraSmall = RoundedCornerShape(8.dp),

    // Formas pequeñas para botones secundarios, inputs
    small = RoundedCornerShape(12.dp),

    // Formas medianas para cards principales
    medium = RoundedCornerShape(16.dp),

    // Formas grandes para cards destacadas, modals
    large = RoundedCornerShape(20.dp),

    // Formas extra grandes para pantallas completas, sheets
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Formas específicas para diferentes componentes
 */
object LlegoCustomShapes {

    // Para botones primarios (más redondeados)
    val primaryButton = RoundedCornerShape(28.dp)

    // Para botones secundarios
    val secondaryButton = RoundedCornerShape(16.dp)

    // Para cards de productos
    val productCard = RoundedCornerShape(16.dp)

    // Para cards de información
    val infoCard = RoundedCornerShape(12.dp)

    // Para elementos circulares (avatars, badges)
    val circular = RoundedCornerShape(50)

    // Para inputs y campos de texto
    val inputField = RoundedCornerShape(14.dp)

    // Para modals y dialogs
    val modal = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Para bottom sheets
    val bottomSheet = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Para navigation bar
    val navigationBar = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
}

/**
 * Curvas personalizadas para formas especiales
 * (como la CurvedBottomShape mencionada en la documentación)
 */
object LlegoCurvedShapes {

    // Forma curva personalizada para cards especiales
    val curvedBottom = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )

    // Forma asimétrica para elementos destacados
    val asymmetric = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 8.dp,
        bottomStart = 8.dp,
        bottomEnd = 20.dp
    )
}

/**
 * Extensiones para facilitar el uso de formas
 */
object ShapeExtensions {

    fun getCornerRadius(size: CornerSize): Shape {
        return when (size) {
            CornerSize.SMALL -> LlegoShapes.small
            CornerSize.MEDIUM -> LlegoShapes.medium
            CornerSize.LARGE -> LlegoShapes.large
            CornerSize.CIRCULAR -> LlegoCustomShapes.circular
        }
    }
}

enum class CornerSize {
    SMALL,
    MEDIUM,
    LARGE,
    CIRCULAR
}