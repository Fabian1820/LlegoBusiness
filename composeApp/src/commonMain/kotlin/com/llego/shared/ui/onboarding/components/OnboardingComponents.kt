package com.llego.shared.ui.onboarding.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.ui.theme.LlegoPrimary
import com.llego.shared.ui.theme.LlegoSecondary
import com.llego.shared.ui.theme.LlegoAccent

// ─────────────────────────────────────────────
// Step Progress Bar
// ─────────────────────────────────────────────

/**
 * Barra de progreso por pasos estilo Apple con animaciones fluidas.
 *
 * Muestra el paso actual, los completados (con check) y los pendientes.
 * La línea de conexión se rellena progresivamente.
 */
@Composable
fun StepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (totalSteps <= 1) 1f else currentStep.toFloat() / (totalSteps - 1).toFloat(),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step number label
        Text(
            text = "Paso ${currentStep + 1} de $totalSteps",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                LlegoPrimary,
                                LlegoAccent
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dots row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (step in 0 until totalSteps) {
                StepDot(
                    stepIndex = step,
                    currentStep = currentStep,
                    isLast = step == totalSteps - 1
                )
            }
        }
    }
}

@Composable
private fun StepDot(
    stepIndex: Int,
    currentStep: Int,
    isLast: Boolean
) {
    val isCompleted = stepIndex < currentStep
    val isCurrent = stepIndex == currentStep

    val dotScale by animateFloatAsState(
        targetValue = if (isCurrent) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dot_scale"
    )

    val dotColor by animateColorAsState(
        targetValue = when {
            isCompleted -> LlegoPrimary
            isCurrent -> LlegoPrimary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "dot_color"
    )

    Box(
        modifier = Modifier
            .size(if (isCurrent) 28.dp else 22.dp)
            .graphicsLayer {
                scaleX = dotScale
                scaleY = dotScale
            }
            .clip(CircleShape)
            .background(dotColor),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color.White
            )
        } else {
            Text(
                text = "${stepIndex + 1}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = if (isCurrent) 12.sp else 10.sp
                )
            )
        }
    }
}

// ─────────────────────────────────────────────
// Page Dot Indicator (for intro pager)
// ─────────────────────────────────────────────

@Composable
fun PageDotIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage

            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "dot_width"
            )

            val color by animateColorAsState(
                targetValue = if (isSelected) LlegoPrimary else LlegoPrimary.copy(alpha = 0.25f),
                animationSpec = tween(300),
                label = "dot_indicator_color"
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Onboarding Step Layout
// ─────────────────────────────────────────────

/**
 * Layout base para cada paso del wizard.
 *
 * Incluye icono animado, titulo, subtitulo, contenido del paso y
 * los botones de navegacion en la parte inferior.
 */
@Composable
fun OnboardingStepLayout(
    stepIcon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(title) {
        visible = false
        kotlinx.coroutines.delay(50)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "step_alpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "step_offset"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY
            },
        horizontalAlignment = Alignment.Start
    ) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            LlegoPrimary,
                            LlegoPrimary.copy(alpha = 0.8f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = stepIcon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        content()
    }
}

// ─────────────────────────────────────────────
// Field Labels
// ─────────────────────────────────────────────

@Composable
fun RequiredFieldLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = "*",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        )
    }
}

@Composable
fun OptionalFieldLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "Opcional",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Navigation Buttons (Back / Next)
// ─────────────────────────────────────────────

/**
 * Barra de navegacion inferior del wizard con botones Atras / Siguiente.
 *
 * El boton "Siguiente" se deshabilita cuando [canAdvance] es false.
 * En el ultimo paso el texto cambia a [finishLabel].
 */
@Composable
fun OnboardingNavigationBar(
    onBack: () -> Unit,
    onNext: () -> Unit,
    canAdvance: Boolean,
    isFirstStep: Boolean,
    isLastStep: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    finishLabel: String = "Crear Negocio"
) {
    val nextText = if (isLastStep) finishLabel else "Siguiente"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        if (!isFirstStep) {
            OutlinedButton(
                onClick = onBack,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Atras",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Next / Create button
        Button(
            onClick = onNext,
            enabled = canAdvance && !isLoading,
            modifier = Modifier
                .weight(if (isFirstStep) 1f else 1.4f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLastStep) LlegoPrimary else LlegoPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 2.dp
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Creando...",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            } else {
                Text(
                    text = nextText,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Intro Page Layout (for OnboardingIntroScreen)
// ─────────────────────────────────────────────

/**
 * Layout para una pagina individual del onboarding introductorio.
 *
 * Muestra un icono grande (DrawableResource pintado fuera de aqui),
 * titulo, descripcion y un contenido inferior opcional.
 */
@Composable
fun IntroPageContent(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    extraContent: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon slot
        icon()

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
                letterSpacing = (-0.3).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 26.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        extraContent()
    }
}

// ─────────────────────────────────────────────
// Animated Info Card
// ─────────────────────────────────────────────

/**
 * Card decorativa para mostrar puntos informativos en las paginas de intro.
 */
@Composable
fun InfoHighlightCard(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = LlegoPrimary.copy(alpha = 0.06f),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            LlegoPrimary.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LlegoPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = LlegoPrimary
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Utility: animateDpAsState wrapper
// ─────────────────────────────────────────────

@Composable
private fun animateDpAsState(
    targetValue: Dp,
    animationSpec: AnimationSpec<Dp>,
    label: String
): State<Dp> {
    return androidx.compose.animation.core.animateDpAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = label
    )
}
