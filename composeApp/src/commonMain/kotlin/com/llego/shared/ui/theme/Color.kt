package com.llego.shared.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores del sistema de diseño Llego
 * Basada en las especificaciones del documento LLEGO_DESIGN_SYSTEM.md
 */

// Colores primarios Llego
val LlegoPrimary = Color(2, 49, 51)                    // #023133 - Teal oscuro principal
val LlegoSecondary = Color(225, 199, 142)              // #E1C78E - Beige cálido
val LlegoTertiary = Color(124, 65, 43)                 // #7C412B - Marrón
val LlegoBackground = Color(0xFFF3F3F3)                // Gris claro de fondo
val LlegoSurface = Color(0xFFFFFFFF)                   // Blanco para cards
val LlegoOnBackground = Color(0xFF1B1B1B)              // Texto principal
val LlegoOnSurfaceVariant = Color(19, 45, 47)          // Texto secundario

// Colores de acento
val LlegoAccentPrimary = Color(178, 214, 154)          // #B2D69A - Verde claro
val LlegoAccentSecondary = Color(157, 205, 120)        // #9DCD78 - Verde medio
val LlegoSurfaceVariant = Color(236, 240, 233)         // #ECF0E9 - Fondo suave

// Colores adicionales para estados
val LlegoError = Color(0xFFD32F2F)                     // Rojo para errores
val LlegoWarning = Color(0xFFF57C00)                   // Naranja para advertencias
val LlegoSuccess = Color(178, 214, 154)                // Verde para éxito (mismo que accent)
val LlegoInfo = Color(0xFF1976D2)                      // Azul para información

/**
 * Esquema de colores claro para Llego
 */
val LlegoLightColorScheme = lightColorScheme(
    primary = LlegoPrimary,
    onPrimary = Color.White,
    secondary = LlegoSecondary,
    onSecondary = LlegoPrimary,
    tertiary = LlegoTertiary,
    onTertiary = Color.White,
    background = LlegoBackground,
    onBackground = LlegoOnBackground,
    surface = LlegoSurface,
    onSurface = LlegoOnBackground,
    surfaceVariant = LlegoSurfaceVariant,
    onSurfaceVariant = LlegoOnSurfaceVariant,
    primaryContainer = LlegoAccentPrimary,
    onPrimaryContainer = LlegoPrimary,
    secondaryContainer = LlegoAccentSecondary,
    onSecondaryContainer = LlegoPrimary,
    error = LlegoError,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = LlegoError
)

/**
 * Esquema de colores oscuro para Llego (para futuras implementaciones)
 */
val LlegoDarkColorScheme = darkColorScheme(
    primary = LlegoAccentPrimary,
    onPrimary = LlegoPrimary,
    secondary = LlegoSecondary,
    onSecondary = LlegoPrimary,
    tertiary = LlegoTertiary,
    onTertiary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    primaryContainer = LlegoPrimary,
    onPrimaryContainer = LlegoAccentPrimary,
    secondaryContainer = LlegoTertiary,
    onSecondaryContainer = LlegoSecondary,
    error = Color(0xFFEF5350),
    onError = Color.White,
    errorContainer = Color(0xFF2D1617),
    onErrorContainer = Color(0xFFEF5350)
)

/**
 * Colores específicos para diferentes tipos de usuario
 */
object LlegoUserColors {
    // Colores específicos para la app de negocios
    val businessPrimary = LlegoPrimary
    val businessAccent = Color(0xFF4CAF50)              // Verde más corporativo
    val businessSecondary = LlegoSecondary

    // Colores específicos para la app de mensajeros/choferes
    val driverPrimary = LlegoPrimary
    val driverAccent = Color(0xFF2196F3)                // Azul para representar movimiento
    val driverSecondary = Color(255, 193, 7)            // Amarillo para alertas de entrega
}