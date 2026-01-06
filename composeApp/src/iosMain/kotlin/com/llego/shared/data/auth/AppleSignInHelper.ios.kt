package com.llego.shared.data.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AuthenticationServices.*
import platform.Foundation.NSError
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.darwin.NSObject

/**
 * Implementación iOS de AppleSignInHelper
 * Usa AuthenticationServices framework (iOS 13+)
 *
 * CONFIGURACIÓN REQUERIDA:
 * 1. En Xcode: Agregar capability "Sign in with Apple"
 * 2. En Apple Developer: Configurar App ID con Sign in with Apple
 * 3. No requiere código adicional (usa frameworks nativos de iOS)
 */
@OptIn(ExperimentalForeignApi::class)
actual class AppleSignInHelper {

    private var onSuccessCallback: ((String, String?) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private var currentNonce: String? = null

    actual fun signIn(
        onSuccess: (identityToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        // Guardar callbacks
        onSuccessCallback = onSuccess
        onErrorCallback = onError

        // Generar nonce para seguridad (prevenir replay attacks)
        currentNonce = generateNonce()

        // Crear request de Apple Sign-In
        val appleIDProvider = ASAuthorizationAppleIDProvider()
        val request = appleIDProvider.createRequest()

        // Solicitar nombre y email del usuario
        request.requestedScopes = listOf(
            ASAuthorizationScopeFullName,
            ASAuthorizationScopeEmail
        )

        // Opcional: Agregar nonce hasheado para mayor seguridad
        // request.nonce = sha256(currentNonce)

        // Crear y ejecutar controller de autorización
        val authorizationController = ASAuthorizationController(listOf(request))

        // Configurar delegate (maneja respuestas)
        val delegate = AppleSignInDelegate(
            onSuccess = { identityToken ->
                onSuccessCallback?.invoke(identityToken, currentNonce)
            },
            onError = { errorMessage ->
                onErrorCallback?.invoke(errorMessage)
            }
        )

        authorizationController.delegate = delegate

        // Configurar presentation context provider
        val presentationDelegate = AppleSignInPresentationContextProvider()
        authorizationController.presentationContextProvider = presentationDelegate

        // Iniciar flujo de autorización
        authorizationController.performRequests()
    }

    private fun generateNonce(): String {
        // Generar nonce aleatorio usando UUID
        return NSUUID().UUIDString()
    }

    // Delegate que maneja las respuestas de Apple Sign-In
    private class AppleSignInDelegate(
        private val onSuccess: (String) -> Unit,
        private val onError: (String) -> Unit
    ) : NSObject(), ASAuthorizationControllerDelegateProtocol {

        override fun authorizationController(
            controller: ASAuthorizationController,
            didCompleteWithAuthorization: ASAuthorization
        ) {
            val credential = didCompleteWithAuthorization.credential

            if (credential is ASAuthorizationAppleIDCredential) {
                // Extraer identity token
                val identityTokenData = credential.identityToken

                if (identityTokenData != null) {
                    // Convertir NSData a String
                    val identityToken = identityTokenData.toKString()

                    if (identityToken.isNotEmpty()) {
                        onSuccess(identityToken)
                    } else {
                        onError("Identity token está vacío")
                    }
                } else {
                    onError("No se pudo obtener identity token de Apple")
                }
            } else {
                onError("Credencial inválida recibida de Apple")
            }
        }

        override fun authorizationController(
            controller: ASAuthorizationController,
            didCompleteWithError: NSError
        ) {
            val errorMessage = when (didCompleteWithError.code) {
                ASAuthorizationErrorCanceled -> "Login cancelado por el usuario"
                ASAuthorizationErrorFailed -> "Autenticación falló. Intenta de nuevo"
                ASAuthorizationErrorInvalidResponse -> "Respuesta inválida de Apple"
                ASAuthorizationErrorNotHandled -> "Solicitud no manejada"
                ASAuthorizationErrorUnknown -> "Error desconocido de Apple Sign-In"
                else -> "Error: ${didCompleteWithError.localizedDescription}"
            }

            onError(errorMessage)
        }
    }

    // Presentation context provider (requerido por iOS)
    private class AppleSignInPresentationContextProvider :
        NSObject(),
        ASAuthorizationControllerPresentationContextProvidingProtocol {

        override fun presentationAnchorForAuthorizationController(
            controller: ASAuthorizationController
        ): ASPresentationAnchor {
            // Retornar la ventana principal de la app
            return UIApplication.sharedApplication.windows.firstOrNull() as ASPresentationAnchor
        }
    }
}

@Composable
actual fun rememberAppleSignInHelper(): AppleSignInHelper {
    return remember {
        AppleSignInHelper()
    }
}

// Extension function para convertir NSData a String
@OptIn(ExperimentalForeignApi::class)
private fun platform.Foundation.NSData.toKString(): String {
    return platform.Foundation.NSString.create(
        data = this,
        encoding = platform.Foundation.NSUTF8StringEncoding
    )?.toString() ?: ""
}
