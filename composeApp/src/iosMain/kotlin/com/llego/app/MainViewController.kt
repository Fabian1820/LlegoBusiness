package com.llego.app

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.llego.business.chats.ui.viewmodel.ChatsViewModel
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.network.GraphQLClient
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.business.RegisterBusinessViewModel

fun MainViewController() = ComposeUIViewController {
    // Inicializar TokenManager y GraphQLClient
    val tokenManager = TokenManager()
    GraphQLClient.initialize(tokenManager)

    val viewModels = remember {
        val invitationRepository = com.llego.business.invitations.data.repository.InvitationRepository(GraphQLClient.apolloClient)
        
        AppViewModels(
            auth = AuthViewModel(),
            chats = ChatsViewModel(),
            orders = OrdersViewModel(tokenManager),
            products = ProductViewModel(tokenManager),
            settings = SettingsViewModel(tokenManager),
            registerBusiness = RegisterBusinessViewModel(tokenManager),
            invitations = InvitationViewModel(invitationRepository)
        )
    }
    App(viewModels)
}
