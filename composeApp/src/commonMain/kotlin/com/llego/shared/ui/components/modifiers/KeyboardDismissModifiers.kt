package com.llego.shared.ui.components.modifiers

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
 *
 * ADVERTENCIA — NO usar en iOS. La deteccion de "tap fuera" depende de que el hijo
 * (TextField) consuma el evento de puntero. En Compose iOS el TextField NO marca el
 * `up` como consumido, asi que este modifier limpia el foco al tocar el propio campo
 * (el teclado se abre y se cierra al instante). Ademas, su `awaitEachGesture` aplicado
 * en la raiz se queda con el primer gesto e impide que el `verticalScroll` hijo scrollee
 * hasta una recomposicion. En Android funciona porque alli el TextField si consume el up.
 * Para cerrar el teclado en iOS usa [dismissKeyboardOnScroll] (via nestedScroll, inocuo).
 */
fun Modifier.dismissKeyboardOnTapOutside(
    focusManager: FocusManager
): Modifier = composed {
    pointerInput(focusManager) {
        awaitEachGesture {
            // Only react to taps that are not already consumed by child components
            // (e.g. TextField). This prevents clearing focus when tapping inside inputs.
            awaitFirstDown(requireUnconsumed = true)
            val up = waitForUpOrCancellation()
            if (up != null) {
                focusManager.clearFocus(force = true)
            }
        }
    }
}

/**
 * Cierra el teclado cuando el usuario hace scroll manualmente.
 *
 * ADVERTENCIA — NO usar en iOS. `onPreScroll` llama `clearFocus(force=true)` ante
 * cualquier delta de scroll de usuario. En iOS, al tocar un TextField dentro de un
 * `verticalScroll`, el contacto del dedo (sub-slop) y el bring-into-view del campo
 * generan micro-deltas que se clasifican como UserInput, asi que limpiaba el foco al
 * instante y el teclado se abria y se cerraba en TODOS los inputs dentro de scrolls.
 * Ademas, su nestedScroll en la raiz interfiere con el primer gesto de scroll. En iOS
 * preferimos no interceptar el scroll; el teclado se cierra con su tecla de retorno.
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
