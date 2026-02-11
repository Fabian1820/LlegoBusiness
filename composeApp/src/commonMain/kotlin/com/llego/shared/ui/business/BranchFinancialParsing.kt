package com.llego.shared.ui.business

import com.llego.shared.data.model.QrPayment
import com.llego.shared.data.model.TransferAccount
import com.llego.shared.data.model.TransferPhone

fun parseTransferAccountsInput(text: String): List<TransferAccount> {
    return text
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { line ->
            val parts = line.split("|").map { it.trim() }
            if (parts.size < 3) return@mapNotNull null
            val cardNumber = parts[0]
            val cardHolderName = parts[1]
            val bankName = parts[2]
            if (cardNumber.isBlank() || cardHolderName.isBlank() || bankName.isBlank()) {
                null
            } else {
                TransferAccount(
                    cardNumber = cardNumber,
                    cardHolderName = cardHolderName,
                    bankName = bankName,
                    isActive = true
                )
            }
        }
        .toList()
}

fun parseQrPaymentsInput(text: String): List<QrPayment> {
    return text
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { value ->
            QrPayment(
                value = value,
                isActive = true
            )
        }
        .toList()
}

fun parseTransferPhonesInput(text: String): List<TransferPhone> {
    return text
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { value ->
            TransferPhone(
                phone = value,
                isActive = true
            )
        }
        .toList()
}

fun formatTransferAccountsInput(accounts: List<TransferAccount>): String {
    return accounts.joinToString("\n") { account ->
        "${account.cardNumber}|${account.cardHolderName}|${account.bankName}"
    }
}

fun formatQrPaymentsInput(qrPayments: List<QrPayment>): String {
    return qrPayments.joinToString("\n") { it.value }
}

fun formatTransferPhonesInput(phones: List<TransferPhone>): String {
    return phones.joinToString("\n") { it.phone }
}
