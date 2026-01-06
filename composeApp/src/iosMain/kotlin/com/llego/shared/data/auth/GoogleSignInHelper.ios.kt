package com.llego.shared.data.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cocoapods.GoogleSignIn.*
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIApplication

/**
 * Implementación iOS de GoogleSignInHelper
 * Usa Google Sign-In SDK para iOS (via CocoaPods)
 *
 * CONFIGURACIÓN REQUERIDA:
 * 1. Agregar en iosApp/Podfile:
 *    pod 'GoogleSignIn'
 *
 * 2. Configurar Client ID en iosApp/iosApp/Info.plist:
 *    <key>GIDClientID</key>
 *    <string>YOUR_CLIENT_ID.apps.googleusercontent.com</string>
 *
 * 3. Agregar URL Scheme en Info.plist:
 *    <key>CFBundleURLTypes</key>
 *    <array>
 *        <dict>
 *            <key>CFBundleURLSchemes</key>
 *            <array>
 *                <string>com.googleusercontent.apps.YOUR_CLIENT_ID_REVERSED</string>
 *            </array>
 *        </dict>
 *    </array>
 */
@OptIn(ExperimentalForeignApi::class)
actual class GoogleSignInHelper {

    // Client ID de Google Cloud Console (iOS)
    // Este Client ID es usado para iOS
    private val clientID = "309268628843-vafbp3o66ul2ea1g2bo6h9bpraqk5sj0.apps.googleusercontent.com"

    private var onSuccessCallback: ((String, String?) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    actual fun signIn(
        onSuccess: (idToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        // Verificar que Client ID está configurado
        if (clientID == "YOUR_IOS_CLIENT_ID_HERE") {
            onError(
                "Google Sign-In iOS no configurado.\n\n" +
                "Pasos para configurar:\n" +
                "1. Agrega 'pod GoogleSignIn' en Podfile\n" +
                "2. Ejecuta 'pod install'\n" +
                "3. Configura Client ID en Info.plist\n" +
                "4. Reemplaza 'YOUR_IOS_CLIENT_ID_HERE' en GoogleSignInHelper.ios.kt"
            )
            return
        }

        // Guardar callbacks
        onSuccessCallback = onSuccess
        onErrorCallback = onError

        // Configurar GIDSignIn con Client ID
        val configuration = GIDConfiguration(clientID = clientID)
        GIDSignIn.sharedInstance.configuration = configuration

        // Obtener UIViewController actual
        val presentingViewController = UIApplication.sharedApplication.keyWindow?.rootViewController

        if (presentingViewController == null) {
            onError("No se pudo obtener el view controller")
            return
        }

        // Iniciar flujo de Sign-In
        GIDSignIn.sharedInstance.signInWithPresentingViewController(
            presentingViewController = presentingViewController
        ) { result, error ->
            handleSignInResult(result, error)
        }
    }

    private fun handleSignInResult(result: GIDSignInResult?, error: NSError?) {
        if (error != null) {
            // Error durante Sign-In
            val errorMessage = when (error.code) {
                GIDSignInErrorCodeCanceled -> "Login cancelado por el usuario"
                GIDSignInErrorCodeEMM -> "Error de Enterprise Mobility Management"
                GIDSignInErrorCodeUnknown -> "Error desconocido de Google Sign-In"
                else -> "Error: ${error.localizedDescription}"
            }
            onErrorCallback?.invoke(errorMessage)
            return
        }

        if (result == null) {
            onErrorCallback?.invoke("No se recibió resultado de Google")
            return
        }

        // Extraer user y authentication
        val user = result.user
        val authentication = user?.idToken

        val idToken = authentication?.tokenString

        if (idToken != null) {
            // Sign-In exitoso
            onSuccessCallback?.invoke(idToken, null)
        } else {
            onErrorCallback?.invoke("No se pudo obtener el idToken de Google")
        }
    }
}

@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper {
    return remember {
        GoogleSignInHelper()
    }
}
