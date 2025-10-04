package com.llego.shared.ui.components.background

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * Componente de fondo con curva animada que divide la pantalla
 * entre un color primario (parte superior) y color de superficie (parte inferior)
 *
 * @param modifier Modificador para el contenedor
 * @param curveStart Porcentaje de altura donde comienza la curva (por defecto 0.22f = 22%)
 * @param curveEnd Porcentaje de altura donde termina la curva (por defecto 0.22f)
 * @param curveInclination Qué tan pronunciada es la curva (por defecto 0.08f)
 * @param showCurve Si true, muestra la curva verde; si false, solo muestra el fondo gris
 * @param content Contenido a renderizar sobre el fondo
 */
@Composable
fun CurvedBackground(
    modifier: Modifier = Modifier,
    curveStart: Float = 0.22f,
    curveEnd: Float = 0.22f,
    curveInclination: Float = 0.08f,
    showCurve: Boolean = true,
    content: @Composable () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.background

    // Animación de la curva
    val actualCurveStart by animateFloatAsState(
        targetValue = curveStart,
        animationSpec = tween(durationMillis = 600),
        label = "curve_start"
    )

    val actualCurveEnd by animateFloatAsState(
        targetValue = curveEnd,
        animationSpec = tween(durationMillis = 600),
        label = "curve_end"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCurvedBackground(
                primaryColor = primaryColor,
                surfaceColor = surfaceColor,
                curveStart = actualCurveStart,
                curveEnd = actualCurveEnd,
                curveInclination = curveInclination,
                showCurve = showCurve
            )
        }

        content()
    }
}

/**
 * Dibuja el fondo con curva usando Canvas
 */
private fun DrawScope.drawCurvedBackground(
    primaryColor: Color,
    surfaceColor: Color,
    curveStart: Float,
    curveEnd: Float,
    curveInclination: Float,
    showCurve: Boolean
) {
    val width = size.width
    val height = size.height
    val curveStartY = height * curveStart
    val curveEndY = height * curveEnd

    // Dibujar fondo completo (superficie)
    drawRect(
        color = surfaceColor,
        size = size
    )

    // Dibujar la parte verde con curva si showCurve es true
    if (showCurve) {
        val path = Path().apply {
            // Empezar desde la esquina superior izquierda
            moveTo(0f, 0f)

            // Línea hasta donde empieza la curva
            lineTo(0f, curveStartY)

            // Calcular altura de la curva
            val curveHeight = height * curveInclination
            val controlPointY = curveStartY + curveHeight

            // Curva cúbica de Bézier que crea una forma suave
            cubicTo(
                width * 0.25f, controlPointY,  // Primer punto de control
                width * 0.75f, controlPointY,  // Segundo punto de control
                width, curveEndY                // Punto final
            )

            // Completar el rectángulo verde
            lineTo(width, 0f)
            close()
        }

        // Dibujar la parte verde
        drawPath(
            path = path,
            color = primaryColor
        )
    }
}
