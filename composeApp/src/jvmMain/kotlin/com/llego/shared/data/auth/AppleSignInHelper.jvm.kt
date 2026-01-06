package com.llego.shared.data.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Implementación Desktop (JVM) de AppleSignInHelper
 *
 * Usa OAuth Web flow en el navegador del sistema
 * Similar a la implementación de Android
 */
actual class AppleSignInHelper {
    actual fun signIn(
        onSuccess: (identityToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        // TODO: Implementar OAuth web flow para desktop

        onError("Apple Sign-In Desktop no implementado. Requiere:\n" +
                "1. Configurar servicio web en Apple Developer\n" +
                "2. Abrir navegador con URL de OAuth\n" +
                "3. Intercambiar code por tokens")

        /* IMPLEMENTACIÓN FUTURA:

        1. Abrir navegador del sistema con URL:
           https://appleid.apple.com/auth/authorize
           ?client_id=YOUR_SERVICE_ID
           &redirect_uri=http://localhost:PORT/callback
           &response_type=code id_token
           &scope=name email
           &response_mode=form_post

        2. Iniciar servidor local HTTP para recibir el callback (form POST)

        3. Extraer identity_token de la respuesta y llamar onSuccess

        Nota: Apple Sign-In en plataformas no-Apple es más complejo
        y requiere configuración de servicios web. Considere solo
        Google Sign-In para Desktop si es suficiente.

        */
    }
}

@Composable
actual fun rememberAppleSignInHelper(): AppleSignInHelper {
    return remember {
        AppleSignInHelper()
    }
}
