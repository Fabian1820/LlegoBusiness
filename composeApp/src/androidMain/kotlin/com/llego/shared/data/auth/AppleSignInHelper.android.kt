package com.llego.shared.data.auth

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Implementación Android de AppleSignInHelper
 *
 * NOTA: Apple Sign-In en Android usa OAuth Web flow
 * Es menos común que en iOS. Requiere configurar servicio web.
 */
actual class AppleSignInHelper(
    private val activity: ComponentActivity
) {
    actual fun signIn(
        onSuccess: (identityToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        // Apple Sign-In en Android requiere OAuth web flow
        // Por ahora retornar error
        onError("Apple Sign-In en Android requiere configuración web OAuth.\n" +
                "Es más común usar Sign in with Apple en iOS.\n" +
                "Para Android, considere solo Google Sign-In.")

        /* IMPLEMENTACIÓN FUTURA (Web OAuth):

        1. Configurar servicio web en Apple Developer
        2. Usar WebView para mostrar página de login de Apple
        3. Interceptar callback con authorization code
        4. Intercambiar code por identity token en backend

        Alternativa: Usar una librería de terceros como firebase-auth
        que maneja el flujo web de Apple automáticamente.

        */
    }
}

@Composable
actual fun rememberAppleSignInHelper(): AppleSignInHelper {
    val context = LocalContext.current
    return remember {
        AppleSignInHelper(context as ComponentActivity)
    }
}
