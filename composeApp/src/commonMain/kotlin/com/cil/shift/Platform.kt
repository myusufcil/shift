package com.cil.shift

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform