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
        ownerId = "", // No viene en la query lista
        globalRating = globalRating,
        avatar = avatar,
        description = description,
        socialMedia = parseStringMap(socialMedia),
        tags = tags,
        isActive = isActive,
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl
    )
}

fun GetBusinessQuery.Business.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        ownerId = ownerId,
        globalRating = globalRating,
        avatar = avatar,
        description = description,
        socialMedia = parseStringMap(socialMedia),
        tags = tags,
        isActive = isActive,
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl
    )
}

fun RegisterBusinessMutation.RegisterBusiness.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        ownerId = "", // Se llenará del context
        globalRating = globalRating,
        avatar = avatar,
        description = description,
        socialMedia = parseStringMap(socialMedia),
        tags = tags,
        isActive = isActive,
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl
    )
}

fun UpdateBusinessMutation.UpdateBusiness.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        ownerId = "", // No cambia en update
        globalRating = globalRating,
        avatar = avatar,
        description = description,
        socialMedia = parseStringMap(socialMedia),
        tags = tags,
        isActive = isActive,
        createdAt = "", // No viene en update
        avatarUrl = avatarUrl
    )
}

// ============= BRANCH MAPPERS (GraphQL -> Domain) =============

/**
 * Mapper para la nueva estructura paginada de branches
 * La respuesta viene en formato: branches.edges[].node
 */
fun GetBranchesQuery.Node.toDomain(): Branch {
    val scheduleMap = parseSchedule(schedule)

    // Convertir tipos de GraphQL a modelo de dominio
    val branchTipos = tipos?.mapNotNull { gqlTipo ->
        when (gqlTipo) {
            com.llego.multiplatform.graphql.type.BranchTipo.RESTAURANTE -> com.llego.shared.data.model.BranchTipo.RESTAURANTE
            com.llego.multiplatform.graphql.type.BranchTipo.TIENDA -> com.llego.shared.data.model.BranchTipo.TIENDA
            com.llego.multiplatform.graphql.type.BranchTipo.DULCERIA -> com.llego.shared.data.model.BranchTipo.DULCERIA
            else -> null
        }
    } ?: emptyList()

    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.toDomain(),
        phone = phone,
        schedule = scheduleMap,
        tipos = branchTipos,
        paymentMethodIds = paymentMethodIds ?: emptyList(),
        managerIds = managerIds ?: emptyList(),
        status = status ?: "active",
        avatar = avatar,
        coverImage = coverImage,
        deliveryRadius = deliveryRadius,
        facilities = facilities ?: emptyList(),
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

fun GetBranchQuery.Branch.toDomain(): Branch {
    val scheduleMap = parseSchedule(schedule)

    val branchTipos = tipos?.mapNotNull { gqlTipo ->
        when (gqlTipo) {
            com.llego.multiplatform.graphql.type.BranchTipo.RESTAURANTE -> com.llego.shared.data.model.BranchTipo.RESTAURANTE
            com.llego.multiplatform.graphql.type.BranchTipo.TIENDA -> com.llego.shared.data.model.BranchTipo.TIENDA
            com.llego.multiplatform.graphql.type.BranchTipo.DULCERIA -> com.llego.shared.data.model.BranchTipo.DULCERIA
            else -> null
        }
    } ?: emptyList()

    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.toDomain(),
        phone = phone,
        schedule = scheduleMap,
        tipos = branchTipos,
        paymentMethodIds = paymentMethodIds ?: emptyList(),
        managerIds = managerIds ?: emptyList(),
        status = status ?: "active",
        avatar = avatar,
        coverImage = coverImage,
        deliveryRadius = deliveryRadius,
        facilities = facilities ?: emptyList(),
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

fun CreateBranchMutation.CreateBranch.toDomain(): Branch {
    val scheduleMap = parseSchedule(schedule)

    val branchTipos = tipos?.mapNotNull { gqlTipo ->
        when (gqlTipo) {
            com.llego.multiplatform.graphql.type.BranchTipo.RESTAURANTE -> com.llego.shared.data.model.BranchTipo.RESTAURANTE
            com.llego.multiplatform.graphql.type.BranchTipo.TIENDA -> com.llego.shared.data.model.BranchTipo.TIENDA
            com.llego.multiplatform.graphql.type.BranchTipo.DULCERIA -> com.llego.shared.data.model.BranchTipo.DULCERIA
            else -> null
        }
    } ?: emptyList()

    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.toDomain(),
        phone = phone,
        schedule = scheduleMap,
        tipos = branchTipos,
        paymentMethodIds = paymentMethodIds ?: emptyList(),
        managerIds = emptyList(),
        status = status,
        avatar = avatar,
        coverImage = coverImage,
        deliveryRadius = deliveryRadius,
        facilities = facilities,
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

fun UpdateBranchMutation.UpdateBranch.toDomain(): Branch {
    val scheduleMap = parseSchedule(schedule)

    val branchTipos = tipos?.mapNotNull { gqlTipo ->
        when (gqlTipo) {
            com.llego.multiplatform.graphql.type.BranchTipo.RESTAURANTE -> com.llego.shared.data.model.BranchTipo.RESTAURANTE
            com.llego.multiplatform.graphql.type.BranchTipo.TIENDA -> com.llego.shared.data.model.BranchTipo.TIENDA
            com.llego.multiplatform.graphql.type.BranchTipo.DULCERIA -> com.llego.shared.data.model.BranchTipo.DULCERIA
            else -> null
        }
    } ?: emptyList()

    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.toDomain(),
        phone = phone,
        schedule = scheduleMap,
        tipos = branchTipos,
        paymentMethodIds = paymentMethodIds ?: emptyList(),
        managerIds = emptyList(),
        status = status,
        avatar = avatar,
        coverImage = coverImage,
        deliveryRadius = deliveryRadius,
        facilities = facilities,
        createdAt = "", // No viene en update
        avatarUrl = avatarUrl,
        coverUrl = coverUrl
    )
}

// ============= COORDINATES MAPPERS =============

/**
 * Mapper para coordenadas de la estructura paginada de branches
 */
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
        avatar = Optional.presentIfNotNull(avatar),
        description = Optional.presentIfNotNull(description),
        socialMedia = Optional.presentIfNotNull(socialMedia),
        tags = Optional.presentIfNotNull(tags)
    )
}

