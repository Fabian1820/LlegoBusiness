package com.llego.business.invitations.data.mappers

import com.apollographql.apollo.api.Optional
import com.llego.business.invitations.data.model.*
import com.llego.multiplatform.graphql.AcceptInvitationCodeMutation
import com.llego.multiplatform.graphql.ActiveInvitationsByBusinessQuery
import com.llego.multiplatform.graphql.BusinessAccessByBusinessQuery
import com.llego.multiplatform.graphql.GenerateInvitationCodeMutation
import com.llego.multiplatform.graphql.InvitationByCodeQuery
import com.llego.multiplatform.graphql.InvitationsByBusinessQuery
import com.llego.multiplatform.graphql.fragment.BranchReferenceFields
import com.llego.multiplatform.graphql.fragment.BusinessReferenceFields
import com.llego.multiplatform.graphql.fragment.UserAccessFields
import com.llego.multiplatform.graphql.fragment.UserReferenceFields
import com.llego.multiplatform.graphql.type.InvitationType as GraphQLInvitationType
import com.llego.multiplatform.graphql.type.InvitationStatus as GraphQLInvitationStatus
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
        business = business?.businessReferenceFields?.toInvitationBusiness()
            ?: InvitationBusiness(id = businessId, name = "Negocio"),
        branch = branch?.branchReferenceFields?.toInvitationBranch(),
        creator = creator?.userReferenceFields?.toInvitationUser(),
        redeemer = redeemer?.userReferenceFields?.toInvitationUser()
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
        business = business?.businessReferenceFields?.toInvitationBusiness()
            ?: InvitationBusiness(id = businessId, name = "Negocio"),
        branch = branch?.branchReferenceFields?.toInvitationBranch(),
        creator = creator?.userReferenceFields?.toInvitationUser(),
        redeemer = redeemer?.userReferenceFields?.toInvitationUser()
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
        business = business?.businessReferenceFields?.toInvitationBusiness()
            ?: InvitationBusiness(id = businessId, name = "Negocio"),
        branch = branch?.branchReferenceFields?.toInvitationBranch(),
        creator = creator?.userReferenceFields?.toInvitationUser(),
        redeemer = redeemer?.userReferenceFields?.toInvitationUser()
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
        business = business?.businessReferenceFields?.toInvitationBusiness()
            ?: InvitationBusiness(id = businessId, name = "Negocio"),
        branch = branch?.branchReferenceFields?.toInvitationBranch(),
        creator = creator?.userReferenceFields?.toInvitationUser(),
        redeemer = redeemer?.userReferenceFields?.toInvitationUser()
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
        business = business?.businessReferenceFields?.toInvitationBusiness()
            ?: InvitationBusiness(id = businessId, name = "Negocio"),
        branch = branch?.branchReferenceFields?.toInvitationBranch(),
        creator = creator?.userReferenceFields?.toInvitationUser(),
        redeemer = redeemer?.userReferenceFields?.toInvitationUser()
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

private fun GraphQLInvitationStatus.toInvitationStatus(): InvitationStatus {
    return when (this) {
        GraphQLInvitationStatus.PENDING -> InvitationStatus.PENDING
        GraphQLInvitationStatus.USED -> InvitationStatus.USED
        GraphQLInvitationStatus.REVOKED -> InvitationStatus.REVOKED
        GraphQLInvitationStatus.UNKNOWN__ -> InvitationStatus.PENDING
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

// BusinessAccess mappers
@OptIn(ExperimentalTime::class)
fun BusinessAccessByBusinessQuery.BusinessAccessByBusiness.toBusinessAccess(): BusinessAccess {
    return BusinessAccess(
        id = id,
        user = user?.userAccessFields?.toInvitationUser()
            ?: InvitationUser(id = "", name = "Usuario"),
        business = business?.businessReferenceFields?.toInvitationBusiness()
            ?: InvitationBusiness(id = "", name = "Negocio"),
        expiresAt = expiresAt?.toString()?.let { parseBackendDateTime(it) },
        isActive = isActive,
        isExpired = isExpired,
        daysUntilExpiration = daysUntilExpiration,
        grantedAt = parseBackendDateTime(grantedAt.toString())
    )
}

private fun BusinessReferenceFields.toInvitationBusiness(): InvitationBusiness {
    return InvitationBusiness(
        id = id,
        name = name
    )
}

private fun BranchReferenceFields.toInvitationBranch(): InvitationBranch {
    return InvitationBranch(
        id = id,
        name = name
    )
}

private fun UserReferenceFields.toInvitationUser(): InvitationUser {
    return InvitationUser(
        id = id,
        name = name
    )
}

private fun UserAccessFields.toInvitationUser(): InvitationUser {
    return InvitationUser(
        id = id,
        name = name,
        email = email
    )
}

// Helper para parsear fechas del backend que no incluyen zona horaria
@OptIn(ExperimentalTime::class)
private fun parseBackendDateTime(dateTimeString: String): Instant {
    // El backend devuelve fechas sin zona horaria, agregamos 'Z' para UTC
    val normalizedString = if (
        dateTimeString.endsWith("Z") ||
        dateTimeString.contains("+") ||
        dateTimeString.substring(10).contains("-")
    ) {
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
