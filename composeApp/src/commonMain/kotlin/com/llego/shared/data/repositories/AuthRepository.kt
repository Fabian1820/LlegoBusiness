package com.llego.shared.data.repositories

import com.llego.shared.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio de autenticación para las apps de Llego
 * Maneja login, logout y gestión de sesión de usuarios
 * Por ahora usa datos mock, se integrará con GraphQL posteriormente
 */

class AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _authToken = MutableStateFlow<AuthToken?>(null)
    val authToken: StateFlow<AuthToken?> = _authToken.asStateFlow()

    private var tokenCounter = 1000L

    /**
     * Realiza el login del usuario
     *
     * USUARIOS DE PRUEBA RÁPIDOS:
     * - Restaurante: email="r" password="r"
     * - Mercado: email="m" password="m"
     * - Agromercado: email="a" password="a"
     * - Tienda Ropa: email="t" password="t"
     * - Farmacia: email="f" password="f"
     */
    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> {
        return try {
            // Simular llamada de red
            delay(1500)

            // Validar credenciales mock
            val mockUser = getMockUser(loginRequest.email, loginRequest.businessType)

            if (mockUser != null && isValidPassword(loginRequest.password)) {
                val token = AuthToken(
                    accessToken = "mock_access_token_${tokenCounter++}",
                    refreshToken = "mock_refresh_token_${tokenCounter++}",
                    expiresIn = 3600
                )

                _authToken.value = token
                _currentUser.value = mockUser
                _isAuthenticated.value = true

                val response = LoginResponse(
                    success = true,
                    message = "Login exitoso",
                    token = token.accessToken,
                    user = mockUser
                )

                Result.success(response)
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Realiza el logout del usuario
     */
    suspend fun logout(): Result<Unit> {
        return try {
            // Simular llamada de red para invalidar token
            delay(500)

            _authToken.value = null
            _currentUser.value = null
            _isAuthenticated.value = false

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica si el token actual es válido
     */
    suspend fun validateToken(): Result<Boolean> {
        return try {
            val token = _authToken.value
            if (token != null) {
                // Simular validación de token
                delay(800)

                // Mock: considerar válido si existe
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refresca el token de autenticación
     */
    suspend fun refreshToken(): Result<AuthToken> {
        return try {
            val currentToken = _authToken.value?.refreshToken
            if (currentToken != null) {
                delay(1000)

                val newToken = AuthToken(
                    accessToken = "refreshed_access_token_${tokenCounter++}",
                    refreshToken = "refreshed_refresh_token_${tokenCounter++}",
                    expiresIn = 3600
                )

                _authToken.value = newToken
                Result.success(newToken)
            } else {
                Result.failure(Exception("No hay refresh token disponible"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el usuario actual
     */
    fun getCurrentUser(): User? {
        return _currentUser.value
    }

    /**
     * Verifica si el usuario está autenticado
     */
    fun isUserAuthenticated(): Boolean {
        return _isAuthenticated.value && _currentUser.value != null
    }

    /**
     * Obtiene el tipo de negocio actual
     */
    fun getCurrentBusinessType(): BusinessType? {
        return _currentUser.value?.businessType
    }

    // Funciones mock para testing

    private fun getMockUser(email: String, businessType: BusinessType): User? {
        return when (businessType) {
            BusinessType.RESTAURANT -> getMockRestaurantUser(email)
            BusinessType.MARKET -> getMockMarketUser(email)
            BusinessType.AGROMARKET -> getMockAgromarketUser(email)
            BusinessType.CLOTHING_STORE -> getMockClothingStoreUser(email)
            BusinessType.PHARMACY -> getMockPharmacyUser(email)
        }
    }

    private fun getMockRestaurantUser(email: String): User {
        return User(
            id = "restaurant_001",
            email = email,
            name = "Restaurante La Havana",
            phone = "+53 5555-1234",
            businessType = BusinessType.RESTAURANT,
            profileImage = null,
            isActive = true,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            businessProfile = BusinessProfile(
                businessId = "restaurant_001",
                businessName = "Restaurante La Havana",
                businessType = BusinessType.RESTAURANT,
                address = "Calle 23 entre L y M",
                city = "La Habana",
                state = "La Habana",
                zipCode = "10400",
                businessPhone = "+53 7831-2345",
                description = "Comida cubana tradicional con el mejor sabor de La Habana",
                isVerified = true,
                operatingHours = OperatingHours(
                    monday = DaySchedule(true, "09:00", "22:00"),
                    tuesday = DaySchedule(true, "09:00", "22:00"),
                    wednesday = DaySchedule(true, "09:00", "22:00"),
                    thursday = DaySchedule(true, "09:00", "22:00"),
                    friday = DaySchedule(true, "09:00", "23:00"),
                    saturday = DaySchedule(true, "09:00", "23:00"),
                    sunday = DaySchedule(true, "10:00", "21:00")
                ),
                deliveryRadius = 8.0,
                averageRating = 4.5,
                totalOrders = 1250
            )
        )
    }

    private fun getMockMarketUser(email: String): User {
        return User(
            id = "market_001",
            email = email,
            name = "Mercado El Ahorro",
            phone = "+53 5555-5678",
            businessType = BusinessType.MARKET,
            profileImage = null,
            isActive = true,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            businessProfile = BusinessProfile(
                businessId = "market_001",
                businessName = "Mercado El Ahorro",
                businessType = BusinessType.MARKET,
                address = "Avenida Central 123",
                city = "La Habana",
                state = "La Habana",
                zipCode = "10500",
                businessPhone = "+53 7831-5678",
                description = "Tu mercado de confianza con los mejores productos",
                isVerified = true,
                operatingHours = OperatingHours(
                    monday = DaySchedule(true, "08:00", "20:00"),
                    tuesday = DaySchedule(true, "08:00", "20:00"),
                    wednesday = DaySchedule(true, "08:00", "20:00"),
                    thursday = DaySchedule(true, "08:00", "20:00"),
                    friday = DaySchedule(true, "08:00", "21:00"),
                    saturday = DaySchedule(true, "08:00", "21:00"),
                    sunday = DaySchedule(true, "09:00", "18:00")
                ),
                deliveryRadius = 10.0,
                averageRating = 4.7,
                totalOrders = 2500
            )
        )
    }

    private fun getMockAgromarketUser(email: String): User {
        return User(
            id = "agromarket_001",
            email = email,
            name = "Agromercado La Finca",
            phone = "+53 5555-7890",
            businessType = BusinessType.AGROMARKET,
            profileImage = null,
            isActive = true,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            businessProfile = BusinessProfile(
                businessId = "agromarket_001",
                businessName = "Agromercado La Finca",
                businessType = BusinessType.AGROMARKET,
                address = "Carretera Central Km 5",
                city = "La Habana",
                state = "La Habana",
                zipCode = "10700",
                businessPhone = "+53 7831-7890",
                description = "Productos frescos directo del campo",
                isVerified = true,
                operatingHours = OperatingHours(
                    monday = DaySchedule(true, "07:00", "19:00"),
                    tuesday = DaySchedule(true, "07:00", "19:00"),
                    wednesday = DaySchedule(true, "07:00", "19:00"),
                    thursday = DaySchedule(true, "07:00", "19:00"),
                    friday = DaySchedule(true, "07:00", "19:00"),
                    saturday = DaySchedule(true, "07:00", "20:00"),
                    sunday = DaySchedule(true, "08:00", "15:00")
                ),
                deliveryRadius = 12.0,
                averageRating = 4.6,
                totalOrders = 1800
            )
        )
    }

    private fun getMockClothingStoreUser(email: String): User {
        return User(
            id = "clothing_001",
            email = email,
            name = "Tienda Moda Actual",
            phone = "+53 5555-3456",
            businessType = BusinessType.CLOTHING_STORE,
            profileImage = null,
            isActive = true,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            businessProfile = BusinessProfile(
                businessId = "clothing_001",
                businessName = "Tienda Moda Actual",
                businessType = BusinessType.CLOTHING_STORE,
                address = "Calle Obispo 234",
                city = "La Habana",
                state = "La Habana",
                zipCode = "10800",
                businessPhone = "+53 7831-3456",
                description = "Las últimas tendencias en moda y estilo",
                isVerified = true,
                operatingHours = OperatingHours(
                    monday = DaySchedule(true, "10:00", "20:00"),
                    tuesday = DaySchedule(true, "10:00", "20:00"),
                    wednesday = DaySchedule(true, "10:00", "20:00"),
                    thursday = DaySchedule(true, "10:00", "20:00"),
                    friday = DaySchedule(true, "10:00", "21:00"),
                    saturday = DaySchedule(true, "10:00", "21:00"),
                    sunday = DaySchedule(true, "11:00", "18:00")
                ),
                deliveryRadius = 7.0,
                averageRating = 4.8,
                totalOrders = 950
            )
        )
    }

    private fun getMockPharmacyUser(email: String): User {
        return User(
            id = "pharmacy_001",
            email = email,
            name = "Farmacia San José",
            phone = "+53 5555-9012",
            businessType = BusinessType.PHARMACY,
            profileImage = null,
            isActive = true,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            businessProfile = BusinessProfile(
                businessId = "pharmacy_001",
                businessName = "Farmacia San José",
                businessType = BusinessType.PHARMACY,
                address = "Calle Real 456",
                city = "La Habana",
                state = "La Habana",
                zipCode = "10600",
                businessPhone = "+53 7831-9012",
                description = "Farmacia con servicio 24/7 y atención especializada",
                isVerified = true,
                operatingHours = OperatingHours(
                    monday = DaySchedule(true, "00:00", "23:59"),
                    tuesday = DaySchedule(true, "00:00", "23:59"),
                    wednesday = DaySchedule(true, "00:00", "23:59"),
                    thursday = DaySchedule(true, "00:00", "23:59"),
                    friday = DaySchedule(true, "00:00", "23:59"),
                    saturday = DaySchedule(true, "00:00", "23:59"),
                    sunday = DaySchedule(true, "00:00", "23:59")
                ),
                deliveryRadius = 15.0,
                averageRating = 4.9,
                totalOrders = 3200
            )
        )
    }

    private fun isValidPassword(password: String): Boolean {
        // Mock: acepta cualquier password con al menos 1 carácter (para testing rápido)
        return password.isNotEmpty()
    }
}