package com.llego.nichos.common.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

@Composable
actual fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    var imageBitmap by remember(url) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    
    LaunchedEffect(url) {
        if (url.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val bufferedImage: BufferedImage? = ImageIO.read(URL(url))
                    imageBitmap = bufferedImage?.toComposeImageBitmap()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
         Box(modifier = modifier)
    }
}
