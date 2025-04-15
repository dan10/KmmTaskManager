package com.danioliveira.taskmanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform