package com.llego.shared.ui.auth

import android.util.Patterns

actual object PlatformEmailValidator {
    actual fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}