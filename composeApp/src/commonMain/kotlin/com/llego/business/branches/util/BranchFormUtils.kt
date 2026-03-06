package com.llego.business.branches.util

fun parseManagerIds(value: String): List<String> {
    return value.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

fun parseExchangeRate(value: String): Int? {
    val normalized = value.trim()
    if (normalized.isEmpty()) return null
    if (!normalized.all { it.isDigit() }) return null
    return normalized.toIntOrNull()
}

fun validateExchangeRateInput(value: String): String? {
    if (value.isBlank()) return null
    val parsed = parseExchangeRate(value) ?: return "La tasa de cambio debe ser un numero entero."
    if (parsed <= 0) return "La tasa de cambio debe ser mayor que 0."
    return null
}
