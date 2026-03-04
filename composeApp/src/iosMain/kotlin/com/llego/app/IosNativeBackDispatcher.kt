package com.llego.app

internal object IosNativeBackDispatcher {
    var enabled: Boolean = false
    var onBack: (() -> Unit)? = null

    fun dispatchBack() {
        if (!enabled) return
        onBack?.invoke()
    }
}
