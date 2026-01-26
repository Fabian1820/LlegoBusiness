package com.llego.business.invitations.data.mappers

import com.apollographql.apollo.api.Optional
import com.llego.business.invitations.data.model.*
import com.llego.multiplatform.graphql.AcceptInvitationCodeMutation
import com.llego.multiplatform.graphql.GenerateInvitationCodeMutation
import com.llego.multiplatform.graphql.InvitationByCodeQuery
import com.llego.multiplatform.graphql.InvitationsByBusinessQuery
import com.llego.multiplatform.graphql.ActiveInvitationsByBusinessQuery
import com.llego.multiplatform.graphql.type.InvitationType as GraphQLInvitationType
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun InvitationByCodeQuery.InvitationByCode?.toInvitation(): Invitation? {
    if (this == null) return null
    
    return Invitation(
        id = id,
        code = code,
        invitationType = invitationType.toInvitationType(),
        status = status.toInvitationStatus(),
        accessDurationDays = accessDurationDays,
        accessExpiresAt = accessExpiresAt?.toString()?.let { parseBackendDateTime(it) },
        isUsable = isUsable,
        accessStatus = accessStatus.toAccessStatus(),
        createdAt = parseBackendDateTime(createdAt.toString()),
        usedAt = usedAt?.toString()?.let { parseBackendDateTime(it) },
        business = InvitationBusiness(
            id = business.id,
            name = business.name
        ),
        branch = branch?.let {
            InvitationBranch(
                id = it.id,
                name = it.name
            )
        },
        creator = creator?.let {
            InvitationUser(
                id = it.id,
                name = it.name
            )
        },
        redeemer = redeemer?.let {
            InvitationUser(
                id = it.id,
                name = it.name
            )
        }
    )
}

@OptIn(ExperimentalTime::class)
fun GenerateInvitationCodeMutation.GenerateInvitationCode.toInvitation(): Invitation {
    return Invitation(
        id = id,
        code = code,
        invitationType = invitationType.toInvitationType(),
        status = status.toInvitationStatus(),
        accessDurationDays = accessDurationDays,
        accessExpiresAt = accessExpiresAt?.toString()?.let { parseBackendDateTime(it) },
        isUsable = isUsable,
        accessStatus = accessStatus.toAccessStatus(),
        createdAt = parseBackendDateTime(createdAt.toString()),
        usedAt = usedAt?.toString()?.let { parseBackendDateTime(it) },
        business = InvitationBusiness(
            id = business.id,
            name = business.name
        ),
        branch = branch?.let {
            InvitationBranch(
                id = it.id,
                name = it.name
            )
        },
        creator = creator?.let {
            InvitationUser(
                id = it.id,
                name = it.name
            )
        },
        redeemer = redeemer?.let {
            InvitationUser(
                id = it.id,
                name = it.name
            )
        }
    )
}

@OptIn(ExperimentalTime::class)
fun AcceptInvitationCodeMutation.AcceptInvitationCode.toInvitation(): Invitation {
    return Invitation(
        id = id,
        code = code,
        invitationType = invitationType.toInvitationType(),
        status = status.toInvitationStatus(),
        accessDurationDays = accessDurationDays,
        accessExpiresAt = accessExpiresAt?.toString()?.let { parseBackendDateTime(it) },
        isUsable = isUsable,
        accessStatus = accessStatus.toAccessStatus(),
        createdAt = parseBackendDateTime(createdAt.toString()),
        usedAt = usedAt?.toString()?.let { parseBackendDateTime(it) },
        business = InvitationBusiness(
            id = business.id,
            name = business.name
        ),
        branch = branch?.let {
            InvitationBranch(
                id = it.id,
                name = it.name
            )
        },
        creator = creator?.let {
            InvitationUser(
                id = it.id,
                name = it.name
            )
        },
        redeemer = redeemer?.let {
            InvitationUser(
                id = it.id,
                name = it.name
            )
        }
    )
}

@OptIn(ExperimentalTime::class)
fun InvitationsByBusinessQuery.InvitationsByBusiness.toInvitation(): Invitation {
    return Invitation(
        id = id,
        code = code,
        invitationType = invitationType.toInvitationType(),
        status = status.toInvitationStatus(),
        accessDurationDays = accessDurationDays,
        accessExpiresAt = accessExpiresAt?.toString()?.let { parseBackendDateTime(it) },
        isUsable = isUsable,
        accessStatus = accessStatus.toAccessStatus(),
        createdAt = parseBackendDateTime(createdAt.toString()),
        usedAt = usedAt?.toString()?.let { parseBackendDateTime(it) },
        business = InvitationBusiness(
            id = business.id,
            name = business.name
        ),
        branch = branch?.let {
            InvitationBranch(
                id = it.id,
                name = it.name
            )
        },
        creator = creator?.let {
            InvitationUser(
                id = it.id,
                name = it.name
            )
        },
        redeemer = redeemer?.let {
            InvitationUser(
                id = it.id,
                name = it.name
            )
        }
    )
}

