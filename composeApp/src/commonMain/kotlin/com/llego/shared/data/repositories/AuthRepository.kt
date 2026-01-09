package com.llego.shared.data.repositories

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloException
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.*
import com.llego.shared.data.network.GraphQLClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio de autenticación para las apps de Llego
 * Usa GraphQL para autenticación real con el backend
 * Maneja login, registro, logout y gestión de sesión de usuarios
 */
class AuthRepository(
    private val client: ApolloClient = GraphQLClient.apolloClient,
    private val tokenManager: TokenManager
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        // Intentar restaurar sesión al inicializar
        val savedToken = tokenManager.getToken()
        if (savedToken != null) {
            // La sesión se restaurará cuando se llame a getCurrentUser()
            _isAuthenticated.value = true
        }
    }

    /**
     * Realiza el login del usuario con email y password
     */
    suspend fun login(email: String, password: String): AuthResult<User> {
        return try {
            val response = client.mutation(
                com.llego.multiplatform.graphql.LoginMutation(
                    input = com.llego.multiplatform.graphql.type.LoginInput(
                        email = email,
                        password = password
                    )
                )
            ).execute()

            response.data?.login?.let { authResponse ->
                // Guardar token
                tokenManager.saveToken(authResponse.accessToken)

                // Convertir usuario GraphQL (UserData) a modelo local
                // Nota: UserData es básico, necesitamos llamar 'me' para datos completos
                val basicUser = authResponse.user.toBasicDomain()

                // Obtener datos completos del usuario
                val fullUserResult = getCurrentUser()
                val fullUser = when (fullUserResult) {
                    is AuthResult.Success -> fullUserResult.data
                    else -> basicUser // Fallback al básico si falla
                }

                // Actualizar estado
                _currentUser.value = fullUser
                _isAuthenticated.value = true

                AuthResult.Success(fullUser)
            } ?: AuthResult.Error("No se recibió respuesta del servidor")

        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexión con el servidor")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Registra un nuevo usuario
     */
    suspend fun register(input: RegisterInput): AuthResult<User> {
        return try {
            val response = client.mutation(
                com.llego.multiplatform.graphql.RegisterMutation(
                    input = com.llego.multiplatform.graphql.type.RegisterInput(
                        name = input.name,
                        email = input.email,
                        password = input.password,
                        phone = com.apollographql.apollo.api.Optional.presentIfNotNull(input.phone),
                        role = com.apollographql.apollo.api.Optional.presentIfNotNull(input.role)
                    )
                )
            ).execute()

            response.data?.register?.let { authResponse ->
                // Guardar token
                tokenManager.saveToken(authResponse.accessToken)

                // Convertir usuario
                val basicUser = authResponse.user.toBasicDomain()

                // Obtener datos completos
                val fullUserResult = getCurrentUser()
                val fullUser = when (fullUserResult) {
                    is AuthResult.Success -> fullUserResult.data
                    else -> basicUser
                }

                // Actualizar estado
                _currentUser.value = fullUser
                _isAuthenticated.value = true

                AuthResult.Success(fullUser)
            } ?: AuthResult.Error("No se recibió respuesta del servidor")

        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexión con el servidor")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Login con Google usando ID Token
     */
    suspend fun loginWithGoogle(idToken: String, nonce: String? = null): AuthResult<User> {
        return try {
            val currentToken = tokenManager.getToken()
            println("AuthRepository.loginWithGoogle: currentToken=${currentToken?.take(20)}...")

            val response = client.mutation(
                com.llego.multiplatform.graphql.LoginWithGoogleMutation(
                    input = com.llego.multiplatform.graphql.type.SocialLoginInput(
                        idToken = idToken,
                        nonce = com.apollographql.apollo.api.Optional.presentIfNotNull(nonce),
                        authorizationCode = com.apollographql.apollo.api.Optional.absent()
                    ),
                    jwt = com.apollographql.apollo.api.Optional.presentIfNotNull(currentToken)
                )
            ).execute()

            response.data?.loginWithGoogle?.let { authResponse ->
                println("AuthRepository.loginWithGoogle: recibida respuesta del backend")
                println("AuthRepository.loginWithGoogle: accessToken length=${authResponse.accessToken.length}")
                println("AuthRepository.loginWithGoogle: user.email=${authResponse.user.email}")
                println("AuthRepository.loginWithGoogle: user.id=${authResponse.user.id}")

                // Guardar nuevo token
                tokenManager.saveToken(authResponse.accessToken)

                // Convertir usuario
                val basicUser = authResponse.user.toBasicDomain()
                println("AuthRepository.loginWithGoogle: basicUser creado (sin businessIds aún)")

                // Obtener datos completos
                println("AuthRepository.loginWithGoogle: llamando getCurrentUser() para datos completos...")
                val fullUserResult = getCurrentUser()
                val fullUser = when (fullUserResult) {
                    is AuthResult.Success -> {
                        println("AuthRepository.loginWithGoogle: getCurrentUser() exitoso")
                        println("AuthRepository.loginWithGoogle: fullUser.businessIds=${fullUserResult.data.businessIds}")
                        println("AuthRepository.loginWithGoogle: fullUser.branchIds=${fullUserResult.data.branchIds}")
                        fullUserResult.data
                    }
                    else -> {
                        println("AuthRepository.loginWithGoogle: getCurrentUser() falló, usando basicUser")
                        basicUser
                    }
                }

                // Actualizar estado
                _currentUser.value = fullUser
                _isAuthenticated.value = true

                AuthResult.Success(fullUser)
            } ?: AuthResult.Error("No se recibió respuesta del servidor")

        } catch (e: ApolloException) {
            println("AuthRepository.loginWithGoogle: ApolloException - ${e.message}")
            AuthResult.Error(e.message ?: "Error de conexión con el servidor")
        } catch (e: Exception) {
            println("AuthRepository.loginWithGoogle: Exception - ${e.message}")
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Login con Apple usando Identity Token
     */
    suspend fun loginWithApple(identityToken: String, nonce: String? = null): AuthResult<User> {
        return try {
            println("AuthRepository.loginWithApple: iniciando con token length=${identityToken.length}")
            println("AuthRepository.loginWithApple: nonce=$nonce")
            
            val response = client.mutation(
                com.llego.multiplatform.graphql.LoginWithAppleMutation(
                    input = com.llego.multiplatform.graphql.type.AppleLoginInput(
                        identityToken = identityToken,
                        authorizationCode = com.apollographql.apollo.api.Optional.absent(),
                        nonce = com.apollographql.apollo.api.Optional.presentIfNotNull(nonce)
                    )
                )
            ).execute()

            println("AuthRepository.loginWithApple: respuesta recibida, data=${response.data != null}")
            println("AuthRepository.loginWithApple: errors=${response.errors}")

            response.data?.loginWithApple?.let { authResponse ->
                println("AuthRepository.loginWithApple: recibida respuesta del backend")
                println("AuthRepository.loginWithApple: accessToken length=${authResponse.accessToken.length}")
                println("AuthRepository.loginWithApple: user.email=${authResponse.user.email}")
                println("AuthRepository.loginWithApple: user.id=${authResponse.user.id}")
                
                // Guardar nuevo token
                tokenManager.saveToken(authResponse.accessToken)

                // Convertir usuario
                val basicUser = authResponse.user.toBasicDomain()
                println("AuthRepository.loginWithApple: basicUser creado")

                // Obtener datos completos
                println("AuthRepository.loginWithApple: llamando getCurrentUser() para datos completos...")
                val fullUserResult = getCurrentUser()
                val fullUser = when (fullUserResult) {
                    is AuthResult.Success -> {
                        println("AuthRepository.loginWithApple: getCurrentUser() exitoso")
                        println("AuthRepository.loginWithApple: fullUser.businessIds=${fullUserResult.data.businessIds}")
                        println("AuthRepository.loginWithApple: fullUser.branchIds=${fullUserResult.data.branchIds}")
                        fullUserResult.data
                    }
                    else -> {
                        println("AuthRepository.loginWithApple: getCurrentUser() falló, usando basicUser")
                        basicUser
                    }
                }

                // Actualizar estado
                _currentUser.value = fullUser
                _isAuthenticated.value = true
                
                println("AuthRepository.loginWithApple: éxito - usuario autenticado")

                AuthResult.Success(fullUser)
            } ?: run {
                println("AuthRepository.loginWithApple: response.data.loginWithApple es null")
                AuthResult.Error("No se recibió respuesta del servidor")
            }

        } catch (e: ApolloException) {
            println("AuthRepository.loginWithApple: ApolloException - ${e.message}")
            e.printStackTrace()
            AuthResult.Error(e.message ?: "Error de conexión con el servidor")
        } catch (e: Exception) {
            println("AuthRepository.loginWithApple: Exception - ${e.message}")
            e.printStackTrace()
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Obtiene el usuario actual desde el backend (query 'me')
     */
    suspend fun getCurrentUser(): AuthResult<User> {
        val token = tokenManager.getToken()
        if (token == null) {
            println("AuthRepository.getCurrentUser: No hay token guardado")
            return AuthResult.Error("No hay sesión activa")
        }

        println("AuthRepository.getCurrentUser: llamando query 'me' con token...")

        return try {
            val response = client.query(
                com.llego.multiplatform.graphql.MeQuery(jwt = token)
            ).execute()

            println("AuthRepository.getCurrentUser: respuesta recibida, data=${response.data != null}")
            println("AuthRepository.getCurrentUser: errors=${response.errors}")

            response.data?.me?.let { userResponse ->
                println("AuthRepository.getCurrentUser: userResponse.id=${userResponse.id}")
                println("AuthRepository.getCurrentUser: userResponse.email=${userResponse.email}")
                println("AuthRepository.getCurrentUser: userResponse.businessIds=${userResponse.businessIds}")
                println("AuthRepository.getCurrentUser: userResponse.branchIds=${userResponse.branchIds}")
                println("AuthRepository.getCurrentUser: userResponse.authProvider=${userResponse.authProvider}")

                val user = userResponse.toDomain()

                println("AuthRepository.getCurrentUser: user convertido, businessIds=${user.businessIds}")

                // Actualizar estado
                _currentUser.value = user
                _isAuthenticated.value = true

                AuthResult.Success(user)
            } ?: run {
                println("AuthRepository.getCurrentUser: response.data.me es null")
                AuthResult.Error("No se pudo obtener el usuario")
            }

        } catch (e: ApolloException) {
            println("AuthRepository.getCurrentUser: ApolloException - ${e.message}")
            // Token inválido o expirado
            tokenManager.clearToken()
            _currentUser.value = null
            _isAuthenticated.value = false

            AuthResult.Error("Sesión expirada")
        } catch (e: Exception) {
            println("AuthRepository.getCurrentUser: Exception - ${e.message}")
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Actualiza el perfil del usuario
     */
    suspend fun updateUser(input: UpdateUserInput): AuthResult<User> {
        val token = tokenManager.getToken()
            ?: return AuthResult.Error("No hay sesión activa")

        return try {
            val response = client.mutation(
                com.llego.multiplatform.graphql.UpdateUserMutation(
                    input = com.llego.multiplatform.graphql.type.UpdateUserInput(
                        name = com.apollographql.apollo.api.Optional.presentIfNotNull(input.name),
                        phone = com.apollographql.apollo.api.Optional.presentIfNotNull(input.phone),
                        avatar = com.apollographql.apollo.api.Optional.presentIfNotNull(input.avatar)
                    ),
                    jwt = token
                )
            ).execute()

            response.data?.updateUser?.let { userResponse ->
                val user = userResponse.toDomain()

                // Actualizar estado
                _currentUser.value = user

                AuthResult.Success(user)
            } ?: AuthResult.Error("No se pudo actualizar el usuario")

        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexión")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Agregar una sucursal al usuario
     */
    suspend fun addBranchToUser(branchId: String): AuthResult<User> {
        val token = tokenManager.getToken()
            ?: return AuthResult.Error("No hay sesión activa")

        return try {
            val response = client.mutation(
                com.llego.multiplatform.graphql.AddBranchToUserMutation(
                    input = com.llego.multiplatform.graphql.type.AddBranchToUserInput(
                        branchId = branchId
                    ),
                    jwt = token
                )
            ).execute()

            response.data?.addBranchToUser?.let { userResponse ->
                // Actualizar solo branchIds en el usuario actual
                _currentUser.value = _currentUser.value?.copy(
                    branchIds = userResponse.branchIds
                )

                // Devolver usuario actualizado
                _currentUser.value?.let { AuthResult.Success(it) }
                    ?: AuthResult.Error("Usuario no encontrado")
            } ?: AuthResult.Error("No se pudo agregar la sucursal")

        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexión")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Remover una sucursal del usuario
     */
    suspend fun removeBranchFromUser(branchId: String): AuthResult<User> {
        val token = tokenManager.getToken()
            ?: return AuthResult.Error("No hay sesión activa")

        return try {
            val response = client.mutation(
                com.llego.multiplatform.graphql.RemoveBranchFromUserMutation(
                    branchId = branchId,
                    jwt = token
                )
            ).execute()

            response.data?.removeBranchFromUser?.let { userResponse ->
                // Actualizar solo branchIds
                _currentUser.value = _currentUser.value?.copy(
                    branchIds = userResponse.branchIds
                )

                _currentUser.value?.let { AuthResult.Success(it) }
                    ?: AuthResult.Error("Usuario no encontrado")
            } ?: AuthResult.Error("No se pudo remover la sucursal")

        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexión")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Elimina la cuenta del usuario
     */
    suspend fun deleteUser(): AuthResult<Boolean> {
        val token = tokenManager.getToken()
            ?: return AuthResult.Error("No hay sesión activa")

        return try {
            val response = client.mutation(
                com.llego.multiplatform.graphql.DeleteUserMutation(jwt = token)
            ).execute()

            val success = response.data?.deleteUser ?: false

            if (success) {
                // Limpiar todo
                logout()
                AuthResult.Success(true)
            } else {
                AuthResult.Error("No se pudo eliminar la cuenta")
            }

        } catch (e: ApolloException) {
            AuthResult.Error(e.message ?: "Error de conexión")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Realiza el logout del usuario
     */
    suspend fun logout(): AuthResult<Unit> {
        return try {
            // Limpiar tokens
            tokenManager.clearAll()

            // Limpiar estado
            _currentUser.value = null
            _isAuthenticated.value = false

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error al cerrar sesión")
        }
    }

    /**
     * Verifica si el usuario está autenticado
     */
    fun isUserAuthenticated(): Boolean {
        return _isAuthenticated.value && tokenManager.getToken() != null
    }

    /**
     * Obtiene el BusinessType del primer negocio del usuario
     * Nota: Esto requerirá consultar el negocio cuando se implemente BusinessRepository
     */
    fun getCurrentBusinessType(): BusinessType? {
        // TODO: Implementar cuando tengamos BusinessRepository
        // Por ahora retornamos null, ya que businessIds son solo IDs
        return null
    }
}

/**
 * Extension functions para convertir tipos GraphQL a modelos de dominio
 */

// UserData (respuesta básica de login/register) a User básico
private fun com.llego.multiplatform.graphql.LoginMutation.User.toBasicDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
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
        avatarUrl = null
    )
}

private fun com.llego.multiplatform.graphql.RegisterMutation.User.toBasicDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        role = role,
        createdAt = createdAt,
        avatar = null,
        businessIds = emptyList(),
        branchIds = emptyList(),
        authProvider = "local",
        providerUserId = null,
        applePrivateEmail = null,
        avatarUrl = null
    )
}

private fun com.llego.multiplatform.graphql.LoginWithGoogleMutation.User.toBasicDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        role = role,
        createdAt = createdAt,
        avatar = null,
        businessIds = emptyList(),
        branchIds = emptyList(),
        authProvider = "google",
        providerUserId = null,
        applePrivateEmail = null,
        avatarUrl = null
    )
}

private fun com.llego.multiplatform.graphql.LoginWithAppleMutation.User.toBasicDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        role = role,
        createdAt = createdAt,
        avatar = null,
        businessIds = emptyList(),
        branchIds = emptyList(),
        authProvider = "apple",
        providerUserId = null,
        applePrivateEmail = null,
        avatarUrl = null
    )
}

// UserType (respuesta completa de 'me') a User completo
private fun com.llego.multiplatform.graphql.MeQuery.Me.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        role = role,
        avatar = avatar,
        businessIds = businessIds,
        branchIds = branchIds,
        createdAt = createdAt.toString(),
        authProvider = authProvider,
        providerUserId = providerUserId,
        applePrivateEmail = applePrivateEmail,
        avatarUrl = avatarUrl
    )
}

// UpdateUser response a User (con campos limitados de la query)
private fun com.llego.multiplatform.graphql.UpdateUserMutation.UpdateUser.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        role = role,
        avatar = avatar,
        businessIds = businessIds,
        branchIds = branchIds,
        avatarUrl = avatarUrl,
        // Campos que no vienen en UpdateUser response
        createdAt = "", // Se mantiene del usuario actual
        authProvider = "local", // Se mantiene del usuario actual
        providerUserId = null,
        applePrivateEmail = null
    )
}
