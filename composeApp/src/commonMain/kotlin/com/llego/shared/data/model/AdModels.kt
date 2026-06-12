package com.llego.shared.data.model

/**
 * Modelos de dominio para campañas de visibilidad (Promociones).
 * El CreativeSpec es declarativo: la app lo dibuja nativamente (preview en vivo)
 * y el feed del cliente lo renderiza igual. Espejo del backend (domain/ads.py).
 */

data class AdPricing(
    val id: String,
    val placement: String,      // "destacado" | "oferta"
    val durationDays: Int,
    val price: Double,
    val currency: String,       // "usd" | "local"
    val label: String?
)

data class CreativeBackground(
    val type: String = "gradient",   // solid | gradient | image
    val colors: List<String> = listOf("#023133", "#0A5C3F"),
    val angle: Int = 45,
    val imagePath: String? = null,
    val imageUrl: String? = null
)

data class CreativeText(
    val role: String,                // eyebrow | title | subtitle | cta_label
    val value: String,
    val color: String = "#FFFFFF",
    val size: String = "md",         // sm | md | lg | xl
    val weight: String = "regular"   // regular | medium | bold
)

data class CreativeBadge(val text: String, val style: String = "offer")  // flash|discount|new|offer

data class CreativeCta(val label: String = "Ver tienda", val deeplink: String? = null)

data class CreativeSpec(
    val aspectRatio: String = "wide",        // wide | square
    val animationPreset: String = "none",    // none|fade_in|slide_in|pulse|gradient_shift|shine
    val background: CreativeBackground = CreativeBackground(),
    val texts: List<CreativeText> = emptyList(),
    val badge: CreativeBadge? = null,
    val cta: CreativeCta? = null
)

data class AdCampaign(
    val id: String,
    val name: String,
    val placement: String,
    val status: String,          // draft|pending_payment|pending_review|active|paused|rejected|ended
    val paymentStatus: String,
    val price: Double,
    val currency: String,
    val durationDays: Int,
    val impressions: Int,
    val clicks: Int,
    val rejectionReason: String?,
    val creative: CreativeSpec
)

object AdPlacement {
    const val DESTACADO = "destacado"
    const val OFERTA = "oferta"
}

object AnimationPresets {
    val all = listOf("none", "fade_in", "slide_in", "pulse", "gradient_shift", "shine")
    fun label(preset: String): String = when (preset) {
        "fade_in" -> "Aparecer"
        "slide_in" -> "Deslizar"
        "pulse" -> "Latido"
        "gradient_shift" -> "Degradado"
        "shine" -> "Brillo"
        else -> "Sin animación"
    }
}
