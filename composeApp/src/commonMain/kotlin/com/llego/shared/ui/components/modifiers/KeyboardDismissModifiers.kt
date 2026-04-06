package com.llego.shared.ui.components.modifiers

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity

/**
 * Cierra el teclado cuando el usuario toca fuera de un campo de texto.
 */
fun Modifier.dismissKeyboardOnTapOutside(
    focusManager: FocusManager
): Modifier = composed {
    pointerInput(focusManager) {
        detectTapGestures {
            focusManager.clearFocus(force = true)
        }
    }
}

/**
 * Cierra el teclado cuando el usuario hace scroll manualmente.
 */
fun Modifier.dismissKeyboardOnScroll(
    focusManager: FocusManager
): Modifier = composed {
    val clearFocusOnScrollConnection = remember(focusManager) {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    focusManager.clearFocus(force = true)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (available.y != 0f) {
                    focusManager.clearFocus(force = true)
                }
                return Velocity.Zero
            }
        }
    }

    nestedScroll(clearFocusOnScrollConnection)
}
