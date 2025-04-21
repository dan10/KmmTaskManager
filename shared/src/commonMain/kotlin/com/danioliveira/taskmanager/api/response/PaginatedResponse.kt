package com.danioliveira.taskmanager.api.response

import kotlinx.serialization.Serializable

/**
 * Response model for paginated results.
 */
@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
)