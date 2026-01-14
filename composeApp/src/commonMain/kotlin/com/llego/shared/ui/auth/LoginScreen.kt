package com.llego.shared.ui.auth

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.llego.shared.data.auth.rememberAppleSignInHelper
import com.llego.shared.data.auth.rememberGoogleSignInHelper
import com.llego.shared.ui.auth.components.AppTipsSection
import com.llego.shared.ui.auth.components.SocialButtons
import kotlinx.coroutines.delay

/**
 * Pantalla de login moderna con diseno mejorado:
 * - Logo fijo en fondo verde (no hace scroll)
 * - Card blanco con contenido scrolleable
 * - Campos con fondo gris claro
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var headerVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }

    val headerOffsetY by animateFloatAsState(
        targetValue = if (headerVisible) 0f else -300f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "header_offset"
    )

    val cardOffsetY by animateFloatAsState(
        targetValue = if (cardVisible) 0f else 1000f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_offset"
    )

    LaunchedEffect(Unit) {
        headerVisible = true
        delay(100)
        cardVisible = true
    }

    // La navegacion se maneja completamente en App.kt basado en:
    // - isAuthenticated: si el usuario tiene sesion
    // - needsBusinessRegistration: si el usuario necesita registrar un negocio
    // - currentBranch: si el usuario tiene una sucursal seleccionada

    // Si el usuario esta autenticado, notificamos al padre
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated && uiState.user != null) {
            onLoginSuccess()
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val cardHeight = maxHeight * 0.72f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to MaterialTheme.colorScheme.primary,
                            0.55f to MaterialTheme.colorScheme.primaryContainer,
                            1.0f to MaterialTheme.colorScheme.primary
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            radius = 600f
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Header centrado verticalmente en el espacio disponible
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                        .graphicsLayer { translationY = headerOffsetY },
                    contentAlignment = Alignment.Center
                ) {
                    LoginHeader()
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight)
                        .graphicsLayer { translationY = cardOffsetY },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp
                    ),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    val termsText = buildAnnotatedString {
                        append("Al iniciar sesion, aceptas nuestros ")
                        pushStringAnnotation(tag = "terms", annotation = "terms")
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("terminos y condiciones")
                        }
                        pop()
                        append(" y nuestra ")
                        pushStringAnnotation(tag = "privacy", annotation = "privacy")
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("politica de privacidad")
                        }
                        pop()
                        append(".")
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp)
                                .padding(top = 48.dp, bottom = 24.dp)
                                .imePadding(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Botones de OAuth - Google y Apple Sign-In
                            val googleSignInHelper = rememberGoogleSignInHelper()
                            val appleSignInHelper = rememberAppleSignInHelper()

                            // Estado para mostrar errores de OAuth
                            var oauthError by remember { mutableStateOf<String?>(null) }

                            // Mostrar error de OAuth si existe
                            oauthError?.let { error ->
                                androidx.compose.material3.AlertDialog(
                                    onDismissRequest = { oauthError = null },
                                    title = { Text("Error de autenticacion") },
                                    text = { Text(error) },
                                    confirmButton = {
                                        androidx.compose.material3.TextButton(
                                            onClick = { oauthError = null }
                                        ) {
                                            Text("Aceptar")
                                        }
                                    }
                                )
                            }

                            SocialButtons(
                                onGoogleClick = {
                                    googleSignInHelper.signIn(
                                        onSuccess = { idToken, nonce ->
                                            viewModel.loginWithGoogle(idToken, nonce)
                                        },
                                        onError = { errorMessage ->
                                            oauthError = errorMessage
                                            println("Error Google Sign-In: $errorMessage")
                                        }
                                    )
                                },
                                onAppleClick = {
                                    appleSignInHelper.signIn(
                                        onSuccess = { identityToken, nonce ->
                                            viewModel.loginWithApple(identityToken, nonce)
                                        },
                                        onError = { errorMessage ->
                                            oauthError = errorMessage
                                            println("Error Google Sign-In: $errorMessage")
                                        }
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(56.dp))

                            // Tips de la app
                            AppTipsSection()

                            Spacer(modifier = Modifier.height(24.dp))

                            // Texto de términos y condiciones (ahora dentro del scroll)
                            ClickableText(
                                text = termsText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { offset ->
                                    termsText.getStringAnnotations(
                                        tag = "terms",
                                        start = offset,
                                        end = offset
                                    ).firstOrNull()?.let {
                                        // TODO: Mostrar Terminos y Condiciones
                                    }

                                    termsText.getStringAnnotations(
                                        tag = "privacy",
                                        start = offset,
                                        end = offset
                                    ).firstOrNull()?.let {
                                        // TODO: Mostrar Politica de Privacidad
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginHeader() {
    val fullTitle = "Bienvenido a\nLlego Negocios"
    val haptic = LocalHapticFeedback.current

    var displayedTitle by remember { mutableStateOf("") }
    var typingDone by remember { mutableStateOf(false) }
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        displayedTitle = ""
        typingDone = false

        for (character in fullTitle) {
            delay(90)
            displayedTitle += character
            if (!character.isWhitespace()) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }

        typingDone = true
        cursorVisible = false
    }

    LaunchedEffect(typingDone) {
        while (!typingDone) {
            delay(500)
            cursorVisible = !cursorVisible
        }
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
        Text(
            text = fullTitle,
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.alpha(0f)
        )

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = displayedTitle,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
            if (!typingDone && cursorVisible) {
                Text(
                    text = "|",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

