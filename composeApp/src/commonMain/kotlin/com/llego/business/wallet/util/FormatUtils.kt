package com.llego.business.wallet.util

/**
 * Utilidades de formateo para Kotlin Multiplatform
 */

/**
 * Formatea un número Double a String con 2 decimales
 */
fun Double.formatToTwoDecimals(): String {
    val wholePart = this.toLong()
    val decimalPart = ((this - wholePart) * 100).toLong().toString().padStart(2, '0')
    return "$wholePart.$decimalPart"
}

/**
 * Formatea un monto con símbolo de moneda
 */
fun Double.formatCurrency(symbol: String): String {
    return "$symbol${this.formatToTwoDecimals()}"
}
