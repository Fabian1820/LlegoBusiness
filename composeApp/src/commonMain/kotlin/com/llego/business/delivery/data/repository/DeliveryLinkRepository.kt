package com.llego.business.delivery.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.llego.business.delivery.data.model.BranchDeliveryRequest
import com.llego.business.delivery.data.model.DeliveryRequestStatus
import com.llego.business.delivery.data.model.LinkedDeliveryPerson
import com.llego.multiplatform.graphql.BranchLinkRequestsQuery
import com.llego.multiplatform.graphql.RespondBranchLinkRequestMutation
import com.llego.multiplatform.graphql.UpdateBranchMutation
import com.llego.multiplatform.graphql.type.DeliveryRequestStatusEnum
import com.llego.multiplatform.graphql.type.RespondBranchLinkInput
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.mappers.toGraphQL
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.network.GraphQLClient

class DeliveryLinkRepository(
    private val apolloClient: ApolloClient = GraphQLClient.apolloClient,
    private val tokenManager: TokenManager = TokenManager()
) {

    suspend fun getBranchLinkRequests(
        branchId: String,
        status: DeliveryRequestStatus? = null
    ): Result<List<BranchDeliveryRequest>> {
        return runCatching {
            val token = tokenManager.getToken()
                ?: throw IllegalStateException("No hay token de autenticacion")

            val response = apolloClient.query(
                BranchLinkRequestsQuery(
                    branchId = branchId,
                    status = Optional.presentIfNotNull(status?.toGraphQl()),
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.firstOrNull()?.message
                    ?: "No fue posible cargar las solicitudes de choferes"
                throw IllegalStateException(errorMessage)
            }

            response.data?.branchLinkRequests?.map { request ->
                request.toDomain()
            } ?: emptyList()
        }
    }

    suspend fun respondBranchLinkRequest(
        requestId: String,
        accept: Boolean
    ): Result<BranchDeliveryRequest> {
        return runCatching {
            val token = tokenManager.getToken()
                ?: throw IllegalStateException("No hay token de autenticacion")

            val response = apolloClient.mutation(
                RespondBranchLinkRequestMutation(
                    input = RespondBranchLinkInput(
                        requestId = requestId,
                        accept = accept
                    ),
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.firstOrNull()?.message
                    ?: "No fue posible responder la solicitud"
                throw IllegalStateException(errorMessage)
            }

            response.data?.respondBranchLinkRequest?.toDomain()
                ?: throw IllegalStateException("El backend no devolvio la solicitud actualizada")
        }
    }

    suspend fun enableOwnDeliveryForBranch(branchId: String): Result<Unit> {
        return runCatching {
            val token = tokenManager.getToken()
                ?: throw IllegalStateException("No hay token de autenticacion")

            val response = apolloClient.mutation(
                UpdateBranchMutation(
                    branchId = branchId,
                    input = UpdateBranchInput(useAppMessaging = false).toGraphQL(),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.firstOrNull()?.message
                    ?: "No fue posible activar el delivery propio"
                throw IllegalStateException(errorMessage)
            }

            if (response.data?.updateBranch == null) {
                throw IllegalStateException("No se recibio respuesta al actualizar la sucursal")
            }
        }
    }

    suspend fun disableOwnDeliveryForBranch(branchId: String): Result<Unit> {
        return runCatching {
            val token = tokenManager.getToken()
                ?: throw IllegalStateException("No hay token de autenticacion")

            val response = apolloClient.mutation(
                UpdateBranchMutation(
                    branchId = branchId,
                    input = UpdateBranchInput(useAppMessaging = true).toGraphQL(),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.firstOrNull()?.message
                    ?: "No fue posible desactivar el delivery propio"
                throw IllegalStateException(errorMessage)
            }

            if (response.data?.updateBranch == null) {
                throw IllegalStateException("No se recibio respuesta al actualizar la sucursal")
            }
        }
    }

    private fun BranchLinkRequestsQuery.BranchLinkRequest.toDomain(): BranchDeliveryRequest {
        return BranchDeliveryRequest(
            id = id,
            deliveryPersonId = deliveryPersonId,
            branchId = branchId,
            status = status.toDomain(),
            message = message,
            respondedBy = respondedBy,
            respondedAt = respondedAt?.toString(),
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            deliveryPerson = deliveryPerson?.toDomain()
        )
    }

    private fun RespondBranchLinkRequestMutation.RespondBranchLinkRequest.toDomain(): BranchDeliveryRequest {
        return BranchDeliveryRequest(
            id = id,
            deliveryPersonId = deliveryPersonId,
            branchId = branchId,
            status = status.toDomain(),
            message = message,
            respondedBy = respondedBy,
            respondedAt = respondedAt?.toString(),
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            deliveryPerson = deliveryPerson?.toDomain()
        )
    }

    private fun BranchLinkRequestsQuery.DeliveryPerson.toDomain(): LinkedDeliveryPerson {
        return LinkedDeliveryPerson(
            id = id,
            name = name,
            phone = phone,
            rating = rating,
            totalDeliveries = totalDeliveries,
            vehicleType = vehicleType.rawValue,
            vehiclePlate = vehiclePlate,
            profileImageUrl = profileImageUrl,
            isOnline = isOnline
        )
    }

    private fun RespondBranchLinkRequestMutation.DeliveryPerson.toDomain(): LinkedDeliveryPerson {
        return LinkedDeliveryPerson(
            id = id,
            name = name,
            phone = phone,
            rating = rating,
            totalDeliveries = totalDeliveries,
            vehicleType = vehicleType.rawValue,
            vehiclePlate = vehiclePlate,
            profileImageUrl = profileImageUrl,
            isOnline = isOnline
        )
    }

    private fun DeliveryRequestStatus.toGraphQl(): DeliveryRequestStatusEnum {
        return when (this) {
            DeliveryRequestStatus.PENDING -> DeliveryRequestStatusEnum.PENDING
            DeliveryRequestStatus.ACCEPTED -> DeliveryRequestStatusEnum.ACCEPTED
            DeliveryRequestStatus.REJECTED -> DeliveryRequestStatusEnum.REJECTED
        }
    }

    private fun DeliveryRequestStatusEnum.toDomain(): DeliveryRequestStatus {
        return when (this) {
            DeliveryRequestStatusEnum.PENDING -> DeliveryRequestStatus.PENDING
            DeliveryRequestStatusEnum.ACCEPTED -> DeliveryRequestStatus.ACCEPTED
            DeliveryRequestStatusEnum.REJECTED -> DeliveryRequestStatus.REJECTED
            DeliveryRequestStatusEnum.UNKNOWN__ -> DeliveryRequestStatus.PENDING
        }
    }
}
