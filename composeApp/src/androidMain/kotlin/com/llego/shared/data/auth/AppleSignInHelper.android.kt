package com.llego.shared.data.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.llego.shared.data.network.BackendConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "AppleSignInHelper"

/**
 * Response del endpoint /apple/start
 */
@Serializable
data class AppleStartResponse(
    val auth_url: String,
    val state: String
)

/**
 * Implementación Android de AppleSignInHelper
 * Usa OAuth Web flow con Custom Tabs para Apple Sign-In
 * 
 * Flujo:
 * 1. Llama GET /apple/start → recibe { auth_url, state }
 * 2. Abre auth_url en Custom Tab (navegador in-app)
 * 3. Usuario se autentica en Apple
 * 4. Apple redirige al backend (POST /apple/callback)
 * 5. Backend valida y redirige a: llegobusiness://auth/callback?token=JWT
 * 6. MainActivity captura el deep link y llama a AuthViewModel.authenticateWithToken()
 * 
 * NOTA: El token que llega en el deep link es el JWT del backend, NO un identity token de Apple.
 * Por eso MainActivity usa authenticateWithToken() en lugar de loginWithApple().
 */
actual class AppleSignInHelper(
    private val activity: ComponentActivity
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    actual fun signIn(
        onSuccess: (identityToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        Log.d(TAG, "signIn: iniciando flujo Apple Sign-In para Android")
        
        // Guardar callbacks en el companion object para notificaciones
        // NOTA: onSuccess no se usa directamente porque MainActivity maneja el token
        // pero lo guardamos para mantener compatibilidad con la interfaz
        pendingOnSuccess = onSuccess
        pendingOnError = onError
        
        // Iniciar el flujo OAuth en una coroutine
        CoroutineScope(Dispatchers.Main).launch {
            try {
                startAppleSignIn(activity)
            } catch (e: Exception) {
                Log.e(TAG, "signIn: error iniciando flujo", e)
                onError(e.message ?: "Error desconocido al iniciar Apple Sign-In")
                clearCallbacks()
            }
        }
    }
    
    private suspend fun startAppleSignIn(context: Context) {
        Log.d(TAG, "startAppleSignIn: llamando a /apple/start")
        
        val response = withContext(Dispatchers.IO) {
            val url = URL("${BackendConfig.BASE_URL}/apple/start")
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                val responseCode = connection.responseCode
                Log.d(TAG, "startAppleSignIn: responseCode = $responseCode")
                
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Error del servidor: $responseCode")
                }
                
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "startAppleSignIn: response = $responseBody")
                
                json.decodeFromString<AppleStartResponse>(responseBody)
            } finally {
                connection.disconnect()
            }
        }
        
        Log.d(TAG, "startAppleSignIn: auth_url = ${response.auth_url}")
        Log.d(TAG, "startAppleSignIn: state = ${response.state}")
        
        // Guardar el state para validación posterior (opcional)
        pendingState = response.state
        
        // Abrir Custom Tab con la URL de Apple
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(false)
            .build()
        
        customTabsIntent.launchUrl(context, Uri.parse(response.auth_url))
        Log.d(TAG, "startAppleSignIn: Custom Tab abierto")
    }
    
    companion object {
        // Callbacks pendientes para notificaciones de UI
        private var pendingOnSuccess: ((String, String?) -> Unit)? = null
        private var pendingOnError: ((String) -> Unit)? = null
        private var pendingState: String? = null
        
        /**
         * Notifica que el flujo de Apple Sign-In terminó exitosamente
         * Llamado desde MainActivity después de authenticateWithToken()
         */
        fun notifySuccess() {
            Log.d(TAG, "notifySuccess: flujo completado exitosamente")
            // No llamamos onSuccess porque MainActivity ya manejó el token
            // Solo limpiamos los callbacks
            clearCallbacks()
        }
        
        /**
         * Notifica que hubo un error en el flujo de Apple Sign-In
         * Llamado desde MainActivity cuando el deep link tiene error
         */
        fun notifyError(errorMessage: String) {
            Log.e(TAG, "notifyError: $errorMessage")
            pendingOnError?.invoke(errorMessage)
            clearCallbacks()
        }
        
        /**
         * Limpia los callbacks pendientes
         */
        private fun clearCallbacks() {
            pendingOnSuccess = null
            pendingOnError = null
            pendingState = null
        }
        
        /**
         * Verifica si hay un flujo de Apple Sign-In pendiente
         */
        fun hasPendingSignIn(): Boolean {
            return pendingOnSuccess != null || pendingOnError != null
        }
        
        /**
         * Cancela el flujo pendiente con un error
         */
        fun cancelPendingSignIn() {
            pendingOnError?.invoke("Apple Sign-In cancelado")
            clearCallbacks()
        }
    }
}

@Composable
actual fun rememberAppleSignInHelper(): AppleSignInHelper {
    val context = LocalContext.current
    return remember {
        AppleSignInHelper(context as ComponentActivity)
    }
}