@OptIn(ExperimentalTime::class)
fun ActiveInvitationsByBusinessQuery.ActiveInvitationsByBusiness.toInvitation(): Invitation {
    return Invitation(
        id = id,
        code = code,
        invitationType = invitationType.toInvitationType(),
        status = status.toInvitationStatus(),
        accessDurationDays = accessDurationDays,
        accessExpiresAt = accessExpiresAt?.toString()?.let { parseBackendDateTime(it) },
        isUsable = isUsable,
        accessStatus = accessStatus.toAccessStatus(),
        createdAt = parseBackendDateTime(createdAt.toString()),
        usedAt = usedAt?.toString()?.let { parseBackendDateTime(it) },
        business = InvitationBusiness(
            id = business.id,
            name = business.name
        ),
        branch = branch?.let {
            InvitationBranch(
                id = it.id,
                name = it.name
            )
        },
        creator = creator?.let {
            InvitationUser(
                id = it.id,
                name = it.name
            )
        },
        redeemer = redeemer?.let {
            InvitationUser(
                id = it.id,
                name = it.name
            )
        }
    )
}

// Enum mappers
private fun GraphQLInvitationType.toInvitationType(): InvitationType {
    return when (this) {
        GraphQLInvitationType.BRANCH -> InvitationType.BRANCH
        GraphQLInvitationType.BUSINESS -> InvitationType.BUSINESS
        GraphQLInvitationType.UNKNOWN__ -> InvitationType.BRANCH
    }
}

private fun String.toInvitationStatus(): InvitationStatus {
    return when (this.uppercase()) {
        "PENDING" -> InvitationStatus.PENDING
        "USED" -> InvitationStatus.USED
        "REVOKED" -> InvitationStatus.REVOKED
        else -> InvitationStatus.PENDING
    }
}

private fun String.toAccessStatus(): AccessStatus {
    return when (this.uppercase()) {
        "PENDING" -> AccessStatus.PENDING
        "ACTIVE" -> AccessStatus.ACTIVE
        "EXPIRED" -> AccessStatus.EXPIRED
        else -> AccessStatus.PENDING
    }
}

// Helper para parsear fechas del backend que no incluyen zona horaria
@OptIn(ExperimentalTime::class)
private fun parseBackendDateTime(dateTimeString: String): Instant {
    // El backend devuelve fechas sin zona horaria, agregamos 'Z' para UTC
    val normalizedString = if (dateTimeString.endsWith("Z") || dateTimeString.contains("+") || dateTimeString.substring(10).contains("-")) {
        dateTimeString
    } else {
        "${dateTimeString}Z"
    }
    return Instant.parse(normalizedString)
}

// Input mappers
fun GenerateInvitationInput.toGraphQLInput(): com.llego.multiplatform.graphql.type.GenerateInvitationInput {
    return com.llego.multiplatform.graphql.type.GenerateInvitationInput(
        invitationType = when (invitationType) {
            InvitationType.BRANCH -> GraphQLInvitationType.BRANCH
            InvitationType.BUSINESS -> GraphQLInvitationType.BUSINESS
        },
        businessId = businessId,
        branchId = if (branchId != null) Optional.present(branchId) else Optional.absent(),
        accessDurationDays = if (accessDurationDays != null) Optional.present(accessDurationDays) else Optional.absent()
    )
}

fun GenerateInvitationInput.toGraphQLInvitationType(): GraphQLInvitationType {
    return when (invitationType) {
        InvitationType.BRANCH -> GraphQLInvitationType.BRANCH
        InvitationType.BUSINESS -> GraphQLInvitationType.BUSINESS
    }
}

fun GenerateInvitationInput.toOptionalBranchId(): Optional<String> {
    return if (branchId != null) Optional.present(branchId) else Optional.absent()
}

fun GenerateInvitationInput.toOptionalAccessDurationDays(): Optional<Int> {
    return if (accessDurationDays != null) Optional.present(accessDurationDays) else Optional.absent()
}
