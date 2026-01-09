package com.llego.shared.utils

import platform.Foundation.NSString
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual fun formatDouble(format: String, value: Double): String {
    // Usar NSString localizedStringWithFormat para formatear
    // Para formateo de double con %.2f u otros patrones estándar
    // Usamos una implementación manual simple ya que varargs en Kotlin/Native es complejo
    
    // Extraer el número de decimales del formato (ej: "%.2f" -> 2)
    val decimals = when {
        format.contains(".2f") -> 2
        format.contains(".1f") -> 1
        format.contains(".0f") -> 0
        else -> 2 // default
    }
    
    // Formatear manualmente
    val multiplier = when (decimals) {
        0 -> 1.0
        1 -> 10.0
        2 -> 100.0
        else -> 100.0
    }
    
    val rounded = kotlin.math.round(value * multiplier) / multiplier
    
    // Formatear manualmente sin usar String.format
    val intPart = rounded.toLong()
    val fracPart = ((rounded - intPart) * multiplier).toLong().toString().padStart(decimals, '0')
    
    return if (decimals > 0) "$intPart.$fracPart" else "$intPart"
}
