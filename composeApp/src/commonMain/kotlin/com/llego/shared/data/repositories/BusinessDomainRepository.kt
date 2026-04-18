package com.llego.shared.data.repositories

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloException
import com.llego.multiplatform.graphql.GetBusinessQuery
import com.llego.multiplatform.graphql.GetMyBusinessesWithBranchesQuery
import com.llego.multiplatform.graphql.RegisterBusinessMutation
import com.llego.multiplatform.graphql.RegisterMultipleBusinessesMutation
import com.llego.multiplatform.graphql.UpdateBusinessMutation
import com.llego.multiplatform.graphql.DeleteBusinessMutation
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.mappers.mapBranchTipo
import com.llego.shared.data.mappers.mapBranchVehicle
import com.llego.shared.data.mappers.parseScheduleFromDays
import com.llego.shared.data.mappers.parseStringMap
import com.llego.shared.data.mappers.toDomain
import com.llego.shared.data.mappers.toGraphQL
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.BusinessWithBranches
import com.llego.shared.data.model.Coordinates
import com.llego.shared.data.model.CreateBusinessInput
import com.llego.shared.data.model.RegisterBranchInput
import com.llego.shared.data.model.UpdateBusinessInput
import com.llego.shared.data.model.WalletBalance

internal class BusinessDomainRepository(
    private val client: ApolloClient,
    private val tokenManager: TokenManager,
    private val state: BusinessRepositoryState
) {
    suspend fun registerBusiness(
        business: CreateBusinessInput,
        branches: List<RegisterBranchInput>
    ): BusinessResult<Business> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                RegisterBusinessMutation(
                    business = business.toGraphQL(),
                    branches = branches.map { it.toGraphQL() },
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val errors = response.errors?.joinToString(", ") { "${it.message} (path: ${it.path})" }
                return BusinessResult.Error(errors ?: "Error desconocido del servidor", "GRAPHQL_ERROR")
            }

            response.exception?.let { ex ->
                return BusinessResult.Error(ex.message ?: "Error de conexion", "RESPONSE_EXCEPTION")
            }

            response.data?.registerBusiness?.let { businessData ->
                val newBusiness = businessData.toDomain()
                state.setCurrentBusiness(newBusiness)
                BusinessResult.Success(newBusiness)
            } ?: BusinessResult.Error(
                "No se recibio respuesta del servidor al crear el negocio",
                "EMPTY_RESPONSE"
            )
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al crear negocio", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al crear negocio", "UNKNOWN_ERROR")
        }
    }

    suspend fun getBusinesses(): BusinessResult<List<Business>> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.query(
                GetMyBusinessesWithBranchesQuery(jwt = token)
            ).execute()

            if (response.hasErrors()) {
                val errors = response.errors?.joinToString(", ") { "${it.message}" }
                return BusinessResult.Error(errors ?: "Error desconocido del servidor", "GRAPHQL_ERROR")
            }

            response.data?.getMyBusinessesWithBranches?.let { businessesData ->
                val businessesList = businessesData.map { it.toDomain() }
                state.setBusinesses(businessesList)

                // No sobreescribir si ya hay un negocio activo válido en la lista
                val existing = state.currentBusiness.value
                if (existing == null || businessesList.none { it.id == existing.id }) {
                    state.setCurrentBusiness(businessesList.firstOrNull())
                }

                BusinessResult.Success(businessesList)
            } ?: BusinessResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al obtener negocios", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al obtener negocios", "UNKNOWN_ERROR")
        }
    }

    suspend fun getBusiness(id: String): BusinessResult<Business> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.query(
                GetBusinessQuery(
                    id = id,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.business?.let { businessData ->
                val business = businessData.toDomain()
                state.setCurrentBusiness(business)
                BusinessResult.Success(business)
            } ?: BusinessResult.Error("No se encontro el negocio", "NOT_FOUND")
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al obtener negocio", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al obtener negocio", "UNKNOWN_ERROR")
        }
    }

    suspend fun updateBusiness(
        businessId: String,
        input: UpdateBusinessInput
    ): BusinessResult<Business> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                UpdateBusinessMutation(
                    businessId = businessId,
                    input = input.toGraphQL(),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.updateBusiness?.let { businessData ->
                val updatedBusiness = businessData.toDomain()

                state.setBusinesses(
                    state.businesses.value.map { if (it.id == businessId) updatedBusiness else it }
                )

                if (state.currentBusiness.value?.id == businessId) {
                    state.setCurrentBusiness(updatedBusiness)
                }

                BusinessResult.Success(updatedBusiness)
            } ?: BusinessResult.Error("No se pudo actualizar el negocio", "UPDATE_FAILED")
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al actualizar negocio", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al actualizar negocio", "UNKNOWN_ERROR")
        }
    }

    suspend fun deleteBusiness(businessId: String): BusinessResult<Boolean> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                DeleteBusinessMutation(
                    businessId = businessId,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val error = response.errors?.firstOrNull()?.message ?: "No se pudo eliminar el negocio"
                return BusinessResult.Error(error, "DELETE_FAILED")
            }

            val deleted = response.data?.deleteBusiness ?: false
            if (!deleted) {
                return BusinessResult.Error("No se pudo eliminar el negocio", "DELETE_FAILED")
            }

            state.setBusinesses(state.businesses.value.filterNot { it.id == businessId })

            if (state.currentBusiness.value?.id == businessId) {
                state.setCurrentBusiness(state.businesses.value.firstOrNull())
            }

            state.setBranches(state.branches.value.filterNot { it.businessId == businessId })
            if (state.currentBranch.value?.businessId == businessId) {
                state.setCurrentBranch(state.branches.value.firstOrNull())
            }

            BusinessResult.Success(true)
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al eliminar negocio", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al eliminar negocio", "UNKNOWN_ERROR")
        }
    }

    suspend fun getBusinessesWithBranches(): BusinessResult<List<BusinessWithBranches>> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.query(
                GetMyBusinessesWithBranchesQuery(jwt = token)
            ).execute()

            if (response.hasErrors()) {
                val errors = response.errors?.joinToString(", ") { "${it.message}" }
                return BusinessResult.Error(errors ?: "Error desconocido del servidor", "GRAPHQL_ERROR")
            }

            response.data?.getMyBusinessesWithBranches?.let { businessesData ->
                val businessesList = businessesData.map { businessData ->
                    val branches = businessData.branches.map { branchData ->
                        val fields = branchData.branchCoreFields
                        val scheduleMap = parseScheduleFromDays(fields.schedule.days.map { Triple(it.day, it.isOpen, it.hours.map { h -> h.open to h.close }) })
                        val branchTipos = fields.tipos.mapNotNull { mapBranchTipo(it) }
                        val branchVehicles = fields.vehicles.mapNotNull { mapBranchVehicle(it) }

                        Branch(
                            id = fields.id,
                            businessId = fields.businessId,
                            name = fields.name,
                            address = fields.address,
                            coordinates = Coordinates(
                                type = fields.coordinates.coordinatesFields.type,
                                coordinates = fields.coordinates.coordinatesFields.coordinates
                            ),
                            phone = fields.phone,
                            schedule = scheduleMap,
                            tipos = branchTipos,
                            useAppMessaging = fields.useAppMessaging,
                            vehicles = branchVehicles,
                            paymentMethodIds = fields.paymentMethodIds,
                            managerIds = fields.managerIds,
                            isActive = fields.isActive,
                            avatar = fields.avatar,
                            coverImage = fields.coverImage,
                            socialMedia = parseStringMap(fields.socialMedia),
                            accounts = fields.accounts.map { account ->
                                com.llego.shared.data.model.TransferAccount(
                                    cardNumber = account.cardNumber,
                                    confirmPhone = account.confirmPhone,
                                    cardHolderName = account.cardHolderName,
                                    pagoQr = account.pagoQr,
                                    isActive = account.isActive
                                )
                            },
                            createdAt = fields.createdAt.toString(),
                            avatarUrl = fields.avatarUrl,
                            avatarUrlBaja = fields.avatarUrlBaja,
                            avatarUrlAlta = fields.avatarUrlAlta,
                            coverUrl = fields.coverUrl,
                            coverUrlBaja = fields.coverUrlBaja,
                            coverUrlAlta = fields.coverUrlAlta,
                            wallet = WalletBalance(
                                local = fields.wallet.walletBalanceFields.local,
                                usd = fields.wallet.walletBalanceFields.usd
                            ),
                            walletStatus = fields.walletStatus,
                            catalogOnly = fields.catalogOnly
                        )
                    }

                    val businessFields = businessData.businessRoleFields
                    BusinessWithBranches(
                        id = businessFields.id,
                        name = businessFields.name,
                        ownerId = businessFields.ownerId,
                        isOwner = businessFields.isOwner,
                        role = businessFields.role,
                        globalRating = businessFields.globalRating,
                        avatar = businessFields.avatar,
                        description = businessFields.description,
                        tags = businessFields.tags ?: emptyList(),
                        isActive = businessFields.isActive,
                        createdAt = businessFields.createdAt.toString(),
                        approvalStatus = businessFields.approvalStatus,
                        rejectionReason = businessFields.rejectionReason,
                        avatarUrl = businessFields.avatarUrl,
                        avatarUrlBaja = businessFields.avatarUrlBaja,
                        avatarUrlAlta = businessFields.avatarUrlAlta,
                        branches = branches
                    )
                }

                val allBranches = businessesList.flatMap { it.branches }
                state.setBusinesses(businessesList.map { it.toBusiness() })
                state.setBranches(allBranches)

                // Intentar mantener el negocio que corresponde a la sucursal actual o guardada.
                // Solo caer al primero si no hay ninguna referencia válida.
                val savedBranchId = tokenManager.getLastSelectedBranchId()
                val currentBranchId = state.currentBranch.value?.id ?: savedBranchId
                val targetBusinessId = allBranches.firstOrNull { it.id == currentBranchId }?.businessId
                val resolvedBusiness = state.businesses.value.firstOrNull { it.id == targetBusinessId }
                    ?: state.businesses.value.firstOrNull()
                state.setCurrentBusiness(resolvedBusiness)

                BusinessResult.Success(businessesList)
            } ?: BusinessResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al obtener negocios", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al obtener negocios", "UNKNOWN_ERROR")
        }
    }

    suspend fun registerMultipleBusinesses(
        businesses: List<Pair<CreateBusinessInput, List<RegisterBranchInput>>>
    ): BusinessResult<List<Business>> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val gqlBusinesses = businesses.map { (business, branches) ->
                com.llego.multiplatform.graphql.type.RegisterBusinessWithBranchesInput(
                    business = business.toGraphQL(),
                    branches = branches.map { it.toGraphQL() }
                )
            }

            val response = client.mutation(
                RegisterMultipleBusinessesMutation(
                    businesses = gqlBusinesses,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val errors = response.errors?.joinToString(", ") { "${it.message}" }
                return BusinessResult.Error(errors ?: "Error desconocido del servidor", "GRAPHQL_ERROR")
            }

            response.data?.registerMultipleBusinesses?.let { businessesData ->
                val businessesList = businessesData.map { businessData ->
                    val fields = businessData.businessOwnedFields
                    val core = fields.businessCoreFields
                    Business(
                        id = core.id,
                        name = core.name,
                        ownerId = core.ownerId,
                        globalRating = core.globalRating,
                        avatar = core.avatar,
                        description = core.description,
                        tags = core.tags ?: emptyList(),
                        isActive = core.isActive,
                        createdAt = core.createdAt.toString(),
                        avatarUrl = core.avatarUrl,
                        avatarUrlBaja = core.avatarUrlBaja,
                        avatarUrlAlta = core.avatarUrlAlta
                    )
                }

                state.setBusinesses(businessesList)
                if (businessesList.isNotEmpty()) {
                    state.setCurrentBusiness(businessesList.first())
                }

                BusinessResult.Success(businessesList)
            } ?: BusinessResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al registrar negocios", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al registrar negocios", "UNKNOWN_ERROR")
        }
    }
}
