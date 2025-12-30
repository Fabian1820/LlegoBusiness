package com.llego.nichos.restaurant.ui.components

import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalView
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
 * Componente de Google Maps para Android - Mejorado con mejor interactividad
 * Ubicación por defecto: La Habana, Cuba (23.1136, -82.3666)
 */
@Composable
actual fun BusinessLocationMap(
    latitude: Double,
    longitude: Double,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier,
    isInteractive: Boolean
) {
    var selectedLocation by remember { mutableStateOf(LatLng(latitude, longitude)) }
    val view = LocalView.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
    }

    // Permite actualizar la cámara y el marcador cuando la ubicación cambia externamente
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
            title = "Ubicación del Negocio",
            snippet = if (isInteractive) "Toca el mapa para cambiar la ubicación" else null,
            draggable = isInteractive
        )
    }
}
