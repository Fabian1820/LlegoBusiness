package com.llego.shared.data.auth

import androidx.compose.runtime.Composable

/**
 * Helper multiplataforma para Google Sign-In
 * Obtiene el idToken de Google OAuth
 */
expect class GoogleSignInHelper {
    /**
     * Inicia el flujo de Sign-In con Google
     * @param onSuccess Callback con el idToken obtenido
     * @param onError Callback con mensaje de error
     */
    fun signIn(
        onSuccess: (idToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    )
}

/**
 * Composable para obtener una instancia del GoogleSignInHelper
 */
@Composable
expect fun rememberGoogleSignInHelper(): GoogleSignInHelper
