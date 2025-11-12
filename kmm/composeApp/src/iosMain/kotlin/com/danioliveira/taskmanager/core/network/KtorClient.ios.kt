package com.danioliveira.taskmanager.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

/**
 * Creates a Darwin engine for iOS platform
 */
actual fun createPlatformEngine(): HttpClientEngine = Darwin.create()

/**
 * Returns the base URL for iOS platform
 * For iOS, localhost correctly refers to the host machine
 */
actual fun getBaseUrl(): String = "http://192.168.68.57:8081"
