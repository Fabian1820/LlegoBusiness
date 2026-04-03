package com.llego.shared.ui.business

import com.llego.shared.data.model.QrPayment
import com.llego.shared.data.model.TransferAccount
import com.llego.shared.data.model.TransferPhone

private fun buildAccountKey(cardNumber: String, cardHolderName: String, bankName: String): String {
    return "${cardNumber.trim()}|${cardHolderName.trim()}|${bankName.trim()}"
}

fun findInvalidTransferAccountsInputLines(text: String): List<Int> {
    return text
        .lineSequence()
        .mapIndexedNotNull { index, rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@mapIndexedNotNull null

            val parts = line.split("|").map { it.trim() }
            val isValid = parts.size >= 3 &&
                parts[0].isNotBlank() &&
                parts[1].isNotBlank() &&
                parts[2].isNotBlank()

            if (isValid) null else index + 1
        }
        .toList()
}

fun parseTransferAccountsInput(
    text: String,
    existingAccounts: List<TransferAccount> = emptyList()
): List<TransferAccount> {
    val existingStatusByAccount = existingAccounts.associate { account ->
        buildAccountKey(account.cardNumber, account.cardHolderName, account.bankName) to account.isActive
    }

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
                    isActive = existingStatusByAccount[
                        buildAccountKey(cardNumber, cardHolderName, bankName)
                    ] ?: true
                )
            }
        }
        .toList()
}

fun parseQrPaymentsInput(
    text: String,
    existingQrPayments: List<QrPayment> = emptyList()
): List<QrPayment> {
    val existingStatusByValue = existingQrPayments.associate { qr ->
        qr.value.trim() to qr.isActive
    }

    return text
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { value ->
            QrPayment(
                value = value,
                isActive = existingStatusByValue[value] ?: true
            )
        }
        .toList()
}

fun parseTransferPhonesInput(
    text: String,
    existingPhones: List<TransferPhone> = emptyList()
): List<TransferPhone> {
    val existingStatusByPhone = existingPhones.associate { phone ->
        phone.phone.trim() to phone.isActive
    }

    return text
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { value ->
            TransferPhone(
                phone = value,
                isActive = existingStatusByPhone[value] ?: true
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
