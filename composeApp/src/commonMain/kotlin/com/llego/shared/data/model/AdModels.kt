package com.llego.shared.data.model

/**
 * Modelos de dominio para campañas de visibilidad (Promociones).
 * El creativo es una sola foto exportada (estilo Canva) que el negocio diseña en
 * LlegoBusiness; el feed del cliente solo muestra esa imagen. Espejo del backend
 * (domain/ads.py).
 */

data class AdPricing(
    val id: String,
    val placement: String,      // "destacado" | "oferta"
    val durationDays: Int,
    val price: Double,
    val currency: String,       // "usd" | "local"
    val label: String?
)

data class AdCampaign(
    val id: String,
    val name: String,
    val placement: String,
    val status: String,          // draft|pending_payment|pending_review|active|paused|rejected|ended
    val paymentStatus: String,
    val approved: Boolean,       // único gate de visibilidad en el feed (lo pone el admin)
    val price: Double,
    val currency: String,
    val durationDays: Int,
    val impressions: Int,
    val clicks: Int,
    val rejectionReason: String?,
    val creativeImageUrl: String?
)

object AdPlacement {
    const val DESTACADO = "destacado"
    const val OFERTA = "oferta"
}
