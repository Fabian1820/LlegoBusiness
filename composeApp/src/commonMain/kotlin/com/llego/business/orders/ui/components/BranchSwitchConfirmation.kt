package com.llego.business.orders.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Datos para mostrar la confirmación de cambio de sucursal
 * 
 * Requirements: 12.4
 */
data class BranchSwitchConfirmationData(
    val previousBranchName: String?,
    val newBranchName: String,
    val orderId: String
)

/**
 * Componente de confirmación de cambio de sucursal
 * 
 * Muestra un Snackbar/Toast animado cuando el usuario cambia
 * de sucursal desde una notificación.
 * 
 * Requirements: 12.4
 * 
 * @param data Datos de la confirmación (null para ocultar)
 * @param onDismiss Callback cuando se cierra la confirmación
 * @param autoDismissMs Tiempo en ms antes de cerrar automáticamente (default 3000)
 */
@Composable
fun BranchSwitchConfirmation(
    data: BranchSwitchConfirmationData?,
    onDismiss: () -> Unit,
    autoDismissMs: Long = 3000L,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss después del tiempo especificado
    LaunchedEffect(data) {
        if (data != null) {
            delay(autoDismissMs)
            onDismiss()
        }
    }
    
    AnimatedVisibility(
        visible = data != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = modifier
    ) {
        data?.let { confirmationData ->
            BranchSwitchSnackbar(
                data = confirmationData,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Snackbar personalizado para confirmación de cambio de sucursal
 */
@Composable
private fun BranchSwitchSnackbar(
    data: BranchSwitchConfirmationData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono de éxito
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50), // Verde éxito
                modifier = Modifier.size(24.dp)
            )
            
            // Texto de confirmación
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Sucursal cambiada",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
                
                if (data.previousBranchName != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = data.previousBranchName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = data.newBranchName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                } else {
                    Text(
                        text = "Ahora en: ${data.newBranchName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Botón de cerrar
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.inversePrimary
                )
            ) {
                Text("OK")
            }
        }
    }
}

/**
 * Snackbar de error cuando no se encuentra la sucursal
 * 
 * @param visible Si el snackbar está visible
 * @param onDismiss Callback cuando se cierra
 */
@Composable
fun BranchNotFoundSnackbar(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss después de 3 segundos
    LaunchedEffect(visible) {
        if (visible) {
            delay(3000)
            onDismiss()
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            tonalElevation = 6.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⚠️ No se encontró la sucursal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("OK")
                }
            }
        }
    }
}
