package com.llego.shared.ui.auth

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.llego.shared.data.auth.rememberAppleSignInHelper
import com.llego.shared.data.auth.rememberGoogleSignInHelper
import com.llego.shared.data.model.BusinessType
import com.llego.shared.data.model.getBusinessType
import com.llego.shared.ui.auth.components.AppTipsSection
import com.llego.shared.ui.auth.components.LlegoLogo
import com.llego.shared.ui.auth.components.SocialButtons
import com.llego.shared.ui.components.background.CurvedBackground
import kotlinx.coroutines.delay

/**
 * Pantalla de login moderna con diseño mejorado:
 * - Logo fijo en fondo verde (no hace scroll)
 * - Card blanco con contenido scrolleable
 * - Campos con fondo gris claro
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (BusinessType) -> Unit,
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

    // NOTA: Ya no navegamos desde LoginScreen
    // La navegación se maneja en App.kt que usa AuthManager.getCurrentBusinessType()
    // para obtener datos reales del backend

    // Si el usuario está autenticado, llamamos onLoginSuccess con un BusinessType temporal
    // pero la navegación real se decidirá en App.kt basado en datos del backend
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated && uiState.user != null) {
            // Usar RESTAURANT como placeholder - App.kt usará el tipo real
            onLoginSuccess(BusinessType.RESTAURANT)
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF023133).copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 100f
                        )
                    )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .graphicsLayer { translationY = headerOffsetY },
                contentAlignment = Alignment.Center
            ) {
                LlegoLogo()
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer { translationY = cardOffsetY },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    topStart = 32.dp,
                    topEnd = 32.dp
                ),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "Bienvenido a Llego",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                )
                            )
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Inicia sesión para gestionar tu negocio",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(48.dp))

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

                    Spacer(modifier = Modifier.height(48.dp))

                    // Tips de la app
                    AppTipsSection()

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
