package com.llego.app

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.shared.ui.auth.AuthViewModel

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "LlegoBusiness",
    ) {
        val viewModels = remember {
            AppViewModels(
                auth = AuthViewModel(),
                chats = ChatsViewModel(),
                orders = OrdersViewModel(),
                menu = MenuViewModel(),
                settings = SettingsViewModel()
            )
        }
        App(viewModels)
    }
}
