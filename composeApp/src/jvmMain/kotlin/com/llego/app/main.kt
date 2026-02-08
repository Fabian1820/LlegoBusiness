package com.llego.app

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "LlegoBusiness",
    ) {
        val appContainer = remember { AppContainer() }
        val viewModels = remember {
            appContainer.createAppViewModels()
        }
        App(viewModels)
    }
}