fun UpdateBusinessInput.toGraphQL(): GQLUpdateBusinessInput {
    return GQLUpdateBusinessInput(
        name = Optional.presentIfNotNull(name),
        avatar = Optional.presentIfNotNull(avatar),
        description = Optional.presentIfNotNull(description),
        socialMedia = Optional.presentIfNotNull(socialMedia),
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
        tipos = tipos.toGraphQLList(),
        paymentMethodIds = paymentMethodIds,
        address = Optional.presentIfNotNull(address),
        managerIds = Optional.presentIfNotNull(managerIds),
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
        tipos = tipos.toGraphQLList(),
        paymentMethodIds = paymentMethodIds,
        address = Optional.presentIfNotNull(address),
        managerIds = Optional.presentIfNotNull(managerIds),
        avatar = Optional.presentIfNotNull(avatar),
        coverImage = Optional.presentIfNotNull(coverImage),
        deliveryRadius = Optional.presentIfNotNull(deliveryRadius),
        facilities = Optional.presentIfNotNull(facilities)
    )
}

fun UpdateBranchInput.toGraphQL(): GQLUpdateBranchInput {
    return GQLUpdateBranchInput(
        name = Optional.presentIfNotNull(name),
        coordinates = Optional.presentIfNotNull(coordinates?.toGraphQL()),
        phone = Optional.presentIfNotNull(phone),
        schedule = Optional.presentIfNotNull(schedule),
        address = Optional.presentIfNotNull(address),
        avatar = Optional.presentIfNotNull(avatar),
        coverImage = Optional.presentIfNotNull(coverImage),
        status = Optional.presentIfNotNull(status),
        deliveryRadius = Optional.presentIfNotNull(deliveryRadius),
        facilities = Optional.presentIfNotNull(facilities),
        managerIds = Optional.presentIfNotNull(managerIds),
        tipos = Optional.presentIfNotNull(tipos?.toGraphQLList()),
        paymentMethodIds = Optional.presentIfNotNull(paymentMethodIds)
    )
}

fun CoordinatesInput.toGraphQL(): GQLCoordinatesInput {
    return GQLCoordinatesInput(
        lat = lat,
        lng = lng
    )
}

private fun parseSchedule(raw: Any?): Map<String, List<String>> {
    val map = raw as? Map<*, *> ?: return emptyMap()
    return map.mapNotNull { (key, value) ->
        val day = key as? String ?: return@mapNotNull null
        val hours = when (value) {
            is String -> listOf(value)
            is List<*> -> value.filterIsInstance<String>()
            else -> emptyList()
        }
        day to hours
    }.toMap()
}

private fun parseStringMap(raw: Any?): Map<String, String>? {
    val map = raw as? Map<*, *> ?: return null
    val parsed = map.mapNotNull { (key, value) ->
        val mapKey = key as? String ?: return@mapNotNull null
        val mapValue = value as? String ?: value?.toString()
        if (mapValue.isNullOrBlank()) null else mapKey to mapValue
    }.toMap()
    return parsed.takeIf { it.isNotEmpty() }
}

private fun List<BranchTipo>.toGraphQLList(): List<com.llego.multiplatform.graphql.type.BranchTipo> {
    return map { tipo ->
        when (tipo) {
            BranchTipo.RESTAURANTE -> com.llego.multiplatform.graphql.type.BranchTipo.RESTAURANTE
            BranchTipo.TIENDA -> com.llego.multiplatform.graphql.type.BranchTipo.TIENDA
            BranchTipo.DULCERIA -> com.llego.multiplatform.graphql.type.BranchTipo.DULCERIA
        }
    }
}
