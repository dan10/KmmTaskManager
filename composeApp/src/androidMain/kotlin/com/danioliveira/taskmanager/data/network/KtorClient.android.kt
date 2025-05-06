package com.danioliveira.taskmanager.data.network

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

/**
 * Creates an OkHttp engine for Android platform
 */
actual fun createPlatformEngine(): HttpClientEngine {
    return OkHttp.create()
}

/**
 * Returns the base URL for Android platform
 * Using 10.0.2.2 which is the special IP to access the host machine from the Android emulator
 */
actual fun getBaseUrl(): String {
    return "http://10.0.2.2:8081"
}
