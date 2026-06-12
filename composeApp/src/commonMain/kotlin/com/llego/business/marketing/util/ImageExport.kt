package com.llego.business.marketing.util

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Codifica un [ImageBitmap] (la tarjeta capturada del lienzo) a PNG en memoria.
 * La implementación es por plataforma: Android usa Bitmap.compress; iOS/JVM usan
 * Skia. El resultado se sube tal cual como creativo de la campaña.
 */
expect fun ImageBitmap.toPngBytes(): ByteArray
