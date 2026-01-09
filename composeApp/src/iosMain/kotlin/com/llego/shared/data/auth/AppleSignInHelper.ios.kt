package com.llego.shared.data.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AuthenticationServices.*
import platform.Foundation.*
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import platform.CoreCrypto.*
import kotlinx.cinterop.*

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
    
    // Mantener referencias fuertes a los delegates para evitar que sean liberados prematuramente
    private var currentDelegate: AppleSignInDelegate? = null
    private var currentPresentationDelegate: AppleSignInPresentationContextProvider? = null

    actual fun signIn(
        onSuccess: (identityToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        println("[AppleSignIn] signIn() iniciado")

        // Guardar callbacks
        onSuccessCallback = onSuccess
        onErrorCallback = onError

        // Generar nonce para seguridad (prevenir replay attacks)
        // NOTA IMPORTANTE: El backend actual acepta nonce=nil para Apple Sign-In
        // Si se habilita el nonce, descomentar las siguientes líneas:
        // currentNonce = generateNonce()
        // println("[AppleSignIn] Nonce generado: ${currentNonce?.take(10)}...")

        // Por ahora usamos nonce=nil (compatible con LlegoiOS)
        currentNonce = null
        println("[AppleSignIn] Nonce deshabilitado (nil)")

        // Crear request de Apple Sign-In
        val appleIDProvider = ASAuthorizationAppleIDProvider()
        val request = appleIDProvider.createRequest()

        // Solicitar nombre y email del usuario
        request.requestedScopes = listOf(
            ASAuthorizationScopeFullName,
            ASAuthorizationScopeEmail
        )

        // Nonce hasheado omitido (nonce=nil)
        // Si se habilita el nonce, descomentar:
        // currentNonce?.let { originalNonce ->
        //     val hashedNonce = sha256(originalNonce)
        //     println("[AppleSignIn] Nonce hasheado (base64): ${hashedNonce.take(20)}...")
        //     request.nonce = hashedNonce
        // }

        // Crear y ejecutar controller de autorización
        val authorizationController = ASAuthorizationController(listOf(request))

        // Configurar delegate (maneja respuestas)
        currentDelegate = AppleSignInDelegate(
            onSuccess = { identityToken ->
                onSuccessCallback?.invoke(identityToken, currentNonce)
                // Limpiar referencias después del éxito
                currentDelegate = null
                currentPresentationDelegate = null
            },
            onError = { errorMessage ->
                onErrorCallback?.invoke(errorMessage)
                // Limpiar referencias después del error
                currentDelegate = null
                currentPresentationDelegate = null
            }
        )

        authorizationController.delegate = currentDelegate

        // Configurar presentation context provider
        currentPresentationDelegate = AppleSignInPresentationContextProvider()
        authorizationController.presentationContextProvider = currentPresentationDelegate

        // Iniciar flujo de autorización
        println("[AppleSignIn] Iniciando performRequests()")
        authorizationController.performRequests()
        println("[AppleSignIn] performRequests() llamado")
    }


    private fun generateNonce(): String {
        // Generar nonce aleatorio usando UUID
        return NSUUID().UUIDString()
    }

    /**
     * Hashea el input con SHA256 y retorna en formato base64
     * Apple requiere que el nonce sea un hash SHA256 codificado en base64
     */
    private fun sha256(input: String): String {
        val data = (input as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return ""
        val digest = NSMutableData.dataWithLength(CC_SHA256_DIGEST_LENGTH.toULong())
            ?: return ""

        data.bytes?.let { dataBytes ->
            CC_SHA256(
                dataBytes.reinterpret<UByteVar>(),
                data.length.toUInt(),
                digest.mutableBytes?.reinterpret()
            )
        }

        // Convertir a base64 (formato requerido por Apple)
        return digest.base64EncodedStringWithOptions(0UL)
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
            println("[AppleSignIn] authorizationController:didCompleteWithAuthorization llamado")
            val credential = didCompleteWithAuthorization.credential

            if (credential is ASAuthorizationAppleIDCredential) {
                println("[AppleSignIn] Credencial es ASAuthorizationAppleIDCredential")
                // Extraer identity token
                val identityTokenData = credential.identityToken
                println("[AppleSignIn] identityTokenData = $identityTokenData")

                if (identityTokenData != null) {
                    // Convertir NSData a String
                    val identityToken = identityTokenData.toKString()
                    println("[AppleSignIn] identityToken length = ${identityToken.length}")

                    if (identityToken.isNotEmpty()) {
                        println("[AppleSignIn] Llamando onSuccess con token")
                        onSuccess(identityToken)
                    } else {
                        println("[AppleSignIn] Token vacío")
                        onError("Identity token está vacío")
                    }
                } else {
                    println("[AppleSignIn] identityTokenData es null")
                    onError("No se pudo obtener identity token de Apple")
                }
            } else {
                println("[AppleSignIn] Credencial no es ASAuthorizationAppleIDCredential")
                onError("Credencial inválida recibida de Apple")
            }
        }

        override fun authorizationController(
            controller: ASAuthorizationController,
            didCompleteWithError: NSError
        ) {
            println("[AppleSignIn] authorizationController:didCompleteWithError llamado - code: ${didCompleteWithError.code}")
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
    return NSString.create(this, NSUTF8StringEncoding)?.toString() ?: ""
}
