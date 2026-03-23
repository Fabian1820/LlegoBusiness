package com.llego.shared.data.mappers

import com.llego.multiplatform.graphql.CreateShowcaseMutation
import com.llego.multiplatform.graphql.ShowcasesByBranchQuery
import com.llego.multiplatform.graphql.UpdateShowcaseMutation
import com.llego.shared.data.model.Showcase
import com.llego.shared.data.model.ShowcaseItem

internal fun CreateShowcaseMutation.CreateShowcase.toDomain(): Showcase {
    return Showcase(
        id = id,
        branchId = branchId,
        title = title,
        image = image,
        description = description,
        items = items?.map { item ->
            ShowcaseItem(
                id = item.id,
                name = item.name,
                description = item.description,
                price = item.price,
                availability = item.availability
            )
        },
        isActive = isActive,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        imageUrl = imageUrl
    )
}

internal fun ShowcasesByBranchQuery.ShowcasesByBranch.toDomain(): Showcase {
    return Showcase(
        id = id,
        branchId = branchId,
        title = title,
        image = image,
        description = description,
        items = items?.map { item ->
            ShowcaseItem(
                id = item.id,
                name = item.name,
                description = item.description,
                price = item.price,
                availability = item.availability
            )
        },
        isActive = isActive,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        imageUrl = imageUrl
    )
}

internal fun UpdateShowcaseMutation.UpdateShowcase.toDomain(): Showcase {
    return Showcase(
        id = id,
        branchId = branchId,
        title = title,
        image = image,
        description = description,
        items = items?.map { item ->
            ShowcaseItem(
                id = item.id,
                name = item.name,
                description = item.description,
                price = item.price,
                availability = item.availability
            )
        },
        isActive = isActive,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        imageUrl = imageUrl
    )
}
