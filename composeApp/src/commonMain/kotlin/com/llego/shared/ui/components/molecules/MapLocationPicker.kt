package com.llego.shared.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Selector de ubicación en mapa
 *
 * Componente para seleccionar coordenadas geográficas.
 * Por ahora es un placeholder que permite entrada manual y un botón para futura integración con Google Maps.
 *
 * TODO: Integrar con Google Maps SDK cuando esté configurado
 * - Android: com.google.android.gms:play-services-maps
 * - iOS: GoogleMaps framework via CocoaPods
 * - Compose Multiplatform: Usar expect/actual para plataformas específicas
 *
 * @param latitude Latitud actual
 * @param longitude Longitud actual
 * @param onLocationSelected Callback cuando se selecciona una ubicación
 * @param modifier Modificador opcional
 */
@Composable
fun MapLocationPicker(
    latitude: Double,
    longitude: Double,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    var showMapPicker by remember { mutableStateOf(false) }
    var latitudeText by remember { mutableStateOf(if (latitude != 0.0) latitude.toString() else "") }
    var longitudeText by remember { mutableStateOf(if (longitude != 0.0) longitude.toString() else "") }

    // Actualizar cuando cambien los props
    LaunchedEffect(latitude, longitude) {
        if (latitude != 0.0) latitudeText = latitude.toString()
        if (longitude != 0.0) longitudeText = longitude.toString()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header con título y botón de mapa
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ubicación",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            )

            // Botón para abrir selector de mapa (futuro)
            OutlinedButton(
                onClick = { showMapPicker = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryColor
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Seleccionar en Mapa",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // Vista previa del mapa (placeholder)
        MapPreview(
            latitude = latitudeText.toDoubleOrNull() ?: 0.0,
            longitude = longitudeText.toDoubleOrNull() ?: 0.0,
            onClick = { showMapPicker = true }
        )

        // Campos de coordenadas manuales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = latitudeText,
                onValueChange = {
                    latitudeText = it
                    val lat = it.toDoubleOrNull() ?: 0.0
                    val lng = longitudeText.toDoubleOrNull() ?: 0.0
                    if (lat != 0.0 && lng != 0.0) {
                        onLocationSelected(lat, lng)
                    }
                },
                label = { Text("Latitud") },
                placeholder = { Text("-12.0464") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor
                ),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = longitudeText,
                onValueChange = {
                    longitudeText = it
                    val lat = latitudeText.toDoubleOrNull() ?: 0.0
                    val lng = it.toDoubleOrNull() ?: 0.0
                    if (lat != 0.0 && lng != 0.0) {
                        onLocationSelected(lat, lng)
                    }
                },
                label = { Text("Longitud") },
                placeholder = { Text("-77.0428") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Texto de ayuda
        Text(
            text = "Ingresa las coordenadas manualmente o selecciónalas en el mapa",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
    }

    // Dialog del mapa (placeholder - futuro)
    if (showMapPicker) {
        MapPickerDialog(
            initialLatitude = latitudeText.toDoubleOrNull() ?: 0.0,
            initialLongitude = longitudeText.toDoubleOrNull() ?: 0.0,
            onLocationPicked = { lat, lng ->
                latitudeText = lat.toString()
                longitudeText = lng.toString()
                onLocationSelected(lat, lng)
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }
}

/**
 * Vista previa del mapa (placeholder)
 */
@Composable
private fun MapPreview(
    latitude: Double,
    longitude: Double,
    onClick: () -> Unit
) {
    val hasLocation = latitude != 0.0 && longitude != 0.0
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = if (hasLocation) primaryColor.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color.Gray.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (hasLocation) primaryColor else Color.Gray,
                modifier = Modifier.size(48.dp)
            )

            if (hasLocation) {
                Text(
                    text = "Ubicación seleccionada",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = primaryColor
                    )
                )
                Text(
                    text = "${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            } else {
                Text(
                    text = "Toca para seleccionar ubicación",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )
            }
        }
    }
}

/**
 * Dialog del selector de mapa (placeholder)
 *
 * TODO: Implementar mapa interactivo real
 * - Android: GoogleMap composable
 * - iOS: MapView wrapper con UIViewRepresentable
 * - Desktop: Alternativa con mapas estáticos o librería web
 */
@Composable
private fun MapPickerDialog(
    initialLatitude: Double,
    initialLongitude: Double,
    onLocationPicked: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccionar Ubicación",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // TODO: Aquí irá el mapa interactivo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Integración de Google Maps",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Próximamente disponible",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Text(
                    text = "Por ahora, ingresa las coordenadas manualmente en los campos de texto",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cerrar")
            }
        }
    )
}
