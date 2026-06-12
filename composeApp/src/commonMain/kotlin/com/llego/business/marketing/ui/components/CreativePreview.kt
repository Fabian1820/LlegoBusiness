package com.llego.business.marketing.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.data.model.CreativeSpec

/**
 * Render nativo del CreativeSpec en Compose (preview en vivo del diseñador).
 * Espejo del CreativeRenderView de iOS para que el negocio vea lo mismo que el cliente.
 */
@Composable
fun CreativePreview(spec: CreativeSpec, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "creative")
    val pulse by infinite.animateFloat(
        0.97f, 1.03f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "pulse"
    )
    val shine by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1800), RepeatMode.Restart), label = "shine"
    )
    val grad by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "grad"
    )

    var appeared by remember(spec.animationPreset) { mutableStateOf(false) }
    LaunchedEffect(spec.animationPreset) { appeared = true }
    val fade by animateFloatAsState(
        if (appeared || spec.animationPreset != "fade_in") 1f else 0f, tween(600), label = "fade"
    )
    val slide by animateFloatAsState(
        if (appeared || spec.animationPreset != "slide_in") 0f else 40f, spring(), label = "slide"
    )

    val scale = if (spec.animationPreset == "pulse") pulse else 1f
    val alpha = if (spec.animationPreset == "fade_in") fade else 1f
    val offsetX = if (spec.animationPreset == "slide_in") slide else 0f

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale; scaleY = scale; this.alpha = alpha; translationX = offsetX
            }
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundBrush(spec, grad))
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            spec.texts.forEach { t ->
                Text(
                    text = t.value,
                    color = hexColor(t.color),
                    fontSize = sizeSp(t.size),
                    fontWeight = weightOf(t.weight),
                    maxLines = if (t.role == "title") 2 else 1
                )
            }
            spec.cta?.let { cta ->
                Spacer(Modifier.height(6.dp))
                Surface(color = Color.White.copy(alpha = 0.92f), shape = RoundedCornerShape(50)) {
                    Text(
                        text = cta.label,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        spec.badge?.let { b ->
            Surface(
                color = badgeColor(b.style),
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
            ) {
                Text(
                    text = b.text,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }

        if (spec.animationPreset == "shine") {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.25f), Color.Transparent),
                        startX = shine * 700f - 200f,
                        endX = shine * 700f + 120f
                    )
                )
            )
        }
    }
}

private fun backgroundBrush(spec: CreativeSpec, grad: Float): Brush {
    val raw = spec.background.colors
    if (raw.size < 2) return SolidColor(hexColor(raw.firstOrNull() ?: "#023133"))
    var cols = raw.map { hexColor(it) }
    if (spec.animationPreset == "gradient_shift" && grad > 0.5f) cols = cols.reversed()
    val angle = ((spec.background.angle % 360) + 360) % 360
    return when {
        angle in 60..120 -> Brush.verticalGradient(cols)
        angle in 150..210 -> Brush.horizontalGradient(cols.reversed())
        angle in 240..300 -> Brush.verticalGradient(cols.reversed())
        angle < 30 || angle > 330 -> Brush.horizontalGradient(cols)
        else -> Brush.linearGradient(cols)
    }
}

fun hexColor(hex: String): Color = try {
    val c = hex.removePrefix("#")
    when (c.length) {
        6 -> Color(("FF$c").toLong(16))
        8 -> Color(c.toLong(16))
        3 -> {
            val r = c[0]; val g = c[1]; val b = c[2]
            Color("FF$r$r$g$g$b$b".toLong(16))
        }
        else -> Color.Gray
    }
} catch (e: Exception) {
    Color.Gray
}

private fun sizeSp(size: String) = when (size) {
    "sm" -> 13.sp
    "lg" -> 22.sp
    "xl" -> 28.sp
    else -> 16.sp
}

private fun weightOf(weight: String) = when (weight) {
    "bold" -> FontWeight.Bold
    "medium" -> FontWeight.Medium
    else -> FontWeight.Normal
}

private fun badgeColor(style: String) = when (style) {
    "flash" -> Color(0xFFE53935)
    "discount" -> Color(0xFFFB8C00)
    "new" -> Color(0xFF43A047)
    else -> Color(0xFF7C412B)
}
