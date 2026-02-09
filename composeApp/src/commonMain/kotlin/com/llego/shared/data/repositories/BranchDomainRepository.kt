package com.llego.shared.data.repositories

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloException
import com.llego.multiplatform.graphql.CreateBranchMutation
import com.llego.multiplatform.graphql.DeleteBranchMutation
import com.llego.multiplatform.graphql.GetBranchQuery
import com.llego.multiplatform.graphql.GetBranchesQuery
import com.llego.multiplatform.graphql.MeQuery
import com.llego.multiplatform.graphql.UpdateBranchMutation
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.mappers.toDomain
import com.llego.shared.data.mappers.toGraphQL
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CreateBranchInput
import com.llego.shared.data.model.UpdateBranchInput

internal class BranchDomainRepository(
    private val client: ApolloClient,
    private val tokenManager: TokenManager,
    private val state: BusinessRepositoryState
) {
    suspend fun getBranches(businessId: String? = null): BusinessResult<List<Branch>> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.query(
                GetBranchesQuery(
                    businessId = Optional.presentIfNotNull(businessId),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            if (response.hasErrors()) {
                val errors = response.errors?.joinToString(", ") { "${it.message}" }
                if (errors?.contains("_wallet") == true) {
                    return fallbackGetBranchesByIds(token, businessId)
                }
                return BusinessResult.Error(errors ?: "Error desconocido del servidor", "GRAPHQL_ERROR")
            }

            response.data?.branches?.let { branchesConnection ->
                val branchesList = branchesConnection.edges.map { it.node.toDomain() }
                state.setBranches(branchesList)
                updateCurrentBranchFromList(branchesList)
                BusinessResult.Success(branchesList)
            } ?: fallbackGetBranchesByIds(token, businessId)
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al obtener sucursales", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al obtener sucursales", "UNKNOWN_ERROR")
        }
    }

    suspend fun getBranch(id: String): BusinessResult<Branch> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.query(
                GetBranchQuery(
                    id = id,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.branch?.let { branchData ->
                val branch = branchData.toDomain()
                state.setCurrentBranch(branch)
                BusinessResult.Success(branch)
            } ?: BusinessResult.Error("No se encontro la sucursal", "NOT_FOUND")
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al obtener sucursal", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al obtener sucursal", "UNKNOWN_ERROR")
        }
    }

    suspend fun createBranch(input: CreateBranchInput): BusinessResult<Branch> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                CreateBranchMutation(
                    input = input.toGraphQL(),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.createBranch?.let { branchData ->
                val newBranch = branchData.toDomain()
                state.setBranches(state.branches.value + newBranch)
                BusinessResult.Success(newBranch)
            } ?: BusinessResult.Error("No se pudo crear la sucursal", "CREATE_FAILED")
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al crear sucursal", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al crear sucursal", "UNKNOWN_ERROR")
        }
    }

    suspend fun updateBranch(
        branchId: String,
        input: UpdateBranchInput
    ): BusinessResult<Branch> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                UpdateBranchMutation(
                    branchId = branchId,
                    input = input.toGraphQL(),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.updateBranch?.let { branchData ->
                val updatedBranch = branchData.toDomain()

                state.setBranches(
                    state.branches.value.map { if (it.id == branchId) updatedBranch else it }
                )

                if (state.currentBranch.value?.id == branchId) {
                    state.setCurrentBranch(updatedBranch)
                }

                BusinessResult.Success(updatedBranch)
            } ?: BusinessResult.Error("No se pudo actualizar la sucursal", "UPDATE_FAILED")
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al actualizar sucursal", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al actualizar sucursal", "UNKNOWN_ERROR")
        }
    }

    suspend fun deleteBranch(branchId: String): BusinessResult<Boolean> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                DeleteBranchMutation(
                    branchId = branchId,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            val deleted = response.data?.deleteBranch == true
            if (!deleted) {
                return BusinessResult.Error("No se pudo eliminar la sucursal", "DELETE_FAILED")
            }

            val updatedBranches = state.branches.value.filterNot { it.id == branchId }
            state.setBranches(updatedBranches)

            if (state.currentBranch.value?.id == branchId) {
                state.setCurrentBranch(
                    when (updatedBranches.size) {
                        1 -> updatedBranches.first()
                        else -> null
                    }
                )
                if (updatedBranches.none { it.id == branchId }) {
                    tokenManager.clearLastSelectedBranchId()
                }
            }

            BusinessResult.Success(true)
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al eliminar sucursal", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al eliminar sucursal", "UNKNOWN_ERROR")
        }
    }

    private suspend fun fallbackGetBranchesByIds(
        token: String,
        businessId: String?
    ): BusinessResult<List<Branch>> {
        return try {
            val meResponse = client.query(MeQuery(jwt = token)).execute()
            if (meResponse.hasErrors()) {
                val errors = meResponse.errors?.joinToString(", ") { "${it.message}" }
                return BusinessResult.Error(
                    errors ?: "Error al obtener usuario para cargar sucursales",
                    "GRAPHQL_ERROR"
                )
            }

            val branchIds = meResponse.data?.me?.userCoreFields?.branchIds ?: emptyList()
            if (branchIds.isEmpty()) {
                state.setBranches(emptyList())
                state.setCurrentBranch(null)
                return BusinessResult.Success(emptyList())
            }

            val branches = mutableListOf<Branch>()
            var hadErrors = false
            branchIds.distinct().forEach { branchId ->
                val branchResponse = client.query(
                    GetBranchQuery(
                        id = branchId,
                        jwt = Optional.presentIfNotNull(token)
                    )
                ).execute()

                if (branchResponse.hasErrors()) {
                    hadErrors = true
                    return@forEach
                }

                branchResponse.data?.branch?.let { branchData ->
                    branches.add(branchData.toDomain())
                }
            }

            val filteredBranches = if (businessId != null) {
                branches.filter { it.businessId == businessId }
            } else {
                branches
            }

            state.setBranches(filteredBranches)
            updateCurrentBranchFromList(filteredBranches)

            if (filteredBranches.isEmpty() && hadErrors) {
                BusinessResult.Error("No se pudieron cargar sucursales", "GRAPHQL_ERROR")
            } else {
                BusinessResult.Success(filteredBranches)
            }
        } catch (e: ApolloException) {
            BusinessResult.Error(e.message ?: "Error de conexion al obtener sucursales", "APOLLO_ERROR")
        } catch (e: Exception) {
            BusinessResult.Error(e.message ?: "Error desconocido al obtener sucursales", "UNKNOWN_ERROR")
        }
    }

    private fun updateCurrentBranchFromList(branchesList: List<Branch>) {
        val current = state.currentBranch.value
        val persistedBranchId = tokenManager.getLastSelectedBranchId()

        val resolvedBranch = when {
            branchesList.isEmpty() -> null
            current != null -> branchesList.firstOrNull { it.id == current.id }
            !persistedBranchId.isNullOrBlank() -> branchesList.firstOrNull { it.id == persistedBranchId }
            branchesList.size == 1 -> branchesList.first()
            // Si hay varias sucursales y no hay seleccion previa valida,
            // forzamos la seleccion manual en BranchSelectorScreen.
            else -> null
        }

        state.setCurrentBranch(resolvedBranch)

        if (resolvedBranch != null) {
            tokenManager.saveLastSelectedBranchId(resolvedBranch.id)
        } else {
            tokenManager.clearLastSelectedBranchId()
        }
    }
}
