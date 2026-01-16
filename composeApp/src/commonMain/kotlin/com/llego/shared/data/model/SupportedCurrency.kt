package com.llego.shared.data.model

/**
 * Enum representing the supported currencies in the application.
 * Each currency includes its code, symbol, and display name.
 */
enum class SupportedCurrency(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    USD("USD", "$", "Dólar estadounidense"),
    CUP("CUP", "$", "Peso cubano"),
    EUR("EUR", "€", "Euro"),
    MLC("MLC", "$", "Moneda libremente convertible");

    companion object {
        /**
         * Get a SupportedCurrency by its code.
         * Returns null if the code doesn't match any currency.
         */
        fun fromCode(code: String): SupportedCurrency? {
            return entries.find { it.code == code }
        }

        /**
         * Get all currency codes as a list.
         */
        fun allCodes(): List<String> {
            return entries.map { it.code }
        }
    }
}
