package com.llego.shared.ui.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.llego.shared.data.model.BusinessType

@Composable
internal fun LoginForm(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onContinueClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    selectedBusinessType: BusinessType? = null,
    onBusinessTypeSelected: ((BusinessType) -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campos de email y contraseña
            ModernTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Correo electrónico",
                leadingIcon = Icons.Filled.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = errorMessage != null
            )

            ModernTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Contraseña",
                leadingIcon = Icons.Filled.Lock,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (!isLoading) onContinueClick() }
                ),
                isPassword = true
            )

            // Link de olvidaste tu contraseña
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* TODO: Implementar recuperación de contraseña */ }
                    )
                )
            }

            PrimaryButton(
                text = "Continuar",
                onClick = onContinueClick,
                enabled = !isLoading, // Sin validaciones para desarrollo
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

            Spacer(modifier = Modifier.height(16.dp))

            // Botones sociales
            SocialButtons()
        }
    }
}

@Composable
internal fun RegisterForm(
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

        ModernTextField(
            value = businessName,
            onValueChange = { businessName = it },
            placeholder = "Nombre del negocio",
            leadingIcon = Icons.Filled.Store,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        ModernTextField(
            value = contactName,
            onValueChange = { contactName = it },
            placeholder = "Nombre del responsable",
            leadingIcon = Icons.Filled.Person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        ModernTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = "Teléfono",
            leadingIcon = Icons.Filled.Phone,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )

        ModernTextField(
            value = address,
            onValueChange = { address = it },
            placeholder = "Dirección",
            leadingIcon = Icons.Filled.LocationOn,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        ModernTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Correo electrónico",
            leadingIcon = Icons.Filled.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = errorMessage != null
        )

        ModernTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Contraseña",
            leadingIcon = Icons.Filled.Lock,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            isPassword = true
        )

        ModernTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Confirmar contraseña",
            leadingIcon = Icons.Filled.Lock,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (!isLoading) onRegisterClick() }
            ),
            isPassword = true,
            isError = confirmPassword.isNotBlank() && confirmPassword != password
        )

        PrimaryButton(
            text = "Registrarse",
            onClick = onRegisterClick,
            enabled = !isLoading && selectedBusinessType != null, // Solo requiere tipo de negocio para desarrollo
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

@Composable
internal fun BusinessTypeChips(
    selectedBusinessType: BusinessType?,
    onBusinessTypeSelected: (BusinessType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primera fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelectableChip(
                text = "🍽️ Restaurante",
                isSelected = selectedBusinessType == BusinessType.RESTAURANT,
                onClick = { onBusinessTypeSelected(BusinessType.RESTAURANT) },
                modifier = Modifier.weight(1f)
            )

            SelectableChip(
                text = "🛒 Mercado",
                isSelected = selectedBusinessType == BusinessType.MARKET,
                onClick = { onBusinessTypeSelected(BusinessType.MARKET) },
                modifier = Modifier.weight(1f)
            )
        }

        // Segunda fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelectableChip(
                text = "🌾 Agromercado",
                isSelected = selectedBusinessType == BusinessType.AGROMARKET,
                onClick = { onBusinessTypeSelected(BusinessType.AGROMARKET) },
                modifier = Modifier.weight(1f)
            )

            SelectableChip(
                text = "👕 Tienda Ropa",
                isSelected = selectedBusinessType == BusinessType.CLOTHING_STORE,
                onClick = { onBusinessTypeSelected(BusinessType.CLOTHING_STORE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isSelected) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        shadowElevation = if (isSelected) 6.dp else 3.dp,
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
                color = if (isSelected) Color(0xFF023133)
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
internal fun ModernTextField(
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
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingIcon = {
            when {
                isPassword -> {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                value.isNotEmpty() && !isPassword -> {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
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
