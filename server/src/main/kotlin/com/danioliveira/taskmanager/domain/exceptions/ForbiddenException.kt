package com.danioliveira.taskmanager.domain.exceptions

import io.ktor.http.*

/**
 * Exception thrown when a user is authenticated but does not have permission to access a resource.
 *
 * @property message A human-readable message describing the error
 * @property resourceType The type of resource the user is trying to access (e.g., "Project", "Task")
 * @property resourceId The ID of the resource the user is trying to access
 */
class ForbiddenException(
    resourceType: String? = null,
    resourceId: String? = null,
    override val message: String = buildMessage(resourceType, resourceId)
) : AppException(
    message = message,
    code = "FORBIDDEN",
    status = HttpStatusCode.Forbidden.value
) {
    companion object {
        private fun buildMessage(resourceType: String?, resourceId: String?): String {
            return when {
                resourceType != null && resourceId != null ->
                    "You don't have permission to access $resourceType with ID '$resourceId'"

                resourceType != null ->
                    "You don't have permission to access this $resourceType"

                else ->
                    "You don't have permission to perform this action"
            }
        }
    }
}