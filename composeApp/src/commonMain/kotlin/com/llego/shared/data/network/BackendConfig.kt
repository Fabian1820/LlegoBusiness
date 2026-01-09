package com.llego.shared.data.network

/**
 * Configuración centralizada del backend de Llego
 *
 * Backend desplegado en Railway:
 * - GraphQL: https://llegobackend-production.up.railway.app/graphql
 * - REST/Upload: https://llegobackend-production.up.railway.app
 */
object BackendConfig {
    /**
     * URL base del backend (sin trailing slash)
     */
    const val BASE_URL = "https://llegobackend-production.up.railway.app"

    /**
     * URL del endpoint GraphQL
     */
    const val GRAPHQL_URL = "$BASE_URL/graphql"

    /**
     * URL base para endpoints REST (upload de imágenes)
     */
    const val REST_URL = BASE_URL
}
