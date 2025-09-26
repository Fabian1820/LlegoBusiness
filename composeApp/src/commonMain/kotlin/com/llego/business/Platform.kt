package com.llego.business

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform