package com.llego.app

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val latestOnBack by rememberUpdatedState(onBack)

    SideEffect {
        IosNativeBackDispatcher.enabled = enabled
        IosNativeBackDispatcher.onBack = { latestOnBack() }
    }

    DisposableEffect(Unit) {
        onDispose {
            IosNativeBackDispatcher.enabled = false
            IosNativeBackDispatcher.onBack = null
        }
    }
}
