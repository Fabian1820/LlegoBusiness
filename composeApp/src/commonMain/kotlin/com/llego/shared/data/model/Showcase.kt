package com.llego.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Showcase(
    val id: String,
    val branchId: String,
    val title: String,
    val image: String,
    val description: String? = null,
    val items: List<ShowcaseItem>? = null,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val imageUrl: String
)

@Serializable
data class ShowcaseItem(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val price: Double? = null,
    val availability: Boolean = true
)

sealed class ShowcasesResult {
    data class Success(val showcases: List<Showcase>) : ShowcasesResult()
    data class Error(val message: String) : ShowcasesResult()
    data object Loading : ShowcasesResult()
}

@Serializable
data class ShowcaseDetectionResponse(
    val products: List<DetectedShowcaseProduct> = emptyList()
)

@Serializable
data class DetectedShowcaseProduct(
    val name: String,
    val description: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    val weight: String? = null
)

sealed class ShowcaseDetectionResult {
    data object Idle : ShowcaseDetectionResult()
    data class Success(val products: List<DetectedShowcaseProduct>) : ShowcaseDetectionResult()
    data class Error(val message: String) : ShowcaseDetectionResult()
    data object Loading : ShowcaseDetectionResult()
}
