package com.llego.app

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.network.GraphQLClient
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.business.RegisterBusinessViewModel

fun MainViewController() = ComposeUIViewController {
    // Inicializar TokenManager y GraphQLClient
    val tokenManager = TokenManager()
    GraphQLClient.initialize(tokenManager)

    val viewModels = remember {
        AppViewModels(
            auth = AuthViewModel(),
            chats = ChatsViewModel(),
            orders = OrdersViewModel(tokenManager),
            menu = MenuViewModel(tokenManager),
            settings = SettingsViewModel(tokenManager),
            registerBusiness = RegisterBusinessViewModel(tokenManager)
        )
    }
    App(viewModels)
}
