package com.llego.business.orders.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloException
import com.llego.business.orders.data.mappers.OrderMappers.toDomain
import com.llego.business.orders.data.mappers.OrderMappers.toGraphQL
import com.llego.business.orders.data.mappers.OrderMappers.toPartialDomain
import com.llego.business.orders.data.model.*
import com.llego.multiplatform.graphql.BranchOrdersQuery
import com.llego.multiplatform.graphql.PendingBranchOrdersQuery
import com.llego.multiplatform.graphql.GetOrderQuery
import com.llego.multiplatform.graphql.OrderStatsQuery
import com.llego.multiplatform.graphql.AcceptOrderMutation
import com.llego.multiplatform.graphql.RejectOrderMutation
import com.llego.multiplatform.graphql.UpdateOrderStatusMutation
import com.llego.multiplatform.graphql.MarkOrderReadyMutation
import com.llego.multiplatform.graphql.ModifyOrderItemsMutation
import com.llego.multiplatform.graphql.AddOrderCommentMutation
import com.llego.multiplatform.graphql.NewBranchOrderSubscription
import com.llego.multiplatform.graphql.BranchOrderUpdatedSubscription
import com.llego.multiplatform.graphql.type.UpdateOrderStatusInput
import com.llego.multiplatform.graphql.type.ModifyOrderItemsInput
import com.llego.multiplatform.graphql.type.AddOrderCommentInput
import com.llego.multiplatform.graphql.type.OrderItemInput as GraphQLOrderItemInput
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.network.GraphQLClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

/**
 * Implementación del repositorio de pedidos usando Apollo GraphQL Client
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 11.1
 */
