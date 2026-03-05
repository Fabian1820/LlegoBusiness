package com.llego.shared.data.mappers

import com.apollographql.apollo.api.Optional
import com.llego.multiplatform.graphql.*
import com.llego.multiplatform.graphql.fragment.BranchCoreFields
import com.llego.multiplatform.graphql.fragment.BranchUpdateFields
import com.llego.multiplatform.graphql.fragment.BusinessCoreFields
import com.llego.multiplatform.graphql.fragment.BusinessOwnedFields
import com.llego.multiplatform.graphql.fragment.BusinessRoleFields
import com.llego.multiplatform.graphql.fragment.BusinessUpdateFields
import com.llego.multiplatform.graphql.fragment.CoordinatesFields
import com.llego.multiplatform.graphql.fragment.ScoredBranchCoreFields
import com.llego.multiplatform.graphql.fragment.WalletBalanceFields
import com.llego.multiplatform.graphql.type.CoordinatesInput as GQLCoordinatesInput
import com.llego.multiplatform.graphql.type.CreateBranchInput as GQLCreateBranchInput
import com.llego.multiplatform.graphql.type.CreateBusinessInput as GQLCreateBusinessInput
import com.llego.multiplatform.graphql.type.RegisterBranchInput as GQLRegisterBranchInput
import com.llego.multiplatform.graphql.type.UpdateBranchInput as GQLUpdateBranchInput
import com.llego.multiplatform.graphql.type.UpdateBusinessInput as GQLUpdateBusinessInput
import com.llego.shared.data.model.*

/**
 * Mappers para convertir tipos GraphQL a modelos de dominio.
 */

// ============= BUSINESS MAPPERS (GraphQL -> Domain) =============

fun GetBusinessesQuery.Business.toDomain(): Business =
    businessCoreFields.toDomain()

fun GetMyBusinessesQuery.GetMyBusiness.toDomain(): Business =
    businessRoleFields.toDomain()

fun GetMyBusinessesWithBranchesQuery.GetMyBusinessesWithBranch.toDomain(): Business =
    businessRoleFields.toDomain()

fun GetBusinessQuery.Business.toDomain(): Business =
    businessOwnedFields.toDomain()

fun RegisterBusinessMutation.RegisterBusiness.toDomain(): Business =
    businessCoreFields.toDomain()

fun UpdateBusinessMutation.UpdateBusiness.toDomain(): Business =
    businessUpdateFields.toDomain()

private fun BusinessCoreFields.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        ownerId = ownerId,
        globalRating = globalRating,
        avatar = avatar,
        description = description,
        tags = tags ?: emptyList(),
        isActive = isActive,
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl
    )
}

private fun BusinessOwnedFields.toDomain(): Business {
    return businessCoreFields.toDomain()
}

private fun BusinessRoleFields.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        ownerId = ownerId,
        globalRating = globalRating,
        avatar = avatar,
        description = description,
        tags = tags ?: emptyList(),
        isActive = isActive,
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl
    )
}

private fun BusinessUpdateFields.toDomain(): Business {
    return Business(
        id = id,
        name = name,
        ownerId = ownerId,
        globalRating = globalRating,
        avatar = avatar,
        description = description,
        tags = tags ?: emptyList(),
        isActive = isActive,
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl
    )
}

// ============= BRANCH MAPPERS (GraphQL -> Domain) =============

fun GetBranchesQuery.Node.toDomain(): Branch =
    scoredBranchCoreFields.toDomain()

fun GetBranchQuery.Branch.toDomain(): Branch =
    branchCoreFields.toDomain()

fun CreateBranchMutation.CreateBranch.toDomain(): Branch =
    branchCoreFields.toDomain()

fun UpdateBranchMutation.UpdateBranch.toDomain(): Branch =
    branchUpdateFields.toDomain()

private fun ScoredBranchCoreFields.toDomain(): Branch {
    val branchTipos = tipos.mapNotNull { gqlTipo -> mapBranchTipo(gqlTipo) }
    val branchVehicles = vehicles.mapNotNull { gqlVehicle -> mapBranchVehicle(gqlVehicle) }

    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.coordinatesFields.toDomain(),
        phone = phone,
        schedule = parseSchedule(schedule),
        tipos = branchTipos,
        useAppMessaging = useAppMessaging,
        vehicles = branchVehicles,
        paymentMethodIds = paymentMethodIds,
        managerIds = managerIds,
        isActive = isActive,
        avatar = avatar,
        coverImage = coverImage,
        socialMedia = parseStringMap(socialMedia),
        accounts = accounts.map { account ->
            TransferAccount(
                cardNumber = account.cardNumber,
                cardHolderName = account.cardHolderName,
                bankName = account.bankName,
                isActive = account.isActive
            )
        },
        qrPayments = qrPayments.map { qr ->
            QrPayment(
                value = qr.value,
                isActive = qr.isActive
            )
        },
        phones = phones.map { phone ->
            TransferPhone(
                phone = phone.phone,
                isActive = phone.isActive
            )
        },
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl,
        wallet = wallet.walletBalanceFields.toDomain(),
        walletStatus = walletStatus
    )
}

