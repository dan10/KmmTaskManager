package com.danioliveira.taskmanager.api.request

import kotlinx.serialization.Serializable

/**
 * Request model for pagination parameters.
 */
@Serializable
data class PaginationRequest(
    val page: Int = 0,
    val size: Int = 10
)