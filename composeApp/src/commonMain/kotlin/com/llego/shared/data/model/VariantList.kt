package com.llego.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VariantList(
    val id: String,
    val branchId: String,
    val name: String,
    val description: String? = null,
    val options: List<VariantOption> = emptyList()
)

@Serializable
data class VariantOption(
    val id: String,
    val name: String,
    val priceAdjustment: Double
)

data class VariantOptionDraft(
    val id: String? = null,
    val name: String,
    val priceAdjustment: Double
)
