package com.llego.nichos.restaurant.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Declaración expect para el mapa de ubicación del negocio
 * Implementación específica en androidMain
 */
@Composable
expect fun BusinessLocationMap(
    latitude: Double,
    longitude: Double,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true
)
