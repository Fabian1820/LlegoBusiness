package com.llego.shared.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.llego.shared.data.model.BusinessType
import com.llego.shared.ui.components.background.CurvedBackground
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource
import llegobusiness.composeapp.generated.resources.Res
import llegobusiness.composeapp.generated.resources.apple
import llegobusiness.composeapp.generated.resources.google
import llegobusiness.composeapp.generated.resources.logo

/**
 * Pantalla de login moderna con diseño mejorado
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

    // Estados de animación de entrada
    var headerVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }

    // Animaciones de entrada al cargar la pantalla
    val headerOffsetY by animateFloatAsState(
        targetValue = if (headerVisible) 0f else -300f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val cardOffsetY by animateFloatAsState(
        targetValue = if (cardVisible) 0f else 1000f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Iniciar animaciones de entrada
    LaunchedEffect(Unit) {
        headerVisible = true
        delay(100)
        cardVisible = true
    }

    // Manejar login exitoso
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated && uiState.currentUser != null) {
            onLoginSuccess(uiState.currentUser!!.businessType)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fondo verde superior fijo con degradado en bordes
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CurvedBackground(
                curveStart = 0.18f,
                curveEnd = 0.18f,
                curveInclination = 0.06f,
                showCurve = true
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }

            // Degradado sutil en los bordes superiores
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
            // Header fijo con logo (no scroll) - ANIMADO DE ARRIBA A ABAJO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .graphicsLayer {
                        translationY = headerOffsetY
                    },
                contentAlignment = Alignment.Center
            ) {
                LlegoLogo()
            }

            // Card blanco con contenido scrolleable (incluye título) - ANIMADO DE ABAJO A ARRIBA
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = cardOffsetY
                    },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                // Todo el contenido scrolleable (incluye título)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Título con degradado sutil - AHORA ES SCROLLEABLE
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

                    // TRANSICIÓN ANIMADA DE TODO EL CONTENIDO
                    // Login -> Registro: TODO el contenido se desplaza a la IZQUIERDA
                    // Registro -> Login: TODO el contenido se desplaza a la DERECHA
                    AnimatedContent(
                        targetState = isRegisterMode,
                        transitionSpec = {
                            if (targetState) {
                                // Ir a REGISTRO: todo sale a la izquierda, registro entra desde la derecha
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth -> fullWidth }, // Entra desde la derecha completa
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) togetherWith
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> -fullWidth }, // Sale completamente a la izquierda
                                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                                        )
                            } else {
                                // Volver a LOGIN: todo sale a la derecha, login entra desde la izquierda
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth -> -fullWidth }, // Entra desde la izquierda completa
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) togetherWith
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth }, // Sale completamente a la derecha
                                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                                        )
                            }
                        }
                    ) { targetIsRegister ->
                        // TODO EL CONTENIDO SE ANIMA JUNTO
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Formulario (login o registro)
                            if (targetIsRegister) {
                                RegisterForm(
                                    email = email,
                                    password = password,
                                    onEmailChange = viewModel::updateEmail,
                                    onPasswordChange = viewModel::updatePassword,
                                    onRegisterClick = viewModel::login, // TODO: Implementar registro real
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

                            // Botones sociales (solo en login)
                            if (!targetIsRegister) {
                                // Divider con "or"
                                DividerWithText(text = "o")

                                Spacer(modifier = Modifier.height(32.dp))

                                // Botones sociales
                                SocialButtons()

                                Spacer(modifier = Modifier.height(48.dp))
                            }

                            // Toggle entre login y registro
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

// ============================================================================
// COMPONENTES DE UI
// ============================================================================

/**
 * Logo de Llego con bordes redondeados
 */
