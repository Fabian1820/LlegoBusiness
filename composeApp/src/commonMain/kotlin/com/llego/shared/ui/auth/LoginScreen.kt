package com.llego.shared.ui.auth

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.llego.shared.data.auth.rememberAppleSignInHelper
import com.llego.shared.data.auth.rememberGoogleSignInHelper
import com.llego.shared.ui.auth.components.AppTipsSection
import com.llego.shared.ui.auth.components.LlegoLogo
import com.llego.shared.ui.auth.components.SocialButtons
import com.llego.shared.ui.components.background.CurvedBackground
import com.llego.shared.ui.components.molecules.LlegoConfirmationDefaults
import com.llego.shared.ui.components.molecules.LlegoConfirmationScreen
import kotlinx.coroutines.delay

/**
 * Pantalla de login moderna con diseño mejorado:
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

    // La navegación se maneja completamente en App.kt basado en:
    // - isAuthenticated: si el usuario tiene sesión
    // - needsBusinessRegistration: si el usuario necesita registrar un negocio
    // - currentBranch: si el usuario tiene una sucursal seleccionada

    // Si el usuario está autenticado, notificamos al padre
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated && uiState.user != null) {
            onLoginSuccess()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            CurvedBackground(
                curveStart = 0.18f,
                curveEnd = 0.18f,
                curveInclination = 0.06f,
                showCurve = true
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .graphicsLayer { translationY = headerOffsetY },
                contentAlignment = Alignment.Center
            ) {
                LlegoLogo(modifier = Modifier.size(120.dp))
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer { translationY = cardOffsetY },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp
                ),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Bienvenido a Llego",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Inicia sesión para gestionar tu negocio",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botones de OAuth - Google y Apple Sign-In
                    val googleSignInHelper = rememberGoogleSignInHelper()
                    val appleSignInHelper = rememberAppleSignInHelper()

                    // Estado para mostrar errores de OAuth
                    var oauthError by remember { mutableStateOf<String?>(null) }
                    
                    // Mostrar error de OAuth si existe
                    oauthError?.let { error ->
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { oauthError = null },
                            title = { Text("Error de autenticación") },
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
                                    println("Error Apple Sign-In: $errorMessage")
                                }
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Tips de la app
                    AppTipsSection()

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