private fun BranchCoreFields.toDomain(): Branch {
    val branchTipos = tipos.mapNotNull { gqlTipo -> mapBranchTipo(gqlTipo) }
    val branchVehicles = vehicles.mapNotNull { gqlVehicle -> mapBranchVehicle(gqlVehicle) }

    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.coordinatesFields.toDomain(),
        phone = phone,
        schedule = parseSchedule(schedule),
        tipos = branchTipos,
        useAppMessaging = useAppMessaging,
        vehicles = branchVehicles,
        paymentMethodIds = paymentMethodIds,
        managerIds = managerIds,
        isActive = isActive,
        avatar = avatar,
        coverImage = coverImage,
        socialMedia = parseStringMap(socialMedia),
        accounts = accounts.map { account ->
            TransferAccount(
                cardNumber = account.cardNumber,
                cardHolderName = account.cardHolderName,
                bankName = account.bankName,
                isActive = account.isActive
            )
        },
        qrPayments = qrPayments.map { qr ->
            QrPayment(
                value = qr.value,
                isActive = qr.isActive
            )
        },
        phones = phones.map { phone ->
            TransferPhone(
                phone = phone.phone,
                isActive = phone.isActive
            )
        },
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl,
        coverUrl = coverUrl,
        wallet = wallet.walletBalanceFields.toDomain(),
        walletStatus = walletStatus
    )
}

private fun BranchUpdateFields.toDomain(): Branch {
    val branchTipos = tipos.mapNotNull { gqlTipo -> mapBranchTipo(gqlTipo) }
    val branchVehicles = vehicles.mapNotNull { gqlVehicle -> mapBranchVehicle(gqlVehicle) }

    return Branch(
        id = id,
        businessId = businessId,
        name = name,
        address = address,
        coordinates = coordinates.coordinatesFields.toDomain(),
        phone = phone,
        schedule = parseSchedule(schedule),
        tipos = branchTipos,
        useAppMessaging = useAppMessaging,
        vehicles = branchVehicles,
        paymentMethodIds = paymentMethodIds,
        managerIds = managerIds,
        isActive = isActive,
        avatar = avatar,
        coverImage = coverImage,
        socialMedia = parseStringMap(socialMedia),
        accounts = accounts.map { account ->
            TransferAccount(
                cardNumber = account.cardNumber,
                cardHolderName = account.cardHolderName,
                bankName = account.bankName,
                isActive = account.isActive
            )
        },
        qrPayments = qrPayments.map { qr ->
            QrPayment(
                value = qr.value,
                isActive = qr.isActive
            )
        },
        phones = phones.map { phone ->
            TransferPhone(
                phone = phone.phone,
                isActive = phone.isActive
            )
        },
        createdAt = "",
        avatarUrl = avatarUrl,
        coverUrl = coverUrl,
        wallet = wallet.walletBalanceFields.toDomain(),
        walletStatus = walletStatus
    )
}

private fun CoordinatesFields.toDomain(): Coordinates {
    return Coordinates(
        type = type,
        coordinates = coordinates
    )
}

private fun WalletBalanceFields.toDomain(): WalletBalance {
    return WalletBalance(
        local = local,
        usd = usd
    )
}

// ============= INPUT CONVERTERS (Domain -> GraphQL) =============

fun CreateBusinessInput.toGraphQL(): GQLCreateBusinessInput {
    return GQLCreateBusinessInput(
        name = name,
        avatar = Optional.presentIfNotNull(avatar),
        description = Optional.presentIfNotNull(description),
        tags = Optional.presentIfNotNull(tags)
    )
}

