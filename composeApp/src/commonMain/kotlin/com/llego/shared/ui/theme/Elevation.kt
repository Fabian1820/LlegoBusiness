package com.llego.shared.ui.theme

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Sistema de elevaciones del diseño Llego
 * Basado en las especificaciones del documento LLEGO_DESIGN_SYSTEM.md
 */

object LlegoElevation {

    // Elevaciones estándar de Material 3
    val level0 = 0.dp      // Sin sombra
    val level1 = 1.dp      // Sombra muy sutil
    val level2 = 3.dp      // Sombra ligera
    val level3 = 6.dp      // Sombra media
    val level4 = 8.dp      // Sombra pronunciada
    val level5 = 12.dp     // Sombra fuerte

    // Elevaciones específicas para componentes Llego
    object Component {
        val button = level1
        val card = level2
        val navigation = level3
        val modal = level4
        val dropdown = level5
    }

    // CardElevations para diferentes tipos de cards
    object Cards {
        @Composable
        fun productCard(): CardElevation = CardDefaults.cardElevation(
            defaultElevation = level2,
            pressedElevation = level3,
            focusedElevation = level3,
            hoveredElevation = level3,
            draggedElevation = level4,
            disabledElevation = level0
        )

        @Composable
        fun infoCard(): CardElevation = CardDefaults.cardElevation(
            defaultElevation = level1,
            pressedElevation = level2,
            focusedElevation = level2,
            hoveredElevation = level2,
            draggedElevation = level3,
            disabledElevation = level0
        )

        @Composable
        fun prominentCard(): CardElevation = CardDefaults.cardElevation(
            defaultElevation = level3,
            pressedElevation = level4,
            focusedElevation = level4,
            hoveredElevation = level4,
            draggedElevation = level5,
            disabledElevation = level0
        )

        @Composable
        fun flatCard(): CardElevation = CardDefaults.cardElevation(
            defaultElevation = level0,
            pressedElevation = level1,
            focusedElevation = level1,
            hoveredElevation = level1,
            draggedElevation = level2,
            disabledElevation = level0
        )
    }
}

/**
 * Definiciones de sombras personalizadas para efectos especiales
 * Equivalentes a las especificaciones CSS mencionadas en la documentación
 */
object LlegoShadows {

    // Sombra sutil para cards principales
    // Equivale a: shadow(color: Color.black.opacity(0.08), radius: 15, x: 0, y: 5)
    val cardShadow = LlegoElevation.level2

    // Sombra para elementos flotantes
    // Equivale a: shadow(color: Color.black.opacity(0.06), radius: 12, x: 0, y: 4)
    val floatingShadow = LlegoElevation.level3

    // Sombra para modals y overlays
    val modalShadow = LlegoElevation.level4

    // Sombra para navigation bar
    val navigationShadow = LlegoElevation.level2
}
