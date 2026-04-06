package com.llego.shared.ui.business

import com.llego.shared.data.model.QrPayment
import com.llego.shared.data.model.TransferAccount
import com.llego.shared.data.model.TransferPhone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val supportedBanks = setOf("BPA", "BANDEC", "METROPOLITANO")
private val jsonParser = Json { ignoreUnknownKeys = true }

private data class ParsedTransferAccountInput(
    val cardNumber: String,
    val cardHolderName: String,
    val bankName: String,
    val isActive: Boolean?
)

private fun normalizeBankName(bankName: String): String {
    return bankName.trim().uppercase()
}

private fun isSupportedBank(bankName: String): Boolean {
    return normalizeBankName(bankName) in supportedBanks
}

private fun buildAccountKey(cardNumber: String, bankName: String): String {
    return "${cardNumber.trim()}|${normalizeBankName(bankName)}"
}

private fun isJsonAccountsInput(text: String): Boolean {
    val trimmed = text.trim()
    return trimmed.startsWith("{") || trimmed.startsWith("[")
}

private fun parseLineAccount(line: String): ParsedTransferAccountInput? {
    val parts = line.split("|").map { it.trim() }
    if (parts.size < 2) return null

    val cardNumber = parts.firstOrNull().orEmpty()
    val bankName = parts.lastOrNull().orEmpty()
    val cardHolderName = if (parts.size > 2) {
        parts.subList(1, parts.lastIndex).joinToString("|").trim()
    } else {
        ""
    }

    if (cardNumber.isBlank() || bankName.isBlank()) return null
    if (!isSupportedBank(bankName)) return null

    return ParsedTransferAccountInput(
        cardNumber = cardNumber,
        cardHolderName = cardHolderName,
        bankName = normalizeBankName(bankName),
        isActive = null
    )
}

private fun parseJsonAccountElement(element: JsonElement): ParsedTransferAccountInput? {
    val accountObject = element as? JsonObject ?: return null

    val cardNumber = accountObject["cardNumber"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
    val cardHolderName = accountObject["cardHolderName"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
    val bankName = accountObject["bankName"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
    val isActive = accountObject["isActive"]?.jsonPrimitive?.booleanOrNull

    if (cardNumber.isBlank() || bankName.isBlank()) return null
    if (!isSupportedBank(bankName)) return null

    return ParsedTransferAccountInput(
        cardNumber = cardNumber,
        cardHolderName = cardHolderName,
        bankName = normalizeBankName(bankName),
        isActive = isActive
    )
}

private fun parseJsonAccountsInput(text: String): List<ParsedTransferAccountInput>? {
    return runCatching {
        val root = jsonParser.parseToJsonElement(text)
        val accountsArray = when (root) {
            is JsonArray -> root
            is JsonObject -> root["accounts"] as? JsonArray ?: return null
            else -> return null
        }

        accountsArray.map { element ->
            parseJsonAccountElement(element) ?: return null
        }
    }.getOrNull()
}

fun findInvalidTransferAccountsInputLines(text: String): List<Int> {
    if (text.isBlank()) return emptyList()
    if (isJsonAccountsInput(text)) {
        return if (parseJsonAccountsInput(text) != null) emptyList() else listOf(1)
    }

    return text
        .lineSequence()
        .mapIndexedNotNull { index, rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@mapIndexedNotNull null

            if (parseLineAccount(line) != null) null else index + 1
        }
        .toList()
}

fun parseTransferAccountsInput(
    text: String,
    existingAccounts: List<TransferAccount> = emptyList()
): List<TransferAccount> {
    if (text.isBlank()) return emptyList()

    val existingStatusByAccount = existingAccounts.associate { account ->
        buildAccountKey(account.cardNumber, account.bankName) to account.isActive
    }

    if (isJsonAccountsInput(text)) {
        val parsedAccounts = parseJsonAccountsInput(text) ?: return emptyList()
        return parsedAccounts.map { parsed ->
            TransferAccount(
                cardNumber = parsed.cardNumber,
                cardHolderName = parsed.cardHolderName,
                bankName = parsed.bankName,
                isActive = parsed.isActive ?: existingStatusByAccount[
                    buildAccountKey(parsed.cardNumber, parsed.bankName)
                ] ?: true
            )
        }
    }

    return text
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { line ->
            val parsed = parseLineAccount(line) ?: return@mapNotNull null
            TransferAccount(
                cardNumber = parsed.cardNumber,
                cardHolderName = parsed.cardHolderName,
                bankName = parsed.bankName,
                isActive = existingStatusByAccount[
                    buildAccountKey(parsed.cardNumber, parsed.bankName)
                ] ?: true
            )
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
