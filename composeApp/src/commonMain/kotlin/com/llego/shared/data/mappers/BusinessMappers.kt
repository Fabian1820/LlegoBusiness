package com.llego.shared.data.mappers

import com.apollographql.apollo.api.Optional
import com.llego.shared.data.model.*
import com.llego.multiplatform.graphql.*
import com.llego.multiplatform.graphql.type.CreateBusinessInput as GQLCreateBusinessInput
import com.llego.multiplatform.graphql.type.UpdateBusinessInput as GQLUpdateBusinessInput
import com.llego.multiplatform.graphql.type.RegisterBranchInput as GQLRegisterBranchInput
import com.llego.multiplatform.graphql.type.CreateBranchInput as GQLCreateBranchInput
import com.llego.multiplatform.graphql.type.UpdateBranchInput as GQLUpdateBranchInput
import com.llego.multiplatform.graphql.type.CoordinatesInput as GQLCoordinatesInput

/**
 * Mappers para convertir tipos GraphQL a modelos de dominio
 * Sigue el mismo patrón que AuthRepository.kt
 */

// ============= BUSINESS MAPPERS (GraphQL -> Domain) =============

fun GetBusinessesQuery.Business.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        type = type,
        ownerId = "", // No viene en la query lista
        globalRating = globalRating,
        avatar = null,
        coverImage = null,
        description = description,
        socialMedia = null,
        tags = tags,
        isActive = isActive,
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

fun GetBusinessQuery.Business.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        type = type,
        ownerId = ownerId,
        globalRating = globalRating,
        avatar = null,
        coverImage = null,
        description = description,
        socialMedia = null,
        tags = tags,
        isActive = isActive,
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

fun RegisterBusinessMutation.RegisterBusiness.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        type = type,
        ownerId = "", // Se llenará del context
        globalRating = globalRating,
        avatar = null,
        coverImage = null,
        description = description,
        socialMedia = null,
        tags = tags,
        isActive = isActive,
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

fun UpdateBusinessMutation.UpdateBusiness.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        type = type,
        ownerId = "", // No cambia en update
        globalRating = globalRating ?: 0.0,
        avatar = null,
        coverImage = null,
        description = description,
        socialMedia = null,
        tags = tags ?: emptyList(),
        isActive = isActive ?: true,
        createdAt = "", // No viene en update
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

// ============= BRANCH MAPPERS (GraphQL -> Domain) =============

