package com.danioliveira.taskmanager.api.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)