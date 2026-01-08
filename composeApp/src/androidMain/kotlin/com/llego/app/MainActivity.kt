package com.llego.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.llego.app.AppViewModels
import com.llego.nichos.restaurant.ui.viewmodel.ChatsViewModel
import com.llego.nichos.restaurant.ui.viewmodel.MenuViewModel
import com.llego.nichos.restaurant.ui.viewmodel.OrdersViewModel
import com.llego.nichos.restaurant.ui.viewmodel.SettingsViewModel
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.network.GraphQLClient
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.business.RegisterBusinessViewModel

class MainActivity : ComponentActivity() {
    private val tokenManager by lazy { TokenManager() }

    private val authViewModel: AuthViewModel by viewModels()
    private val chatsViewModel: ChatsViewModel by viewModels()
    private val ordersViewModel: OrdersViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return OrdersViewModel(tokenManager) as T
            }
        }
    }
    private val menuViewModel: MenuViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MenuViewModel(tokenManager) as T
            }
        }
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(tokenManager) as T
            }
        }
    }
    private val registerBusinessViewModel: RegisterBusinessViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RegisterBusinessViewModel(tokenManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inicializar TokenManager con contexto para persistencia
        TokenManager.initialize(applicationContext)
        
        // Inicializar GraphQLClient
        GraphQLClient.initialize(tokenManager)

        setContent {
            App(
                AppViewModels(
                    auth = authViewModel,
                    chats = chatsViewModel,
                    orders = ordersViewModel,
                    menu = menuViewModel,
                    settings = settingsViewModel,
                    registerBusiness = registerBusinessViewModel
                )
            )
        }
    }
}
