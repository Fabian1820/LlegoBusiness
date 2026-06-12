package com.llego.shared.data.repositories

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.llego.multiplatform.graphql.CreateAdCampaignMutation
import com.llego.multiplatform.graphql.GetAdPricingQuery
import com.llego.multiplatform.graphql.GetPaymentMethodsQuery
import com.llego.multiplatform.graphql.MyAdCampaignsQuery
import com.llego.multiplatform.graphql.PurchaseAdCampaignMutation
import com.llego.multiplatform.graphql.type.CreateAdCampaignInput
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.AdCampaign
import com.llego.shared.data.model.AdPricing
import com.llego.shared.data.model.PaymentMethod
import com.llego.shared.data.network.GraphQLClient

/** Repositorio de campañas de visibilidad (Promociones). */
class MarketingRepository(
    private val client: ApolloClient = GraphQLClient.apolloClient,
    private val tokenManager: TokenManager = TokenManager()
) {
    suspend fun getPricing(): Result<List<AdPricing>> = try {
        val response = client.query(GetAdPricingQuery()).execute()
        if (response.hasErrors() || response.data == null) {
            Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error"))
        } else {
            Result.success(response.data!!.adPricing.map {
                AdPricing(it.id, it.placement, it.durationDays, it.price, it.currency, it.label)
            })
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMyCampaigns(): Result<List<AdCampaign>> = try {
        val token = tokenManager.getToken()
        val response = client.query(
            MyAdCampaignsQuery(jwt = Optional.presentIfNotNull(token))
        ).execute()
        if (response.hasErrors() || response.data == null) {
            Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error"))
        } else {
            Result.success(response.data!!.myAdCampaigns.map { c ->
                AdCampaign(
                    id = c.id,
                    name = c.name,
                    placement = c.placement,
                    status = c.status,
                    paymentStatus = c.paymentStatus,
                    approved = c.approved,
                    price = c.price,
                    currency = c.currency,
                    durationDays = c.durationDays,
                    impressions = c.impressions,
                    clicks = c.clicks,
                    rejectionReason = c.rejectionReason,
                    creativeImageUrl = c.creativeImageUrl
                )
            })
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Métodos de pago para campañas — incluye wallet (a diferencia del checkout de
     * pedidos). Wallet cobra en el acto; transferencia/Stripe quedan pendientes
     * hasta atar el flujo de pago de campañas.
     */
    suspend fun getPaymentMethods(): Result<List<PaymentMethod>> = try {
        val token = tokenManager.getToken()
        val response = client.query(
            GetPaymentMethodsQuery(jwt = Optional.presentIfNotNull(token))
        ).execute()
        if (response.hasErrors() || response.data == null) {
            Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error"))
        } else {
            Result.success(response.data!!.paymentMethods.map {
                PaymentMethod(id = it.id, name = it.name, currency = it.currency, method = it.method)
            })
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Crea la campaña (borrador) con la foto exportada ya subida. Devuelve el id. */
    suspend fun createCampaign(
        businessId: String,
        branchId: String,
        name: String,
        placement: String,
        durationDays: Int,
        creativeImagePath: String
    ): Result<String> = try {
        val token = tokenManager.getToken()
        val input = CreateAdCampaignInput(
            businessId = businessId,
            branchId = branchId,
            name = name,
            placement = placement,
            durationDays = durationDays,
            creativeImagePath = creativeImagePath
        )
        val response = client.mutation(
            CreateAdCampaignMutation(input = input, jwt = Optional.presentIfNotNull(token))
        ).execute()
        if (response.hasErrors() || response.data == null) {
            Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al crear"))
        } else {
            Result.success(response.data!!.createAdCampaign.id)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Paga la campaña. Devuelve el nuevo status. */
    suspend fun purchaseCampaign(id: String, paymentMethodId: String): Result<String> = try {
        val token = tokenManager.getToken()
        val response = client.mutation(
            PurchaseAdCampaignMutation(
                id = id,
                paymentMethodId = paymentMethodId,
                jwt = Optional.presentIfNotNull(token)
            )
        ).execute()
        if (response.hasErrors() || response.data == null) {
            Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al pagar"))
        } else {
            Result.success(response.data!!.purchaseAdCampaign.status)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
