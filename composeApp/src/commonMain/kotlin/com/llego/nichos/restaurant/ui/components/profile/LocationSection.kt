package com.llego.nichos.restaurant.ui.components.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.llego.shared.data.model.Branch
import com.llego.nichos.restaurant.ui.components.BusinessLocationMap
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlinx.coroutines.delay

/**
 * Sección de mapa de ubicación
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

    val hasLocationChange = abs(selectedLatitude - originalLatitude) > 0.000001 ||
            abs(selectedLongitude - originalLongitude) > 0.000001

    val onLocationSelected: (Double, Double) -> Unit = { lat, lng ->
        selectedLatitude = lat
        selectedLongitude = lng
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
                onLocationSave(selectedLatitude, selectedLongitude)
            },
            onDismiss = {
                selectedLatitude = originalLatitude
                selectedLongitude = originalLongitude
                showFullScreenMap = false
            },
            hasLocationChange = hasLocationChange
        )
    }

    ProfileSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Ubicación del Negocio", emoji = "📍")
            
            IconButton(
                onClick = { showFullScreenMap = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Ampliar mapa",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Mapa preview
        BusinessLocationMap(
            latitude = selectedLatitude,
            longitude = selectedLongitude,
            onLocationSelected = onLocationSelected,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { showFullScreenMap = true },
            isInteractive = false
        )

        // Coordenadas
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Coordenadas:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Lat: ${formatCoordinate(selectedLatitude)}, Lng: ${formatCoordinate(selectedLongitude)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.9f)
                )
            }
        }
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
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                // TopBar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
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

                // Mapa
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
                    color = MaterialTheme.colorScheme.primary,
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
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = Color.White.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Deshacer")
                            }

                            Button(
                                onClick = {
                                    onConfirm()
                                    contentVisible = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = MaterialTheme.colorScheme.primary
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
