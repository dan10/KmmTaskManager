package com.danioliveira.taskmanager.data.network

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

/**
 * Creates an OkHttp engine for Android platform with timeout configuration
 */
actual fun createPlatformEngine(): HttpClientEngine {
    return OkHttp.create()
}

/**
 * Returns the base URL for Android platform
 * For emulators: 10.0.2.2 is the special IP to access the host machine
 * For physical devices: Use your computer's IP address on the local network
 *
 * To find your computer's IP address:
 * - On macOS/Linux: Run 'ifconfig' in terminal and look for your network interface (e.g., en0, wlan0)
 * - On Windows: Run 'ipconfig' in command prompt and look for your network adapter's IPv4 address
 */
actual fun getBaseUrl(): String {

    val physicalDeviceUrl = "http://192.168.68.59:8081"

    // Use physicalDeviceUrl when testing on a physical device
    return physicalDeviceUrl
}
