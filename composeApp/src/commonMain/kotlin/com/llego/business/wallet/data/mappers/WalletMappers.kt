package com.llego.business.wallet.data.mappers

import com.llego.business.wallet.data.model.TransactionStatus
import com.llego.business.wallet.data.model.TransactionType
import com.llego.business.wallet.data.model.WalletCurrency
import com.llego.business.wallet.data.model.WalletTransaction
import com.llego.multiplatform.graphql.BranchWalletQuery
import com.llego.multiplatform.graphql.BranchWalletTransactionsQuery
import com.llego.multiplatform.graphql.BranchTransferMoneyMutation
import com.llego.multiplatform.graphql.BranchWithdrawMoneyMutation
import com.llego.multiplatform.graphql.DepositMoneyMutation
import com.llego.multiplatform.graphql.MyWalletQuery
import com.llego.multiplatform.graphql.MyWalletTransactionsQuery
import com.llego.multiplatform.graphql.TransferMoneyMutation
import com.llego.multiplatform.graphql.WithdrawMoneyMutation

internal fun MyWalletQuery.Balance.toBalanceMap(): Map<WalletCurrency, Double> {
    return mapOf(
        WalletCurrency.CUP to local,
        WalletCurrency.USD to usd
    )
}

internal fun BranchWalletQuery.Balance.toBalanceMap(): Map<WalletCurrency, Double> {
    return mapOf(
        WalletCurrency.CUP to local,
        WalletCurrency.USD to usd
    )
}

internal fun MyWalletTransactionsQuery.MyWalletTransaction.toDomain(): WalletTransaction {
    return WalletTransaction(
        id = id,
        businessId = toOwnerId ?: fromOwnerId ?: "",
        type = type.toTransactionType(),
        currency = currency.toWalletCurrency(),
        amount = amount,
        description = description ?: "",
        relatedOrderId = null,
        status = status.toTransactionStatus(),
        createdAt = createdAt.toString(),
        completedAt = completedAt?.toString()
    )
}

internal fun BranchWalletTransactionsQuery.BranchWalletTransaction.toDomain(): WalletTransaction {
    return WalletTransaction(
        id = id,
        businessId = toOwnerId ?: fromOwnerId ?: "",
        type = type.toTransactionType(),
        currency = currency.toWalletCurrency(),
        amount = amount,
        description = description ?: "",
        relatedOrderId = null,
        status = status.toTransactionStatus(),
        createdAt = createdAt.toString(),
        completedAt = completedAt?.toString()
    )
}

internal fun TransferMoneyMutation.TransferMoney.toWalletTransaction(): WalletTransaction {
    return WalletTransaction(
        id = id,
        businessId = fromOwnerId ?: "",
        type = TransactionType.TRANSFER,
        currency = currency.toWalletCurrency(),
        amount = -amount,
        description = description ?: "Transferencia",
        status = status.toTransferStatus(),
        createdAt = createdAt.toString(),
        completedAt = completedAt?.toString()
    )
}

internal fun DepositMoneyMutation.DepositMoney.toWalletTransaction(): WalletTransaction {
    return WalletTransaction(
        id = id,
        businessId = toOwnerId ?: "",
        type = TransactionType.INCOME,
        currency = currency.toWalletCurrency(),
        amount = amount,
        description = description ?: "DepÃ³sito",
        status = status.toTransferStatus(),
        createdAt = createdAt.toString(),
        completedAt = completedAt?.toString()
    )
}

internal fun WithdrawMoneyMutation.WithdrawMoney.toWalletTransaction(): WalletTransaction {
    return WalletTransaction(
        id = id,
        businessId = fromOwnerId ?: "",
        type = TransactionType.WITHDRAWAL,
        currency = currency.toWalletCurrency(),
        amount = -amount,
        description = description ?: "Retiro",
        status = status.toWithdrawStatus(),
        createdAt = createdAt.toString(),
        completedAt = completedAt?.toString()
    )
}

internal fun BranchTransferMoneyMutation.BranchTransferMoney.toWalletTransaction(): WalletTransaction {
    return WalletTransaction(
        id = id,
        businessId = fromOwnerId ?: "",
        type = TransactionType.TRANSFER,
        currency = currency.toWalletCurrency(),
        amount = -amount,
        description = description ?: "Transferencia",
        status = status.toTransferStatus(),
        createdAt = createdAt.toString(),
        completedAt = completedAt?.toString()
    )
}

internal fun BranchWithdrawMoneyMutation.BranchWithdrawMoney.toWalletTransaction(): WalletTransaction {
    return WalletTransaction(
        id = id,
        businessId = fromOwnerId ?: "",
        type = TransactionType.WITHDRAWAL,
        currency = currency.toWalletCurrency(),
        amount = -amount,
        description = description ?: "Retiro",
        status = status.toWithdrawStatus(),
        createdAt = createdAt.toString(),
        completedAt = completedAt?.toString()
    )
}

private fun String.toTransactionType(): TransactionType = when (lowercase()) {
    "transfer" -> TransactionType.TRANSFER
    "deposit" -> TransactionType.INCOME
    "withdrawal" -> TransactionType.WITHDRAWAL
    else -> TransactionType.ADJUSTMENT
}

private fun String.toTransactionStatus(): TransactionStatus = when (lowercase()) {
    "pending" -> TransactionStatus.PENDING
    "processing" -> TransactionStatus.PROCESSING
    "completed" -> TransactionStatus.COMPLETED
    "failed" -> TransactionStatus.FAILED
    "cancelled" -> TransactionStatus.CANCELLED
    else -> TransactionStatus.PENDING
}

private fun String.toTransferStatus(): TransactionStatus = when (lowercase()) {
    "pending" -> TransactionStatus.PENDING
    "completed" -> TransactionStatus.COMPLETED
    "failed" -> TransactionStatus.FAILED
    else -> TransactionStatus.COMPLETED
}

private fun String.toWithdrawStatus(): TransactionStatus = when (lowercase()) {
    "pending" -> TransactionStatus.PENDING
    "processing" -> TransactionStatus.PROCESSING
    "completed" -> TransactionStatus.COMPLETED
    "failed" -> TransactionStatus.FAILED
    else -> TransactionStatus.PENDING
}

private fun String.toWalletCurrency(): WalletCurrency = when (uppercase()) {
    "USD" -> WalletCurrency.USD
    "LOCAL", "CUP" -> WalletCurrency.CUP
    else -> WalletCurrency.CUP
}
