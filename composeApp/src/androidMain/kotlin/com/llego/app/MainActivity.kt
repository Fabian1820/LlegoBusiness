package com.llego.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.llego.shared.data.auth.AppleSignInHelper
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.network.GraphQLClient
import com.llego.shared.data.upload.ImageUploadServiceFactory
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.business.RegisterBusinessViewModel

private const val TAG = "MainActivity"

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
        
        // Inicializar ImageUploadServiceFactory con contexto para manejar content:// URIs
        ImageUploadServiceFactory.initialize(applicationContext)
        
        // Inicializar GraphQLClient
        GraphQLClient.initialize(tokenManager)
        
        // Manejar deep link inicial (si la app fue abierta desde un deep link)
        handleAppleAuthDeepLink(intent)

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
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: recibido nuevo intent")
        // Manejar deep link cuando la app ya está abierta
        handleAppleAuthDeepLink(intent)
    }
    
    /**
     * Procesa el deep link de Apple Auth callback
     * llegobusiness://auth/callback?token=xxx o llegobusiness://auth/callback?error=xxx
     */
    private fun handleAppleAuthDeepLink(intent: Intent?) {
        val uri = intent?.data
        Log.d(TAG, "handleAppleAuthDeepLink: uri = $uri")
        
        if (uri != null) {
            val handled = AppleSignInHelper.handleDeepLink(uri)
            Log.d(TAG, "handleAppleAuthDeepLink: handled = $handled")
        }
    }
}

