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
        val token = tokenManager.getToken()
        if (token == null) {
        } else {
        }
        return try {
            val response = client.query(
                GetPaymentMethodsQuery(jwt = Optional.presentIfNotNull(token))
            ).execute()


            if (response.hasErrors()) {
                val errorMessage = response.errors?.joinToString(", ") { it.message }
                Result.failure(Exception(errorMessage ?: "Unknown error"))
            } else {
                val paymentMethods = response.data?.paymentMethods?.mapNotNull { method ->
                    PaymentMethod(
                        id = method.id,
                        currency = method.currency,
                        method = method.method
                    )
                } ?: emptyList()


                Result.success(paymentMethods)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
