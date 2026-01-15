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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import com.llego.business.shared.ui.components.BusinessLocationMap
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Selector de ubicación con Google Maps real integrado
 *
 * @param latitude Latitud actual
 * @param longitude Longitud actual
 * @param onLocationSelected Callback cuando se selecciona una ubicación
 * @param onOpenMapSelection Callback para abrir la pantalla de selección de mapa (opcional, si no se proporciona usa Dialog)
 * @param modifier Modificador opcional
 */
@Composable
fun MapLocationPickerReal(
    latitude: Double,
    longitude: Double,
    onLocationSelected: (Double, Double) -> Unit,
    onOpenMapSelection: ((String, Double, Double, (Double, Double) -> Unit) -> Unit)? = null,
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
                onClick = {
                    if (onOpenMapSelection != null) {
                        // Usar navegación
                        onOpenMapSelection("Seleccionar ubicación", selectedLatitude, selectedLongitude) { lat, lng ->
                            selectedLatitude = lat
                            selectedLongitude = lng
                            onLocationSelected(lat, lng)
                        }
                    } else {
                        // Usar Dialog (fallback)
                        showFullScreenMap = true
                    }
                },
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
            onLocationSelected = { _: Double, _: Double -> }, // No interactivo en preview
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
                .clickable {
                    if (onOpenMapSelection != null) {
                        onOpenMapSelection("Seleccionar ubicación", selectedLatitude, selectedLongitude) { lat, lng ->
                            selectedLatitude = lat
                            selectedLongitude = lng
                            onLocationSelected(lat, lng)
                        }
                    } else {
                        showFullScreenMap = true
                    }
                },
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

    // Dialog del mapa fullscreen (solo si no se usa navegación)
    if (showFullScreenMap && onOpenMapSelection == null) {
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
 * Diálogo de mapa a pantalla completa edge-to-edge
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
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
            androidx.compose.material3.Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Seleccionar ubicación",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { contentVisible = false }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    "Volver"
                                )
                            }
                        },
                        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                    )
                },
                bottomBar = {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                        shadowElevation = 8.dp,
                        shape = LlegoCustomShapes.modal
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Coordenadas
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = LlegoShapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${formatCoordinate(latitude)}, ${formatCoordinate(longitude)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Botones
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onReset,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = LlegoCustomShapes.secondaryButton,
                                    enabled = hasLocationChange,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Deshacer")
                                }

                                Button(
                                    onClick = onConfirm,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = LlegoCustomShapes.primaryButton,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
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
            ) { paddingValues ->
                // Mapa fullscreen
                BusinessLocationMap(
                    latitude = latitude,
                    longitude = longitude,
                    onLocationSelected = onLocationChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    isInteractive = true
                )
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
