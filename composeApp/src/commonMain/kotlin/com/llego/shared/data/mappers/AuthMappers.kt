package com.llego.shared.data.mappers

import com.llego.multiplatform.graphql.LoginMutation
import com.llego.multiplatform.graphql.LoginWithAppleMutation
import com.llego.multiplatform.graphql.LoginWithGoogleMutation
import com.llego.multiplatform.graphql.MeQuery
import com.llego.multiplatform.graphql.RegisterMutation
import com.llego.multiplatform.graphql.UpdateUserMutation
import com.llego.shared.data.model.User
import com.llego.shared.data.model.WalletBalance

/**
 * Extension functions para convertir tipos GraphQL a modelos de dominio
 */

// UserData (respuesta bÃ¡sica de login/register) a User bÃ¡sico
internal fun LoginMutation.User.toBasicDomain(): User {
    val fields = userAuthFields
    return User(
        id = fields.id,
        name = fields.name,
        email = fields.email,
        username = "", // Login no retorna username, se obtiene con query 'me'
        phone = fields.phone,
        role = fields.role,
        createdAt = fields.createdAt,
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

internal fun RegisterMutation.User.toBasicDomain(): User {
    val fields = userAuthFields
    return User(
        id = fields.id,
        name = fields.name,
        email = fields.email,
        username = "", // Register no retorna username, se obtiene con query 'me'
        phone = fields.phone,
        role = fields.role,
        createdAt = fields.createdAt,
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

internal fun LoginWithGoogleMutation.User.toBasicDomain(): User {
    val fields = userAuthFields
    return User(
        id = fields.id,
        name = fields.name,
        email = fields.email,
        username = "", // Google login no retorna username, se obtiene con query 'me'
        phone = fields.phone,
        role = fields.role,
        createdAt = fields.createdAt,
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

internal fun LoginWithAppleMutation.User.toBasicDomain(): User {
    val fields = userAuthFields
    return User(
        id = fields.id,
        name = fields.name,
        email = fields.email,
        username = "", // Apple login no retorna username, se obtiene con query 'me'
        phone = fields.phone,
        role = fields.role,
        createdAt = fields.createdAt,
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
internal fun MeQuery.Me.toDomain(): User {
    val fields = userCoreFields
    return User(
        id = fields.id,
        name = fields.name,
        email = fields.email,
        username = fields.username,
        phone = fields.phone,
        role = fields.role,
        avatar = fields.avatar,
        businessIds = fields.businessIds,
        branchIds = fields.branchIds,
        createdAt = fields.createdAt.toString(),
        authProvider = fields.authProvider,
        providerUserId = fields.providerUserId,
        applePrivateEmail = fields.applePrivateEmail,
        wallet = WalletBalance(
            local = fields.wallet.local,
            usd = fields.wallet.usd
        ),
        walletStatus = fields.walletStatus,
        avatarUrl = fields.avatarUrl
    )
}

// UpdateUser response a User (con campos limitados de la query)
internal fun UpdateUserMutation.UpdateUser.toDomain(): User {
    val fields = userUpdateFields
    return User(
        id = fields.id,
        name = fields.name,
        email = fields.email,
        username = fields.username,
        phone = fields.phone,
        role = fields.role,
        avatar = fields.avatar,
        businessIds = fields.businessIds,
        branchIds = fields.branchIds,
        wallet = WalletBalance(
            local = fields.wallet.local,
            usd = fields.wallet.usd
        ),
        walletStatus = fields.walletStatus,
        avatarUrl = fields.avatarUrl,
        // Campos que no vienen en UpdateUser response
        createdAt = "", // Se mantiene del usuario actual
        authProvider = "local", // Se mantiene del usuario actual
        providerUserId = null,
        applePrivateEmail = null
    )
}
