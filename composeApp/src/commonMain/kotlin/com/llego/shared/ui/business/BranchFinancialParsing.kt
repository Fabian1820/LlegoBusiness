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

private val jsonParser = Json { ignoreUnknownKeys = true }

private data class ParsedTransferAccountInput(
    val cardNumber: String,
    val confirmPhone: String,
    val cardHolderName: String?,
    val pagoQr: String?,
    val isActive: Boolean?
)

private fun digits(value: String): String {
    return value.filter { it.isDigit() }
}

fun normalizeCardNumber(cardNumber: String): String? {
    val normalized = digits(cardNumber)
    return normalized.takeIf { it.length == 16 }
}

fun normalizeConfirmPhone(confirmPhone: String): String? {
    var normalized = digits(confirmPhone)
    if (normalized.length == 10 && normalized.startsWith("53")) {
        normalized = normalized.drop(2)
    }
    return normalized.takeIf { it.length == 8 }
}

private fun buildAccountKey(cardNumber: String, confirmPhone: String): String {
    return "${cardNumber.trim()}|${confirmPhone.trim()}"
}

private fun isJsonAccountsInput(text: String): Boolean {
    val trimmed = text.trim()
    return trimmed.startsWith("{") || trimmed.startsWith("[")
}

private fun parseLineAccount(line: String): ParsedTransferAccountInput? {
    val parts = line.split("|").map { it.trim() }
    if (parts.size < 2) return null

    val cardNumber = parts.firstOrNull().orEmpty()
    val confirmPhone = parts.getOrElse(1) { "" }
    val cardHolderName = if (parts.size > 2) {
        parts.subList(2, parts.size).joinToString("|").trim()
    } else {
        null
    }
    if (cardNumber.isBlank() || confirmPhone.isBlank()) return null

    return ParsedTransferAccountInput(
        cardNumber = cardNumber,
        confirmPhone = confirmPhone,
        cardHolderName = cardHolderName,
        pagoQr = null,
        isActive = null
    )
}

