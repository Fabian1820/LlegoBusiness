package com.llego.shared.data.mappers

import com.llego.multiplatform.graphql.GetCombosQuery
import com.llego.multiplatform.graphql.GetComboQuery
import com.llego.shared.data.model.*

/**
 * Mappers para convertir tipos GraphQL de combos a modelos de dominio
 */

// Mapper para GetCombosQuery.CombosByBranch a Combo
fun GetCombosQuery.CombosByBranch.toDomain(): Combo {
    return Combo(
        id = id,
        branchId = branchId,
        name = name,
        description = description,
        image = image,
        startingFinalPrice = startingFinalPrice,
        startingSavings = startingSavings,
        discountType = when (discountType.name) {
            "NONE" -> DiscountType.NONE
            "PERCENTAGE" -> DiscountType.PERCENTAGE
            "FIXED" -> DiscountType.FIXED
            else -> DiscountType.NONE
        },
        discountValue = discountValue,
        availability = availability,
        slots = slots.map { it.toDomain() },
        representativeProducts = representativeProducts.map { it.toDomain() },
        giftOptions = giftOptions.map { ComboGiftOption(productId = it.productId) },
        createdAt = createdAt.toString(),
        imageUrl = imageUrl ?: image
    )
}

// Mapper para GetComboQuery.Combo a Combo
fun GetComboQuery.Combo.toDomain(): Combo {
    return Combo(
        id = id,
        branchId = branchId,
        name = name,
        description = description,
        image = image,
        startingFinalPrice = startingFinalPrice,
        startingSavings = startingSavings,
        discountType = when (discountType.name) {
            "NONE" -> DiscountType.NONE
            "PERCENTAGE" -> DiscountType.PERCENTAGE
            "FIXED" -> DiscountType.FIXED
            else -> DiscountType.NONE
        },
        discountValue = discountValue,
        availability = availability,
        slots = slots.map { it.toDomain() },
        representativeProducts = representativeProducts.map { it.toDomain() },
        giftOptions = giftOptions.map { ComboGiftOption(productId = it.productId) },
        createdAt = createdAt.toString(),
        imageUrl = imageUrl ?: image
    )
}

// Mapper para Slot (GetCombosQuery)
fun GetCombosQuery.Slot.toDomain(): ComboSlot {
    return ComboSlot(
        id = id,
        name = name,
        description = description,
        minSelections = minSelections,
        maxSelections = maxSelections,
        isFree = isFree,
        displayOrder = displayOrder,
        options = options.map { it.toDomain() }
    )
}

// Mapper para Slot (GetComboQuery)
fun GetComboQuery.Slot.toDomain(): ComboSlot {
    return ComboSlot(
        id = id,
        name = name,
        description = description,
        minSelections = minSelections,
        maxSelections = maxSelections,
        isFree = isFree,
        displayOrder = displayOrder,
        options = options.map { it.toDomain() }
    )
}

// Mapper para Option (GetCombosQuery)
fun GetCombosQuery.Option.toDomain(): ComboOption {
    return ComboOption(
        productId = productId,
        product = product?.let {
            Product(
                id = it.id,
                branchId = "",
                name = it.name,
                description = "",
                weight = "",
                price = it.price,
                currency = "",
                image = "",
                availability = true,
                categoryId = null,
                createdAt = "",
                imageUrl = it.imageUrl
            )
        },
        isDefault = isDefault,
        priceAdjustment = priceAdjustment,
        availableModifiers = availableModifiers.map { it.toDomain() }
    )
}

// Mapper para Option (GetComboQuery)
fun GetComboQuery.Option.toDomain(): ComboOption {
    return ComboOption(
        productId = productId,
        product = product?.let {
            Product(
                id = it.id,
                branchId = "",
                name = it.name,
                description = "",
                weight = "",
                price = it.price,
                currency = "",
                image = "",
                availability = true,
                categoryId = null,
                createdAt = "",
                imageUrl = it.imageUrl
            )
        },
        isDefault = isDefault,
        priceAdjustment = priceAdjustment,
        availableModifiers = availableModifiers.map { it.toDomain() }
    )
}

// Mapper para AvailableModifier (GetCombosQuery)
fun GetCombosQuery.AvailableModifier.toDomain(): ComboModifier {
    return ComboModifier(
        name = name,
        priceAdjustment = priceAdjustment
    )
}

// Mapper para AvailableModifier (GetComboQuery)
fun GetComboQuery.AvailableModifier.toDomain(): ComboModifier {
    return ComboModifier(
        name = name,
        priceAdjustment = priceAdjustment
    )
}

// Mapper para RepresentativeProduct (GetCombosQuery)
fun GetCombosQuery.RepresentativeProduct.toDomain(): com.llego.shared.data.model.RepresentativeProduct {
    return com.llego.shared.data.model.RepresentativeProduct(
        id = id,
        name = name,
        imageUrl = imageUrl ?: ""
    )
}

// Mapper para RepresentativeProduct (GetComboQuery)
fun GetComboQuery.RepresentativeProduct.toDomain(): com.llego.shared.data.model.RepresentativeProduct {
    return com.llego.shared.data.model.RepresentativeProduct(
        id = id,
        name = name,
        imageUrl = imageUrl ?: ""
    )
}

/**
 * Función helper para convertir lista de combos
 */
fun List<GetCombosQuery.CombosByBranch>.toDomainList(): List<Combo> {
    return map { it.toDomain() }
}
