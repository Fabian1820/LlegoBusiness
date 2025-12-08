package com.llego.shared.data.network

import com.apollographql.apollo.ApolloClient

/**
 * GraphQL Client configurado para conectarse al backend de Llego
 * Backend URL: https://llegobackend-production.up.railway.app/graphql
 */
object GraphQLClient {

    /**
     * Apollo Client configurado con la URL del backend en Railway
     */
    val apolloClient: ApolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl("https://llegobackend-production.up.railway.app/graphql")
            .build()
    }

    /**
     * Cierra el cliente y libera recursos
     * Llamar cuando la aplicación se cierre
     */
    fun close() {
        apolloClient.close()
    }
}
