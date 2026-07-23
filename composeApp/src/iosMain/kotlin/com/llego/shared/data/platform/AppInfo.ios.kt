package com.llego.shared.data.platform

import platform.Foundation.NSBundle

actual fun appVersionString(): String {
    val bundle = NSBundle.mainBundle
    val short = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
    val build = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
    return when {
        short != null && build != null && build != short -> "$short ($build)"
        short != null -> short
        else -> "—"
    }
}