class OrderRepositoryImpl(
    private val tokenManager: TokenManager,
    private val apolloClient: ApolloClient = GraphQLClient.apolloClient
) : OrderRepository {

    // ==================== QUERIES ====================

    /**
     * Obtiene pedidos de una sucursal con filtros opcionales
     * Requirements: 2.1, 2.3, 2.4, 2.5
     */
    override suspend fun getBranchOrders(
        branchId: String,
        status: OrderStatus?,
        fromDate: String?,
        toDate: String?,
        limit: Int,
        offset: Int
    ): OrdersResult {
        println("🔄 OrderRepository.getBranchOrders()")
        println("   branchId: $branchId")
        println("   status: $status")
        println("   fromDate: $fromDate")
        println("   toDate: $toDate")
        println("   limit: $limit, offset: $offset")

        return try {
            val token = tokenManager.getToken()
            if (token == null) {
                println("❌ OrderRepository: No authentication token")
                return OrdersResult.Error("No authentication token")
            }
            println("   token: ${token.take(20)}...")

            val response = apolloClient.query(
                BranchOrdersQuery(
                    branchId = branchId,
                    jwt = token,
                    status = Optional.presentIfNotNull(status?.toGraphQL()),
                    fromDate = Optional.presentIfNotNull(fromDate),
                    toDate = Optional.presentIfNotNull(toDate),
                    limit = Optional.present(limit),
                    offset = Optional.present(offset)
                )
            ).execute()

            println("   response.hasErrors: ${response.hasErrors()}")
            if (response.hasErrors()) {
                val errorMsg = response.errors?.firstOrNull()?.message ?: "Error al obtener pedidos"
                println("❌ OrderRepository GraphQL errors: ${response.errors}")
                OrdersResult.Error(errorMsg)
            } else {
                val data = response.data?.branchOrders
                if (data != null) {
                    println("✅ OrderRepository: ${data.orders.size} orders, totalCount=${data.totalCount}, hasMore=${data.hasMore}")
                    OrdersResult.Success(
                        orders = data.orders.map { it.toDomain() },
                        totalCount = data.totalCount,
                        hasMore = data.hasMore
                    )
                } else {
                    println("❌ OrderRepository: data is null")
                    OrdersResult.Error("No se recibieron datos del servidor")
                }
            }
        } catch (e: ApolloException) {
            println("❌ OrderRepository ApolloException: ${e.message}")
            e.printStackTrace()
            OrdersResult.Error("Error de red: ${e.message}")
        } catch (e: Exception) {
            println("❌ OrderRepository Exception: ${e.message}")
            e.printStackTrace()
            OrdersResult.Error("Error inesperado: ${e.message}")
        }
    }

    /**
     * Obtiene pedidos pendientes de una sucursal
     * Requirements: 2.2
     */
    override suspend fun getPendingBranchOrders(branchId: String): Result<List<Order>> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            
            val response = apolloClient.query(
                PendingBranchOrdersQuery(
                    branchId = branchId,
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al obtener pedidos pendientes"))
            } else {
                val orders = response.data?.pendingBranchOrders?.map { it.toDomain() } ?: emptyList()
                Result.success(orders)
            }
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Obtiene un pedido específico por ID
     * Requirements: 2.6
     */
    override suspend fun getOrder(orderId: String): Result<Order?> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            
            val response = apolloClient.query(
                GetOrderQuery(
                    id = orderId,
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al obtener pedido"))
            } else {
                val order = response.data?.order?.toDomain()
                Result.success(order)
            }
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Obtiene estadísticas de pedidos
     * Requirements: 11.1
     */
    override suspend fun getOrderStats(
        fromDate: String,
        toDate: String,
        branchId: String?
    ): Result<OrderStats?> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            
            val response = apolloClient.query(
                OrderStatsQuery(
                    fromDate = fromDate,
                    toDate = toDate,
                    jwt = token,
                    branchId = Optional.presentIfNotNull(branchId)
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al obtener estadísticas"))
            } else {
                val stats = response.data?.orderStats?.toDomain()
                Result.success(stats)
            }
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }


    // ==================== MUTATIONS ====================

    /**
     * Acepta un pedido pendiente
     * Requirements: 5.1
     */
    override suspend fun acceptOrder(orderId: String, estimatedMinutes: Int): Result<Order> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            
            val response = apolloClient.mutation(
                AcceptOrderMutation(
                    orderId = orderId,
                    estimatedMinutes = estimatedMinutes,
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al aceptar pedido"))
            } else {
                val order = response.data?.acceptOrder?.toPartialDomain()
                if (order != null) {
                    Result.success(order)
                } else {
                    Result.failure(Exception("No se recibió respuesta del servidor"))
                }
            }
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Rechaza un pedido pendiente
     * Requirements: 5.2
     */
    override suspend fun rejectOrder(orderId: String, reason: String): Result<Order> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            
            val response = apolloClient.mutation(
                RejectOrderMutation(
                    orderId = orderId,
                    reason = reason,
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al rechazar pedido"))
            } else {
                val order = response.data?.rejectOrder?.toPartialDomain()
                if (order != null) {
                    Result.success(order)
                } else {
                    Result.failure(Exception("No se recibió respuesta del servidor"))
                }
            }
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Actualiza el estado de un pedido
     * Requirements: 5.3
     */
    override suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus,
        message: String?
    ): Result<Order> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            
            val response = apolloClient.mutation(
                UpdateOrderStatusMutation(
                    input = UpdateOrderStatusInput(
                        orderId = orderId,
                        status = status.toGraphQL(),
                        message = Optional.presentIfNotNull(message)
                    ),
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al actualizar estado"))
            } else {
                val order = response.data?.updateOrderStatus?.toPartialDomain()
                if (order != null) {
                    Result.success(order)
                } else {
                    Result.failure(Exception("No se recibió respuesta del servidor"))
                }
            }
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Marca un pedido como listo para recoger
     * Requirements: 5.4
     */
    override suspend fun markOrderReady(orderId: String): Result<Order> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            
            val response = apolloClient.mutation(
                MarkOrderReadyMutation(
                    orderId = orderId,
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al marcar como listo"))
            } else {
                val order = response.data?.markOrderReady?.toPartialDomain()
                if (order != null) {
                    Result.success(order)
                } else {
                    Result.failure(Exception("No se recibió respuesta del servidor"))
                }
            }
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Modifica los items de un pedido
     * Requirements: 6.6
     */
    override suspend fun modifyOrderItems(
        orderId: String,
        items: List<OrderItemInput>,
        reason: String
    ): Result<Order> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            
            val graphqlItems = items.map { item ->
                GraphQLOrderItemInput(
                    productId = item.productId,
                    quantity = item.quantity
                )
            }
            
            val response = apolloClient.mutation(
                ModifyOrderItemsMutation(
                    input = ModifyOrderItemsInput(
                        orderId = orderId,
                        items = graphqlItems,
                        reason = reason
                    ),
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al modificar items"))
            } else {
                val order = response.data?.modifyOrderItems?.toPartialDomain()
                if (order != null) {
                    Result.success(order)
                } else {
                    Result.failure(Exception("No se recibió respuesta del servidor"))
                }
            }
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    /**
     * Agrega un comentario a un pedido
     * Requirements: 7.3
     */
    override suspend fun addOrderComment(orderId: String, message: String): Result<Order> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            
            val response = apolloClient.mutation(
                AddOrderCommentMutation(
                    input = AddOrderCommentInput(
                        orderId = orderId,
                        message = message
                    ),
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error al agregar comentario"))
            } else {
                val order = response.data?.addOrderComment?.toPartialDomain()
                if (order != null) {
                    Result.success(order)
                } else {
                    Result.failure(Exception("No se recibió respuesta del servidor"))
                }
            }
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }


    // ==================== SUBSCRIPTIONS ====================

    /**
     * Suscripción a nuevos pedidos de una sucursal
     * Requirements: 3.1
     */
    override fun subscribeToNewOrders(branchId: String): Flow<Order> = flow {
        apolloClient.subscription(
            NewBranchOrderSubscription(branchId = branchId)
        ).toFlow().collect { response ->
            response.data?.newBranchOrder?.let { order ->
                emit(order.toDomain())
            }
        }
    }.catch { e ->
        // Log error but don't crash the flow
        println("Error en suscripción de nuevos pedidos: ${e.message}")
    }

    /**
     * Suscripción a actualizaciones de pedidos de una sucursal
     * Requirements: 3.2
     */
    override fun subscribeToOrderUpdates(branchId: String): Flow<Order> = flow {
        apolloClient.subscription(
            BranchOrderUpdatedSubscription(branchId = branchId)
        ).toFlow().collect { response ->
            response.data?.branchOrderUpdated?.let { order ->
                emit(order.toDomain())
            }
        }
    }.catch { e ->
        // Log error but don't crash the flow
        println("Error en suscripción de actualizaciones: ${e.message}")
    }

    companion object {
        private var instance: OrderRepositoryImpl? = null

        /**
         * Obtiene una instancia singleton del repositorio
         */
        fun getInstance(tokenManager: TokenManager): OrderRepositoryImpl {
            return instance ?: OrderRepositoryImpl(tokenManager).also { instance = it }
        }
    }
}


