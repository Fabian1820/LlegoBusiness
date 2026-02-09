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
import llegobusiness.composeapp.generated.resources.logo
import llegobusiness.composeapp.generated.resources.onboarding_branch
import llegobusiness.composeapp.generated.resources.onboarding_business
import llegobusiness.composeapp.generated.resources.onboarding_start
import org.jetbrains.compose.resources.painterResource

/**
 * Pantallas introductorias del onboarding.
 *
 * Pagina 0 es la bienvenida con el logo de Llego (reemplaza al antiguo WelcomeScreen).
 * Las paginas siguientes explican como funcionan los negocios y sucursales.
 *
 * Todas las paginas muestran "Omitir" para saltar directo al login.
 * El boton principal avanza de pagina; en la ultima pagina lleva al login.
 */

// ─────────────────────────────────────────────────
//  Page model
// ─────────────────────────────────────────────────

private sealed class IntroPageData {
    /** Pagina 0 — bienvenida con logo */
    data object Welcome : IntroPageData()

    /** Paginas informativas con ilustracion vectorial */
    data class Info(
        val title: String,
        val description: String,
        val highlights: List<Pair<ImageVector, String>>,
        val drawableKey: String
    ) : IntroPageData()
}

private val introPages: List<IntroPageData> = listOf(
    IntroPageData.Welcome,
    IntroPageData.Info(
        title = "Tu negocio en Llego",
        description = "El negocio es la cara de tu empresa en la plataforma. " +
                "Es lo primero que ven tus clientes y donde se refleja la identidad de tu marca.",
        highlights = listOf(
            Icons.Default.Visibility to "Los clientes descubren tu marca y tus productos",
            Icons.Default.Inventory to "Gestiona todo desde un solo lugar"
        ),
        drawableKey = "business"
    ),
    IntroPageData.Info(
        title = "Las sucursales",
        description = "Cada sucursal es una sede de tu negocio. " +
                "Es donde los clientes ven el catalogo de productos, realizan pedidos y reciben entregas.",
        highlights = listOf(
            Icons.Default.ShoppingCart to "Los clientes hacen pedidos en cada sucursal",
            Icons.Default.Inventory to "Cada sucursal tiene su propio catalogo y horario"
        ),
        drawableKey = "branch"
    ),
    IntroPageData.Info(
        title = "Crea tu primer negocio",
        description = "Te guiaremos paso a paso para configurar tu negocio y tu primera sucursal. " +
                "Solo necesitas unos minutos para empezar a recibir pedidos.",
        highlights = emptyList(),
        drawableKey = "start"
    )
)

// ─────────────────────────────────────────────────
//  Public composable
// ─────────────────────────────────────────────────

/**
 * @param onFinish se llama cuando el usuario termina o salta el onboarding.
 *                 El llamador debe navegar al login.
 */
@Composable
fun OnboardingIntroScreen(
    onFinish: () -> Unit,
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
            // ── Top bar ─────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back arrow (hidden on page 0)
                if (currentPage > 0) {
                    IconButton(onClick = { currentPage-- }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Omitir — always visible, skips to login
                TextButton(onClick = { onFinish() }) {
                    Text(
                        text = "Omitir",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Page content ────────────────────────
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
                    when (val data = introPages[page]) {
                        is IntroPageData.Welcome -> WelcomePageLayout()
                        is IntroPageData.Info -> InfoPageLayout(page = data, pageIndex = page)
                    }
                }
            }

            // ── Bottom section ──────────────────────
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

                // Primary button
                val isLastPage = currentPage == totalPages - 1

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isLastPage) {
                            onFinish()
                        } else {
                            currentPage++
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
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

// ═════════════════════════════════════════════════
//  Page 0 — Welcome
// ═════════════════════════════════════════════════

@Composable
private fun WelcomePageLayout() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120)
        visible = true
    }

    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "welcome_logo_scale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "welcome_content_alpha"
    )

    val contentOffsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 50f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "welcome_content_offset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .graphicsLayer {
                alpha = contentAlpha
                translationY = contentOffsetY
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.15f))

        // Logo with glow
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = logoScale
                scaleY = logoScale
            }
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                LlegoSecondary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Logo Llego",
                modifier = Modifier.size(110.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = "Bienvenido a",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.3).sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Llego Negocios",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "La plataforma que impulsa tu negocio\ny lo conecta con miles de clientes",
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 26.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Badges row
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

        Spacer(modifier = Modifier.weight(0.2f))
    }
}

// ═════════════════════════════════════════════════
//  Info pages (1-3)
// ═════════════════════════════════════════════════

@Composable
private fun InfoPageLayout(
    page: IntroPageData.Info,
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
            // Glow circle behind icon
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                when (pageIndex) {
                                    1 -> LlegoSecondary.copy(alpha = 0.15f)
                                    2 -> LlegoAccent.copy(alpha = 0.15f)
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
        }

        Spacer(modifier = Modifier.weight(0.12f))
    }
}

// ─────────────────────────────────────────────────
//  Mini Stat Badge
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
                    color = MaterialTheme.colorScheme.primary
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