fun GetBranchesQuery.Branch.toDomain(): Branch {
    @Suppress("UNCHECKED_CAST")
    val scheduleMap = (schedule as? Map<String, String>) ?: emptyMap()
    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.toDomain(),
        phone = phone,
        schedule = scheduleMap,
        managerIds = managerIds ?: emptyList(),
        status = status ?: "active",
        avatar = null,
        coverImage = null,
        deliveryRadius = deliveryRadius,
        facilities = facilities ?: emptyList(),
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

fun GetBranchQuery.Branch.toDomain(): Branch {
    @Suppress("UNCHECKED_CAST")
    val scheduleMap = (schedule as? Map<String, String>) ?: emptyMap()
    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.toDomain(),
        phone = phone,
        schedule = scheduleMap,
        managerIds = managerIds ?: emptyList(),
        status = status ?: "active",
        avatar = null,
        coverImage = null,
        deliveryRadius = deliveryRadius,
        facilities = facilities ?: emptyList(),
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

fun CreateBranchMutation.CreateBranch.toDomain(): Branch {
    @Suppress("UNCHECKED_CAST")
    val scheduleMap = (schedule as? Map<String, String>) ?: emptyMap()
    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.toDomain(),
        phone = phone,
        schedule = scheduleMap,
        managerIds = emptyList(),
        status = status ?: "active",
        avatar = null,
        coverImage = null,
        deliveryRadius = deliveryRadius,
        facilities = facilities ?: emptyList(),
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

fun UpdateBranchMutation.UpdateBranch.toDomain(): Branch {
    @Suppress("UNCHECKED_CAST")
    val scheduleMap = (schedule as? Map<String, String>) ?: emptyMap()
    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.toDomain(),
        phone = phone,
        schedule = scheduleMap,
        managerIds = emptyList(),
        status = status ?: "active",
        avatar = null,
        coverImage = null,
        deliveryRadius = deliveryRadius,
        facilities = facilities ?: emptyList(),
        createdAt = "", // No viene en update
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

// ============= COORDINATES MAPPERS =============

fun GetBranchesQuery.Coordinates.toDomain(): com.llego.shared.data.model.Coordinates {
    return com.llego.shared.data.model.Coordinates(
        type = type ?: "Point",
        coordinates = coordinates ?: listOf(0.0, 0.0)
    )
}

fun GetBranchQuery.Coordinates.toDomain(): com.llego.shared.data.model.Coordinates {
    return com.llego.shared.data.model.Coordinates(
        type = type ?: "Point",
        coordinates = coordinates ?: listOf(0.0, 0.0)
    )
}

fun CreateBranchMutation.Coordinates.toDomain(): com.llego.shared.data.model.Coordinates {
    return com.llego.shared.data.model.Coordinates(
        type = type ?: "Point",
        coordinates = coordinates ?: listOf(0.0, 0.0)
    )
}

fun UpdateBranchMutation.Coordinates.toDomain(): com.llego.shared.data.model.Coordinates {
    return com.llego.shared.data.model.Coordinates(
        type = type ?: "Point",
        coordinates = coordinates ?: listOf(0.0, 0.0)
    )
}

// ============= INPUT CONVERTERS (Domain -> GraphQL) =============

fun CreateBusinessInput.toGraphQL(): GQLCreateBusinessInput {
    return GQLCreateBusinessInput(
        name = name,
        type = type,
        avatar = Optional.presentIfNotNull(avatar),
        coverImage = Optional.presentIfNotNull(coverImage),
        description = Optional.presentIfNotNull(description),
        tags = Optional.presentIfNotNull(tags)
    )
}

fun UpdateBusinessInput.toGraphQL(): GQLUpdateBusinessInput {
    return GQLUpdateBusinessInput(
        name = Optional.presentIfNotNull(name),
        avatar = Optional.presentIfNotNull(avatar),
        coverImage = Optional.presentIfNotNull(coverImage),
        description = Optional.presentIfNotNull(description),
        tags = Optional.presentIfNotNull(tags),
        isActive = Optional.presentIfNotNull(isActive)
    )
}

fun RegisterBranchInput.toGraphQL(): GQLRegisterBranchInput {
    return GQLRegisterBranchInput(
        name = name,
        coordinates = coordinates.toGraphQL(),
        phone = phone,
        schedule = schedule,
        tipos = listOf(com.llego.multiplatform.graphql.type.BranchTipo.RESTAURANTE), // Default: RESTAURANTE
        address = Optional.presentIfNotNull(address),
        avatar = Optional.presentIfNotNull(avatar),
        coverImage = Optional.presentIfNotNull(coverImage),
        deliveryRadius = Optional.presentIfNotNull(deliveryRadius),
        facilities = Optional.presentIfNotNull(facilities)
    )
}

fun CreateBranchInput.toGraphQL(): GQLCreateBranchInput {
    return GQLCreateBranchInput(
        businessId = businessId,
        name = name,
        coordinates = coordinates.toGraphQL(),
        phone = phone,
        schedule = schedule,
        tipos = listOf(com.llego.multiplatform.graphql.type.BranchTipo.RESTAURANTE), // Default: RESTAURANTE
        address = Optional.presentIfNotNull(address),
        avatar = Optional.presentIfNotNull(avatar),
        coverImage = Optional.presentIfNotNull(coverImage),
        deliveryRadius = Optional.presentIfNotNull(deliveryRadius),
        facilities = Optional.presentIfNotNull(facilities)
    )
}

fun UpdateBranchInput.toGraphQL(): GQLUpdateBranchInput {
    return GQLUpdateBranchInput(
        name = Optional.presentIfNotNull(name),
        phone = Optional.presentIfNotNull(phone),
        schedule = Optional.presentIfNotNull(schedule),
        address = Optional.presentIfNotNull(address),
        avatar = Optional.presentIfNotNull(avatar),
        coverImage = Optional.presentIfNotNull(coverImage),
        status = Optional.presentIfNotNull(status),
        deliveryRadius = Optional.presentIfNotNull(deliveryRadius),
        facilities = Optional.presentIfNotNull(facilities)
    )
}

fun CoordinatesInput.toGraphQL(): GQLCoordinatesInput {
    return GQLCoordinatesInput(
        lat = lat,
        lng = lng
    )
}
