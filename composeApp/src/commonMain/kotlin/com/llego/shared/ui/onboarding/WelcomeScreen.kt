package com.llego.shared.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.ui.theme.LlegoPrimary
import com.llego.shared.ui.theme.LlegoSecondary
import com.llego.shared.ui.theme.LlegoAccent
import kotlinx.coroutines.delay
import llegobusiness.composeapp.generated.resources.Res
import llegobusiness.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

/**
 * Pantalla de bienvenida — primer punto de contacto del usuario con la app.
 *
 * Presenta dos caminos:
 * - "Ya tengo un negocio"  → va al login existente
 * - "Quiero unirme a Llego" → inicia el flujo de onboarding para nuevos usuarios
 *
 * Diseño inspirado en Apple: gradientes suaves, animaciones spring,
 * tipografía limpia y uso de la identidad visual de Llego.
 */
@Composable
fun WelcomeScreen(
    onExistingUser: () -> Unit,
    onNewUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // ── Animaciones de entrada ──────────────────
    var logoVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var cardsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        logoVisible = true
        delay(250)
        titleVisible = true
        delay(200)
        cardsVisible = true
    }

    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "logo_alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val titleOffsetY by animateFloatAsState(
        targetValue = if (titleVisible) 0f else 60f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "title_offset"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "title_alpha"
    )

    val cardsOffsetY by animateFloatAsState(
        targetValue = if (cardsVisible) 0f else 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cards_offset"
    )
    val cardsAlpha by animateFloatAsState(
        targetValue = if (cardsVisible) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "cards_alpha"
    )

    // ── UI ──────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to LlegoPrimary,
                        0.45f to LlegoPrimary.copy(alpha = 0.92f),
                        0.65f to Color(0xFF034547),
                        1.0f to Color(0xFF023133)
                    )
                )
            )
    ) {
        // Radial accent glow
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            LlegoAccent.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.12f))

            // ── Logo ────────────────────────────
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = logoAlpha
                        scaleX = logoScale
                        scaleY = logoScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Glow behind logo
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    LlegoSecondary.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "Logo Llego",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Title & Subtitle ────────────────
            Column(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = titleAlpha
                        translationY = titleOffsetY
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido a",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.3).sp
                    ),
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = "Llego Negocios",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "La plataforma que impulsa tu negocio\ny lo conecta con miles de clientes",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // ── Option Cards ────────────────────
            Column(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = cardsAlpha
                        translationY = cardsOffsetY
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: New user
                WelcomeOptionCard(
                    icon = Icons.Default.RocketLaunch,
                    title = "Quiero unirme a Llego",
                    subtitle = "Crea tu negocio paso a paso en minutos",
                    accentColor = LlegoAccent,
                    isPrimary = true,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNewUser()
                    }
                )

                // Card 2: Existing user
                WelcomeOptionCard(
                    icon = Icons.Default.Storefront,
                    title = "Ya tengo un negocio",
                    subtitle = "Inicia sesion para administrar tu negocio",
                    accentColor = LlegoSecondary,
                    isPrimary = false,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onExistingUser()
                    }
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // ── Footer ──────────────────────────
            Text(
                text = "Al continuar, aceptas nuestros terminos y condiciones",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────
// Welcome Option Card
// ─────────────────────────────────────────────────

@Composable
private fun WelcomeOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    val containerColor = if (isPrimary) {
        Color.White
    } else {
        Color.White.copy(alpha = 0.08f)
    }

    val contentColor = if (isPrimary) {
        LlegoPrimary
    } else {
        Color.White
    }

    val subtitleColor = if (isPrimary) {
        LlegoPrimary.copy(alpha = 0.6f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }

    val borderColor = if (isPrimary) {
        Color.Transparent
    } else {
        Color.White.copy(alpha = 0.12f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = if (!isPrimary) {
            androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        } else null,
        shadowElevation = if (isPrimary) 6.dp else 0.dp,
        tonalElevation = if (isPrimary) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isPrimary) {
                            Brush.linearGradient(
                                colors = listOf(
                                    LlegoPrimary,
                                    LlegoPrimary.copy(alpha = 0.85f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.White.copy(alpha = 0.06f)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isPrimary) Color.White else accentColor
                )
            }

            // Texts
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 18.sp
                    ),
                    color = subtitleColor
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isPrimary) LlegoPrimary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.3f)
            )
        }
    }
}
