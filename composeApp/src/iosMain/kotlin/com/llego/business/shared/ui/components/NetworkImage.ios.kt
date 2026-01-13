package com.llego.business.shared.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.layout.ContentScale
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.*
import platform.Foundation.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

import platform.CoreGraphics.CGRectMake

@OptIn(ExperimentalForeignApi::class)
private class AsyncImageView : UIImageView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    var currentUrl: String? = null
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    UIKitView(
        factory = {
            val imageView = AsyncImageView()
            imageView.contentMode = when(contentScale) {
                ContentScale.Fit -> UIViewContentMode.UIViewContentModeScaleAspectFit
                ContentScale.Crop -> UIViewContentMode.UIViewContentModeScaleAspectFill
                else -> UIViewContentMode.UIViewContentModeScaleAspectFill
            }
            imageView.clipsToBounds = true
            imageView
        },
        update = { imageView ->
            val view = imageView as AsyncImageView
            
            if (view.currentUrl != url) {
                view.currentUrl = url
                
                if (url.isNotEmpty()) {
                    val nsUrl = NSURL(string = url)
                    val request = NSURLRequest.requestWithURL(nsUrl)
                    NSURLSession.sharedSession.dataTaskWithRequest(request) { data, _, _ ->
                         if (data != null) {
                             val image = UIImage(data = data)
                             dispatch_async(dispatch_get_main_queue()) {
                                 // Verify URL hasn't changed while loading
                                 if (view.currentUrl == url) {
                                     view.image = image
                                 }
                             }
                         }
                    }.resume()
                } else {
                    view.image = null
                }
            }
        },
        modifier = modifier
    )
}
