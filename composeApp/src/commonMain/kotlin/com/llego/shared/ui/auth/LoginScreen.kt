package com.llego.shared.ui.auth

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import com.llego.shared.ui.theme.LlegoAccentPrimary
import com.llego.shared.ui.theme.LlegoAccentSecondary
import com.llego.shared.ui.theme.LlegoPrimary
import com.llego.shared.data.auth.rememberAppleSignInHelper
import com.llego.shared.data.auth.rememberGoogleSignInHelper
import com.llego.shared.ui.auth.components.AppTipsSection
import com.llego.shared.ui.auth.components.SocialButtons
import com.llego.shared.ui.components.atoms.LoadingOverlay
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
    val loginError by viewModel.loginError.collectAsState()

    var headerVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }
    var oauthError by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }
    
    // Mostrar loading desde que inicia autenticación hasta que se complete la navegación
    val showLoading = isAuthenticating || (uiState.isAuthenticated && uiState.user != null)

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
    // pero mantenemos el loading visible
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated && uiState.user != null) {
            onLoginSuccess()
        }
    }
    
    // Resetear el estado de autenticación si hay error
    LaunchedEffect(uiState.error, loginError, oauthError) {
        if (uiState.error != null || loginError != null || oauthError != null) {
            isAuthenticating = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
        val authError = remember(oauthError, loginError, uiState.error) {
            val rawError = oauthError ?: loginError ?: uiState.error
            rawError?.toFriendlyAuthErrorMessage()
        }

        val cardHeight = maxHeight * 0.72f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to LlegoPrimary,
                            0.55f to LlegoAccentPrimary,
                            1.0f to LlegoPrimary
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
                                LlegoAccentSecondary.copy(alpha = 0.35f),
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
                                .padding(horizontal = 24.dp)
                                .padding(top = 32.dp, bottom = 12.dp)
                                .imePadding(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val googleSignInHelper = rememberGoogleSignInHelper()
                            val appleSignInHelper = rememberAppleSignInHelper()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                AnimatedVisibility(
                                    visible = authError != null,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "No pudimos iniciar sesion",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                                Text(
                                                    text = authError.orEmpty(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    oauthError = null
                                                    viewModel.clearLoginError()
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Cerrar",
                                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                }

                                SocialButtons(
                                    onGoogleClick = {
                                        isAuthenticating = true
                                        googleSignInHelper.signIn(
                                            onSuccess = { idToken, nonce ->
                                                oauthError = null
                                                viewModel.loginWithGoogle(idToken, nonce)
                                            },
                                            onError = { errorMessage ->
                                                isAuthenticating = false
                                                if (errorMessage.isUserAuthCancellation()) {
                                                    oauthError = null
                                                    viewModel.clearLoginError()
                                                } else {
                                                    oauthError = errorMessage
                                                }
                                            }
                                        )
                                    },
                                    onAppleClick = {
                                        isAuthenticating = true
                                        appleSignInHelper.signIn(
                                            onSuccess = { identityToken, nonce ->
                                                oauthError = null
                                                viewModel.loginWithApple(identityToken, nonce)
                                            },
                                            onError = { errorMessage ->
                                                isAuthenticating = false
                                                if (errorMessage.isUserAuthCancellation()) {
                                                    oauthError = null
                                                    viewModel.clearLoginError()
                                                } else {
                                                    oauthError = errorMessage
                                                }
                                            }
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(48.dp))
                                AppTipsSection()
                                Spacer(modifier = Modifier.height(24.dp))
                                ClickableText(
                                    text = termsText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
        }
        
        // Overlay de carga encima del contenido
        if (showLoading) {
            LoadingOverlay(message = "Cargando")
        }
    }
}

private fun String.isUserAuthCancellation(): Boolean {
    val lower = lowercase()
    return lower.contains("cancel") ||
            lower.contains("canceled") ||
            lower.contains("12501")
}

private fun String.toFriendlyAuthErrorMessage(): String {
    val normalized = this
        .replace("Ã¡", "a")
        .replace("Ã©", "e")
        .replace("Ã­", "i")
        .replace("Ã³", "o")
        .replace("Ãº", "u")
        .replace("Ã±", "n")
        .replace("Â¿", "")
        .replace("Â¡", "")

    val lower = normalized.lowercase()
    return when {
        lower.contains("cancel") -> "El inicio de sesion fue cancelado. Puedes intentarlo otra vez."
        lower.contains("timeout") || lower.contains("conexion") || lower.contains("network") -> {
            "No hay conexion con el servidor en este momento. Revisa tu internet e intentalo nuevamente."
        }

        lower.contains("token") || lower.contains("unauthorized") || lower.contains("401") -> {
            "Tu sesion no pudo validarse. Intenta iniciar sesion nuevamente."
        }

        lower.contains("credencial") || lower.contains("password") || lower.contains("contras") -> {
            "Tus datos de acceso no fueron aceptados. Verifica la cuenta e intenta otra vez."
        }

        else -> "Ocurrio un problema al autenticar tu cuenta. Intenta nuevamente en unos segundos."
    }
}

@Composable
private fun LoginHeader() {
    val fullTitle = "Bienvenido a\nLlego Negocios"
    val haptic = LocalHapticFeedback.current
    val headerStyle = MaterialTheme.typography.displayLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp
    )

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
            style = headerStyle,
            color = Color.White,
            modifier = Modifier.alpha(0f)
        )

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = displayedTitle,
                style = headerStyle,
                color = Color.White
            )
            if (!typingDone && cursorVisible) {
                Text(
                    text = "|",
                    style = headerStyle,
                    color = Color.White
                )
            }
        }
    }
}
