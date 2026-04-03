package com.llego.business.orders.data.mappers

import com.llego.business.orders.data.model.CustomerCashKycStatus
import com.llego.multiplatform.graphql.CustomerCashKycStatusQuery

fun CustomerCashKycStatusQuery.CashKycStatusByAccount.toDomain(): CustomerCashKycStatus =
    CustomerCashKycStatus(
        verificationId = verificationId,
        kycEvalStatus = kycEvalStatus,
        cashCoverageStatus = cashCoverageStatus,
        allowCash = allowCash,
        appCoversCash = appCoversCash,
        nextAction = nextAction,
        expiresAt = expiresAt?.toString()
    )
