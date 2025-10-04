package com.llego.shared.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llego.shared.data.model.BusinessType
import com.llego.shared.ui.components.background.CurvedBackground
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
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val selectedBusinessType by viewModel.selectedBusinessType.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    var isRegisterMode by remember { mutableStateOf(false) }

    // Manejar login exitoso
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated && uiState.currentUser != null) {
            onLoginSuccess(uiState.currentUser!!.businessType)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fondo verde superior fijo (no hace scroll)
        CurvedBackground(
            curveStart = 0.18f,
            curveEnd = 0.18f,
            curveInclination = 0.06f,
            showCurve = true
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Header fijo con logo (no scroll)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                LlegoLogo()
            }

            // Card blanco con contenido scrolleable
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Título con fondo sutil (parte del card, no hace scroll)
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isRegisterMode) "Crea tu cuenta" else "Inicia sesión",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }

                    // Contenido scrolleable
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                            .imePadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))

                        // Formulario de login/registro
                        if (isRegisterMode) {
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

                        // Solo mostrar botones sociales en login, no en registro
                        if (!isRegisterMode) {
                            // Divider con "or"
                            DividerWithText(text = "o")

                            Spacer(modifier = Modifier.height(24.dp))

                            // Botones sociales
                            SocialButtons()

                            Spacer(modifier = Modifier.height(32.dp))
                        }

                        // Toggle entre login y registro
                        ToggleAuthMode(
                            isRegisterMode = isRegisterMode,
                            onToggle = { isRegisterMode = !isRegisterMode }
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
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
    Column(
        modifier = Modifier.fillMaxWidth(),
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

        // Link "Olvidaste tu contraseña?"
        Text(
            text = "¿Olvidaste tu contraseña?",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline
            ),
            modifier = Modifier
                .align(Alignment.End)
                .clickable { /* TODO: Navegar a recuperar contraseña */ }
        )

        // Botón Continue
        PrimaryButton(
            text = "Continuar",
            onClick = onContinueClick,
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
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
 * Chip seleccionable con texto más pequeño
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
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
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
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * TextField moderno con fondo gris claro más visible
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
                           else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                value.isNotEmpty() && !isPassword -> {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
 * Botón principal estilo iOS (altura 56dp según especificaciones)
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
            .height(56.dp),
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
                modifier = Modifier.size(24.dp),
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
 */
@Composable
private fun SocialButtons() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
 * Botón social outlined estilo iOS
 */
@Composable
private fun SocialButton(
    text: String,
    icon: DrawableResource,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo real con bordes redondeados
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(24.dp)
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * Toggle entre login y registro
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

        Text(
            text = if (isRegisterMode) "Inicia sesión" else "Regístrate",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onToggle() }
        )
    }
}