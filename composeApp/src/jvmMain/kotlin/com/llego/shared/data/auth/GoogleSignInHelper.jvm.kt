package com.llego.shared.data.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Implementación Desktop (JVM) de GoogleSignInHelper
 *
 * Usa OAuth Web flow en el navegador del sistema
 */
actual class GoogleSignInHelper {
    actual fun signIn(
        onSuccess: (idToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        // TODO: Implementar OAuth web flow para desktop

        onError("Google Sign-In Desktop no implementado. Requiere:\n" +
                "1. Abrir navegador con URL de OAuth\n" +
                "2. Escuchar callback en localhost\n" +
                "3. Intercambiar code por tokens")

        /* IMPLEMENTACIÓN FUTURA:

        1. Abrir navegador del sistema con URL:
           https://accounts.google.com/o/oauth2/v2/auth
           ?client_id=YOUR_CLIENT_ID
           &redirect_uri=http://localhost:PORT/callback
           &response_type=code
           &scope=openid%20email%20profile

        2. Iniciar servidor local HTTP en puerto libre
           para escuchar el callback

        3. Cuando llegue el callback con 'code', intercambiarlo por tokens:
           POST https://oauth2.googleapis.com/token
           Con client_id, client_secret, code, redirect_uri

        4. Extraer id_token de la respuesta y llamar onSuccess

        Alternativa: Usar librería como google-auth-library-java

        */
    }
}

@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper {
    return remember {
        GoogleSignInHelper()
    }
}
