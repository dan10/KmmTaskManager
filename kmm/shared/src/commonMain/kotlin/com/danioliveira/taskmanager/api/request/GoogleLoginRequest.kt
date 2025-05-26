package com.danioliveira.taskmanager.api.request

import kotlinx.serialization.Serializable

@Serializable
data class GoogleLoginRequest(val idToken: String)