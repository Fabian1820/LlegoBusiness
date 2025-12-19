package com.llego.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.llego.shared.data.model.UserType

/**
 * Tema principal del sistema de diseño Llego
 * Unifica colores, tipografía, formas y elevaciones
 */

/**
 * CompositionLocal para acceder a configuraciones específicas de Llego
 */
data class LlegoThemeConfig(
    val userType: UserType = UserType.BUSINESS,
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
            userType = UserType.BUSINESS,
            enableAnimations = true,
            isDarkMode = false
        ),
        darkTheme = false,
        content = content
    )
}

/**
 * Tema específico para la app de mensajeros/choferes
 * Siempre en modo claro para consistencia de marca
 */
@Composable
fun LlegoDriverTheme(
    darkTheme: Boolean = false, // Siempre modo claro
    content: @Composable () -> Unit
) {
    LlegoTheme(
        config = LlegoThemeConfig(
            userType = UserType.DRIVER,
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
    fun isBusinessApp(): Boolean {
        return LocalLlegoTheme.current.userType == UserType.BUSINESS
    }

    @Composable
    fun isDriverApp(): Boolean {
        return LocalLlegoTheme.current.userType == UserType.DRIVER
    }

    @Composable
    fun areAnimationsEnabled(): Boolean {
        return LocalLlegoTheme.current.enableAnimations
    }
}