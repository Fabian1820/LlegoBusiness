package com.llego.shared.ui.components.molecules

import androidx.compose.animation.animateColorAsState
// import androidx.compose.animation.animateFloatAsState  // TODO: Habilitar cuando esté soportado en KMP
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.UserType
import com.llego.shared.ui.theme.LlegoCustomShapes

/**
 * Selector de tipo de usuario con animaciones elegantes
 * Permite seleccionar entre Negocio y Chofer/Mensajero
 */

@Composable
fun UserTypeSelector(
    selectedUserType: UserType,
    onUserTypeSelected: (UserType) -> Unit,
    modifier: Modifier = Modifier
) {
    val userTypes = listOf(
        UserTypeOption(
            type = UserType.BUSINESS,
            title = "Negocio",
            subtitle = "Restaurantes y comercios",
            icon = "🏪"
        ),
        UserTypeOption(
            type = UserType.DRIVER,
            title = "Chofer",
            subtitle = "Mensajeros y repartidores",
            icon = "🏍️"
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tipo de cuenta",
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        userTypes.forEach { userTypeOption ->
            UserTypeCard(
                userTypeOption = userTypeOption,
                isSelected = selectedUserType == userTypeOption.type,
                onSelected = { onUserTypeSelected(userTypeOption.type) }
            )
        }
    }
}

@Composable
private fun UserTypeCard(
    userTypeOption: UserTypeOption,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animaciones
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(300),
        label = "border_color"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "background_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "text_color"
    )

    val subtitleColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "subtitle_color"
    )

    val scale = 1f // by animateFloatAsState(
        // targetValue = if (isSelected) 1.02f else 1f,
        // animationSpec = spring(
        //     dampingRatio = Spring.DampingRatioMediumBouncy,
        //     stiffness = Spring.StiffnessLow
        // ),
        // label = "scale"
    // )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        shape = LlegoCustomShapes.infoCard,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icono
            Text(
                text = userTypeOption.icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .background(
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            )

            // Textos
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = userTypeOption.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = userTypeOption.subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = subtitleColor
                    )
                )
            }

            // Indicador de selección
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Data class para las opciones de tipo de usuario
 */
private data class UserTypeOption(
    val type: UserType,
    val title: String,
    val subtitle: String,
    val icon: String
)