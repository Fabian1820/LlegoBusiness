package com.llego.shared.data.auth

import com.llego.shared.data.model.*
import com.llego.shared.data.repositories.AuthRepository
import com.llego.shared.data.repositories.BusinessRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Manager centralizado para la autenticación en las apps de Llego
 * Coordina entre el AuthRepository, BusinessRepository y las diferentes pantallas
 * Actualizado para usar GraphQL con Business y Branch data
 */
class AuthManager(private val tokenManager: TokenManager) {

    private val authRepository = AuthRepository(tokenManager = tokenManager)
    private val businessRepository = BusinessRepository(tokenManager = tokenManager)

    // Exposición de los flows del repository
    val currentUser: StateFlow<User?> = authRepository.currentUser
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated

    // Exposición de los flows de Business
    val currentBusiness: StateFlow<Business?> = businessRepository.currentBusiness
    val businesses: StateFlow<List<Business>> = businessRepository.businesses
    val branches: StateFlow<List<Branch>> = businessRepository.branches
    val currentBranch: StateFlow<Branch?> = businessRepository.currentBranch

    /**
     * Realiza el login del usuario
     */
    suspend fun login(email: String, password: String): AuthResult<User> {
        return authRepository.login(email, password)
    }

    /**
     * Registra un nuevo usuario
     */
    suspend fun register(input: RegisterInput): AuthResult<User> {
        return authRepository.register(input)
    }

    /**
     * Login con Google
     */
    suspend fun loginWithGoogle(idToken: String, nonce: String? = null): AuthResult<User> {
        return authRepository.loginWithGoogle(idToken, nonce)
    }

    /**
     * Login con Apple
     */
    suspend fun loginWithApple(identityToken: String, nonce: String? = null): AuthResult<User> {
        return authRepository.loginWithApple(identityToken, nonce)
    }

    /**
     * Obtiene el usuario actual desde el backend
     */
    suspend fun getCurrentUser(): AuthResult<User> {
        return authRepository.getCurrentUser()
    }

    /**
     * Actualiza el perfil del usuario
     */
    suspend fun updateUser(input: UpdateUserInput): AuthResult<User> {
        return authRepository.updateUser(input)
    }

    /**
     * Agrega una sucursal al usuario
     */
    suspend fun addBranchToUser(branchId: String): AuthResult<User> {
        return authRepository.addBranchToUser(branchId)
    }

    /**
     * Remueve una sucursal del usuario
     */
    suspend fun removeBranchFromUser(branchId: String): AuthResult<User> {
        return authRepository.removeBranchFromUser(branchId)
    }

    /**
     * Elimina la cuenta del usuario
     */
    suspend fun deleteUser(): AuthResult<Boolean> {
        return authRepository.deleteUser()
    }

    /**
     * Realiza el logout del usuario y limpia datos de negocio
     */
    suspend fun logout(): AuthResult<Unit> {
        businessRepository.clear()
        return authRepository.logout()
    }

    // ============= BUSINESS OPERATIONS =============

    /**
     * Registra un nuevo negocio con sus sucursales
     */
    suspend fun registerBusiness(
        business: CreateBusinessInput,
        branches: List<RegisterBranchInput>
    ): BusinessResult<Business> {
        return businessRepository.registerBusiness(business, branches)
    }

    /**
     * Obtiene todos los negocios del usuario actual
     */
    suspend fun getBusinesses(): BusinessResult<List<Business>> {
        return businessRepository.getBusinesses()
    }

    /**
     * Obtiene un negocio específico por ID
     */
    suspend fun getBusiness(id: String): BusinessResult<Business> {
        return businessRepository.getBusiness(id)
    }

    /**
     * Actualiza un negocio
     */
    suspend fun updateBusiness(
        businessId: String,
        input: UpdateBusinessInput
    ): BusinessResult<Business> {
        return businessRepository.updateBusiness(businessId, input)
    }

    // ============= BRANCH OPERATIONS =============

    /**
     * Obtiene todas las sucursales de un negocio
     * Si businessId es null, obtiene todas las sucursales del usuario
     */
    suspend fun getBranches(businessId: String? = null): BusinessResult<List<Branch>> {
        return businessRepository.getBranches(businessId)
    }

    /**
     * Obtiene una sucursal específica por ID
     */
    suspend fun getBranch(id: String): BusinessResult<Branch> {
        return businessRepository.getBranch(id)
    }

    /**
     * Crea una nueva sucursal
     */
    suspend fun createBranch(input: CreateBranchInput): BusinessResult<Branch> {
        return businessRepository.createBranch(input)
    }

    /**
     * Actualiza una sucursal
     */
    suspend fun updateBranch(
        branchId: String,
        input: UpdateBranchInput
    ): BusinessResult<Branch> {
        return businessRepository.updateBranch(branchId, input)
    }

    /**
     * Establece la sucursal actual
     */
    fun setCurrentBranch(branch: Branch) {
        businessRepository.setCurrentBranch(branch)
    }

    /**
     * Verifica si el usuario está autenticado (sincrónico)
     */
    fun isUserAuthenticated(): Boolean {
        return authRepository.isUserAuthenticated()
    }

    /**
     * Obtiene el usuario actual del estado (sincrónico)
     */
    fun getCurrentUserSync(): User? {
        return currentUser.value
    }

    // ============= HELPER METHODS =============

    /**
     * Obtiene el tipo de negocio actual desde el currentBusiness
     */
    fun getCurrentBusinessType(): BusinessType? {
        val business = currentBusiness.value ?: return null
        return when (business.type.lowercase()) {
            "restaurant" -> BusinessType.RESTAURANT
            "grocery", "market" -> BusinessType.MARKET
            "candy_store", "candy" -> BusinessType.CANDY_STORE
            else -> null
        }
    }

    /**
     * Obtiene el perfil de negocio del usuario actual
     * Combina datos de Business y Branch actual
     */
    fun getBusinessProfile(): BusinessProfile? {
        val business = currentBusiness.value ?: return null
        val branch = currentBranch.value

        return business.toBusinessProfile(branch)
    }

    /**
     * Obtiene el negocio actual sincrónico
     */
    fun getCurrentBusinessSync(): Business? {
        return currentBusiness.value
    }

    /**
     * Obtiene la sucursal actual sincrónico
     */
    fun getCurrentBranchSync(): Branch? {
        return currentBranch.value
    }

    companion object {
        private var INSTANCE: AuthManager? = null

        fun getInstance(tokenManager: TokenManager): AuthManager {
            // Simple singleton sin synchronized (no soportado en common)
            return INSTANCE ?: AuthManager(tokenManager).also { INSTANCE = it }
        }

        fun resetInstance() {
            INSTANCE = null
        }
    }
}

/**
 * Modelos de compatibilidad para código legacy
 */

// NOTE: BusinessProfile, OperatingHours, DaySchedule, and AuthUiState
// are now defined in com.llego.shared.data.model.AuthModels.kt
