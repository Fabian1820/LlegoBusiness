package com.llego.shared.data.network

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.llego.shared.data.auth.TokenManager

/**
 * GraphQL Client configurado para conectarse al backend de Llego
 * Backend URL: https://llegobackend-production.up.railway.app/graphql
 *
 * Incluye interceptor para agregar JWT tokens automáticamente a todas las requests
 */
object GraphQLClient {

    private var tokenManager: TokenManager? = null

    /**
     * Inicializa el cliente con el TokenManager
     * Debe llamarse al inicio de la aplicación
     */
    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    /**
     * Apollo Client configurado con la URL del backend en Railway
     * Incluye interceptor para autenticación JWT
     */
    val apolloClient: ApolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl("https://llegobackend-production.up.railway.app/graphql")
            .addHttpHeader("Content-Type", "application/json")
            .addHttpInterceptor(AuthInterceptor())
            .build()
    }

    /**
     * Cierra el cliente y libera recursos
     * Llamar cuando la aplicación se cierre
     */
    fun close() {
        // No need to check isInitialized for lazy properties
        // Just close if accessed
        try {
            apolloClient.close()
        } catch (e: Exception) {
            // Ignore if not initialized
        }
    }

    /**
     * Interceptor HTTP que agrega el JWT token a las requests
     */
    private class AuthInterceptor : HttpInterceptor {
        override suspend fun intercept(
            request: HttpRequest,
            chain: HttpInterceptorChain
        ): HttpResponse {
            val token = tokenManager?.getToken()

            val newRequest = if (token != null) {
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                request
            }

            return chain.proceed(newRequest)
        }
    }
}
