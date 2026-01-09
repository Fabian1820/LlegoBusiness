package com.llego.shared.ui.components.molecules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.llego.nichos.restaurant.ui.components.BusinessLocationMap
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Selector de ubicación con Google Maps real integrado
 *
 * @param latitude Latitud actual
 * @param longitude Longitud actual
 * @param onLocationSelected Callback cuando se selecciona una ubicación
 * @param modifier Modificador opcional
 */
@Composable
fun MapLocationPickerReal(
    latitude: Double,
    longitude: Double,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val originalLatitude = latitude
    val originalLongitude = longitude

    var selectedLatitude by remember(latitude) { mutableStateOf(latitude) }
    var selectedLongitude by remember(longitude) { mutableStateOf(longitude) }
    var showFullScreenMap by remember { mutableStateOf(false) }

    val hasLocationChange = abs(selectedLatitude - originalLatitude) > 0.000001 ||
            abs(selectedLongitude - originalLongitude) > 0.000001

    val onLocationChange: (Double, Double) -> Unit = { lat, lng ->
        selectedLatitude = lat
        selectedLongitude = lng
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = primaryColor
                )
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                )
            }

            // Botón para abrir selector de mapa
            OutlinedButton(
                onClick = { showFullScreenMap = true },
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

        // Vista previa del mapa
        BusinessLocationMap(
            latitude = selectedLatitude,
            longitude = selectedLongitude,
            onLocationSelected = { _, _ -> }, // No interactivo en preview
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = if (selectedLatitude != 0.0 && selectedLongitude != 0.0)
                        primaryColor.copy(alpha = 0.5f)
                    else
                        Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { showFullScreenMap = true },
            isInteractive = false
        )

        // Coordenadas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = if (selectedLatitude != 0.0) formatCoordinate(selectedLatitude) else "",
                onValueChange = { },
                label = { Text("Latitud") },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = if (selectedLongitude != 0.0) formatCoordinate(selectedLongitude) else "",
                onValueChange = { },
                label = { Text("Longitud") },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Texto de ayuda
        Text(
            text = if (selectedLatitude == 0.0 && selectedLongitude == 0.0)
                "⚠️ Toca 'Seleccionar en Mapa' para elegir la ubicación de tu negocio"
            else
                "✓ Ubicación seleccionada. Toca 'Seleccionar en Mapa' para cambiar",
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (selectedLatitude == 0.0 && selectedLongitude == 0.0)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        )
    }

    // Dialog del mapa fullscreen
    if (showFullScreenMap) {
        FullScreenMapDialog(
            latitude = selectedLatitude,
            longitude = selectedLongitude,
            onLocationChange = onLocationChange,
            onReset = {
                selectedLatitude = originalLatitude
                selectedLongitude = originalLongitude
            },
            onConfirm = {
                onLocationSelected(selectedLatitude, selectedLongitude)
                showFullScreenMap = false
            },
            onDismiss = {
                selectedLatitude = originalLatitude
                selectedLongitude = originalLongitude
                showFullScreenMap = false
            },
            hasLocationChange = hasLocationChange
        )
    }
}

/**
 * Diálogo de mapa a pantalla completa
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenMapDialog(
    latitude: Double,
    longitude: Double,
    onLocationChange: (Double, Double) -> Unit,
    onReset: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    hasLocationChange: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { contentVisible = true }

    LaunchedEffect(contentVisible) {
        if (!contentVisible) {
            delay(180)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { contentVisible = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.98f, animationSpec = tween(200)),
            exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.98f, animationSpec = tween(180))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(primaryColor)
            ) {
                // TopBar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryColor)
                        .statusBarsPadding()
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Seleccionar ubicación",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { contentVisible = false }) {
                                Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        windowInsets = WindowInsets(0)
                    )
                }

                // Mapa interactivo
                BusinessLocationMap(
                    latitude = latitude,
                    longitude = longitude,
                    onLocationSelected = onLocationChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    isInteractive = true
                )

                // Bottom bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = primaryColor,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Coordenadas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "📍 ${formatCoordinate(latitude)}, ${formatCoordinate(longitude)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Color.White
                            )
                        }

                        // Botones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onReset,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                enabled = hasLocationChange,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = primaryColor,
                                    disabledContainerColor = Color.White.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Deshacer")
                            }

                            Button(
                                onClick = onConfirm,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = primaryColor
                                )
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Confirmar")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formatea coordenadas a 6 decimales
 */
private fun formatCoordinate(value: Double): String {
    val rounded = (value * 1_000_000.0).roundToLong() / 1_000_000.0
    val text = rounded.toString()
    return if (text.contains(".")) {
        val parts = text.split(".")
        val decimals = parts[1].padEnd(6, '0').take(6)
        parts[0] + "." + decimals
    } else {
        text + ".000000"
    }
}
