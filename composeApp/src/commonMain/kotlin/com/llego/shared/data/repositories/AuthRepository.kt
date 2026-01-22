package com.llego.shared.data.repositories

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloException
import com.llego.multiplatform.graphql.*
import com.llego.multiplatform.graphql.type.AddBranchToUserInput as GQLAddBranchToUserInput
import com.llego.multiplatform.graphql.type.AppleLoginInput as GQLAppleLoginInput
import com.llego.multiplatform.graphql.type.LoginInput as GQLLoginInput
import com.llego.multiplatform.graphql.type.RegisterInput as GQLRegisterInput
import com.llego.multiplatform.graphql.type.SocialLoginInput as GQLSocialLoginInput
import com.llego.multiplatform.graphql.type.UpdateUserInput as GQLUpdateUserInput
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.*
import com.llego.shared.data.network.GraphQLClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


/**
 * Extension functions para convertir tipos GraphQL a modelos de dominio
 */

// UserData (respuesta básica de login/register) a User básico
private fun com.llego.multiplatform.graphql.LoginMutation.User.toBasicDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        username = "", // Login no retorna username, se obtiene con query 'me'
        phone = phone,
        role = role,
        createdAt = createdAt,
        // Campos que requieren query 'me'
        avatar = null,
        businessIds = emptyList(),
        branchIds = emptyList(),
        authProvider = "local",
        providerUserId = null,
        applePrivateEmail = null,
        wallet = WalletBalance(local = 0.0, usd = 0.0), // Placeholder, se obtiene con 'me'
        walletStatus = "active",
        avatarUrl = null
    )
}

private fun com.llego.multiplatform.graphql.RegisterMutation.User.toBasicDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        username = "", // Register no retorna username, se obtiene con query 'me'
        phone = phone,
        role = role,
        createdAt = createdAt,
        avatar = null,
        businessIds = emptyList(),
        branchIds = emptyList(),
        authProvider = "local",
        providerUserId = null,
        applePrivateEmail = null,
        wallet = WalletBalance(local = 0.0, usd = 0.0), // Placeholder, se obtiene con 'me'
        walletStatus = "active",
        avatarUrl = null
    )
}

private fun com.llego.multiplatform.graphql.LoginWithGoogleMutation.User.toBasicDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        username = "", // Google login no retorna username, se obtiene con query 'me'
        phone = phone,
        role = role,
        createdAt = createdAt,
        avatar = null,
        businessIds = emptyList(),
        branchIds = emptyList(),
        authProvider = "google",
        providerUserId = null,
        applePrivateEmail = null,
        wallet = WalletBalance(local = 0.0, usd = 0.0), // Placeholder, se obtiene con 'me'
        walletStatus = "active",
        avatarUrl = null
    )
}

private fun com.llego.multiplatform.graphql.LoginWithAppleMutation.User.toBasicDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        username = "", // Apple login no retorna username, se obtiene con query 'me'
        phone = phone,
        role = role,
        createdAt = createdAt,
        avatar = null,
        businessIds = emptyList(),
        branchIds = emptyList(),
        authProvider = "apple",
        providerUserId = null,
        applePrivateEmail = null,
        wallet = WalletBalance(local = 0.0, usd = 0.0), // Placeholder, se obtiene con 'me'
        walletStatus = "active",
        avatarUrl = null
    )
}

// UserType (respuesta completa de 'me') a User completo
private fun com.llego.multiplatform.graphql.MeQuery.Me.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        username = username,
        phone = phone,
        role = role,
        avatar = avatar,
        businessIds = businessIds,
        branchIds = branchIds,
        createdAt = createdAt.toString(),
        authProvider = authProvider,
        providerUserId = providerUserId,
        applePrivateEmail = applePrivateEmail,
        wallet = WalletBalance(
            local = wallet.local,
            usd = wallet.usd
        ),
        walletStatus = walletStatus,
        avatarUrl = avatarUrl
    )
}

// UpdateUser response a User (con campos limitados de la query)
private fun com.llego.multiplatform.graphql.UpdateUserMutation.UpdateUser.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        username = username,
        phone = phone,
        role = role,
        avatar = avatar,
        businessIds = businessIds,
        branchIds = branchIds,
        wallet = WalletBalance(
            local = wallet.local,
            usd = wallet.usd
        ),
        walletStatus = walletStatus,
        avatarUrl = avatarUrl,
        // Campos que no vienen en UpdateUser response
        createdAt = "", // Se mantiene del usuario actual
        authProvider = "local", // Se mantiene del usuario actual
        providerUserId = null,
        applePrivateEmail = null
    )
}

