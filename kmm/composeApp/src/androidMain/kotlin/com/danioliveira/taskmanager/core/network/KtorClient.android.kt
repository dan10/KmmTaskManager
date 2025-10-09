package com.danioliveira.taskmanager.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun createPlatformEngine(): HttpClientEngine = OkHttp.create()

actual fun getBaseUrl(): String = "http://192.168.68.54:8081"
