package com.danioliveira.taskmanager.domain.exceptions

import io.ktor.http.*

/**
 * Exception thrown when a requested resource is not found.
 *
 * @property message A human-readable message describing the error
 * @property resourceType The type of resource that was not found (e.g., "User", "Project", "Task")
 * @property resourceId The ID of the resource that was not found
 */
class NotFoundException(
    resourceType: String,
    resourceId: String? = null
) : AppException(
    message = buildMessage(resourceType, resourceId),
    code = "RESOURCE_NOT_FOUND",
    status = HttpStatusCode.NotFound.value
) {
    companion object {
        private fun buildMessage(resourceType: String, resourceId: String?): String {
            return if (resourceId != null) {
                "$resourceType with ID '$resourceId' not found"
            } else {
                "$resourceType not found"
            }
        }
    }
}