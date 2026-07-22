package com.llego.shared.data.auth

/**
 * Guarda el id del usuario dueño de la sesión actual (lo que hay en memoria post-login).
 *
 * Se usa como guard en los resolvers que restauran `currentBranch` / `currentBusiness`
 * desde estado en memoria o desde el `last_branch_id` persistido: si un branch
 * "recordado" no aparece en la lista que trae el backend para el usuario actual, no
 * se restaura. Es una defensa en profundidad contra fugas de estado entre sesiones
 * cuando el logout deja algún singleton sin limpiar.
 */
object SessionScope {
    var currentUserId: String? = null
        private set

    fun setCurrentUser(userId: String?) {
        currentUserId = userId
    }

    fun clear() {
        currentUserId = null
    }
}
