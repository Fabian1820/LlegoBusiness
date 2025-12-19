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
    latitude: Double,
    longitude: Double,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier,
    isInteractive: Boolean
) {
    val coordinator = remember { MapCoordinator(onLocationSelected) }

    UIKitView(
        factory = {
            val mapView = MKMapView()
            
            val initialCoordinate = CLLocationCoordinate2DMake(latitude, longitude)
            val region = MKCoordinateRegionMakeWithDistance(initialCoordinate, 1000.0, 1000.0)
            mapView.setRegion(region, animated = false)
            
            // Add initial annotation
            val annotation = MKPointAnnotation()
            annotation.setCoordinate(initialCoordinate)
            mapView.addAnnotation(annotation)
            
            val tapGesture = UITapGestureRecognizer(
                target = coordinator,
                action = sel_registerName("handleTap:")
            )
            mapView.addGestureRecognizer(tapGesture)
            coordinator.mapView = mapView
            mapView.setUserInteractionEnabled(isInteractive)
            mapView
        },
        update = { mapView ->
            val coordinate = CLLocationCoordinate2DMake(latitude, longitude)
            val region = MKCoordinateRegionMakeWithDistance(coordinate, 1000.0, 1000.0)
            mapView.setRegion(region, animated = true)
            mapView.removeAnnotations(mapView.annotations)
            
            val annotation = MKPointAnnotation()
            annotation.setCoordinate(coordinate)
            mapView.addAnnotation(annotation)
            mapView.setUserInteractionEnabled(isInteractive)
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
