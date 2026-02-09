package com.llego.shared.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.llego.shared.ui.onboarding.components.InfoHighlightCard
import com.llego.shared.ui.onboarding.components.PageDotIndicator
import com.llego.shared.ui.theme.LlegoAccent
import com.llego.shared.ui.theme.LlegoPrimary
import com.llego.shared.ui.theme.LlegoSecondary
import kotlinx.coroutines.delay
import llegobusiness.composeapp.generated.resources.Res
import llegobusiness.composeapp.generated.resources.onboarding_branch
import llegobusiness.composeapp.generated.resources.onboarding_business
import llegobusiness.composeapp.generated.resources.onboarding_start
import org.jetbrains.compose.resources.painterResource

/**
 * Pantallas introductorias del onboarding para nuevos usuarios.
 *
 * Presenta 3 paginas animadas que explican como funcionan los negocios
 * y las sucursales en Llego, antes de redirigir al usuario a crear
 * su cuenta y su primer negocio.
 *
 * Diseno inspirado en Apple: transiciones suaves, tipografia limpia,
 * gradientes elegantes e iconografia personalizada.
 */

private data class IntroPage(
    val title: String,
    val description: String,
    val highlights: List<Pair<ImageVector, String>>,
    val drawableKey: String
)

private val introPages = listOf(
    IntroPage(
        title = "Tu negocio en Llego",
        description = "El negocio es la cara de tu empresa en la plataforma. " +
                "Es lo primero que ven tus clientes y donde se refleja la identidad de tu marca.",
        highlights = listOf(
            Icons.Default.Visibility to "Los clientes descubren tu marca y tus productos",
            Icons.Default.Inventory to "Gestiona todo desde un solo lugar"
        ),
        drawableKey = "business"
    ),
    IntroPage(
        title = "Las sucursales",
        description = "Cada sucursal es una sede de tu negocio. " +
                "Es donde los clientes ven el catalogo de productos, realizan pedidos y reciben entregas.",
        highlights = listOf(
            Icons.Default.ShoppingCart to "Los clientes hacen pedidos en cada sucursal",
            Icons.Default.Inventory to "Cada sucursal tiene su propio catalogo y horario"
        ),
        drawableKey = "branch"
    ),
    IntroPage(
        title = "Crea tu primer negocio",
        description = "Te guiaremos paso a paso para configurar tu negocio y tu primera sucursal. " +
                "Solo necesitas unos minutos para empezar a recibir pedidos.",
        highlights = emptyList(),
        drawableKey = "start"
    )
)

@Composable
fun OnboardingIntroScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = introPages.size

    // Entrance animation
    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        screenVisible = true
    }

    val screenAlpha by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "screen_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer { alpha = screenAlpha }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Top Bar ─────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    if (currentPage > 0) {
                        currentPage--
                    } else {
                        onBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Skip button (only on non-last pages)
                if (currentPage < totalPages - 1) {
                    TextButton(onClick = {
                        currentPage = totalPages - 1
                    }) {
                        Text(
                            text = "Omitir",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // ── Page Content ────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        val direction = if (targetState > initialState) {
                            AnimatedContentTransitionScope.SlideDirection.Left
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.Right
                        }
                        slideIntoContainer(
                            towards = direction,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(
                            animationSpec = tween(300)
                        ) togetherWith slideOutOfContainer(
                            towards = direction,
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(
                            animationSpec = tween(200)
                        )
                    },
                    label = "intro_page_transition"
                ) { page ->
                    IntroPageLayout(
                        page = introPages[page],
                        pageIndex = page
                    )
                }
            }

            // ── Bottom Section ──────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Dot indicator
                PageDotIndicator(
                    pageCount = totalPages,
                    currentPage = currentPage
                )

                // Action button
                val isLastPage = currentPage == totalPages - 1

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isLastPage) {
                            onContinue()
                        } else {
                            currentPage++
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LlegoPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = if (isLastPage) "Empezar" else "Siguiente",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────
// Individual Intro Page
// ─────────────────────────────────────────────────

@Composable
private fun IntroPageLayout(
    page: IntroPage,
    pageIndex: Int
) {
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(pageIndex) {
        contentVisible = false
        delay(100)
        contentVisible = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "content_alpha_$pageIndex"
    )

    val contentOffsetY by animateFloatAsState(
        targetValue = if (contentVisible) 0f else 30f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "content_offset_$pageIndex"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .graphicsLayer {
                alpha = contentAlpha
                translationY = contentOffsetY
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.08f))

        // ── Illustration ────────────────────────
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Subtle glow circle behind icon
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                when (pageIndex) {
                                    0 -> LlegoSecondary.copy(alpha = 0.15f)
                                    1 -> LlegoAccent.copy(alpha = 0.15f)
                                    else -> LlegoPrimary.copy(alpha = 0.1f)
                                },
                                Color.Transparent
                            )
                        )
                    )
            )

            val painter = when (page.drawableKey) {
                "business" -> painterResource(Res.drawable.onboarding_business)
                "branch" -> painterResource(Res.drawable.onboarding_branch)
                else -> painterResource(Res.drawable.onboarding_start)
            }

            Image(
                painter = painter,
                contentDescription = page.title,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // ── Title ────────────────────────────────
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
                letterSpacing = (-0.3).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ── Description ──────────────────────────
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 26.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        // ── Highlight cards ──────────────────────
        if (page.highlights.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                page.highlights.forEachIndexed { index, (icon, text) ->
                    var highlightVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(pageIndex) {
                        highlightVisible = false
                        delay(350L + index * 150L)
                        highlightVisible = true
                    }

                    val highlightAlpha by animateFloatAsState(
                        targetValue = if (highlightVisible) 1f else 0f,
                        animationSpec = tween(350),
                        label = "highlight_alpha_${pageIndex}_$index"
                    )
                    val highlightOffsetX by animateFloatAsState(
                        targetValue = if (highlightVisible) 0f else 40f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "highlight_offset_${pageIndex}_$index"
                    )

                    Box(
                        modifier = Modifier.graphicsLayer {
                            alpha = highlightAlpha
                            translationX = highlightOffsetX
                        }
                    ) {
                        InfoHighlightCard(
                            icon = icon,
                            text = text
                        )
                    }
                }
            }
        } else {
            // Last page — show motivational mini-stats
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStatBadge(
                    value = "5 min",
                    label = "Configuracion",
                    color = LlegoPrimary
                )
                MiniStatBadge(
                    value = "Gratis",
                    label = "Para empezar",
                    color = LlegoAccent
                )
                MiniStatBadge(
                    value = "24/7",
                    label = "Soporte",
                    color = LlegoSecondary
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.12f))
    }
}

// ─────────────────────────────────────────────────
// Mini Stat Badge (used on last intro page)
// ─────────────────────────────────────────────────

@Composable
private fun MiniStatBadge(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LlegoPrimary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
