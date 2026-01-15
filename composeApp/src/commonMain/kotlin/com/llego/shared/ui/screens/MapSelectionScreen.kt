package com.llego.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.business.shared.ui.components.BusinessLocationMap
import com.llego.shared.ui.theme.LlegoCustomShapes
import com.llego.shared.ui.theme.LlegoShapes
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Pantalla de selección de ubicación en mapa
 * 
 * @param title Título de la pantalla
 * @param initialLatitude Latitud inicial
 * @param initialLongitude Longitud inicial
 * @param onNavigateBack Callback para volver atrás
 * @param onLocationConfirmed Callback cuando se confirma la ubicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSelectionScreen(
    title: String,
    initialLatitude: Double,
    initialLongitude: Double,
    onNavigateBack: () -> Unit,
    onLocationConfirmed: (Double, Double) -> Unit
) {
    var selectedLatitude by remember { mutableStateOf(initialLatitude) }
    var selectedLongitude by remember { mutableStateOf(initialLongitude) }

    val hasLocationChange = abs(selectedLatitude - initialLatitude) > 0.000001 ||
            abs(selectedLongitude - initialLongitude) > 0.000001

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                shape = LlegoCustomShapes.modal
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                text = "${formatCoordinate(selectedLatitude)}, ${formatCoordinate(selectedLongitude)}",
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
                            onClick = {
                                selectedLatitude = initialLatitude
                                selectedLongitude = initialLongitude
                            },
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
                            onClick = {
                                onLocationConfirmed(selectedLatitude, selectedLongitude)
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
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Mapa fullscreen
        BusinessLocationMap(
            latitude = selectedLatitude,
            longitude = selectedLongitude,
            onLocationSelected = { lat, lng ->
                selectedLatitude = lat
                selectedLongitude = lng
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            isInteractive = true
        )
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
