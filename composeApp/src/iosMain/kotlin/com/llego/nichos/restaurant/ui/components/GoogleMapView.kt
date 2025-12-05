package com.llego.nichos.restaurant.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UITapGestureRecognizer
import platform.darwin.NSObject
import platform.objc.sel_registerName

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BusinessLocationMap(
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier
) {
    val coordinator = remember { MapCoordinator(onLocationSelected) }

    UIKitView(
        factory = {
            val mapView = MKMapView()
            
            // Set initial region to Havana (23.1136, -82.3666)
            val havana = CLLocationCoordinate2DMake(23.1136, -82.3666)
            val region = MKCoordinateRegionMakeWithDistance(havana, 1000.0, 1000.0)
            mapView.setRegion(region, animated = false)
            
            // Add initial annotation
            val annotation = MKPointAnnotation()
            annotation.setCoordinate(havana)
            mapView.addAnnotation(annotation)
            
            val tapGesture = UITapGestureRecognizer(
                target = coordinator,
                action = sel_registerName("handleTap:")
            )
            mapView.addGestureRecognizer(tapGesture)
            coordinator.mapView = mapView
            mapView
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalForeignApi::class)
class MapCoordinator(
    val onLocationSelected: (Double, Double) -> Unit
) : NSObject() {
    var mapView: MKMapView? = null

    @ObjCAction
    fun handleTap(gesture: UITapGestureRecognizer) {
        if (gesture.state == UIGestureRecognizerStateEnded) {
            val map = mapView ?: return
            val point = gesture.locationInView(map)
            val coordinate = map.convertPoint(point, toCoordinateFromView = map)

            map.removeAnnotations(map.annotations)
            val annotation = MKPointAnnotation()
            annotation.setCoordinate(coordinate)
            map.addAnnotation(annotation)

            coordinate.useContents {
                onLocationSelected(latitude, longitude)
            }
        }
    }
}
