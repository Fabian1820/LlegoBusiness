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
 * Usa modo claro/oscuro según preferencia del sistema (o override explícito).
 */
@Composable
fun LlegoTheme(
    config: LlegoThemeConfig = LlegoThemeConfig(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LlegoDarkColorScheme else LlegoLightColorScheme

    val updatedConfig = config.copy(isDarkMode = darkTheme)

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
 * Hereda modo claro/oscuro desde el sistema por defecto.
 */
@Composable
fun LlegoBusinessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    LlegoTheme(
        config = LlegoThemeConfig(
            enableAnimations = true,
            isDarkMode = darkTheme
        ),
        darkTheme = darkTheme,
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
