package com.llego.app

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    val appContainer = remember { AppContainer() }

    val viewModels = remember {
        appContainer.createAppViewModels()
    }
    App(viewModels)
}

