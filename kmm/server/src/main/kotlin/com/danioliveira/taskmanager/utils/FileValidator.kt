package com.danioliveira.taskmanager.utils

/**
 * Utility class for validating file types.
 */
object FileValidator {
    /**
     * List of allowed MIME types for files.
     * Only photos (JPEG, PNG, GIF) and PDFs are allowed.
     */
    private val ALLOWED_MIME_TYPES = listOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "application/pdf"
    )

    /**
     * Validates if the given MIME type is allowed.
     *
     * @param mimeType The MIME type to validate
     * @return True if the MIME type is allowed, false otherwise
     */
    fun isValidMimeType(mimeType: String): Boolean {
        return ALLOWED_MIME_TYPES.contains(mimeType.lowercase())
    }

    /**
     * Gets a list of allowed MIME types as a string.
     *
     * @return A string representation of allowed MIME types
     */
    fun getAllowedMimeTypesAsString(): String {
        return ALLOWED_MIME_TYPES.joinToString(", ")
    }
}