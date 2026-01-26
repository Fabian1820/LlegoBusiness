package com.llego.business.invitations.data.model

import kotlinx.datetime.Instant

enum class InvitationType {
    BRANCH,
    BUSINESS
}

enum class InvitationStatus {
    PENDING,
    USED,
    REVOKED
}

enum class AccessStatus {
    PENDING,
    ACTIVE,
    EXPIRED
}

data class Invitation(
    val id: String,
    val code: String,
    val invitationType: InvitationType,
    val status: InvitationStatus,
    val accessDurationDays: Int?,
    val accessExpiresAt: Instant?,
    val isUsable: Boolean,
    val accessStatus: AccessStatus,
    val createdAt: Instant,
    val usedAt: Instant?,
    val business: InvitationBusiness,
    val branch: InvitationBranch?,
    val creator: InvitationUser?,
    val redeemer: InvitationUser?
)

data class InvitationBusiness(
    val id: String,
    val name: String
)

data class InvitationBranch(
    val id: String,
    val name: String
)

data class InvitationUser(
    val id: String,
    val name: String
)

data class GenerateInvitationInput(
    val invitationType: InvitationType,
    val businessId: String,
    val branchId: String?,
    val accessDurationDays: Int?
)

sealed class InvitationDuration {
    object Indefinite : InvitationDuration()
    data class Days(val days: Int) : InvitationDuration()
    
    companion object {
        val ONE_DAY = Days(1)
        val ONE_WEEK = Days(7)
        val ONE_MONTH = Days(30)
    }
    
    fun toDays(): Int? = when (this) {
        is Indefinite -> null
        is Days -> days
    }
}
