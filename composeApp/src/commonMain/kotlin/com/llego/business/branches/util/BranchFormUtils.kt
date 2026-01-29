package com.llego.business.branches.util

fun parseManagerIds(value: String): List<String> {
    return value.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