@Composable
private fun LlegoLogo() {
    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.size(100.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = "Logo Llego",
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Formulario de login con email y contraseña
 * NOTA: Campos opcionales para testing rápido
 */
@Composable
private fun LoginForm(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onContinueClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    // Contenedor centrado con ancho reducido
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f), // 90% del ancho para campos más estrechos
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo de email
            ModernTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Correo electrónico",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = errorMessage != null
            )

            // Campo de contraseña
            ModernTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Contraseña",
                leadingIcon = Icons.Default.Lock,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (!isLoading) onContinueClick() }
                ),
                isPassword = true,
                isError = errorMessage != null
            )

            // Link "Olvidaste tu contraseña?" - Texto más pequeño sin ripple
            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    fontSize = 13.sp
                ),
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(
                        onClick = { /* TODO: Navegar a recuperar contraseña */ },
                        indication = null, // Sin efecto ripple
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )

            // Botón Iniciar sesión
            PrimaryButton(
                text = "Iniciar sesión",
                onClick = onContinueClick,
                enabled = !isLoading,
                isLoading = isLoading
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

/**
 * Formulario de registro completo con campos reales
 */
@Composable
private fun RegisterForm(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    selectedBusinessType: BusinessType?,
    onBusinessTypeSelected: (BusinessType) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector de tipo de negocio
        Text(
            text = "Tipo de negocio",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        BusinessTypeChips(
            selectedBusinessType = selectedBusinessType,
            onBusinessTypeSelected = onBusinessTypeSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre del negocio
        ModernTextField(
            value = businessName,
            onValueChange = { businessName = it },
            placeholder = "Nombre del negocio",
            leadingIcon = Icons.Default.Store,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Nombre del contacto/responsable
        ModernTextField(
            value = contactName,
            onValueChange = { contactName = it },
            placeholder = "Nombre del responsable",
            leadingIcon = Icons.Default.Person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Teléfono
        ModernTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = "Teléfono",
            leadingIcon = Icons.Default.Phone,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )

        // Dirección
        ModernTextField(
            value = address,
            onValueChange = { address = it },
            placeholder = "Dirección",
            leadingIcon = Icons.Default.LocationOn,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Email
        ModernTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Correo electrónico",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = errorMessage != null
        )

        // Contraseña
        ModernTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Contraseña",
            leadingIcon = Icons.Default.Lock,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            isPassword = true
        )

        // Confirmar contraseña
        ModernTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Confirmar contraseña",
            leadingIcon = Icons.Default.Lock,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (!isLoading) onRegisterClick() }
            ),
            isPassword = true
        )

        // Botón Registrarse
        PrimaryButton(
            text = "Registrarse",
            onClick = onRegisterClick,
            enabled = !isLoading &&
                      email.isNotBlank() &&
                      password.isNotBlank() &&
                      businessName.isNotBlank() &&
                      contactName.isNotBlank() &&
                      phone.isNotBlank() &&
                      address.isNotBlank() &&
                      confirmPassword == password &&
                      selectedBusinessType != null,
            isLoading = isLoading
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/**
 * Selector de tipo de negocio con chips horizontales - texto más pequeño
 */
@Composable
private fun BusinessTypeChips(
    selectedBusinessType: BusinessType?,
    onBusinessTypeSelected: (BusinessType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chip Restaurant
        SelectableChip(
            text = "🍽️ Restaurante",
            isSelected = selectedBusinessType == BusinessType.RESTAURANT,
            onClick = { onBusinessTypeSelected(BusinessType.RESTAURANT) },
            modifier = Modifier.weight(1f)
        )

        // Chip Market/Supermercado
        SelectableChip(
            text = "🛒 Supermercado",
            isSelected = selectedBusinessType == BusinessType.GROCERY,
            onClick = { onBusinessTypeSelected(BusinessType.GROCERY) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Chip seleccionable con elevación y sombra
 * Color secundario cuando está seleccionado
 */
@Composable
private fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isSelected) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        shadowElevation = if (isSelected) 6.dp else 3.dp, // Elevación con sombra
        tonalElevation = if (isSelected) 4.dp else 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp
                ),
                color = if (isSelected) Color(0xFF023133) // Verde oscuro para contraste con dorado
                        else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * TextField moderno con fondo gris claro más visible
 * Altura reducida para diseño compacto
 */
@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isPassword: Boolean = false,
    isError: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E)
            )
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp) // Ícono más pequeño
                )
            }
        },
        trailingIcon = {
            when {
                isPassword -> {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility
                                         else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                value.isNotEmpty() && !isPassword -> {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFEEEEEE),
            focusedContainerColor = Color(0xFFE8E8E8),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        ),
        isError = isError,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Botón principal con altura reducida
 */
@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp), // Altura reducida de 56dp a 48dp
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp), // Tamaño reducido
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

/**
 * Divider con texto "o" al centro
 */
@Composable
private fun DividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

/**
 * Botones sociales (Google y Apple) - Solo para login
 * Diseño minimalista con línea inferior
 */
@Composable
private fun SocialButtons() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón Google
        SocialButton(
            text = "Continuar con Google",
            icon = Res.drawable.google,
            onClick = { /* TODO: Implementar Google Sign In */ }
        )

        // Botón Apple
        SocialButton(
            text = "Continuar con Apple",
            icon = Res.drawable.apple,
            onClick = { /* TODO: Implementar Apple Sign In */ }
        )
    }
}

/**
 * Botón social minimalista con línea inferior
 * Sin efecto ripple/fondo gris al presionar
 */
@Composable
private fun SocialButton(
    text: String,
    icon: DrawableResource,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null, // SIN efecto ripple
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo con tamaño consistente
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(28.dp),
                shadowElevation = 2.dp
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Texto alineado a la izquierda del ícono - Más pequeño
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Línea inferior sutil
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

/**
 * Toggle entre login y registro
 * Color secundario para el botón de acción
 */
@Composable
private fun ToggleAuthMode(
    isRegisterMode: Boolean,
    onToggle: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isRegisterMode) "¿Ya tienes cuenta?" else "¿No tienes cuenta?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Texto con color secundario (dorado)
        Text(
            text = if (isRegisterMode) "Inicia sesión" else "Regístrate",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.secondary, // Color secundario #E1C78E
            modifier = Modifier.clickable { onToggle() }
        )
    }
}
