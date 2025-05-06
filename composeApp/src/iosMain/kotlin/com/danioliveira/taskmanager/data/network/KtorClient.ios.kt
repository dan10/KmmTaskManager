package com.danioliveira.taskmanager.data.network

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

/**
 * Creates a Darwin engine for iOS platform
 */
actual fun createPlatformEngine(): HttpClientEngine {
    return Darwin.create()
}

/**
 * Returns the base URL for iOS platform
 * For iOS, localhost correctly refers to the host machine
 */
actual fun getBaseUrl(): String {
    return "http://localhost:8081"
}
