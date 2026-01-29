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

internal fun RegisterMutation.User.toBasicDomain(): User {
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

internal fun LoginWithGoogleMutation.User.toBasicDomain(): User {
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

internal fun LoginWithAppleMutation.User.toBasicDomain(): User {
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
internal fun MeQuery.Me.toDomain(): User {
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
internal fun UpdateUserMutation.UpdateUser.toDomain(): User {
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
