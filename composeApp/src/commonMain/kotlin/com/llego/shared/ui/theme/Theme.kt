package com.llego.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Tema principal del sistema de diseño Llego
 * Unifica colores, tipografía, formas y elevaciones
 */

/**
 * CompositionLocal para acceder a configuraciones específicas de Llego
 */
data class LlegoThemeConfig(
    val enableAnimations: Boolean = true,
    val isDarkMode: Boolean = false
)

val LocalLlegoTheme = staticCompositionLocalOf { LlegoThemeConfig() }

/**
 * Tema principal de Llego que envuelve todas las configuraciones
 * Forzado a modo claro para mantener consistencia de marca
 */
@Composable
fun LlegoTheme(
    config: LlegoThemeConfig = LlegoThemeConfig(),
    darkTheme: Boolean = false, // Siempre modo claro
    content: @Composable () -> Unit
) {
    // Siempre usar el esquema de colores claro
    val colorScheme = LlegoLightColorScheme

    val updatedConfig = config.copy(isDarkMode = false)

    CompositionLocalProvider(LocalLlegoTheme provides updatedConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LlegoTypography,
            shapes = LlegoShapes,
            content = content
        )
    }
}

/**
 * Tema específico para la app de negocios
 * Siempre en modo claro para consistencia de marca
 */
@Composable
fun LlegoBusinessTheme(
    darkTheme: Boolean = false, // Siempre modo claro
    content: @Composable () -> Unit
) {
    LlegoTheme(
        config = LlegoThemeConfig(
            enableAnimations = true,
            isDarkMode = false
        ),
        darkTheme = false,
        content = content
    )
}

/**
 * Extensiones para acceder fácilmente a la configuración del tema
 */
object LlegoThemeExtensions {

    @Composable
    fun getCurrentConfig(): LlegoThemeConfig {
        return LocalLlegoTheme.current
    }

    @Composable
    fun areAnimationsEnabled(): Boolean {
        return LocalLlegoTheme.current.enableAnimations
    }
}
