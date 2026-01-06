package com.llego.nichos.common.utils

import llegobusiness.composeapp.generated.resources.Res
import llegobusiness.composeapp.generated.resources.arrozblanco
import llegobusiness.composeapp.generated.resources.arrozmoro
import llegobusiness.composeapp.generated.resources.batidofresa
import llegobusiness.composeapp.generated.resources.batidomamey
import llegobusiness.composeapp.generated.resources.pastelfresa
import llegobusiness.composeapp.generated.resources.pizza
import llegobusiness.composeapp.generated.resources.spaggetti
import llegobusiness.composeapp.generated.resources.tresleches
import org.jetbrains.compose.resources.DrawableResource

/**
 * Utilidad para obtener imágenes de productos
 * Reutiliza las imágenes de restaurante para todos los nichos
 */
@Suppress("MagicNumber")
fun getProductImage(productId: String): DrawableResource {
    val images = listOf(
        Res.drawable.pizza,
        Res.drawable.spaggetti,
        Res.drawable.arrozblanco,
        Res.drawable.arrozmoro,
        Res.drawable.pastelfresa,
        Res.drawable.tresleches,
        Res.drawable.batidofresa,
        Res.drawable.batidomamey
    )

    val index = productId.hashCode().let { if (it < 0) -it else it } % images.size
    return images[index]
}





