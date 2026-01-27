package com.llego.shared.utils

import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business

/**
 * Helper para validaciÃ³n de permisos en el sistema multi-negocio
 */
object PermissionsHelper {

    /**
     * Solo el propietario puede crear sucursales
     */
    fun canCreateBranch(business: Business, userId: String): Boolean {
        return business.ownerId == userId
    }

    /**
     * Puede editar si:
     * 1. Es owner del negocio
     * 2. EstÃ¡ en los managers de la sucursal
     */
    fun canEditBranch(branch: Branch, business: Business, userId: String): Boolean {
        if (business.ownerId == userId) return true
        if (branch.managerIds.contains(userId)) return true
        return false
    }

    /**
     * Solo el propietario puede eliminar sucursales
     */
    fun canDeleteBranch(business: Business, userId: String): Boolean {
        return business.ownerId == userId
    }

    /**
     * Solo el propietario puede cambiar managers
     */
    fun canChangeManagers(business: Business, userId: String): Boolean {
        return business.ownerId == userId
    }
}
