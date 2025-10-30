package com.llego.nichos.restaurant.ui.components.menu

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

@Suppress("MagicNumber")
internal fun getProductImage(menuItemId: String): DrawableResource {
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

    val index = menuItemId.hashCode().let { if (it < 0) -it else it } % images.size
    return images[index]
}
