package com.llego.nichos.restaurant.ui.components.menu

import com.llego.nichos.common.utils.getProductImage

/**
 * @deprecated Usar com.llego.nichos.common.utils.getProductImage en su lugar
 * Mantenido para compatibilidad con código existente
 */
@Deprecated("Usar com.llego.nichos.common.utils.getProductImage", ReplaceWith("getProductImage(menuItemId)", "com.llego.nichos.common.utils.getProductImage"))
internal fun getProductImage(menuItemId: String) = com.llego.nichos.common.utils.getProductImage(menuItemId)
