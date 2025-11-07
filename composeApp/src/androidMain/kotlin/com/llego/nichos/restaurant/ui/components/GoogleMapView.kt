package com.llego.nichos.restaurant.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * Componente de Google Maps para Android
 * Ubicación por defecto: La Habana, Cuba (23.1136, -82.3666)
 */
@Composable
actual fun BusinessLocationMap(
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier
) {
    // Ubicación de La Habana, Cuba
    var selectedLocation by remember { mutableStateOf(LatLng(23.1136, -82.3666)) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
    }

    GoogleMap(
        modifier = modifier,
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
            scrollGesturesEnabled = true
        ),
        onMapClick = { latLng ->
            selectedLocation = latLng
            onLocationSelected(latLng.latitude, latLng.longitude)
        }
    ) {
        // Marcador en la ubicación seleccionada
        Marker(
            state = MarkerState(position = selectedLocation),
            title = "Ubicación del Negocio",
            snippet = "Toca el mapa para cambiar la ubicación"
        )
    }
}
