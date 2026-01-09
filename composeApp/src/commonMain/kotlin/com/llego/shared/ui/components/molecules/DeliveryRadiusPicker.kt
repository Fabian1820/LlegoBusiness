package com.llego.shared.ui.components.molecules

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Selector de radio de entrega con slider interactivo
 *
 * Permite al usuario seleccionar el radio de entrega en kilómetros
 * con un slider visual y muestra el valor actual prominentemente.
 *
 * @param radiusKm Radio actual en kilómetros
 * @param onRadiusChange Callback cuando cambia el radio
 * @param minRadius Radio mínimo en km (default: 1.0)
 * @param maxRadius Radio máximo en km (default: 20.0)
 * @param modifier Modificador opcional
 */
@Composable
fun DeliveryRadiusPicker(
    radiusKm: Double,
    onRadiusChange: (Double) -> Unit,
    minRadius: Double = 1.0,
    maxRadius: Double = 20.0,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con ícono
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Radio de Entrega",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                )
            }

            // Valor actual grande
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${radiusKm.roundToInt()}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                )
                Text(
                    text = "kilómetros",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            }

            // Slider
            Slider(
                value = radiusKm.toFloat(),
                onValueChange = { onRadiusChange(it.toDouble()) },
                valueRange = minRadius.toFloat()..maxRadius.toFloat(),
                steps = ((maxRadius - minRadius) * 2).toInt() - 1, // Pasos de 0.5 km
                colors = SliderDefaults.colors(
                    thumbColor = primaryColor,
                    activeTrackColor = primaryColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Etiquetas de rango
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${minRadius.roundToInt()} km",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                Text(
                    text = "${maxRadius.roundToInt()} km",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }

            // Descripción informativa
            Text(
                text = getRadiusDescription(radiusKm),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Obtiene una descripción contextual según el radio seleccionado
 */
private fun getRadiusDescription(radiusKm: Double): String {
    return when {
        radiusKm < 2.0 -> "⚡ Radio pequeño - Ideal para entregas rápidas en zonas cercanas"
        radiusKm < 5.0 -> "🏙️ Radio moderado - Cubre áreas urbanas cercanas"
        radiusKm < 10.0 -> "🌆 Radio amplio - Abarca gran parte de la ciudad"
        radiusKm < 15.0 -> "🗺️ Radio extenso - Llega a zonas periféricas"
        else -> "🚚 Radio máximo - Entregas a larga distancia (puede aumentar tiempo de entrega)"
    }
}
