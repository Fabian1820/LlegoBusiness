package com.llego.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.llego.app.AppViewModels
import com.llego.business.invitations.ui.viewmodel.InvitationViewModel
import com.llego.business.branches.ui.viewmodel.BranchesManagementViewModel
import com.llego.business.orders.ui.viewmodel.OrdersViewModel
import com.llego.business.products.ui.viewmodel.ProductViewModel
import com.llego.business.settings.ui.viewmodel.SettingsViewModel
import com.llego.shared.data.auth.AppleSignInHelper
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.upload.ImageUploadServiceFactory
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.branch.BranchSelectorViewModel
import com.llego.shared.ui.business.RegisterBusinessViewModel

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer

    private fun <VM : ViewModel> appViewModelFactory(create: () -> VM): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return create() as T
            }
        }
    }

    private val authViewModel: AuthViewModel by viewModels {
        appViewModelFactory { appContainer.createAuthViewModel() }
    }
    private val ordersViewModel: OrdersViewModel by viewModels {
        appViewModelFactory { appContainer.createOrdersViewModel() }
    }
    private val productViewModel: ProductViewModel by viewModels {
        appViewModelFactory { appContainer.createProductViewModel() }
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        appViewModelFactory { appContainer.createSettingsViewModel() }
    }
    private val registerBusinessViewModel: RegisterBusinessViewModel by viewModels {
        appViewModelFactory { appContainer.createRegisterBusinessViewModel() }
    }
    private val invitationViewModel: InvitationViewModel by viewModels {
        appViewModelFactory { appContainer.createInvitationViewModel() }
    }
    private val branchSelectorViewModel: BranchSelectorViewModel by viewModels {
        appViewModelFactory { appContainer.createBranchSelectorViewModel() }
    }
    private val branchesManagementViewModel: BranchesManagementViewModel by viewModels {
        appViewModelFactory { appContainer.createBranchesManagementViewModel() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inicializar TokenManager con contexto para persistencia
        TokenManager.initialize(applicationContext)
        
        // Inicializar ImageUploadServiceFactory con contexto para manejar content:// URIs
        ImageUploadServiceFactory.initialize(applicationContext)

        // Inicializar composition root
        appContainer = AppContainer()
        
        // Manejar deep link inicial (si la app fue abierta desde un deep link)
        handleAppleAuthDeepLink(intent)

        setContent {
            App(
                AppViewModels(
                    auth = authViewModel,
                    orders = ordersViewModel,
                    products = productViewModel,
                    settings = settingsViewModel,
                    registerBusiness = registerBusinessViewModel,
                    invitations = invitationViewModel,
                    branchSelector = branchSelectorViewModel,
                    branchesManagement = branchesManagementViewModel
                )
            )
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Manejar deep link cuando la app ya estÃ¡ abierta
        handleAppleAuthDeepLink(intent)
    }
    
    /**
     * Procesa el deep link de Apple Auth callback
     * llegobusiness://auth/callback?token=xxx o llegobusiness://auth/callback?error=xxx
     * 
     * El token que llega es el JWT final del backend, no un identity token de Apple.
     * Por eso usamos authenticateWithToken() en lugar de loginWithApple()
     */
    private fun handleAppleAuthDeepLink(intent: Intent?) {
        val uri = intent?.data
        
        if (uri == null) return
        
        // Verificar que es nuestro deep link
        if (uri.scheme != "llegobusiness" || uri.host != "auth") {
            return
        }
        
        val token = uri.getQueryParameter("token")
        val error = uri.getQueryParameter("error")
        val message = uri.getQueryParameter("message")
        
        
        when {
            !token.isNullOrEmpty() -> {
                // El token es el JWT del backend, usamos authenticateWithToken
                authViewModel.authenticateWithToken(token)
                // Notificar al AppleSignInHelper que el flujo terminÃ³ exitosamente
                AppleSignInHelper.notifySuccess()
            }
            !error.isNullOrEmpty() -> {
                val errorMessage = message ?: error
                Log.e(TAG, "handleAppleAuthDeepLink: error recibido: $errorMessage")
                // Notificar al AppleSignInHelper del error
                AppleSignInHelper.notifyError(errorMessage)
            }
            else -> {
                Log.e(TAG, "handleAppleAuthDeepLink: deep link sin token ni error")
                AppleSignInHelper.notifyError("Respuesta invÃ¡lida de Apple Sign-In")
            }
        }
    }
}


