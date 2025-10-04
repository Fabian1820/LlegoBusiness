package com.llego.business

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llego.shared.data.model.BusinessType
import com.llego.shared.ui.auth.AuthViewModel
import com.llego.shared.ui.auth.LoginScreen
import com.llego.shared.ui.navigation.*
import com.llego.shared.ui.theme.LlegoBusinessTheme
import com.llego.nichos.restaurant.ui.screens.RestaurantProfileScreen
import com.llego.nichos.market.ui.screens.MarketProfileScreen

@Composable
fun App() {
    LlegoBusinessTheme {
        val authViewModel: AuthViewModel = viewModel()

        var isAuthenticated by remember { mutableStateOf(false) }
        var currentBusinessType by remember { mutableStateOf<BusinessType?>(null) }

        // Observar estado de autenticación
        LaunchedEffect(authViewModel) {
            authViewModel.uiState.collect { uiState ->
                isAuthenticated = uiState.isAuthenticated
                currentBusinessType = uiState.currentUser?.businessType
            }
        }

        if (isAuthenticated && currentBusinessType != null) {
            // Usuario autenticado - mostrar pantalla según tipo de negocio
            when (currentBusinessType) {
                BusinessType.RESTAURANT -> {
                    RestaurantProfileScreen(
                        authViewModel = authViewModel
                    )
                }
                BusinessType.GROCERY -> {
                    MarketProfileScreen(
                        authViewModel = authViewModel
                    )
                }
                BusinessType.PHARMACY -> {
                    // TODO: Implementar pantalla de pharmacy
                    RestaurantProfileScreen(
                        authViewModel = authViewModel
                    )
                }
                else -> {
                    LoginScreen(
                        onLoginSuccess = { businessType ->
                            isAuthenticated = true
                            currentBusinessType = businessType
                        }
                    )
                }
            }
        } else {
            // Usuario no autenticado - mostrar login
            LoginScreen(
                onLoginSuccess = { businessType ->
                    isAuthenticated = true
                    currentBusinessType = businessType
                }
            )
        }
    }
}