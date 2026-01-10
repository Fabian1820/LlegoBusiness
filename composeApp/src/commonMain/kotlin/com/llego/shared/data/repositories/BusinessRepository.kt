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
        
        // Log para debug
        println("RegisterBusiness: token disponible = ${token != null}, length = ${token?.length ?: 0}")
        
        if (token == null) {
            return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")
        }

        return try {
            println("RegisterBusiness: Ejecutando mutation...")
            println("RegisterBusiness: Business input = $business")
            println("RegisterBusiness: Branches input = $branches")
            
            val gqlBusiness = business.toGraphQL()
            val gqlBranches = branches.map { it.toGraphQL() }
            
            println("RegisterBusiness: GraphQL Business = $gqlBusiness")
            println("RegisterBusiness: GraphQL Branches = $gqlBranches")
            
            val response = client.mutation(
                RegisterBusinessMutation(
                    business = gqlBusiness,
                    branches = gqlBranches,
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            // Log completo de la respuesta
            println("RegisterBusiness: response.hasErrors() = ${response.hasErrors()}")
            println("RegisterBusiness: response.errors = ${response.errors}")
            println("RegisterBusiness: response.data = ${response.data}")
            println("RegisterBusiness: response.exception = ${response.exception}")

            // Log de errores GraphQL
            if (response.hasErrors()) {
                val errors = response.errors?.joinToString(", ") { "${it.message} (path: ${it.path})" }
                println("RegisterBusiness: GraphQL errors = $errors")
                return BusinessResult.Error(
                    errors ?: "Error desconocido del servidor",
                    "GRAPHQL_ERROR"
                )
            }
            
            // Verificar si hay excepción
            response.exception?.let { ex ->
                println("RegisterBusiness: Exception en response = ${ex.message}")
                return BusinessResult.Error(
                    ex.message ?: "Error de conexión",
                    "RESPONSE_EXCEPTION"
                )
            }

            println("RegisterBusiness: response.data?.registerBusiness = ${response.data?.registerBusiness}")

            response.data?.registerBusiness?.let { businessData ->
                val newBusiness = businessData.toDomain()
                _currentBusiness.value = newBusiness
                println("RegisterBusiness: Negocio creado exitosamente con id = ${newBusiness.id}")
                BusinessResult.Success(newBusiness)
            } ?: BusinessResult.Error(
                "No se recibió respuesta del servidor al crear el negocio (data.registerBusiness es null)",
                "EMPTY_RESPONSE"
            )

        } catch (e: ApolloException) {
            println("RegisterBusiness: ApolloException = ${e.message}")
            println("RegisterBusiness: ApolloException cause = ${e.cause}")
            e.printStackTrace()
            BusinessResult.Error(
                e.message ?: "Error de conexión al crear negocio",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            println("RegisterBusiness: Exception = ${e.message}")
            println("RegisterBusiness: Exception type = ${e::class.simpleName}")
            e.printStackTrace()
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
        println("BusinessRepository.getBusinesses: iniciando...")

        val token = tokenManager.getToken()
        if (token == null) {
            println("BusinessRepository.getBusinesses: NO_TOKEN")
            return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")
        }

        println("BusinessRepository.getBusinesses: token encontrado (length: ${token.length})")

        return try {
            println("BusinessRepository.getBusinesses: ejecutando query GetBusinesses...")

            val response = client.query(
                GetBusinessesQuery(
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            println("BusinessRepository.getBusinesses: respuesta recibida")
            println("BusinessRepository.getBusinesses: data=${response.data != null}")
            println("BusinessRepository.getBusinesses: errors=${response.errors}")
            println("BusinessRepository.getBusinesses: hasErrors=${response.hasErrors()}")

            if (response.hasErrors()) {
                val errors = response.errors?.joinToString(", ") { "${it.message}" }
                println("BusinessRepository.getBusinesses: GraphQL errors = $errors")
                return BusinessResult.Error(
                    errors ?: "Error desconocido del servidor",
                    "GRAPHQL_ERROR"
                )
            }

            response.data?.businesses?.let { businessesData ->
                println("BusinessRepository.getBusinesses: businessesData count=${businessesData.size}")

                val businessesList = businessesData.map { it.toDomain() }
                _businesses.value = businessesList

                println("BusinessRepository.getBusinesses: negocios convertidos=${businessesList.size}")
                businessesList.forEach { business ->
                    println("  - Business: id=${business.id}, name=${business.name}")
                }

                // Si solo hay un negocio, establecerlo como actual
                if (businessesList.size == 1) {
                    _currentBusiness.value = businessesList.first()
                    println("BusinessRepository.getBusinesses: establecido currentBusiness=${businessesList.first().name}")
                } else if (businessesList.isNotEmpty()) {
                    // Si hay múltiples negocios, establecer el primero como actual
                    _currentBusiness.value = businessesList.first()
                    println("BusinessRepository.getBusinesses: múltiples negocios, establecido primero=${businessesList.first().name}")
                }

                BusinessResult.Success(businessesList)
            } ?: run {
                println("BusinessRepository.getBusinesses: response.data.businesses es null")
                BusinessResult.Error(
                    "No se recibió respuesta del servidor",
                    "EMPTY_RESPONSE"
                )
            }

        } catch (e: ApolloException) {
            println("BusinessRepository.getBusinesses: ApolloException=${e.message}")
            e.printStackTrace()
            BusinessResult.Error(
                e.message ?: "Error de conexión al obtener negocios",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            println("BusinessRepository.getBusinesses: Exception=${e.message}")
            e.printStackTrace()
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
        println("BusinessRepository.getBranches: iniciando...")
        println("BusinessRepository.getBranches: businessId=$businessId")

        val token = tokenManager.getToken()
        if (token == null) {
            println("BusinessRepository.getBranches: NO_TOKEN")
            return BusinessResult.Error("No hay sesión activa", "NO_TOKEN")
        }

        println("BusinessRepository.getBranches: token encontrado (length: ${token.length})")

        return try {
            println("BusinessRepository.getBranches: ejecutando query GetBranches...")

            val response = client.query(
                GetBranchesQuery(
                    businessId = Optional.presentIfNotNull(businessId),
                    jwt = Optional.presentIfNotNull(token)
                )
            ).execute()

            println("BusinessRepository.getBranches: respuesta recibida")
            println("BusinessRepository.getBranches: data=${response.data != null}")
            println("BusinessRepository.getBranches: errors=${response.errors}")
            println("BusinessRepository.getBranches: hasErrors=${response.hasErrors()}")

            if (response.hasErrors()) {
                val errors = response.errors?.joinToString(", ") { "${it.message}" }
                println("BusinessRepository.getBranches: GraphQL errors = $errors")
                return BusinessResult.Error(
                    errors ?: "Error desconocido del servidor",
                    "GRAPHQL_ERROR"
                )
            }

            response.data?.branches?.let { branchesData ->
                println("BusinessRepository.getBranches: branchesData count=${branchesData.size}")

                val branchesList = branchesData.map { it.toDomain() }
                _branches.value = branchesList

                println("BusinessRepository.getBranches: sucursales convertidas=${branchesList.size}")
                branchesList.forEach { branch ->
                    println("  - Branch: id=${branch.id}, name=${branch.name}")
                }

                // Si solo hay una sucursal, establecerla como actual
                if (branchesList.size == 1) {
                    _currentBranch.value = branchesList.first()
                    println("BusinessRepository.getBranches: establecida currentBranch=${branchesList.first().name}")
                } else if (branchesList.isNotEmpty()) {
                    _currentBranch.value = branchesList.first()
                    println("BusinessRepository.getBranches: múltiples sucursales, establecida primera=${branchesList.first().name}")
                } else {
                    println("BusinessRepository.getBranches: no hay sucursales para este negocio")
                }

                BusinessResult.Success(branchesList)
            } ?: run {
                println("BusinessRepository.getBranches: response.data.branches es null")
                BusinessResult.Error(
                    "No se recibió respuesta del servidor",
                    "EMPTY_RESPONSE"
                )
            }

        } catch (e: ApolloException) {
            println("BusinessRepository.getBranches: ApolloException=${e.message}")
            e.printStackTrace()
            BusinessResult.Error(
                e.message ?: "Error de conexión al obtener sucursales",
                "APOLLO_ERROR"
            )
        } catch (e: Exception) {
            println("BusinessRepository.getBranches: Exception=${e.message}")
            e.printStackTrace()
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
     * Establece la sucursal actual
     */
    fun setCurrentBranch(branch: Branch) {
        _currentBranch.value = branch
    }

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
