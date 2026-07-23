package com.llego.shared.data.platform

import android.content.Context

/**
 * Guarda el applicationContext para consultas ligeras de metadata (versión, etc.).
 * MainActivity lo llena al arrancar.
 */
object AppContextHolder {
    @Volatile
    var applicationContext: Context? = null
}

actual fun appVersionString(): String {
    val ctx = AppContextHolder.applicationContext ?: return "—"
    return runCatching {
        val info = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        info.versionName ?: "—"
    }.getOrDefault("—")
}