fun UpdateBusinessInput.toGraphQL(): GQLUpdateBusinessInput {
    return GQLUpdateBusinessInput(
        name = Optional.presentIfNotNull(name),
        avatar = Optional.presentIfNotNull(avatar),
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
        tipos = tipos.toGraphQLList(),
        paymentMethodIds = paymentMethodIds,
        address = Optional.presentIfNotNull(address),
        managerIds = Optional.presentIfNotNull(managerIds),
        avatar = Optional.presentIfNotNull(avatar),
        coverImage = Optional.presentIfNotNull(coverImage),
        socialMedia = Optional.presentIfNotNull(socialMedia),
        accounts = Optional.presentIfNotNull(accounts?.toGraphQLAccountList()),
        qrPayments = Optional.presentIfNotNull(qrPayments?.toGraphQLQrPaymentList()),
        phones = Optional.presentIfNotNull(phones?.toGraphQLPhoneList())
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
        useAppMessaging = Optional.present(useAppMessaging),
        vehicles = Optional.presentIfNotNull(vehicles.takeIf { it.isNotEmpty() }?.toGraphQLVehicleList()),
        paymentMethodIds = paymentMethodIds,
        address = Optional.presentIfNotNull(address),
        managerIds = Optional.presentIfNotNull(managerIds),
        avatar = Optional.presentIfNotNull(avatar),
        coverImage = Optional.presentIfNotNull(coverImage),
        socialMedia = Optional.presentIfNotNull(socialMedia),
        accounts = Optional.presentIfNotNull(accounts?.toGraphQLAccountList()),
        qrPayments = Optional.presentIfNotNull(qrPayments?.toGraphQLQrPaymentList()),
        phones = Optional.presentIfNotNull(phones?.toGraphQLPhoneList())
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
        isActive = Optional.presentIfNotNull(isActive),
        socialMedia = Optional.presentIfNotNull(socialMedia),
        managerIds = Optional.presentIfNotNull(managerIds),
        tipos = Optional.presentIfNotNull(tipos?.toGraphQLList()),
        useAppMessaging = Optional.presentIfNotNull(useAppMessaging),
        vehicles = Optional.presentIfNotNull(vehicles?.toGraphQLVehicleList()),
        paymentMethodIds = Optional.presentIfNotNull(paymentMethodIds),
        accounts = Optional.presentIfNotNull(accounts?.toGraphQLAccountList()),
        qrPayments = Optional.presentIfNotNull(qrPayments?.toGraphQLQrPaymentList()),
        phones = Optional.presentIfNotNull(phones?.toGraphQLPhoneList())
    )
}

fun CoordinatesInput.toGraphQL(): GQLCoordinatesInput {
    return GQLCoordinatesInput(
        lat = lat,
        lng = lng
    )
}

private fun List<BranchTipo>.toGraphQLList(): List<com.llego.multiplatform.graphql.type.BranchTipo> {
    return map { tipo ->
        tipo.toGraphQL()
    }
}

private fun List<BranchVehicle>.toGraphQLVehicleList(): List<com.llego.multiplatform.graphql.type.BranchVehicle> {
    return map { vehicle -> vehicle.toGraphQL() }
}

private fun List<TransferAccount>.toGraphQLAccountList(): List<com.llego.multiplatform.graphql.type.TransferAccountInput> {
    return map { account ->
        com.llego.multiplatform.graphql.type.TransferAccountInput(
            cardNumber = account.cardNumber,
            cardHolderName = account.cardHolderName,
            bankName = account.bankName,
            isActive = Optional.present(account.isActive)
        )
    }
}

private fun List<QrPayment>.toGraphQLQrPaymentList(): List<com.llego.multiplatform.graphql.type.QrPaymentInput> {
    return map { qr ->
        com.llego.multiplatform.graphql.type.QrPaymentInput(
            value = qr.value,
            isActive = Optional.present(qr.isActive)
        )
    }
}

private fun List<TransferPhone>.toGraphQLPhoneList(): List<com.llego.multiplatform.graphql.type.TransferPhoneInput> {
    return map { phone ->
        com.llego.multiplatform.graphql.type.TransferPhoneInput(
            phone = phone.phone,
            isActive = Optional.present(phone.isActive)
        )
    }
}

private fun BranchTipo.toGraphQL(): com.llego.multiplatform.graphql.type.BranchTipo {
    val candidateNames = when (this) {
        BranchTipo.RESTAURANTE -> listOf("RESTAURANTE", "RESTAURANT")
        BranchTipo.TIENDA -> listOf("TIENDA", "STORE")
        BranchTipo.DULCERIA -> listOf("DULCERIA", "BAKERY")
        BranchTipo.PERFUMERIA -> listOf("PERFUMERIA", "PERFUME")
    }

    for (candidate in candidateNames) {
        try {
            return com.llego.multiplatform.graphql.type.BranchTipo.valueOf(candidate)
        } catch (_: IllegalArgumentException) {
            // Intentar con el siguiente nombre candidato
        }
    }

    // Fallback: primer valor disponible del enum GraphQL
    return com.llego.multiplatform.graphql.type.BranchTipo.values().first()
}

private fun BranchVehicle.toGraphQL(): com.llego.multiplatform.graphql.type.BranchVehicle {
    return when (this) {
        BranchVehicle.MOTO -> com.llego.multiplatform.graphql.type.BranchVehicle.MOTO
        BranchVehicle.BICICLETA -> com.llego.multiplatform.graphql.type.BranchVehicle.BICICLETA
        BranchVehicle.CARRO -> com.llego.multiplatform.graphql.type.BranchVehicle.CARRO
        BranchVehicle.CAMIONETA -> com.llego.multiplatform.graphql.type.BranchVehicle.CAMIONETA
        BranchVehicle.CAMINANDO -> com.llego.multiplatform.graphql.type.BranchVehicle.CAMINANDO
    }
}
