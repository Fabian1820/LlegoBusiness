package com.llego.shared.data.auth

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task

private const val TAG = "GoogleSignInHelper"

/**
 * ImplementaciÃ³n Android de GoogleSignInHelper
 * Usa Google Play Services para obtener el idToken
 *
 * CONFIGURACIÃ“N REQUERIDA:
 * 1. Agregar dependencia en build.gradle.kts:
 *    implementation("com.google.android.gms:play-services-auth:20.7.0")
 *
 * 2. Configurar Web Client ID desde Google Cloud Console
 *    Reemplazar WEB_CLIENT_ID abajo con tu Client ID real
 *
 * 3. IMPORTANTE: Registrar el SHA-1 de tu app en Google Cloud Console
 *    - Ve a Google Cloud Console > APIs & Services > Credentials
 *    - Crea un OAuth 2.0 Client ID de tipo "Android"
 *    - Agrega el package name: com.llego.business
 *    - Agrega el SHA-1 fingerprint de tu keystore
 *    - Para debug: ./gradlew signingReport
 */
actual class GoogleSignInHelper(
    private val activity: ComponentActivity
) {
    // Web Client ID de Google Cloud Console (Android)
    // Este Client ID es usado para solicitar el idToken
    // IMPORTANTE: Debe ser el Client ID de tipo "Web application", NO el de tipo "Android"
    private val webClientId = "309268628843-no826lc6m63tn2b30q542q53ea1ffpbe.apps.googleusercontent.com"

    private var onSuccessCallback: ((String, String?) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    // Launcher serÃ¡ inyectado desde el composable
    var launcher: ActivityResultLauncher<Intent>? = null

    private val googleSignInClient: GoogleSignInClient

    init {
        
        // Configurar opciones de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun handleActivityResult(data: Intent?) {
        handleSignInResult(data)
    }

    actual fun signIn(
        onSuccess: (idToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        
        // Verificar que Web Client ID estÃ¡ configurado
        if (webClientId == "YOUR_WEB_CLIENT_ID_HERE") {
            val error = "Google Sign-In no configurado.\n\n" +
                "Pasos para configurar:\n" +
                "1. Ve a Google Cloud Console\n" +
                "2. Crea/selecciona proyecto\n" +
                "3. Habilita Google Sign-In API\n" +
                "4. Crea credencial OAuth 2.0 (Web application)\n" +
                "5. Copia el Client ID\n" +
                "6. Reemplaza 'YOUR_WEB_CLIENT_ID_HERE' en GoogleSignInHelper.android.kt"
            Log.e(TAG, error)
            onError(error)
            return
        }

        // Guardar callbacks
        onSuccessCallback = onSuccess
        onErrorCallback = onError
        

        // Cerrar sesiÃ³n anterior (para permitir seleccionar cuenta)
        googleSignInClient.signOut().addOnCompleteListener {
            
            // Iniciar flujo de Sign-In
            val signInIntent = googleSignInClient.signInIntent
            if (launcher != null) {
                launcher?.launch(signInIntent)
            } else {
                val error = "Error: ActivityResultLauncher no estÃ¡ disponible"
                Log.e(TAG, error)
                onError(error)
            }
        }
    }

    private fun handleSignInResult(data: Intent?) {
        
        if (data == null) {
            Log.e(TAG, "Intent data es null - posible cancelaciÃ³n o error")
            onErrorCallback?.invoke("No se recibiÃ³ respuesta de Google Sign-In")
            return
        }
        
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            
            val account = task.getResult(ApiException::class.java)

            val idToken = account?.idToken

            if (idToken != null) {
                // Sign-In exitoso
                onSuccessCallback?.invoke(idToken, null)
            } else {
                val error = "No se pudo obtener el idToken de Google. " +
                    "Verifica que el SHA-1 de tu app estÃ© registrado en Google Cloud Console."
                Log.e(TAG, error)
                onErrorCallback?.invoke(error)
            }
        } catch (e: ApiException) {
            // Error durante Sign-In
            Log.e(TAG, "ApiException: statusCode=${e.statusCode}, message=${e.message}", e)
            
            val errorMessage = when (e.statusCode) {
                CommonStatusCodes.CANCELED, 12501 -> "Login cancelado por el usuario"
                CommonStatusCodes.DEVELOPER_ERROR, 10 -> 
                    "Error de configuraciÃ³n (DEVELOPER_ERROR). " +
                    "Verifica:\n" +
                    "1. El SHA-1 de tu app estÃ¡ registrado en Google Cloud Console\n" +
                    "2. El package name coincide (com.llego.business)\n" +
                    "3. Tienes un OAuth Client ID de tipo 'Android' configurado\n" +
                    "Para obtener SHA-1: ./gradlew signingReport"
                12500 -> "Error de configuraciÃ³n. Verifica el Web Client ID y SHA-1"
                CommonStatusCodes.NETWORK_ERROR, 7 -> "Error de conexiÃ³n a internet"
                CommonStatusCodes.SIGN_IN_REQUIRED, 4 -> "Se requiere iniciar sesiÃ³n"
                else -> "Error en Google Sign-In: ${e.message} (Code: ${e.statusCode})"
            }
            onErrorCallback?.invoke(errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Exception inesperada: ${e.message}", e)
            onErrorCallback?.invoke("Error inesperado: ${e.message}")
        }
    }
}

@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
        ?: throw IllegalStateException("GoogleSignInHelper requiere ComponentActivity")


    // Crear el helper primero (sin launcher aÃºn)
    val helper = remember(activity) {
        GoogleSignInHelper(activity)
    }

    // Ahora crear el launcher que referencia al helper
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        helper.handleActivityResult(result.data)
    }

    // Inyectar el launcher en el helper
    DisposableEffect(launcher) {
        helper.launcher = launcher
        onDispose {
            helper.launcher = null
        }
    }

    return helper
}
