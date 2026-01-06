package com.llego.shared.data.repositories

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloException
import com.llego.multiplatform.graphql.*
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.mappers.*
import com.llego.shared.data.model.*
import com.llego.shared.data.network.GraphQLClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository para gestionar negocios y sucursales
 * Sigue el mismo patrón que AuthRepository.kt
 */
class BusinessRepository(
    private val client: ApolloClient = GraphQLClient.apolloClient,
    private val tokenManager: TokenManager
) {

    // ============= STATE FLOWS =============

    private val _currentBusiness = MutableStateFlow<Business?>(null)
    val currentBusiness: StateFlow<Business?> = _currentBusiness.asStateFlow()

    private val _businesses = MutableStateFlow<List<Business>>(emptyList())
    val businesses: StateFlow<List<Business>> = _businesses.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _currentBranch = MutableStateFlow<Branch?>(null)
    val currentBranch: StateFlow<Branch?> = _currentBranch.asStateFlow()

    // ============= BUSINESS OPERATIONS =============

    /**
     * Registra un nuevo negocio con sus sucursales
     */
    suspend fun registerBusiness(
        business: CreateBusinessInput,
        branches: List<RegisterBranchInput>
    ): BusinessResult<Business> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                RegisterBusinessMutation(
                    business = business.toGraphQL(),
                    branches = branches.map { it.toGraphQL() },
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.registerBusiness?.let { businessData ->
                val newBusiness = businessData.toDomain()
                _currentBusiness.value = newBusiness
                BusinessResult.Success(newBusiness)
            } ?: BusinessResult.Error(
                "No se recibió respuesta del servidor al crear el negocio",
                "EMPTY_RESPONSE"
            )

        } catch (e: ApolloException) {
            BusinessResult.Error(
                e.message ?: "Error de conexión al crear negocio",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            BusinessResult.Error(
                e.message ?: "Error desconocido al crear negocio",
                "UNKNOWN_ERROR"
            )
        }
    }

    /**
     * Obtiene todos los negocios del usuario actual
     */
    suspend fun getBusinesses(): BusinessResult<List<Business>> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")

        return try {
            val response = client.query(
                GetBusinessesQuery(
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.businesses?.let { businessesData ->
                val businessesList = businessesData.map { it.toDomain() }
                _businesses.value = businessesList

                // Si solo hay un negocio, establecerlo como actual
                if (businessesList.size == 1) {
                    _currentBusiness.value = businessesList.first()
                }

                BusinessResult.Success(businessesList)
            } ?: BusinessResult.Error(
                "No se recibió respuesta del servidor",
                "EMPTY_RESPONSE"
            )

        } catch (e: ApolloException) {
            BusinessResult.Error(
                e.message ?: "Error de conexión al obtener negocios",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            BusinessResult.Error(
                e.message ?: "Error desconocido al obtener negocios",
                "UNKNOWN_ERROR"
            )
        }
    }

    /**
     * Obtiene un negocio específico por ID
     */
    suspend fun getBusiness(id: String): BusinessResult<Business> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")

        return try {
            val response = client.query(
                GetBusinessQuery(
                    id = id,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.business?.let { businessData ->
                val business = businessData.toDomain()
                _currentBusiness.value = business
                BusinessResult.Success(business)
            } ?: BusinessResult.Error(
                "No se encontró el negocio",
                "NOT_FOUND"
            )

        } catch (e: ApolloException) {
            BusinessResult.Error(
                e.message ?: "Error de conexión al obtener negocio",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            BusinessResult.Error(
                e.message ?: "Error desconocido al obtener negocio",
                "UNKNOWN_ERROR"
            )
        }
    }

    /**
     * Actualiza un negocio
     */
    suspend fun updateBusiness(
        businessId: String,
        input: UpdateBusinessInput
    ): BusinessResult<Business> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")

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

                // Actualizar en la lista si existe
                _businesses.value = _businesses.value.map {
                    if (it.id == businessId) updatedBusiness else it
                }

                // Actualizar current si es el mismo
                if (_currentBusiness.value?.id == businessId) {
                    _currentBusiness.value = updatedBusiness
                }

                BusinessResult.Success(updatedBusiness)
            } ?: BusinessResult.Error(
                "No se pudo actualizar el negocio",
                "UPDATE_FAILED"
            )

        } catch (e: ApolloException) {
            BusinessResult.Error(
                e.message ?: "Error de conexión al actualizar negocio",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            BusinessResult.Error(
                e.message ?: "Error desconocido al actualizar negocio",
                "UNKNOWN_ERROR"
            )
        }
    }

    // ============= BRANCH OPERATIONS =============

    /**
     * Obtiene todas las sucursales de un negocio
     * Si businessId es null, obtiene todas las sucursales del usuario
     */
    suspend fun getBranches(businessId: String? = null): BusinessResult<List<Branch>> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")

        return try {
            val response = client.query(
                GetBranchesQuery(
                    businessId = Optional.presentIfNotNull(businessId),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.branches?.let { branchesData ->
                val branchesList = branchesData.map { it.toDomain() }
                _branches.value = branchesList

                // Si solo hay una sucursal, establecerla como actual
                if (branchesList.size == 1) {
                    _currentBranch.value = branchesList.first()
                }

                BusinessResult.Success(branchesList)
            } ?: BusinessResult.Error(
                "No se recibió respuesta del servidor",
                "EMPTY_RESPONSE"
            )

        } catch (e: ApolloException) {
            BusinessResult.Error(
                e.message ?: "Error de conexión al obtener sucursales",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            BusinessResult.Error(
                e.message ?: "Error desconocido al obtener sucursales",
                "UNKNOWN_ERROR"
            )
        }
    }

    /**
     * Obtiene una sucursal específica por ID
     */
    suspend fun getBranch(id: String): BusinessResult<Branch> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")

        return try {
            val response = client.query(
                GetBranchQuery(
                    id = id,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.branch?.let { branchData ->
                val branch = branchData.toDomain()
                _currentBranch.value = branch
                BusinessResult.Success(branch)
            } ?: BusinessResult.Error(
                "No se encontró la sucursal",
                "NOT_FOUND"
            )

        } catch (e: ApolloException) {
            BusinessResult.Error(
                e.message ?: "Error de conexión al obtener sucursal",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            BusinessResult.Error(
                e.message ?: "Error desconocido al obtener sucursal",
                "UNKNOWN_ERROR"
            )
        }
    }

    /**
     * Crea una nueva sucursal
     */
    suspend fun createBranch(input: CreateBranchInput): BusinessResult<Branch> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                CreateBranchMutation(
                    input = input.toGraphQL(),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            response.data?.createBranch?.let { branchData ->
                val newBranch = branchData.toDomain()

                // Agregar a la lista de sucursales
                _branches.value = _branches.value + newBranch

                BusinessResult.Success(newBranch)
            } ?: BusinessResult.Error(
                "No se pudo crear la sucursal",
                "CREATE_FAILED"
            )

        } catch (e: ApolloException) {
            BusinessResult.Error(
                e.message ?: "Error de conexión al crear sucursal",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            BusinessResult.Error(
                e.message ?: "Error desconocido al crear sucursal",
                "UNKNOWN_ERROR"
            )
        }
    }

    /**
     * Actualiza una sucursal
     */
    suspend fun updateBranch(
        branchId: String,
        input: UpdateBranchInput
    ): BusinessResult<Branch> {
        val token = tokenManager.getToken()
            ?: return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")

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

                // Actualizar en la lista si existe
                _branches.value = _branches.value.map {
                    if (it.id == branchId) updatedBranch else it
                }

                // Actualizar current si es la misma
                if (_currentBranch.value?.id == branchId) {
                    _currentBranch.value = updatedBranch
                }

                BusinessResult.Success(updatedBranch)
            } ?: BusinessResult.Error(
                "No se pudo actualizar la sucursal",
                "UPDATE_FAILED"
            )

        } catch (e: ApolloException) {
            BusinessResult.Error(
                e.message ?: "Error de conexión al actualizar sucursal",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            BusinessResult.Error(
                e.message ?: "Error desconocido al actualizar sucursal",
                "UNKNOWN_ERROR"
            )
        }
    }

    // ============= HELPER METHODS =============

    /**
     * Limpia todos los datos del repository
     */
    fun clear() {
        _currentBusiness.value = null
        _businesses.value = emptyList()
        _branches.value = emptyList()
        _currentBranch.value = null
    }
}
