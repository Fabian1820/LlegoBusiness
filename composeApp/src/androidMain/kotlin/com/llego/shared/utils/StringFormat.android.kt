package com.llego.shared.utils

actual fun formatDouble(format: String, value: Double): String {
    return String.format(format, value)
}
