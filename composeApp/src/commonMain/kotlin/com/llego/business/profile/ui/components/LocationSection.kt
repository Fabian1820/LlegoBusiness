package com.llego.business.profile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.llego.business.shared.ui.components.BusinessLocationMap
import com.llego.shared.data.model.Branch
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlinx.coroutines.delay

/**
 * Seccion de mapa de ubicacion
 */
@Composable
fun LocationMapSection(
    branch: Branch?,
    onLocationSave: (Double, Double) -> Unit = { _, _ -> }
) {
    val originalLatitude = branch?.coordinates?.latitude ?: 0.0
    val originalLongitude = branch?.coordinates?.longitude ?: 0.0

    var selectedLatitude by remember(branch) { mutableStateOf(originalLatitude) }
    var selectedLongitude by remember(branch) { mutableStateOf(originalLongitude) }
    var showFullScreenMap by remember { mutableStateOf(false) }
    var resetOnDismiss by remember { mutableStateOf(true) }

    val hasLocationChange = abs(selectedLatitude - originalLatitude) > 0.000001 ||
        abs(selectedLongitude - originalLongitude) > 0.000001

    val onLocationSelected: (Double, Double) -> Unit = { lat, lng ->
        selectedLatitude = lat
        selectedLongitude = lng
    }

    val openFullScreenMap = {
        resetOnDismiss = true
        showFullScreenMap = true
    }

    if (showFullScreenMap) {
        FullScreenMapDialog(
            latitude = selectedLatitude,
            longitude = selectedLongitude,
            onLocationChange = onLocationSelected,
            onReset = {
                selectedLatitude = originalLatitude
                selectedLongitude = originalLongitude
            },
            onConfirm = {
                resetOnDismiss = false
                onLocationSave(selectedLatitude, selectedLongitude)
            },
            onDismiss = {
                if (resetOnDismiss) {
                    selectedLatitude = originalLatitude
                    selectedLongitude = originalLongitude
                }
                showFullScreenMap = false
                resetOnDismiss = true
            },
            hasLocationChange = hasLocationChange
        )
    }

    ProfileSectionCard {
        SectionHeader(
            title = "Ubicacion del negocio",
            trailing = {
                IconButton(
                    onClick = openFullScreenMap,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = LlegoShapes.small
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Ampliar mapa",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )

        // Mapa preview
        BusinessLocationMap(
            latitude = selectedLatitude,
            longitude = selectedLongitude,
            onLocationSelected = onLocationSelected,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(LlegoCustomShapes.infoCard)
                .clickable { openFullScreenMap() },
            isInteractive = false
        )

        // Coordenadas
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = LlegoShapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Coordenadas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Lat: ${formatCoordinate(selectedLatitude)}, Lng: ${formatCoordinate(selectedLongitude)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Dialogo de mapa a pantalla completa edge-to-edge
 */
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Mapa fullscreen edge-to-edge
                BusinessLocationMap(
                    latitude = latitude,
                    longitude = longitude,
                    onLocationSelected = onLocationChange,
                    modifier = Modifier.fillMaxSize(),
                    isInteractive = true
                )

                // Overlay: Boton cerrar en la esquina superior izquierda
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    IconButton(onClick = { contentVisible = false }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Overlay: Titulo en el centro superior
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 20.dp)
                        .align(Alignment.TopCenter),
                    shape = LlegoShapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "Seleccionar ubicacion",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Overlay: Bottom bar con coordenadas y botones
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
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
                                Text("Restaurar")
                            }

                            Button(
                                onClick = {
                                    onConfirm()
                                    contentVisible = false
                                },
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
                                Text("Guardar")
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
