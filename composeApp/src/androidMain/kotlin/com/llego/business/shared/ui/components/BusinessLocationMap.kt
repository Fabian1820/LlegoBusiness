package com.llego.business.shared.ui.components

import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

/**
 * Android map component with graceful fallback when Google Maps/Play Services are unavailable.
 */
@Composable
actual fun BusinessLocationMap(
    latitude: Double,
    longitude: Double,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier,
    isInteractive: Boolean
) {
    val context = LocalContext.current
    val hasPlayServices = remember(context) {
        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    if (!hasPlayServices) {
        MapUnavailableFallback(modifier = modifier)
        return
    }

    var selectedLocation by remember { mutableStateOf(LatLng(latitude, longitude)) }
    val view = LocalView.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
    }

    LaunchedEffect(latitude, longitude) {
        val incoming = LatLng(latitude, longitude)
        if (incoming != selectedLocation) {
            selectedLocation = incoming
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(incoming, 15f),
                durationMs = 500
            )
        }
    }

    runCatching {
        GoogleMap(
            modifier = modifier.pointerInteropFilter { event ->
                if (!isInteractive) return@pointerInteropFilter false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> view.parent?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        view.parent?.requestDisallowInterceptTouchEvent(false)
                }
                false
            },
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                zoomGesturesEnabled = true,
                scrollGesturesEnabled = isInteractive,
                scrollGesturesEnabledDuringRotateOrZoom = isInteractive,
                rotationGesturesEnabled = isInteractive,
                tiltGesturesEnabled = false,
                compassEnabled = true
            ),
            onMapClick = { latLng ->
                if (!isInteractive) return@GoogleMap
                selectedLocation = latLng
                onLocationSelected(latLng.latitude, latLng.longitude)
            }
        ) {
            val markerState = rememberMarkerState(position = selectedLocation)

            LaunchedEffect(selectedLocation) {
                if (markerState.position != selectedLocation) {
                    markerState.position = selectedLocation
                }
            }

            LaunchedEffect(markerState.position) {
                if (markerState.position != selectedLocation) {
                    selectedLocation = markerState.position
                    onLocationSelected(markerState.position.latitude, markerState.position.longitude)
                }
            }

            Marker(
                state = markerState,
                title = "Ubicacion del negocio",
                snippet = if (isInteractive) "Toca el mapa para cambiar la ubicacion" else null,
                draggable = isInteractive
            )
        }
    }.onFailure { throwable ->
        Log.e("BusinessLocationMap", "GoogleMap render failed", throwable)
        MapUnavailableFallback(modifier = modifier)
    }
}

@Composable
private fun MapUnavailableFallback(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No se pudo cargar el mapa",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Verifica Google Play Services en este dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
