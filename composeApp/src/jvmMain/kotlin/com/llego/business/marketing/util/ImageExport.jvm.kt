package com.llego.business.marketing.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageBitmap.toPngBytes(): ByteArray {
    val image = Image.makeFromBitmap(this.asSkiaBitmap())
    val data = image.encodeToData(EncodedImageFormat.PNG)
        ?: error("No se pudo codificar el creativo a PNG")
    return data.bytes
}
