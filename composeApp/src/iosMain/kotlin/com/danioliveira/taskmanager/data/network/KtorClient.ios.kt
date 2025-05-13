package com.danioliveira.taskmanager.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

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
    return "http://192.168.68.52:8081"
}
