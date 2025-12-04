package com.llego.shared.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import com.llego.shared.data.model.BusinessType
import com.llego.shared.ui.auth.components.DividerWithText
import com.llego.shared.ui.auth.components.LlegoLogo
import com.llego.shared.ui.auth.components.LoginForm
import com.llego.shared.ui.auth.components.RegisterForm
import com.llego.shared.ui.auth.components.SocialButtons
import com.llego.shared.ui.auth.components.ToggleAuthMode
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
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val selectedBusinessType by viewModel.selectedBusinessType.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }

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

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated && uiState.currentUser != null) {
            onLoginSuccess(uiState.currentUser!!.businessType)
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
                    .height(220.dp)
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
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = if (isRegisterMode) "Crea tu cuenta" else "Inicia sesión",
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

                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedContent(
                        targetState = isRegisterMode,
                        transitionSpec = {
                            if (targetState) {
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth -> fullWidth },
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) togetherWith slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -fullWidth },
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                )
                            } else {
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth -> -fullWidth },
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) togetherWith slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> fullWidth },
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                )
                            }
                        },
                        label = "auth_mode_transition"
                    ) { targetIsRegister ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (targetIsRegister) {
                                RegisterForm(
                                    email = email,
                                    password = password,
                                    onEmailChange = viewModel::updateEmail,
                                    onPasswordChange = viewModel::updatePassword,
                                    onRegisterClick = viewModel::login,
                                    isLoading = uiState.isLoading,
                                    errorMessage = loginError,
                                    selectedBusinessType = selectedBusinessType,
                                    onBusinessTypeSelected = viewModel::selectBusinessType
                                )
                            } else {
                                LoginForm(
                                    email = email,
                                    password = password,
                                    onEmailChange = viewModel::updateEmail,
                                    onPasswordChange = viewModel::updatePassword,
                                    onContinueClick = viewModel::login,
                                    isLoading = uiState.isLoading,
                                    errorMessage = loginError
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            ToggleAuthMode(
                                isRegisterMode = targetIsRegister,
                                onToggle = { isRegisterMode = !isRegisterMode }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
