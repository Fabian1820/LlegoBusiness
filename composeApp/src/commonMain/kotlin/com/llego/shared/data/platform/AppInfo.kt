package com.llego.shared.data.platform

/**
 * Versión visible de la app tal como la publica la plataforma
 * (BuildConfig.VERSION_NAME en Android, CFBundleShortVersionString en iOS).
 * Si por alguna razón no está disponible, cae a "—" para no mostrar un hardcode falso.
 */
expect fun appVersionString(): String