private fun parseJsonAccountElement(element: JsonElement): ParsedTransferAccountInput? {
    val accountObject = element as? JsonObject ?: return null

    val cardNumber = accountObject["cardNumber"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
    val confirmPhone = accountObject["confirmPhone"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
    val cardHolderName = accountObject["cardHolderName"]?.jsonPrimitive?.contentOrNull?.trim()
    val pagoQr = accountObject["pagoQr"]?.jsonPrimitive?.contentOrNull?.trim()
    val isActive = accountObject["isActive"]?.jsonPrimitive?.booleanOrNull

    if (cardNumber.isBlank() || confirmPhone.isBlank()) return null

    return ParsedTransferAccountInput(
        cardNumber = cardNumber,
        confirmPhone = confirmPhone,
        cardHolderName = cardHolderName,
        pagoQr = pagoQr,
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
        buildAccountKey(account.cardNumber, account.confirmPhone) to account.isActive
    }
    val existingQrByAccount = existingAccounts.associate { account ->
        buildAccountKey(account.cardNumber, account.confirmPhone) to account.pagoQr
    }

    if (isJsonAccountsInput(text)) {
        val parsedAccounts = parseJsonAccountsInput(text) ?: return emptyList()
        return parsedAccounts.mapNotNull { parsed ->
            val normalizedCard = normalizeCardNumber(parsed.cardNumber) ?: return@mapNotNull null
            val normalizedPhone = normalizeConfirmPhone(parsed.confirmPhone) ?: return@mapNotNull null
            TransferAccount(
                cardNumber = normalizedCard,
                confirmPhone = normalizedPhone,
                cardHolderName = parsed.cardHolderName,
                pagoQr = parsed.pagoQr ?: existingQrByAccount[
                    buildAccountKey(normalizedCard, normalizedPhone)
                ],
                isActive = parsed.isActive ?: existingStatusByAccount[
                    buildAccountKey(normalizedCard, normalizedPhone)
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
            val normalizedCard = normalizeCardNumber(parsed.cardNumber) ?: return@mapNotNull null
            val normalizedPhone = normalizeConfirmPhone(parsed.confirmPhone) ?: return@mapNotNull null
            TransferAccount(
                cardNumber = normalizedCard,
                confirmPhone = normalizedPhone,
                cardHolderName = parsed.cardHolderName,
                pagoQr = existingQrByAccount[
                    buildAccountKey(normalizedCard, normalizedPhone)
                ],
                isActive = existingStatusByAccount[
                    buildAccountKey(normalizedCard, normalizedPhone)
                ] ?: true
            )
        }
        .toList()
}

fun findInvalidTransferAccountItems(accounts: List<TransferAccount>): List<Int> {
    return accounts.mapIndexedNotNull { index, account ->
        val cardNumber = account.cardNumber.trim()
        val confirmPhone = account.confirmPhone.trim()
        val cardHolderName = account.cardHolderName.orEmpty().trim()
        val isEmptyRow = cardNumber.isEmpty() && confirmPhone.isEmpty() && cardHolderName.isEmpty()
        if (isEmptyRow) return@mapIndexedNotNull null

        val isValid = normalizeCardNumber(cardNumber) != null &&
            normalizeConfirmPhone(confirmPhone) != null
        if (isValid) null else index + 1
    }
}

fun normalizeTransferAccountsInput(
    accounts: List<TransferAccount>,
    existingAccounts: List<TransferAccount> = emptyList()
): List<TransferAccount> {
    val existingStatusByAccount = existingAccounts.associate { account ->
        buildAccountKey(account.cardNumber, account.confirmPhone) to account.isActive
    }
    val existingQrByAccount = existingAccounts.associate { account ->
        buildAccountKey(account.cardNumber, account.confirmPhone) to account.pagoQr
    }

    return accounts.mapNotNull { account ->
        val cardNumber = account.cardNumber.trim()
        val confirmPhone = account.confirmPhone.trim()
        val cardHolderName = account.cardHolderName.orEmpty().trim()

        val isEmptyRow = cardNumber.isEmpty() && confirmPhone.isEmpty() && cardHolderName.isEmpty()
        if (isEmptyRow) return@mapNotNull null
        val normalizedCard = normalizeCardNumber(cardNumber) ?: return@mapNotNull null
        val normalizedPhone = normalizeConfirmPhone(confirmPhone) ?: return@mapNotNull null

        TransferAccount(
            cardNumber = normalizedCard,
            confirmPhone = normalizedPhone,
            cardHolderName = cardHolderName.ifBlank { null },
            pagoQr = account.pagoQr ?: existingQrByAccount[
                buildAccountKey(normalizedCard, normalizedPhone)
            ],
            isActive = account.isActive
        )
    }
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

fun normalizeQrPaymentsInput(
    qrPayments: List<QrPayment>,
    existingQrPayments: List<QrPayment> = emptyList()
): List<QrPayment> {
    val existingStatusByValue = existingQrPayments.associate { qr ->
        qr.value.trim() to qr.isActive
    }

    return qrPayments.mapNotNull { qr ->
        val value = qr.value.trim()
        if (value.isEmpty()) return@mapNotNull null
        QrPayment(
            value = value,
            isActive = qr.isActive.takeIf { it } ?: existingStatusByValue[value] ?: true
        )
    }
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

fun normalizeTransferPhonesInput(
    phones: List<TransferPhone>,
    existingPhones: List<TransferPhone> = emptyList()
): List<TransferPhone> {
    val existingStatusByPhone = existingPhones.associate { phone ->
        phone.phone.trim() to phone.isActive
    }

    return phones.mapNotNull { phone ->
        val value = phone.phone.trim()
        if (value.isEmpty()) return@mapNotNull null
        TransferPhone(
            phone = value,
            isActive = phone.isActive.takeIf { it } ?: existingStatusByPhone[value] ?: true
        )
    }
}

fun formatTransferAccountsInput(accounts: List<TransferAccount>): String {
    return accounts.joinToString("\n") { account ->
        val holder = account.cardHolderName.orEmpty()
        if (holder.isBlank()) {
            "${account.cardNumber}|${account.confirmPhone}"
        } else {
            "${account.cardNumber}|${account.confirmPhone}|$holder"
        }
    }
}

fun formatQrPaymentsInput(qrPayments: List<QrPayment>): String {
    return qrPayments.joinToString("\n") { it.value }
}

fun formatTransferPhonesInput(phones: List<TransferPhone>): String {
    return phones.joinToString("\n") { it.phone }
}