/**
 * Repository para autenticacion de usuarios.
 * Maneja login, registro y operaciones de usuario via GraphQL.
 */
class AuthRepository(
    private val client: ApolloClient = GraphQLClient.apolloClient,
    private val tokenManager: TokenManager
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    /**
     * Login con email/password.
     */
    suspend fun login(email: String, password: String): AuthResult<User> {
        return try {
            val response = client.mutation(
                LoginMutation(
                    input = GQLLoginInput(
                        email = email,
                        password = password
                    )
                )
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors?.firstOrNull()?.message ?: "Error de autenticacion"
                return AuthResult.Error(message, "GRAPHQL_ERROR")
            }

            val authResponse = response.data?.login
                ?: return AuthResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")

            tokenManager.saveToken(authResponse.accessToken)
            val user = authResponse.user.toBasicDomain()
            _currentUser.value = user
            _isAuthenticated.value = true
            AuthResult.Success(user)
        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexion", "APOLLO_ERROR")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado", "UNKNOWN_ERROR")
        }
    }

    /**
     * Registro de nuevo usuario.
     */
    suspend fun register(input: RegisterInput): AuthResult<User> {
        return try {
            val response = client.mutation(
                RegisterMutation(
                    input = GQLRegisterInput(
                        name = input.name,
                        email = input.email,
                        password = input.password,
                        phone = Optional.presentIfNotNull(input.phone)
                    )
                )
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors?.firstOrNull()?.message ?: "Error en registro"
                return AuthResult.Error(message, "GRAPHQL_ERROR")
            }

            val authResponse = response.data?.register
                ?: return AuthResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")

            tokenManager.saveToken(authResponse.accessToken)
            val user = authResponse.user.toBasicDomain()
            _currentUser.value = user
            _isAuthenticated.value = true
            AuthResult.Success(user)
        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexion", "APOLLO_ERROR")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado", "UNKNOWN_ERROR")
        }
    }

    /**
     * Login con Google.
     */
    suspend fun loginWithGoogle(idToken: String, nonce: String? = null): AuthResult<User> {
        return try {
            val response = client.mutation(
                LoginWithGoogleMutation(
                    input = GQLSocialLoginInput(
                        idToken = idToken,
                        nonce = Optional.presentIfNotNull(nonce)
                    ),
                    jwt = Optional.presentIfNotNull(tokenManager.getToken())
                )
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors?.firstOrNull()?.message ?: "Error en login con Google"
                return AuthResult.Error(message, "GRAPHQL_ERROR")
            }

            val authResponse = response.data?.loginWithGoogle
                ?: return AuthResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")

            tokenManager.saveToken(authResponse.accessToken)
            val user = authResponse.user.toBasicDomain()
            _currentUser.value = user
            _isAuthenticated.value = true
            AuthResult.Success(user)
        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexion", "APOLLO_ERROR")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado", "UNKNOWN_ERROR")
        }
    }

    /**
     * Login con Apple.
     */
    suspend fun loginWithApple(identityToken: String, nonce: String? = null): AuthResult<User> {
        return try {
            val response = client.mutation(
                LoginWithAppleMutation(
                    input = GQLAppleLoginInput(
                        identityToken = identityToken,
                        nonce = Optional.presentIfNotNull(nonce)
                    )
                )
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors?.firstOrNull()?.message ?: "Error en login con Apple"
                return AuthResult.Error(message, "GRAPHQL_ERROR")
            }

            val authResponse = response.data?.loginWithApple
                ?: return AuthResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")

            tokenManager.saveToken(authResponse.accessToken)
            val user = authResponse.user.toBasicDomain()
            _currentUser.value = user
            _isAuthenticated.value = true
            AuthResult.Success(user)
        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexion", "APOLLO_ERROR")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado", "UNKNOWN_ERROR")
        }
    }

    /**
     * Autenticacion directa con un token JWT ya validado.
     */
    suspend fun authenticateWithToken(token: String): AuthResult<User> {
        tokenManager.saveToken(token)
        return getCurrentUser()
    }

    /**
     * Obtiene el usuario actual desde el backend.
     */
    suspend fun getCurrentUser(): AuthResult<User> {
        val token = tokenManager.getToken()
            ?: return AuthResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.query(
                MeQuery(jwt = token)
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors?.firstOrNull()?.message ?: "Error al obtener usuario"
                return AuthResult.Error(message, "GRAPHQL_ERROR")
            }

            val user = response.data?.me?.toDomain()
                ?: return AuthResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")

            _currentUser.value = user
            _isAuthenticated.value = true
            AuthResult.Success(user)
        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexion", "APOLLO_ERROR")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado", "UNKNOWN_ERROR")
        }
    }

    /**
     * Actualiza el perfil del usuario.
     */
    suspend fun updateUser(input: UpdateUserInput): AuthResult<User> {
        val token = tokenManager.getToken()
            ?: return AuthResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                UpdateUserMutation(
                    input = GQLUpdateUserInput(
                        name = Optional.presentIfNotNull(input.name),
                        username = Optional.presentIfNotNull(input.username),
                        phone = Optional.presentIfNotNull(input.phone),
                        avatar = Optional.presentIfNotNull(input.avatar)
                    ),
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors?.firstOrNull()?.message ?: "Error al actualizar usuario"
                return AuthResult.Error(message, "GRAPHQL_ERROR")
            }

            val user = response.data?.updateUser?.toDomain()
                ?: return AuthResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")

            _currentUser.value = mergeUser(user)
            AuthResult.Success(_currentUser.value ?: user)
        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexion", "APOLLO_ERROR")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado", "UNKNOWN_ERROR")
        }
    }

    /**
     * Agrega una sucursal al usuario actual.
     */
    suspend fun addBranchToUser(branchId: String): AuthResult<User> {
        val token = tokenManager.getToken()
            ?: return AuthResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                AddBranchToUserMutation(
                    input = GQLAddBranchToUserInput(branchId = branchId),
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors?.firstOrNull()?.message ?: "Error al agregar sucursal"
                return AuthResult.Error(message, "GRAPHQL_ERROR")
            }

            val updated = response.data?.addBranchToUser
                ?: return AuthResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")

            val current = _currentUser.value
            if (current != null) {
                val newUser = current.copy(branchIds = updated.branchIds)
                _currentUser.value = newUser
                return AuthResult.Success(newUser)
            }

            return getCurrentUser()
        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexion", "APOLLO_ERROR")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado", "UNKNOWN_ERROR")
        }
    }

    /**
     * Remueve una sucursal del usuario actual.
     */
    suspend fun removeBranchFromUser(branchId: String): AuthResult<User> {
        val token = tokenManager.getToken()
            ?: return AuthResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                RemoveBranchFromUserMutation(
                    branchId = branchId,
                    jwt = token
                )
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors?.firstOrNull()?.message ?: "Error al remover sucursal"
                return AuthResult.Error(message, "GRAPHQL_ERROR")
            }

            val updated = response.data?.removeBranchFromUser
                ?: return AuthResult.Error("No se recibio respuesta del servidor", "EMPTY_RESPONSE")

            val current = _currentUser.value
            if (current != null) {
                val newUser = current.copy(branchIds = updated.branchIds)
                _currentUser.value = newUser
                return AuthResult.Success(newUser)
            }

            return getCurrentUser()
        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexion", "APOLLO_ERROR")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado", "UNKNOWN_ERROR")
        }
    }

    /**
     * Elimina la cuenta del usuario actual.
     */
    suspend fun deleteUser(): AuthResult<Boolean> {
        val token = tokenManager.getToken()
            ?: return AuthResult.Error("No hay sesion activa", "NO_TOKEN")

        return try {
            val response = client.mutation(
                DeleteUserMutation(jwt = token)
            ).execute()

            if (response.hasErrors()) {
                val message = response.errors?.firstOrNull()?.message ?: "Error al eliminar usuario"
                return AuthResult.Error(message, "GRAPHQL_ERROR")
            }

            val deleted = response.data?.deleteUser == true
            if (deleted) {
                tokenManager.clearAll()
                _currentUser.value = null
                _isAuthenticated.value = false
            }
            AuthResult.Success(deleted)
        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexion", "APOLLO_ERROR")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error inesperado", "UNKNOWN_ERROR")
        }
    }

    /**
     * Logout del usuario.
     */
    suspend fun logout(): AuthResult<Unit> {
        tokenManager.clearAll()
        _currentUser.value = null
        _isAuthenticated.value = false
        return AuthResult.Success(Unit)
    }

    /**
     * Verifica autenticacion sincronica.
     */
    fun isUserAuthenticated(): Boolean {
        return tokenManager.getToken() != null
    }

    private fun mergeUser(updated: User): User {
        val current = _currentUser.value
        if (current == null) return updated
        return current.copy(
            name = updated.name,
            email = updated.email,
            username = updated.username,
            phone = updated.phone,
            avatar = updated.avatar,
            businessIds = updated.businessIds,
            branchIds = updated.branchIds,
            wallet = updated.wallet,
            walletStatus = updated.walletStatus,
            avatarUrl = updated.avatarUrl
        )
    }
}
