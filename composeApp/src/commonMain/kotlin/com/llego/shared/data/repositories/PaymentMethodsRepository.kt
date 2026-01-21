package com.llego.shared.data.repositories

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.llego.multiplatform.graphql.GetPaymentMethodsQuery
import com.llego.shared.data.network.GraphQLClient
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.PaymentMethod

/**
 * Repository for payment methods operations
 */
class PaymentMethodsRepository(
    private val client: ApolloClient = GraphQLClient.apolloClient,
    private val tokenManager: TokenManager = TokenManager()
) {
    /**
     * Fetches all available payment methods
     */
    suspend fun getPaymentMethods(): Result<List<PaymentMethod>> {
        println("PaymentMethodsRepository.getPaymentMethods: iniciando...")
        val token = tokenManager.getToken()
        if (token == null) {
            println("PaymentMethodsRepository.getPaymentMethods: NO_TOKEN")
        } else {
            println("PaymentMethodsRepository.getPaymentMethods: token encontrado (length: ${token.length})")
        }
        return try {
            println("PaymentMethodsRepository.getPaymentMethods: ejecutando query GetPaymentMethods...")
            val response = client.query(
                GetPaymentMethodsQuery(jwt = Optional.presentIfNotNull(token))
            ).execute()

            println("PaymentMethodsRepository.getPaymentMethods: respuesta recibida")
            println("PaymentMethodsRepository.getPaymentMethods: data=${response.data != null}")
            println("PaymentMethodsRepository.getPaymentMethods: errors=${response.errors}")
            println("PaymentMethodsRepository.getPaymentMethods: hasErrors=${response.hasErrors()}")

            if (response.hasErrors()) {
                val errorMessage = response.errors?.joinToString(", ") { it.message }
                println("PaymentMethodsRepository.getPaymentMethods: GraphQL errors=$errorMessage")
                Result.failure(Exception(errorMessage ?: "Unknown error"))
            } else {
                val paymentMethods = response.data?.paymentMethods?.mapNotNull { method ->
                    PaymentMethod(
                        id = method.id,
                        currency = method.currency,
                        method = method.method
                    )
                } ?: emptyList()

                println("PaymentMethodsRepository.getPaymentMethods: paymentMethods count=${paymentMethods.size}")

                Result.success(paymentMethods)
            }
        } catch (e: Exception) {
            println("PaymentMethodsRepository.getPaymentMethods: Exception=${e.message}")
            Result.failure(e)
        }
    }
}
