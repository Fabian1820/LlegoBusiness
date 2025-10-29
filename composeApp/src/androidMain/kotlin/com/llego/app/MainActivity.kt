package com.llego.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.llego.app.AppViewModels
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.shared.ui.auth.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val chatsViewModel: ChatsViewModel by viewModels()
    private val ordersViewModel: OrdersViewModel by viewModels()
    private val menuViewModel: MenuViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                AppViewModels(
                    auth = authViewModel,
                    chats = chatsViewModel,
                    orders = ordersViewModel,
                    menu = menuViewModel,
                    settings = settingsViewModel
                )
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(
        AppViewModels(
            auth = AuthViewModel(),
            chats = ChatsViewModel(),
            orders = OrdersViewModel(),
            menu = MenuViewModel(),
            settings = SettingsViewModel()
        )
    )
}
