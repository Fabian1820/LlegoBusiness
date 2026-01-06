package com.llego.shared.data.auth

import androidx.compose.runtime.Composable

/**
 * Helper multiplataforma para Apple Sign-In
 * Obtiene el identityToken de Sign in with Apple
 */
expect class AppleSignInHelper {
    /**
     * Inicia el flujo de Sign-In con Apple
     * @param onSuccess Callback con el identityToken obtenido
     * @param onError Callback con mensaje de error
     */
    fun signIn(
        onSuccess: (identityToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    )
}

/**
 * Composable para obtener una instancia del AppleSignInHelper
 */
@Composable
expect fun rememberAppleSignInHelper(): AppleSignInHelper
